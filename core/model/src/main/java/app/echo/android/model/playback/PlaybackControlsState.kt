package app.echo.android.model.playback

data class PlaybackControlsState(
    val state: EchoPlaybackState = EchoPlaybackState.Idle,
    val isPlaying: Boolean = false,
    val repeatMode: EchoRepeatMode = EchoRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val playbackPitch: Float = 1f,
    val canSkipNext: Boolean = false,
    val canSkipPrevious: Boolean = false,
    val canSeek: Boolean = false,
)
