package app.echo.android.model.connect

enum class EchoRemoteConnectionState {
    Disconnected,
    Pairing,
    Connecting,
    Connected,
    Reconnecting,
    Error,
}
