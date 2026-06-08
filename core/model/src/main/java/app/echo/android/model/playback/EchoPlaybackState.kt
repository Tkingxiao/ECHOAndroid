package app.echo.android.model.playback

enum class EchoPlaybackState {
    Idle,
    Loading,
    Playing,
    Paused,
    Seeking,
    Buffering,
    Ended,
    Stopped,
    Error,
}
