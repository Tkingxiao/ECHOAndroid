package app.echo.android.model.library

data class EchoAlbum(
    val id: String,
    val title: String,
    val artist: String? = null,
    val artworkUri: String? = null,
    val trackCount: Int = 0,
    val durationMs: Long = 0L,
)
