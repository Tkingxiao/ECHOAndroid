package app.echo.android.model.library

data class ArtistSummary(
    val artistKey: String,
    val name: String,
    val artworkUri: String?,
    val albumCount: Int,
    val trackCount: Int,
    val durationMs: Long,
)

enum class ArtistSortMode {
    Name,
    AlbumCount,
    TrackCount,
    Duration,
}
