package app.echo.android.feature.settings

import androidx.compose.runtime.Composable
import app.echo.android.model.playback.EchoPlaybackStatus

@Composable
fun SettingsScreen(status: EchoPlaybackStatus) {
    DiagnosticsScreen(status = status)
}
