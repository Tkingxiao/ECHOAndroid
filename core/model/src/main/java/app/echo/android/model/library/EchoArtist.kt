package app.echo.android.model.library

data class EchoArtist(
    val id: String,
    val name: String,
    val artworkUri: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
)
