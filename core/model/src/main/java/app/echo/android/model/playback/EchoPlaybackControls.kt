package app.echo.android.model.playback

sealed interface EchoPlaybackCommand {
    data class PlayTrack(val track: EchoTrackRef, val startPositionMs: Long = 0L) : EchoPlaybackCommand
    data object PlayPause : EchoPlaybackCommand
    data object Pause : EchoPlaybackCommand
    data object Stop : EchoPlaybackCommand
    data object Next : EchoPlaybackCommand
    data object Previous : EchoPlaybackCommand
    data class SeekTo(val positionMs: Long) : EchoPlaybackCommand
}

data class EchoPlaybackControls(
    val canPlayPause: Boolean = true,
    val canSeek: Boolean = false,
    val canSkipNext: Boolean = false,
    val canSkipPrevious: Boolean = false,
)
