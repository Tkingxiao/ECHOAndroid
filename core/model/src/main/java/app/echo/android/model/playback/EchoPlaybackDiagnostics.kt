package app.echo.android.model.playback

data class EchoPlaybackDiagnostics(
    val codec: String? = null,
    val sampleRateHz: Int? = null,
    val channelCount: Int? = null,
    val bitDepth: Int? = null,
    val bitrate: Int? = null,
    val outputRoute: String = "system",
    val offloadActive: Boolean = false,
    val usbExclusiveEnabled: Boolean = false,
    val usbConnected: Boolean = false,
    val usbDeviceName: String? = null,
    val usbBitPerfectSupported: Boolean = false,
    val usbBitPerfectActive: Boolean = false,
    val usbSupportedSampleRates: List<Int> = emptyList(),
    val usbLastRequestedSampleRateHz: Int? = null,
    val usbLastRequestError: EchoPlaybackError? = null,
    val bufferedMs: Long = 0L,
    val requestToken: Long = 0L,
    val lastCommand: String? = null,
    val lastError: EchoPlaybackError? = null,
)
