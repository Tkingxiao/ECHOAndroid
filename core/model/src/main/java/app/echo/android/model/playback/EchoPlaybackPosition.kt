package app.echo.android.model.playback

data class EchoPlaybackPosition(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
)
