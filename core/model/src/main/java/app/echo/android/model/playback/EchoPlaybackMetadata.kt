package app.echo.android.model.playback

data class EchoPlaybackMetadata(
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUri: String? = null,
)
