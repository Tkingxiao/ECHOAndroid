package app.echo.android.data

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

class MediaStoreTrackScanner(
    private val contentResolver: ContentResolver,
) {
    suspend fun scanAudio(
        batchSize: Int = DefaultBatchSize,
        onBatch: suspend (List<LibraryTrackEntity>) -> Unit,
        onProgress: suspend (scannedCount: Int, currentTrack: LibraryTrackEntity?) -> Unit,
    ): Int {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        return contentResolver.query(collection, Projection, selection, null, sortOrder)
            ?.use { cursor -> cursor.scanTrackBatches(collection, batchSize, onBatch, onProgress) }
            ?: 0
    }

    private suspend fun Cursor.scanTrackBatches(
        collection: Uri,
        batchSize: Int,
        onBatch: suspend (List<LibraryTrackEntity>) -> Unit,
        onProgress: suspend (scannedCount: Int, currentTrack: LibraryTrackEntity?) -> Unit,
    ): Int {
        val columns = MediaStoreColumns.from(this)
        val safeBatchSize = batchSize.coerceAtLeast(1)
        val batch = ArrayList<LibraryTrackEntity>(safeBatchSize)
        var scannedCount = 0

        while (moveToNext()) {
            coroutineContext.ensureActive()
            runCatching {
                toTrackEntity(collection, columns)
            }.onSuccess { track ->
                batch += track
                scannedCount += 1
                onProgress(scannedCount, track)
                if (batch.size >= safeBatchSize) {
                    onBatch(batch.toList())
                    batch.clear()
                }
            }.onFailure { error ->
                Log.w(TAG, "Skipping unreadable MediaStore audio row.", error)
            }
        }

        if (batch.isNotEmpty()) {
            onBatch(batch.toList())
            batch.clear()
        }
        onProgress(scannedCount, null)
        return scannedCount
    }

    private fun Cursor.toTrackEntity(collection: Uri, columns: MediaStoreColumns): LibraryTrackEntity {
        val mediaId = getLong(columns.idIndex)
        val contentUri = Uri.withAppendedPath(collection, mediaId.toString()).toString()
        val title = getStringOrNull(columns.titleIndex)?.takeIf { it.isNotBlank() } ?: "未知曲目"
        val artist = getStringOrNull(columns.artistIndex)?.takeIf { it.isNotBlank() } ?: "未知艺术家"
        val rawTrack = getLongOrNull(columns.trackIndex)?.toInt()
        val albumId = getLongOrNull(columns.albumIdIndex)?.takeIf { it > 0L }

        return LibraryTrackEntity(
            id = "mediastore:$mediaId",
            contentUri = contentUri,
            title = title,
            artist = artist,
            album = getStringOrNull(columns.albumIndex)?.takeIf { it.isNotBlank() },
            albumArtist = getStringOrNull(columns.albumArtistIndex)?.takeIf { it.isNotBlank() },
            artworkUri = albumId?.let { "content://media/external/audio/albumart/$it" },
            durationMs = getLongOrNull(columns.durationIndex) ?: 0L,
            trackNumber = rawTrack?.rem(1000)?.takeIf { it > 0 },
            discNumber = rawTrack?.div(1000)?.takeIf { it > 0 },
            year = getLongOrNull(columns.yearIndex)?.toInt()?.takeIf { it > 0 },
            mimeType = getStringOrNull(columns.mimeIndex),
            sizeBytes = getLongOrNull(columns.sizeIndex) ?: 0L,
            dateModifiedSeconds = getLongOrNull(columns.modifiedIndex) ?: 0L,
        ).withScanMetadata()
    }

    private data class MediaStoreColumns(
        val idIndex: Int,
        val titleIndex: Int,
        val artistIndex: Int,
        val albumIndex: Int,
        val albumArtistIndex: Int,
        val albumIdIndex: Int,
        val durationIndex: Int,
        val trackIndex: Int,
        val yearIndex: Int,
        val mimeIndex: Int,
        val sizeIndex: Int,
        val modifiedIndex: Int,
    ) {
        companion object {
            fun from(cursor: Cursor): MediaStoreColumns =
                MediaStoreColumns(
                    idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID),
                    titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE),
                    artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST),
                    albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM),
                    albumArtistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST),
                    albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID),
                    durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION),
                    trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK),
                    yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR),
                    mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE),
                    sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE),
                    modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED),
                )
        }
    }

    private companion object {
        const val DefaultBatchSize = 500
        const val TAG = "MediaStoreTrackScanner"

        val Projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
    }
}
