package app.echo.android.model.playback

data class EchoPlaybackStatus(
    val state: EchoPlaybackState = EchoPlaybackState.Idle,
    val track: EchoTrackRef? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val repeatMode: EchoRepeatMode = EchoRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val playbackPitch: Float = 1f,
    val diagnostics: EchoPlaybackDiagnostics = EchoPlaybackDiagnostics(),
)
