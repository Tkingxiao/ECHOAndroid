package app.echo.android.data

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(tokenizer = "unicode61")
@Entity(tableName = "library_tracks_fts")
data class LibraryTrackFtsEntity(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val normalizedText: String,
)
