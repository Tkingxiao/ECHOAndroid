package app.echo.android.data

private val SearchTokenRegex = Regex("[\\p{L}\\p{M}\\p{N}]+")

internal fun sanitizeFtsQuery(rawQuery: String): String? {
    val tokens = SearchTokenRegex.findAll(rawQuery.trim().lowercase())
        .flatMap { match -> match.value.toFtsTokens().asSequence() }
        .filter { it.isNotBlank() }
        .map { token -> "$token*" }
        .toList()

    return tokens.takeIf { it.isNotEmpty() }?.joinToString(" AND ")
}

internal fun ftsRankQuery(rawQuery: String): String =
    "%${rawQuery.trim().lowercase()}%"

internal fun LibraryTrackEntity.toFtsEntity(): LibraryTrackFtsEntity =
    LibraryTrackFtsEntity(
        trackId = id,
        title = title,
        artist = artist,
        album = album.orEmpty(),
        albumArtist = albumArtist.orEmpty(),
        normalizedText = buildFtsNormalizedText(
            title,
            artist,
            album.orEmpty(),
            albumArtist.orEmpty(),
            normalizedTitle.orEmpty(),
            normalizedArtist.orEmpty(),
            normalizedAlbum.orEmpty(),
            normalizedAlbumArtist.orEmpty(),
        ),
    )

private fun String.toFtsTokens(): List<String> =
    if (any(Char::isCjk)) {
        filter { it.isLetterOrDigit() }
            .map { it.lowercase() }
    } else {
        listOf(this)
    }

private fun buildFtsNormalizedText(vararg parts: String): String {
    val joined = parts
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .lowercase()
    val cjkTokens = joined
        .asSequence()
        .filter(Char::isCjk)
        .map(Char::toString)
        .joinToString(" ")
    return listOf(joined, cjkTokens)
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

private fun Char.isCjk(): Boolean =
    this in '\u3400'..'\u4DBF' ||
        this in '\u4E00'..'\u9FFF' ||
        this in '\uF900'..'\uFAFF'
