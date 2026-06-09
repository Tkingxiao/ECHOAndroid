package app.echo.android.playback

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioMixerAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import app.echo.android.model.playback.EchoAudioErrorKind
import app.echo.android.model.playback.EchoPlaybackDiagnostics
import app.echo.android.model.playback.EchoPlaybackError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EchoUsbAudioStatus(
    val exclusiveEnabled: Boolean = false,
    val deviceName: String? = null,
    val connected: Boolean = false,
    val supportedSampleRates: List<Int> = emptyList(),
    val bitPerfectSupported: Boolean = false,
    val bitPerfectActive: Boolean = false,
    val lastRequestedSampleRateHz: Int? = null,
    val lastRequestError: EchoPlaybackError? = null,
) {
    val outputRoute: String
        get() = when {
            bitPerfectActive && deviceName != null -> "USB DAC: $deviceName / bit-perfect"
            connected && deviceName != null -> "USB DAC: $deviceName / Android mixer"
            connected -> "USB DAC / Android mixer"
            else -> "Media3 / AudioTrack"
        }
}

class EchoUsbAudioMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val _status = MutableStateFlow(scan())
    val status: StateFlow<EchoUsbAudioStatus> = _status.asStateFlow()
    private var exclusiveEnabled = false

    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            refresh()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            refresh()
        }
    }

    fun start() {
        audioManager.registerAudioDeviceCallback(callback, mainHandler)
        refresh()
    }

    fun stop() {
        audioManager.unregisterAudioDeviceCallback(callback)
    }

    fun setExclusiveEnabled(enabled: Boolean) {
        if (exclusiveEnabled == enabled) return
        exclusiveEnabled = enabled
        if (!enabled) clearPreferredMixerAttributes()
        refresh()
    }

    fun prepareForTrack(sampleRateHz: Int?) {
        val safeSampleRate = sampleRateHz?.takeIf { it > 0 }
        if (!exclusiveEnabled || safeSampleRate == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            refresh()
            return
        }

        val device = findUsbOutputDevice()
        if (device == null) {
            refresh()
            return
        }

        val requestedAttributes = findBitPerfectAttributes(device, safeSampleRate)
        if (requestedAttributes == null) {
            _status.value = scan().copy(
                lastRequestedSampleRateHz = safeSampleRate,
                lastRequestError = EchoPlaybackError(
                    kind = EchoAudioErrorKind.OutputRouteFailure,
                    message = "USB DAC does not advertise bit-perfect ${safeSampleRate}Hz playback",
                    recoverable = true,
                ),
            )
            return
        }

        val applied = runCatching {
            audioManager.setPreferredMixerAttributes(androidMusicAttributes(), device, requestedAttributes)
        }.getOrDefault(false)

        _status.value = scan().copy(
            lastRequestedSampleRateHz = safeSampleRate,
            lastRequestError = if (applied) {
                null
            } else {
                EchoPlaybackError(
                    kind = EchoAudioErrorKind.OutputRouteFailure,
                    message = "Android refused USB bit-perfect mixer attributes",
                    recoverable = true,
                )
            },
        )
    }

    private fun refresh() {
        _status.value = scan().copy(
            lastRequestedSampleRateHz = _status.value.lastRequestedSampleRateHz,
            lastRequestError = _status.value.lastRequestError,
        )
    }

    private fun scan(): EchoUsbAudioStatus {
        val device = findUsbOutputDevice()
        if (device == null) return EchoUsbAudioStatus(exclusiveEnabled = exclusiveEnabled)

        val supportedAttributes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching { audioManager.getSupportedMixerAttributes(device) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val preferredAttributes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching { audioManager.getPreferredMixerAttributes(androidMusicAttributes(), device) }.getOrNull()
        } else {
            null
        }

        return EchoUsbAudioStatus(
            exclusiveEnabled = exclusiveEnabled,
            deviceName = device.getDisplayName(),
            connected = true,
            supportedSampleRates = device.sampleRates.filter { it > 0 }.distinct().sorted(),
            bitPerfectSupported = supportedAttributes.any { it.isBitPerfect() },
            bitPerfectActive = preferredAttributes?.isBitPerfect() == true,
        )
    }

    private fun clearPreferredMixerAttributes() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val device = findUsbOutputDevice() ?: return
        runCatching {
            audioManager.clearPreferredMixerAttributes(androidMusicAttributes(), device)
        }
    }

    private fun findUsbOutputDevice(): AudioDeviceInfo? =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.isSink && it.isUsbAudioOutput() }
            .maxByOrNull { device ->
                when (device.type) {
                    AudioDeviceInfo.TYPE_USB_DEVICE -> 3
                    AudioDeviceInfo.TYPE_USB_HEADSET -> 2
                    AudioDeviceInfo.TYPE_USB_ACCESSORY -> 1
                    else -> 0
                }
            }

    private fun findBitPerfectAttributes(
        device: AudioDeviceInfo,
        sampleRateHz: Int,
    ): AudioMixerAttributes? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        return runCatching { audioManager.getSupportedMixerAttributes(device) }
            .getOrDefault(emptyList())
            .filter { it.isBitPerfect() }
            .firstOrNull { it.format.sampleRate == sampleRateHz }
    }

    private fun AudioDeviceInfo.isUsbAudioOutput(): Boolean =
        type == AudioDeviceInfo.TYPE_USB_DEVICE ||
            type == AudioDeviceInfo.TYPE_USB_HEADSET ||
            type == AudioDeviceInfo.TYPE_USB_ACCESSORY

    private fun AudioDeviceInfo.getDisplayName(): String =
        productName?.toString()?.takeIf { it.isNotBlank() }
            ?: address.takeIf { it.isNotBlank() }
            ?: "USB audio"

    private fun AudioMixerAttributes.isBitPerfect(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT

    private fun androidMusicAttributes(): android.media.AudioAttributes =
        android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
}

fun EchoPlaybackDiagnostics.withUsbAudioStatus(status: EchoUsbAudioStatus): EchoPlaybackDiagnostics =
    copy(
        outputRoute = status.outputRoute,
        offloadActive = status.bitPerfectActive,
        usbExclusiveEnabled = status.exclusiveEnabled,
        usbConnected = status.connected,
        usbDeviceName = status.deviceName,
        usbBitPerfectSupported = status.bitPerfectSupported,
        usbBitPerfectActive = status.bitPerfectActive,
        usbSupportedSampleRates = status.supportedSampleRates,
        usbLastRequestedSampleRateHz = status.lastRequestedSampleRateHz,
        usbLastRequestError = status.lastRequestError,
    )
