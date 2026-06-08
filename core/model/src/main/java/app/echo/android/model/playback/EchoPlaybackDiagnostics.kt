package app.echo.android.model.playback

data class EchoPlaybackDiagnostics(
    val codec: String? = null,
    val sampleRateHz: Int? = null,
    val channelCount: Int? = null,
    val bitrate: Int? = null,
    val outputRoute: String = "system",
    val offloadActive: Boolean = false,
    val bufferedMs: Long = 0L,
    val requestToken: Long = 0L,
    val lastCommand: String? = null,
    val lastError: EchoPlaybackError? = null,
)
