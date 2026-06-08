package app.echo.android.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import app.echo.android.model.library.EchoTrack
import app.echo.android.model.playback.EchoPlaybackDiagnostics
import app.echo.android.model.playback.EchoPlaybackState
import app.echo.android.model.playback.EchoPlaybackStatus
import app.echo.android.model.playback.EchoRepeatMode
import app.echo.android.model.playback.EchoTrackRef

fun MediaItem.toEchoTrackRef(durationMs: Long = 0L): EchoTrackRef {
    val metadata = mediaMetadata
    return EchoTrackRef(
        id = mediaId,
        uri = localConfiguration?.uri?.toString().orEmpty(),
        title = metadata.title?.toString().orEmpty().ifBlank { "Unknown Track" },
        artist = metadata.artist?.toString().orEmpty().ifBlank { "Unknown Artist" },
        album = metadata.albumTitle?.toString(),
        artworkUri = metadata.artworkUri?.toString(),
        durationMs = durationMs.coerceAtLeast(0L),
    )
}

fun EchoTrackRef.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri?.let(Uri::parse))
                .build(),
        )
        .build()

fun EchoTrack.toEchoTrackRef(): EchoTrackRef =
    EchoTrackRef(
        id = id,
        uri = uri,
        title = title,
        artist = artist,
        album = album,
        artworkUri = artworkUri,
        durationMs = durationMs,
    )

fun EchoTrack.toMediaItem(): MediaItem =
    toEchoTrackRef().toMediaItem()

fun Player.toEchoPlaybackStatus(): EchoPlaybackStatus {
    val item = currentMediaItem
    val track = item?.toEchoTrackRef(durationMs = duration.takeIf { it > 0L } ?: 0L)
    val safeDuration = duration.takeIf { it > 0L } ?: track?.durationMs ?: 0L
    return EchoPlaybackStatus(
        state = toEchoPlaybackState(),
        track = track,
        positionMs = currentPosition.coerceAtLeast(0L),
        durationMs = safeDuration,
        isPlaying = isPlaying,
        repeatMode = repeatMode.toEchoRepeatMode(),
        shuffleEnabled = shuffleModeEnabled,
        diagnostics = EchoPlaybackDiagnostics(
            outputRoute = "Media3 / AudioTrack",
            bufferedMs = (bufferedPosition - currentPosition).coerceAtLeast(0L),
            requestToken = item?.mediaId?.hashCode()?.toLong() ?: 0L,
            lastCommand = if (isPlaying) "play" else "idle",
        ),
    )
}

fun Player.toEchoPlaybackState(): EchoPlaybackState = when {
    playbackState == Player.STATE_IDLE && currentMediaItem == null -> EchoPlaybackState.Idle
    playbackState == Player.STATE_IDLE -> EchoPlaybackState.Stopped
    playbackState == Player.STATE_BUFFERING -> EchoPlaybackState.Buffering
    playbackState == Player.STATE_ENDED -> EchoPlaybackState.Ended
    isPlaying -> EchoPlaybackState.Playing
    playWhenReady -> EchoPlaybackState.Loading
    else -> EchoPlaybackState.Paused
}

fun Int.toEchoRepeatMode(): EchoRepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> EchoRepeatMode.All
    Player.REPEAT_MODE_ONE -> EchoRepeatMode.One
    else -> EchoRepeatMode.Off
}
