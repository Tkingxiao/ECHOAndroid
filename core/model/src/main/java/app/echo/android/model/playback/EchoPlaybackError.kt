package app.echo.android.model.playback

enum class EchoAudioErrorKind {
    PermissionDenied,
    FileMissing,
    UnsupportedFormat,
    DecodeFailure,
    OutputRouteFailure,
    AudioFocusLost,
    SystemInterrupted,
    Unknown,
}

data class EchoPlaybackError(
    val kind: EchoAudioErrorKind,
    val message: String,
    val recoverable: Boolean,
)
