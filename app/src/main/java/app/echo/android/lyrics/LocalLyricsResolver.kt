package app.echo.android.lyrics

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import app.echo.android.data.LibraryTrackEntity
import app.echo.android.lyrics.EchoLyricsParser
import app.echo.android.model.lyrics.EchoLyrics
import java.io.File

class LocalLyricsResolver(
    private val contentResolver: ContentResolver,
) {
    fun loadForTrack(track: LibraryTrackEntity): EchoLyrics? {
        val candidates = buildCandidateNames(track)
        return loadFromFileUri(track.contentUri, candidates)
            ?: loadFromMediaStore(track, candidates)
    }

    fun loadFromUri(uri: Uri): EchoLyrics? {
        val sourceLabel = displayName(uri) ?: uri.lastPathSegment
        return readText(uri)
            ?.let { EchoLyricsParser.parse(it, sourceLabel = sourceLabel) }
    }

    private fun loadFromFileUri(contentUri: String, candidates: List<String>): EchoLyrics? {
        val uri = runCatching { Uri.parse(contentUri) }.getOrNull() ?: return null
        if (uri.scheme != ContentResolver.SCHEME_FILE) return null
        val audioFile = uri.path?.let(::File) ?: return null
        val parent = audioFile.parentFile ?: return null

        return candidates.asSequence()
            .map { File(parent, it) }
            .firstOrNull { it.isFile && it.canRead() }
            ?.let { file ->
                EchoLyricsParser.parse(file.readText(), sourceLabel = file.name)
            }
    }

    private fun loadFromMediaStore(track: LibraryTrackEntity, candidates: List<String>): EchoLyrics? {
        val relativePath = track.relativePath?.takeIf { it.isNotBlank() } ?: return null
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
        )
        val displayNamePlaceholders = candidates.joinToString(",") { "?" }
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Files.FileColumns.RELATIVE_PATH} = ? AND " +
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} IN ($displayNamePlaceholders)"
        } else {
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} IN ($displayNamePlaceholders)"
        }
        val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(relativePath) + candidates.toTypedArray()
        } else {
            candidates.toTypedArray()
        }

        return contentResolver.query(collection, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val displayName = cursor.getString(nameIndex)
                    val lyricsUri = Uri.withAppendedPath(collection, id.toString())
                    val parsed = readText(lyricsUri)
                        ?.let { EchoLyricsParser.parse(it, sourceLabel = displayName) }
                    if (parsed != null) return@use parsed
                }
                null
            }
    }

    private fun readText(uri: Uri): String? =
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().use { it.readText() }
            }
        }.getOrNull()

    private fun buildCandidateNames(track: LibraryTrackEntity): List<String> {
        val bases = linkedSetOf<String>()
        displayNameBase(track.contentUri)?.let(bases::add)
        track.title.takeIf { it.isNotBlank() }?.let(bases::add)

        return bases
            .flatMap { base -> LyricsExtensions.map { extension -> "$base$extension" } }
            .distinct()
    }

    private fun displayNameBase(contentUri: String): String? {
        val uri = runCatching { Uri.parse(contentUri) }.getOrNull() ?: return null
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            return uri.path?.let(::File)?.nameWithoutExtension
        }
        return contentResolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    ?.substringBeforeLast('.', missingDelimiterValue = "")
                    ?.takeIf { it.isNotBlank() }
            } else {
                null
            }
        }
    }

    private fun displayName(uri: Uri): String? =
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }

    private companion object {
        val LyricsExtensions = listOf(
            ".lrc",
            ".elrc",
            ".lrcx",
            ".yrc",
            ".ttml",
            ".srt",
            ".vtt",
            ".webvtt",
            ".ass",
            ".ssa",
            ".qrc",
            ".txt",
        )
    }
}
