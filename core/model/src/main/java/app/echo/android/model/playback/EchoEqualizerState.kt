package app.echo.android.model.playback

import kotlin.math.abs

data class EchoEqualizerBand(
    val index: Int,
    val frequencyHz: Int,
    val gainDb: Float,
    val minGainDb: Float,
    val maxGainDb: Float,
)

data class EchoEqualizerPresetDefinition(
    val id: String,
    val name: String,
    val gainsDb: List<Float>,
)

data class EchoEqualizerState(
    val enabled: Boolean = false,
    val supported: Boolean = false,
    val available: Boolean = false,
    val presetId: String = EchoEqualizerPreset.Flat,
    val presetName: String = EchoEqualizerPresets.nameFor(EchoEqualizerPreset.Flat),
    val bands: List<EchoEqualizerBand> = EchoEqualizerPresets.defaultBands(),
    val warning: String? = null,
) {
    val active: Boolean
        get() = enabled && supported && bands.any { abs(it.gainDb) >= 0.1f }

    val gainsDb: List<Float>
        get() = bands.map { it.gainDb }
}

object EchoEqualizerPreset {
    const val Flat = "flat"
    const val Warm = "warm"
    const val Bass = "bass"
    const val Vocal = "vocal"
    const val Bright = "bright"
    const val Custom = "custom"
}

object EchoEqualizerPresets {
    val defaultFrequenciesHz: List<Int> = listOf(60, 230, 910, 3600, 14000)

    val presets: List<EchoEqualizerPresetDefinition> = listOf(
        EchoEqualizerPresetDefinition(EchoEqualizerPreset.Flat, "Flat", listOf(0f, 0f, 0f, 0f, 0f)),
        EchoEqualizerPresetDefinition(EchoEqualizerPreset.Warm, "Warm", listOf(2f, 1.2f, 0f, -0.8f, -1.4f)),
        EchoEqualizerPresetDefinition(EchoEqualizerPreset.Bass, "Bass", listOf(4f, 2.8f, 0.4f, -0.8f, -1.2f)),
        EchoEqualizerPresetDefinition(EchoEqualizerPreset.Vocal, "Vocal", listOf(-1.2f, -0.4f, 2.2f, 1.8f, 0.2f)),
        EchoEqualizerPresetDefinition(EchoEqualizerPreset.Bright, "Bright", listOf(-2f, -0.8f, 0f, 2f, 3.2f)),
    )

    fun normalizePresetId(id: String?): String =
        when (id) {
            EchoEqualizerPreset.Warm,
            EchoEqualizerPreset.Bass,
            EchoEqualizerPreset.Vocal,
            EchoEqualizerPreset.Bright,
            EchoEqualizerPreset.Custom,
            EchoEqualizerPreset.Flat,
            -> id
            else -> EchoEqualizerPreset.Flat
        }

    fun nameFor(id: String): String =
        if (id == EchoEqualizerPreset.Custom) {
            "Custom"
        } else {
            presets.firstOrNull { it.id == id }?.name ?: presets.first().name
        }

    fun gainsForPreset(
        id: String,
        frequenciesHz: List<Int> = defaultFrequenciesHz,
        customGainsDb: List<Float> = emptyList(),
    ): List<Float> {
        if (id == EchoEqualizerPreset.Custom) {
            return resizedGains(customGainsDb, frequenciesHz.size)
        }
        val preset = presets.firstOrNull { it.id == id } ?: presets.first()
        return frequenciesHz.map { frequencyHz ->
            val sourceIndex = defaultFrequenciesHz.indices.minByOrNull { index ->
                kotlin.math.abs(defaultFrequenciesHz[index] - frequencyHz)
            } ?: 0
            preset.gainsDb.getOrElse(sourceIndex) { 0f }
        }
    }

    fun defaultBands(
        gainsDb: List<Float> = gainsForPreset(EchoEqualizerPreset.Flat),
        minGainDb: Float = -12f,
        maxGainDb: Float = 12f,
    ): List<EchoEqualizerBand> =
        defaultFrequenciesHz.mapIndexed { index, frequencyHz ->
            EchoEqualizerBand(
                index = index,
                frequencyHz = frequencyHz,
                gainDb = gainsDb.getOrElse(index) { 0f }.coerceIn(minGainDb, maxGainDb),
                minGainDb = minGainDb,
                maxGainDb = maxGainDb,
            )
        }

    private fun resizedGains(gainsDb: List<Float>, size: Int): List<Float> =
        List(size) { index -> gainsDb.getOrElse(index) { 0f } }
}
