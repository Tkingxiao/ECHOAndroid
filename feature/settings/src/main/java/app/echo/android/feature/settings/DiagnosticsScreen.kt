package app.echo.android.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentText
import app.echo.android.design.EchoGlassBorder
import app.echo.android.design.EchoHomeBlue
import app.echo.android.design.EchoHomeMist
import app.echo.android.design.EchoPlaceholderLine
import app.echo.android.design.EchoSectionTitle
import app.echo.android.design.PageChrome
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.design.formatDuration
import app.echo.android.model.playback.EchoPlaybackState
import app.echo.android.model.playback.EchoPlaybackStatus
import app.echo.android.model.playback.EchoRepeatMode

@Composable
fun DiagnosticsScreen(status: EchoPlaybackStatus) {
    val diagnostics = status.diagnostics
    val bufferSeconds = "${diagnostics.bufferedMs / 1000}s"
    val codec = diagnostics.codec ?: "Media3"
    val lastCommand = commandLabel(diagnostics.lastCommand)
    PageChrome(title = "信号", subtitle = "音频链路与解码状态", badge = "状态", scrollable = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SignalHeroCard(
                status = status,
                output = diagnostics.outputRoute,
                codec = codec,
                buffer = bufferSeconds,
                lastCommand = lastCommand,
            )
            SignalFlowPanel(
                codec = codec,
                output = diagnostics.outputRoute,
                offloadActive = diagnostics.offloadActive,
            )
            CurrentStreamPanel(
                status = status,
                lastCommand = lastCommand,
                requestToken = diagnostics.requestToken,
            )
            HealthPanel(status = status)
        }
    }
}

