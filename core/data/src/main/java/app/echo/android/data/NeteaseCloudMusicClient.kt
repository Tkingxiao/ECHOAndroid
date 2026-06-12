package app.echo.android.data

const val NeteaseSourceId = "netease"

fun parseNeteaseSongId(trackId: String): Long? =
    trackId.removePrefix("$NeteaseSourceId:song:")
        .takeIf { it != trackId }
        ?.toLongOrNull()
