package app.echo.android.model.library

data class LibrarySource(
    val id: String,
) {
    companion object {
        val MediaStore = LibrarySource("mediastore")
        val Unknown = LibrarySource("unknown")
    }
}
