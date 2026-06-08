package app.echo.android.feature.connect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echo.android.design.ArtworkTile
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentText
import app.echo.android.design.EchoColors
import app.echo.android.design.EchoGlassBorder
import app.echo.android.design.EchoHomeBlue
import app.echo.android.design.EchoHomeMist
import app.echo.android.design.EchoMetricTile
import app.echo.android.design.EchoPanel
import app.echo.android.design.EchoPlaceholderLine
import app.echo.android.design.EchoSectionTitle
import app.echo.android.design.EchoSegmentChip
import app.echo.android.design.EchoTextButton
import app.echo.android.design.PageChrome
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.model.connect.EchoRemoteConnectionState

@Composable
fun ConnectScreen(
    remoteState: EchoRemoteConnectionState,
    pcTitle: String,
    trackTitle: String,
    trackArtist: String,
    isPlaying: Boolean,
    onPairDemo: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = remoteState == EchoRemoteConnectionState.Connected
    PageChrome(title = "连接", subtitle = "串流服务 · PC 联动", badge = "互联", scrollable = true) {
        EchoSectionTitle("音乐服务", "连接你的曲库来源")
        Spacer(Modifier.height(12.dp))
        ServiceCard(
            name = "网易云音乐",
            subtitle = "歌单 · 每日推荐 · 私人 FM",
            icon = Icons.Rounded.CloudQueue,
            brandColor = Color(0xFFE0243A),
            statusLabel = "即将上线",
            active = false,
            locked = true,
            onClick = {},
        )
        Spacer(Modifier.height(10.dp))
        ServiceCard(
            name = "本地曲库",
            subtitle = "已扫描本机音频文件",
            icon = Icons.Rounded.LibraryMusic,
            brandColor = Color(0xFF35C28E),
            statusLabel = "已连接",
            active = true,
            locked = false,
            onClick = {},
        )
        Spacer(Modifier.height(20.dp))
        EchoSectionTitle("设备联动", if (connected) "手机控制，PC 输出" else "配对后接管 PC ECHO 播放")
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.70f),
                            EchoHomeMist.copy(alpha = 0.62f),
                            EchoHomeBlue.copy(alpha = 0.08f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(1.dp, EchoGlassBorder.copy(alpha = 0.86f)),
                    RoundedCornerShape(20.dp),
                ),
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(EchoHomeBlue),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Devices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp),
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            pcTitle,
                            color = RoonInk,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            remoteConnectionLabel(remoteState),
                            color = RoonMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    ServiceStatusPill(
                        label = if (connected) "已配对" else "未配对",
                        active = connected,
                        locked = false,
                    )
                }
                if (connected) {
                    RemoteNowPlaying(
                        title = trackTitle,
                        artist = trackArtist,
                        isPlaying = isPlaying,
                        controlsEnabled = true,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                    )
                } else {
                    EchoPlaceholderLine("局域网内发现 PC ECHO 后，输入配对码即可接管播放")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EchoTextButton(
                        text = if (connected) "已配对" else "配对 PC",
                        onClick = onPairDemo,
                        enabled = !connected,
                    )
                    if (connected) {
                        TextButton(onClick = onDisconnect) {
                            Text("断开", color = EchoHomeBlue)
                        }
                    }
                }
            }
        }
    }
}

