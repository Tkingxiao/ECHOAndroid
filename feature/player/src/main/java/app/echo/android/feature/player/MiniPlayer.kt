package app.echo.android.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echo.android.design.ArtworkTile
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentDeep
import app.echo.android.design.EchoAccentText
import app.echo.android.design.EchoGlassBorder
import app.echo.android.design.EchoHomeBlue
import app.echo.android.design.EchoHomeMist
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.design.progressFraction
import app.echo.android.model.playback.EchoPlaybackState
import app.echo.android.model.playback.EchoPlaybackStatus

@Composable
fun MiniPlayer(
    status: EchoPlaybackStatus,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = EchoAccentDeep.copy(alpha = 0.16f),
                spotColor = EchoHomeBlue.copy(alpha = 0.12f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.50f),
                        EchoHomeMist.copy(alpha = 0.34f),
                        EchoAccentDeep.copy(alpha = 0.14f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, EchoGlassBorder.copy(alpha = 0.76f)), shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ArtworkTile(
                artworkUri = status.track?.artworkUri,
                modifier = Modifier.size(52.dp),
                accent = EchoAccent,
                showSignal = status.track?.artworkUri == null,
                cornerRadius = 12.dp,
                elevation = 2.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = EchoAccentDeep.copy(alpha = 0.74f),
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        status.track?.title ?: "ECHO Mobile",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = RoonInk,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    status.track?.artist ?: "就绪",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = RoonMuted,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                LinearProgressIndicator(
                    progress = { progressFraction(status.positionMs, status.durationMs) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color(0xFFFF9FC8),
                    trackColor = Color.White.copy(alpha = 0.46f),
                )
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(EchoAccentDeep.copy(alpha = 0.16f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.58f)), CircleShape)
                    .clickable(
                        enabled = status.state != EchoPlaybackState.Idle || status.track != null,
                        onClick = onPlayPause,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (status.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "播放或暂停",
                    tint = EchoAccentText.copy(alpha = 0.92f),
                    modifier = Modifier.size(34.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = "播放队列",
                    tint = RoonMuted,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
