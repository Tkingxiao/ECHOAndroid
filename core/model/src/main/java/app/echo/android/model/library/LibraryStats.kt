package app.echo.android.model.library

data class LibraryStats(
    val trackCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val durationMs: Long = 0L,
    val totalSizeBytes: Long = 0L,
)
