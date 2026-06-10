package app.echo.android.playback

import android.media.audiofx.Equalizer
import androidx.media3.common.C
import app.echo.android.model.playback.EchoEqualizerBand
import app.echo.android.model.playback.EchoEqualizerPreset
import app.echo.android.model.playback.EchoEqualizerPresets
import app.echo.android.model.playback.EchoEqualizerState
import app.echo.android.model.playback.OpraHeadphoneCorrectionPreset
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EchoEqualizerController(
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(EchoEqualizerState())
    val state: StateFlow<EchoEqualizerState> = _state.asStateFlow()

    private var equalizer: Equalizer? = null
    private var audioSessionId: Int = C.AUDIO_SESSION_ID_UNSET
    private var transitionJob: Job? = null
    private var desiredEnabled: Boolean = false
    private var desiredPresetId: String = EchoEqualizerPreset.Flat
    private var desiredGainsDb: List<Float> = EchoEqualizerPresets.gainsForPreset(EchoEqualizerPreset.Flat)
    private var warning: String? = null

    fun setConfig(enabled: Boolean, presetId: String, gainsDb: List<Float>) {
        desiredEnabled = enabled
        desiredPresetId = EchoEqualizerPresets.normalizePresetId(presetId)
        desiredGainsDb = gainsDb.ifEmpty {
            EchoEqualizerPresets.gainsForPreset(desiredPresetId)
        }
        applyDesiredState()
    }

    fun setEnabled(enabled: Boolean) {
        desiredEnabled = enabled
        applyDesiredState()
    }

    fun setPreset(presetId: String) {
        desiredPresetId = EchoEqualizerPresets.normalizePresetId(presetId)
        desiredGainsDb = EchoEqualizerPresets.gainsForPreset(
            id = desiredPresetId,
            frequenciesHz = currentFrequenciesHz(),
            customGainsDb = desiredGainsDb,
        )
        applyDesiredState()
    }

    fun setBandGain(index: Int, gainDb: Float) {
        val currentBands = _state.value.bands
        val safeIndex = index.coerceIn(0, (currentBands.size - 1).coerceAtLeast(0))
        val band = currentBands.getOrNull(safeIndex)
        val nextGains = currentBands.mapIndexed { bandIndex, currentBand ->
            if (bandIndex == safeIndex) {
                gainDb.coerceIn(currentBand.minGainDb, currentBand.maxGainDb)
            } else {
                currentBand.gainDb
            }
        }
        desiredPresetId = EchoEqualizerPreset.Custom
        desiredGainsDb = nextGains.ifEmpty { listOf(gainDb) }
        applyDesiredState()
        if (band == null && desiredGainsDb.isNotEmpty()) {
            rebuildState()
        }
    }

    fun reset() {
        desiredPresetId = EchoEqualizerPreset.Flat
        desiredGainsDb = EchoEqualizerPresets.gainsForPreset(EchoEqualizerPreset.Flat, currentFrequenciesHz())
        applyDesiredState()
    }

    fun applyOpraPreset(preset: OpraHeadphoneCorrectionPreset): List<Float> {
        val currentBands = _state.value.bands.ifEmpty {
            EchoEqualizerPresets.defaultBands()
        }
        desiredEnabled = true
        desiredPresetId = EchoEqualizerPreset.Custom
        desiredGainsDb = currentBands.map { band ->
            sampleOpraPreset(preset, band.frequencyHz.toFloat())
                .coerceIn(band.minGainDb, band.maxGainDb)
        }
        applyDesiredState()
        return desiredGainsDb
    }

    fun syncToAudioSession(sessionId: Int) {
        if (sessionId == audioSessionId) return
        transitionJob?.cancel()
        releaseEqualizer()
        audioSessionId = sessionId
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) {
            warning = "等待播放器音频会话"
            rebuildState()
            return
        }

        runCatching {
            Equalizer(0, sessionId).also { effect ->
                equalizer = effect
                warning = null
            }
        }.onFailure { error ->
            equalizer = null
            warning = error.message ?: "当前设备不支持系统 Equalizer"
        }
        applyDesiredState()
    }

    fun release() {
        transitionJob?.cancel()
        releaseEqualizer()
        audioSessionId = C.AUDIO_SESSION_ID_UNSET
        rebuildState()
    }

    private fun applyDesiredState() {
        val effect = equalizer
        if (effect == null) {
            rebuildState()
            return
        }

        runCatching {
            val bands = readBands(effect)
            val targetGains = EchoEqualizerPresets.gainsForPreset(
                id = desiredPresetId,
                frequenciesHz = bands.map { it.frequencyHz },
                customGainsDb = desiredGainsDb,
            )
            desiredGainsDb = targetGains
            if (desiredEnabled) {
                applyEnabledState(effect, bands, targetGains)
            } else {
                applyDisabledState(effect, bands)
            }
            warning = null
        }.onFailure { error ->
            warning = error.message ?: "EQ 参数应用失败"
            rebuildState()
        }
    }

    private fun rebuildState() {
        val effect = equalizer
        val bands = if (effect == null) {
            EchoEqualizerPresets.defaultBands(desiredGainsDb)
        } else {
            runCatching { readBands(effect) }.getOrElse {
                warning = it.message ?: "EQ 频段读取失败"
                EchoEqualizerPresets.defaultBands(desiredGainsDb)
            }.withStateGains()
        }
        val supported = effect != null
        val nextState = EchoEqualizerState(
            enabled = desiredEnabled,
            supported = supported,
            available = audioSessionId != C.AUDIO_SESSION_ID_UNSET,
            presetId = desiredPresetId,
            presetName = EchoEqualizerPresets.nameFor(desiredPresetId),
            bands = bands,
            warning = warning,
        )
        if (_state.value != nextState) {
            _state.value = nextState
        }
    }

    private fun applyEnabledState(
        effect: Equalizer,
        bands: List<EchoEqualizerBand>,
        targetGains: List<Float>,
    ) {
        val startGains = if (effect.enabled) {
            bands.map { it.gainDb }
        } else {
            List(bands.size) { 0f }
        }
        transitionJob?.cancel()
        if (!effect.enabled) {
            setBandLevels(effect, bands, startGains)
            effect.enabled = true
        }
        if (shouldRamp(startGains, targetGains)) {
            rampBandLevels(
                effect = effect,
                bands = bands,
                fromGainsDb = startGains,
                toGainsDb = targetGains,
                disableAtEnd = false,
            )
        } else {
            setBandLevels(effect, bands, targetGains)
            rebuildState()
        }
    }

    private fun applyDisabledState(
        effect: Equalizer,
        bands: List<EchoEqualizerBand>,
    ) {
        val zeroGains = List(bands.size) { 0f }
        val startGains = if (effect.enabled) {
            bands.map { it.gainDb }
        } else {
            zeroGains
        }
        transitionJob?.cancel()
        if (effect.enabled && shouldRamp(startGains, zeroGains)) {
            rampBandLevels(
                effect = effect,
                bands = bands,
                fromGainsDb = startGains,
                toGainsDb = zeroGains,
                disableAtEnd = true,
            )
        } else {
            setBandLevels(effect, bands, zeroGains)
            effect.enabled = false
            rebuildState()
        }
    }

    private fun rampBandLevels(
        effect: Equalizer,
        bands: List<EchoEqualizerBand>,
        fromGainsDb: List<Float>,
        toGainsDb: List<Float>,
        disableAtEnd: Boolean,
    ) {
        transitionJob = scope.launch {
            try {
                if (equalizer !== effect) return@launch
                effect.enabled = true
                repeat(EqualizerRampSteps) { step ->
                    val fraction = (step + 1).toFloat() / EqualizerRampSteps
                    val nextGains = bands.mapIndexed { index, band ->
                        val from = fromGainsDb.getOrElse(index) { 0f }
                        val to = toGainsDb.getOrElse(index) { 0f }
                        (from + (to - from) * fraction).coerceIn(band.minGainDb, band.maxGainDb)
                    }
                    setBandLevels(effect, bands, nextGains)
                    delay(EqualizerRampDelayMs)
                }
                if (equalizer === effect) {
                    setBandLevels(effect, bands, toGainsDb)
                    if (disableAtEnd && !desiredEnabled) {
                        effect.enabled = false
                    }
                    rebuildState()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                warning = error.message ?: "EQ 平滑切换失败"
                rebuildState()
            }
        }
        rebuildState()
    }

    private fun setBandLevels(
        effect: Equalizer,
        bands: List<EchoEqualizerBand>,
        gainsDb: List<Float>,
    ) {
        bands.forEachIndexed { index, band ->
            effect.setBandLevel(
                index.toShort(),
                dbToMillibels(gainsDb.getOrElse(index) { 0f }.coerceIn(band.minGainDb, band.maxGainDb)),
            )
        }
    }

    private fun shouldRamp(fromGainsDb: List<Float>, toGainsDb: List<Float>): Boolean =
        fromGainsDb.indices.any { index ->
            abs(fromGainsDb[index] - toGainsDb.getOrElse(index) { 0f }) >= EqualizerRampThresholdDb
        }

    private fun List<EchoEqualizerBand>.withStateGains(): List<EchoEqualizerBand> {
        val stateGains = if (desiredEnabled) {
            map { it.gainDb }
        } else {
            EchoEqualizerPresets.gainsForPreset(
                id = desiredPresetId,
                frequenciesHz = map { it.frequencyHz },
                customGainsDb = desiredGainsDb,
            )
        }
        return mapIndexed { index, band ->
            band.copy(gainDb = stateGains.getOrElse(index) { 0f }.coerceIn(band.minGainDb, band.maxGainDb))
        }
    }

    private fun readBands(effect: Equalizer): List<EchoEqualizerBand> {
        val range = effect.bandLevelRange
        val minGainDb = millibelsToDb(range.getOrElse(0) { (-1200).toShort() })
        val maxGainDb = millibelsToDb(range.getOrElse(1) { 1200.toShort() })
        return List(effect.numberOfBands.toInt()) { index ->
            val band = index.toShort()
            EchoEqualizerBand(
                index = index,
                frequencyHz = (effect.getCenterFreq(band) / 1000).coerceAtLeast(1),
                gainDb = millibelsToDb(effect.getBandLevel(band)),
                minGainDb = minGainDb,
                maxGainDb = maxGainDb,
            )
        }
    }

    private fun currentFrequenciesHz(): List<Int> =
        _state.value.bands.map { it.frequencyHz }.ifEmpty { EchoEqualizerPresets.defaultFrequenciesHz }

    private fun releaseEqualizer() {
        runCatching {
            equalizer?.enabled = false
            equalizer?.release()
        }
        equalizer = null
    }

    private fun dbToMillibels(value: Float): Short = (value * 100f).toInt().toShort()

    private fun millibelsToDb(value: Short): Float = value / 100f

    private fun sampleOpraPreset(preset: OpraHeadphoneCorrectionPreset, frequencyHz: Float): Float {
        val sampleRateHz = maxOf(DefaultSampleRateHz, frequencyHz * 2.4f)
        return preset.bands.fold(preset.preampDb) { gainDb, band ->
            gainDb + when (band.type) {
                "peak_dip" -> peakingResponseDb(
                    frequencyHz = frequencyHz,
                    sampleRateHz = sampleRateHz,
                    centerFrequencyHz = band.frequencyHz,
                    gainDb = band.gainDb,
                    q = band.q ?: 1f,
                )
                "low_shelf" -> shelfResponseDb(
                    highShelf = false,
                    frequencyHz = frequencyHz,
                    sampleRateHz = sampleRateHz,
                    centerFrequencyHz = band.frequencyHz,
                    gainDb = band.gainDb,
                    q = band.q ?: 0.707f,
                )
                "high_shelf" -> shelfResponseDb(
                    highShelf = true,
                    frequencyHz = frequencyHz,
                    sampleRateHz = sampleRateHz,
                    centerFrequencyHz = band.frequencyHz,
                    gainDb = band.gainDb,
                    q = band.q ?: 0.707f,
                )
                "band_stop" -> notchResponseDb(
                    frequencyHz = frequencyHz,
                    sampleRateHz = sampleRateHz,
                    centerFrequencyHz = band.frequencyHz,
                    q = band.q ?: 1f,
                )
                else -> 0f
            }
        }
    }

    private fun peakingResponseDb(
        frequencyHz: Float,
        sampleRateHz: Float,
        centerFrequencyHz: Float,
        gainDb: Float,
        q: Float,
    ): Float {
        val omega = omega(centerFrequencyHz, sampleRateHz)
        val alpha = sin(omega) / (2.0 * q.coerceAtLeast(0.1f))
        val cosOmega = cos(omega)
        val a = 10.0.pow(gainDb / 40.0)
        return Biquad(
            b0 = 1.0 + alpha * a,
            b1 = -2.0 * cosOmega,
            b2 = 1.0 - alpha * a,
            a0 = 1.0 + alpha / a,
            a1 = -2.0 * cosOmega,
            a2 = 1.0 - alpha / a,
        ).responseDb(frequencyHz, sampleRateHz)
    }

    private fun shelfResponseDb(
        highShelf: Boolean,
        frequencyHz: Float,
        sampleRateHz: Float,
        centerFrequencyHz: Float,
        gainDb: Float,
        q: Float,
    ): Float {
        val omega = omega(centerFrequencyHz, sampleRateHz)
        val sinOmega = sin(omega)
        val cosOmega = cos(omega)
        val a = 10.0.pow(gainDb / 40.0)
        val sqrtA = sqrt(a)
        val slope = q.coerceAtLeast(0.1f)
        val alpha = sinOmega / 2.0 * sqrt(((a + 1.0 / a) * (1.0 / slope - 1.0) + 2.0).coerceAtLeast(0.0))
        return if (highShelf) {
            Biquad(
                b0 = a * ((a + 1.0) + (a - 1.0) * cosOmega + 2.0 * sqrtA * alpha),
                b1 = -2.0 * a * ((a - 1.0) + (a + 1.0) * cosOmega),
                b2 = a * ((a + 1.0) + (a - 1.0) * cosOmega - 2.0 * sqrtA * alpha),
                a0 = (a + 1.0) - (a - 1.0) * cosOmega + 2.0 * sqrtA * alpha,
                a1 = 2.0 * ((a - 1.0) - (a + 1.0) * cosOmega),
                a2 = (a + 1.0) - (a - 1.0) * cosOmega - 2.0 * sqrtA * alpha,
            )
        } else {
            Biquad(
                b0 = a * ((a + 1.0) - (a - 1.0) * cosOmega + 2.0 * sqrtA * alpha),
                b1 = 2.0 * a * ((a - 1.0) - (a + 1.0) * cosOmega),
                b2 = a * ((a + 1.0) - (a - 1.0) * cosOmega - 2.0 * sqrtA * alpha),
                a0 = (a + 1.0) + (a - 1.0) * cosOmega + 2.0 * sqrtA * alpha,
                a1 = -2.0 * ((a - 1.0) + (a + 1.0) * cosOmega),
                a2 = (a + 1.0) + (a - 1.0) * cosOmega - 2.0 * sqrtA * alpha,
            )
        }.responseDb(frequencyHz, sampleRateHz)
    }

    private fun notchResponseDb(
        frequencyHz: Float,
        sampleRateHz: Float,
        centerFrequencyHz: Float,
        q: Float,
    ): Float {
        val omega = omega(centerFrequencyHz, sampleRateHz)
        val alpha = sin(omega) / (2.0 * q.coerceAtLeast(0.1f))
        val cosOmega = cos(omega)
        return Biquad(
            b0 = 1.0,
            b1 = -2.0 * cosOmega,
            b2 = 1.0,
            a0 = 1.0 + alpha,
            a1 = -2.0 * cosOmega,
            a2 = 1.0 - alpha,
        ).responseDb(frequencyHz, sampleRateHz)
    }

    private fun omega(frequencyHz: Float, sampleRateHz: Float): Double {
        val safeFrequency = frequencyHz.coerceIn(1f, sampleRateHz * 0.49f)
        return 2.0 * PI * safeFrequency / sampleRateHz
    }

    private data class Biquad(
        val b0: Double,
        val b1: Double,
        val b2: Double,
        val a0: Double,
        val a1: Double,
        val a2: Double,
    ) {
        fun responseDb(frequencyHz: Float, sampleRateHz: Float): Float {
            val omega = 2.0 * PI * frequencyHz.coerceIn(1f, sampleRateHz * 0.49f) / sampleRateHz
            val cos1 = cos(omega)
            val sin1 = sin(omega)
            val cos2 = cos(2.0 * omega)
            val sin2 = sin(2.0 * omega)
            val numeratorReal = b0 + b1 * cos1 + b2 * cos2
            val numeratorImaginary = -b1 * sin1 - b2 * sin2
            val denominatorReal = a0 + a1 * cos1 + a2 * cos2
            val denominatorImaginary = -a1 * sin1 - a2 * sin2
            val numeratorPower = numeratorReal * numeratorReal + numeratorImaginary * numeratorImaginary
            val denominatorPower = denominatorReal * denominatorReal + denominatorImaginary * denominatorImaginary
            if (numeratorPower <= 0.0 || denominatorPower <= 0.0) return 0f
            return (20.0 * log10(sqrt(numeratorPower / denominatorPower))).toFloat()
        }
    }

    private companion object {
        const val DefaultSampleRateHz = 48_000f
        const val EqualizerRampSteps = 8
        const val EqualizerRampDelayMs = 12L
        const val EqualizerRampThresholdDb = 0.2f
    }
}
