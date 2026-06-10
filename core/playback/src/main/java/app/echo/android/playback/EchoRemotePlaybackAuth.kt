package app.echo.android.playback

import android.net.Uri
import android.util.Base64
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

data class EchoWebDavPlaybackCredential(
    val baseUrl: String,
    val username: String,
    val password: String,
) {
    val normalizedBaseUrl: String =
        baseUrl.trim().trimEnd('/')

    val authorizationHeader: String =
        basicAuthorization(username.trim(), password)
}

object EchoRemotePlaybackAuthRegistry {
    private val webDavCredentials = AtomicReference<List<EchoWebDavPlaybackCredential>>(emptyList())

    fun replaceWebDavCredentials(credentials: List<EchoWebDavPlaybackCredential>) {
        webDavCredentials.set(
            credentials
                .filter { it.normalizedBaseUrl.isNotBlank() && it.username.isNotBlank() && it.password.isNotBlank() }
                .distinctBy { it.normalizedBaseUrl },
        )
    }

    internal fun resolve(dataSpec: DataSpec): DataSpec {
        val uri = dataSpec.uri
        val uriString = uri.toString()
        val userInfo = uri.userInfoDecoded()
        val matchedCredential = webDavCredentials.get()
            .firstOrNull { credential ->
                uriString == credential.normalizedBaseUrl ||
                    uriString.startsWith("${credential.normalizedBaseUrl}/")
            }
        val authorization = when {
            matchedCredential != null -> matchedCredential.authorizationHeader
            !userInfo.isNullOrBlank() -> basicAuthorizationFromUserInfo(userInfo)
            else -> null
        } ?: return dataSpec

        val cleanUri = uri.withoutUserInfo()
        val headers = LinkedHashMap(dataSpec.httpRequestHeaders)
        headers["Authorization"] = authorization
        return dataSpec.buildUpon()
            .setUri(cleanUri)
            .setHttpRequestHeaders(headers)
            .build()
    }
}

@UnstableApi
fun echoRemoteAuthDataSourceFactory(context: android.content.Context): ResolvingDataSource.Factory =
    ResolvingDataSource.Factory(
        DefaultDataSource.Factory(context),
        ResolvingDataSource.Resolver { dataSpec -> EchoRemotePlaybackAuthRegistry.resolve(dataSpec) },
    )

private fun basicAuthorization(username: String, password: String): String {
    val raw = "$username:$password"
    val encoded = Base64.encodeToString(raw.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    return "Basic $encoded"
}

private fun basicAuthorizationFromUserInfo(userInfo: String): String? {
    val separator = userInfo.indexOf(':')
    if (separator <= 0) return null
    return basicAuthorization(
        username = userInfo.substring(0, separator),
        password = userInfo.substring(separator + 1),
    )
}

private fun Uri.userInfoDecoded(): String? =
    encodedUserInfo?.let { encoded ->
        runCatching { URLDecoder.decode(encoded, StandardCharsets.UTF_8.name()) }.getOrNull()
    }

private fun Uri.withoutUserInfo(): Uri {
    if (encodedUserInfo.isNullOrBlank()) return this
    return buildUpon()
        .encodedAuthority(
            buildString {
                append(host.orEmpty())
                if (port != -1) append(':').append(port)
            },
        )
        .build()
}
