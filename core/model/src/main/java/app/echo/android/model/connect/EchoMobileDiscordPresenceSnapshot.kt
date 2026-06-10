package app.echo.android.model.connect

data class EchoMobileDiscordPresenceSnapshot(
    val enabled: Boolean = false,
    val state: EchoRemotePlaybackState = EchoRemotePlaybackState.Idle,
    val track: EchoRemoteTrack? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val deviceName: String = "ECHOAndroid",
    val updatedAtEpochMs: Long = 0L,
)
