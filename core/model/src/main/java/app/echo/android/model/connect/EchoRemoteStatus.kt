package app.echo.android.model.connect

data class EchoRemoteStatus(
    val connectionState: EchoRemoteConnectionState = EchoRemoteConnectionState.Disconnected,
    val endpoint: EchoRemoteEndpoint? = null,
    val playback: EchoRemotePlaybackSnapshot = EchoRemotePlaybackSnapshot(),
    val mobileDiscordPresence: EchoMobileDiscordPresenceSnapshot? = null,
    val error: String? = null,
)
