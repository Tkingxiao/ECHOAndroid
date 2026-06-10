package app.echo.android.data

import android.content.Context
import app.echo.android.model.playback.OpraDatabaseStatus
import app.echo.android.model.playback.OpraEqBand
import app.echo.android.model.playback.OpraHeadphoneCorrectionPreset
import app.echo.android.model.playback.OpraHeadphoneCorrectionProduct
import java.io.File
import java.text.Normalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class OpraHeadphoneCorrectionRepository(
    context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val cacheFile = File(context.cacheDir, "opra/database_v1.jsonl")
    private var cachedDatabase: OpraDatabase? = null

    suspend fun search(
        query: String,
        refresh: Boolean = false,
        limit: Int = 16,
    ): Result<OpraSearchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val database = loadDatabase(refresh)
            val tokens = normalizeSearchText(query).split(' ').filter { it.isNotBlank() }
            if (tokens.isEmpty()) {
                return@runCatching OpraSearchResult(emptyList(), database.status)
            }
            val products = database.products.values
                .mapNotNull { product ->
                    val vendor = database.vendors[product.vendorId] ?: OpraVendor(product.vendorId, product.vendorId)
                    val score = scoreProduct(tokens, product, vendor)
                    if (score < 0) {
                        null
                    } else {
                        score to createProductResult(database, product, vendor)
                    }
                }
                .sortedWith(compareByDescending<Pair<Int, OpraHeadphoneCorrectionProduct>> { it.first }.thenBy { it.second.productName })
                .map { it.second }
                .take(limit)
            OpraSearchResult(products, database.status)
        }
    }

    private fun loadDatabase(refresh: Boolean): OpraDatabase {
        cachedDatabase?.takeIf { !refresh }?.let { return it }
        val shouldFetch = refresh || !cacheFile.isFile
        val rawText = if (shouldFetch) {
            fetchDatabaseText().also { text ->
                cacheFile.parentFile?.mkdirs()
                cacheFile.writeText(text)
            }
        } else {
            cacheFile.readText()
        }
        return parseDatabase(rawText, if (shouldFetch) "network" else "cache").also {
            cachedDatabase = it
        }
    }

    private fun fetchDatabaseText(): String {
        val request = Request.Builder()
            .url(OpraDatabaseUrl)
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("opra_fetch_failed_${response.code}")
            }
            return response.body?.string() ?: error("opra_empty_response")
        }
    }

    private fun parseDatabase(rawText: String, source: String): OpraDatabase {
        val vendors = mutableMapOf<String, OpraVendor>()
        val products = mutableMapOf<String, OpraProduct>()
        val eqsByProductId = mutableMapOf<String, MutableList<OpraEq>>()
        var eqCount = 0

        rawText.lineSequence().forEach { line ->
            if (line.isBlank()) return@forEach
            runCatching {
                val record = JSONObject(line)
                val id = record.optTrimmedString("id") ?: return@runCatching
                val data = record.optJSONObject("data") ?: return@runCatching
                when (record.optTrimmedString("type")) {
                    "vendor" -> {
                        vendors[id] = OpraVendor(
                            id = id,
                            name = data.optTrimmedString("name") ?: id,
                        )
                    }
                    "product" -> {
                        val vendorId = data.optTrimmedString("vendor_id") ?: return@runCatching
                        val name = data.optTrimmedString("name") ?: return@runCatching
                        products[id] = OpraProduct(
                            id = id,
                            vendorId = vendorId,
                            name = name,
                            subtype = data.optTrimmedString("subtype"),
                        )
                    }
                    "eq" -> {
                        if (data.optTrimmedString("type") != "parametric_eq") return@runCatching
                        val productId = data.optTrimmedString("product_id") ?: return@runCatching
                        val parameters = data.optJSONObject("parameters") ?: return@runCatching
                        val bands = parseBands(parameters)
                        if (bands.isEmpty()) return@runCatching
                        val eq = OpraEq(
                            id = id,
                            productId = productId,
                            author = data.optTrimmedString("author") ?: "OPRA",
                            details = data.optTrimmedString("details"),
                            link = data.optTrimmedString("link"),
                            preampDb = parameters.optFloat("gain_db") ?: 0f,
                            bands = bands,
                        )
                        eqsByProductId.getOrPut(productId) { mutableListOf() }.add(eq)
                        eqCount += 1
                    }
                }
            }
        }

        return OpraDatabase(
            vendors = vendors,
            products = products,
            eqsByProductId = eqsByProductId,
            status = OpraDatabaseStatus(
                source = source,
                vendorCount = vendors.size,
                productCount = products.size,
                eqCount = eqCount,
            ),
        )
    }

    private fun parseBands(parameters: JSONObject): List<OpraEqBand> {
        val array = parameters.optJSONArray("bands") ?: return emptyList()
        return List(array.length()) { index -> array.optJSONObject(index) }
            .mapNotNull { band ->
                val type = band?.optTrimmedString("type") ?: return@mapNotNull null
                val frequency = band.optFloat("frequency") ?: return@mapNotNull null
                OpraEqBand(
                    type = type,
                    frequencyHz = frequency,
                    gainDb = band.optFloat("gain_db") ?: 0f,
                    q = band.optFloat("q"),
                    slope = band.optFloat("slope"),
                )
            }
    }

    private fun createProductResult(
        database: OpraDatabase,
        product: OpraProduct,
        vendor: OpraVendor,
    ): OpraHeadphoneCorrectionProduct {
        val presets = database.eqsByProductId[product.id]
            .orEmpty()
            .map { eq ->
                OpraHeadphoneCorrectionPreset(
                    eqId = eq.id,
                    productId = product.id,
                    productName = product.name,
                    vendorName = vendor.name,
                    author = eq.author,
                    details = eq.details,
                    sourceUrl = eq.link,
                    preampDb = eq.preampDb,
                    bands = eq.bands,
                )
            }
        return OpraHeadphoneCorrectionProduct(
            productId = product.id,
            productName = product.name,
            vendorName = vendor.name,
            subtype = product.subtype,
            presets = presets,
        )
    }

    private fun scoreProduct(tokens: List<String>, product: OpraProduct, vendor: OpraVendor): Int {
        val haystack = normalizeSearchText("${vendor.name} ${product.name} ${product.id.replace('_', ' ')}")
        if (!tokens.all(haystack::contains)) return -1
        val productName = normalizeSearchText(product.name)
        val vendorName = normalizeSearchText(vendor.name)
        return tokens.sumOf { token ->
            when {
                productName == token || vendorName == token -> 120
                productName.startsWith(token) || vendorName.startsWith(token) -> 70
                else -> 20
            }
        }
    }

    private fun normalizeSearchText(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFKD)
            .replace(Regex("[\\u0300-\\u036f]"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9\\u4e00-\\u9fff]+"), " ")
            .trim()

    private data class OpraVendor(
        val id: String,
        val name: String,
    )

    private data class OpraProduct(
        val id: String,
        val vendorId: String,
        val name: String,
        val subtype: String?,
    )

    private data class OpraEq(
        val id: String,
        val productId: String,
        val author: String,
        val details: String?,
        val link: String?,
        val preampDb: Float,
        val bands: List<OpraEqBand>,
    )

    private data class OpraDatabase(
        val vendors: Map<String, OpraVendor>,
        val products: Map<String, OpraProduct>,
        val eqsByProductId: Map<String, List<OpraEq>>,
        val status: OpraDatabaseStatus,
    )

    companion object {
        private const val OpraDatabaseUrl = "https://opra.roonlabs.net/database_v1.jsonl"
    }
}

data class OpraSearchResult(
    val products: List<OpraHeadphoneCorrectionProduct>,
    val status: OpraDatabaseStatus,
)

private fun JSONObject.optTrimmedString(name: String): String? =
    optString(name).trim().takeIf { it.isNotBlank() }

private fun JSONObject.optFloat(name: String): Float? {
    if (!has(name) || isNull(name)) return null
    val value = optDouble(name, Double.NaN)
    return value.takeIf { it.isFinite() }?.toFloat()
}
