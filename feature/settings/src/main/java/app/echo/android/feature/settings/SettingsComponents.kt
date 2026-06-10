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
import app.echo.android.design.LocalEchoDarkTheme
import app.echo.android.design.PageChrome
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.design.formatDuration
import app.echo.android.model.playback.EchoPlaybackState
import app.echo.android.model.playback.EchoPlaybackStatus
import app.echo.android.model.playback.EchoRepeatMode

@Composable
private fun signalPanelColor(lightAlpha: Float = 0.64f): Color {
    val scheme = MaterialTheme.colorScheme
    return if (LocalEchoDarkTheme.current) scheme.surface.copy(alpha = 0.86f) else Color.White.copy(alpha = lightAlpha)
}

@Composable
private fun signalPanelBorder(lightAlpha: Float = 0.84f): BorderStroke {
    val scheme = MaterialTheme.colorScheme
    return BorderStroke(
        1.dp,
        if (LocalEchoDarkTheme.current) scheme.outlineVariant.copy(alpha = 0.58f) else EchoGlassBorder.copy(alpha = lightAlpha),
    )
}

@Composable
private fun signalHeroBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        if (LocalEchoDarkTheme.current) {
            listOf(
                scheme.surface.copy(alpha = 0.94f),
                scheme.surfaceVariant.copy(alpha = 0.82f),
                scheme.primary.copy(alpha = 0.14f),
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.72f),
                EchoHomeMist.copy(alpha = 0.64f),
                EchoHomeBlue.copy(alpha = 0.12f),
            )
        },
    )
}

@Composable
internal fun SignalHeroCard(
    status: EchoPlaybackStatus,
    output: String,
    codec: String,
    buffer: String,
    lastCommand: String,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                signalHeroBrush(),
            )
            .border(signalPanelBorder(0.86f), RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "链路总览",
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (status.isPlaying) "正在输出稳定音频流" else "等待播放，链路保持就绪",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                StatePill(label = playbackStateLabel(status.state), active = status.isPlaying)
            }
            SignalBars(active = status.isPlaying)
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                SignalStatTile("输出", output, EchoAccent, Modifier.weight(1f))
                SignalStatTile("解码", codec, EchoAccent, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                SignalStatTile("缓冲", buffer, Color(0xFF35C28E), Modifier.weight(1f))
                SignalStatTile("命令", lastCommand, EchoAccent, Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun StatePill(label: String, active: Boolean) {
    val accent = if (active) Color(0xFF1A9B68) else EchoAccentText
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun SignalStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(signalPanelColor(0.62f))
            .border(signalPanelBorder(0.80f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                label,
                color = accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                value,
                color = scheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SignalFlowPanel(
    codec: String,
    output: String,
    offloadActive: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(signalPanelColor(0.64f))
            .border(signalPanelBorder(0.84f), RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            EchoSectionTitle("链路路径", "从曲库到输出设备")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SignalFlowStage("曲库", "本机", EchoAccent, Modifier.weight(1f))
                FlowArrow()
                SignalFlowStage("解码", codec, EchoAccent, Modifier.weight(1f))
                FlowArrow()
                SignalFlowStage("输出", output, EchoAccent, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FlowChip("本机优先", selected = true, Modifier.weight(1f))
                FlowChip("DSP ${if (offloadActive) "开启" else "关闭"}", selected = offloadActive, Modifier.weight(1f))
                FlowChip("PC 待接力", selected = false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun SignalFlowStage(
    title: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f)),
                ),
            )
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.32f)), RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                detail,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun FlowArrow() {
    Text(
        "→",
        modifier = Modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
internal fun FlowChip(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    val accent = EchoAccent
    val dark = LocalEchoDarkTheme.current
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) accent.copy(alpha = 0.14f) else signalPanelColor(0.54f))
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) accent.copy(alpha = 0.28f) else if (dark) scheme.outlineVariant.copy(alpha = 0.52f) else EchoGlassBorder.copy(alpha = 0.74f),
                ),
                RoundedCornerShape(20.dp),
            )
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
internal fun CurrentStreamPanel(
    status: EchoPlaybackStatus,
    lastCommand: String,
    requestToken: Long,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(signalPanelColor(0.64f))
            .border(signalPanelBorder(0.84f), RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EchoSectionTitle("当前流", status.track?.album ?: "暂无曲目")
            DiagnosticLine("曲目", status.track?.title ?: "无")
            DiagnosticLine("进度", "${formatDuration(status.positionMs)} / ${formatDuration(status.durationMs)}")
            DiagnosticLine("命令", lastCommand)
            DiagnosticLine("令牌", requestToken.toString())
        }
    }
}

@Composable
internal fun HealthPanel(status: EchoPlaybackStatus) {
    val diagnostics = status.diagnostics
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(signalPanelColor(0.64f))
            .border(signalPanelBorder(0.84f), RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EchoSectionTitle("健康", diagnostics.lastError?.message ?: "未记录掉音")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FlowChip("稳定", selected = diagnostics.lastError == null, Modifier.weight(1f))
                FlowChip("本机", selected = true, Modifier.weight(1f))
                FlowChip("接力", selected = false, Modifier.weight(1f))
            }
            EchoPlaceholderLine(if (diagnostics.lastError == null) "解码回退记录为空" else "查看解码回退")
            EchoPlaceholderLine("PC 链路质量待测")
        }
    }
}

@Composable
internal fun SignalBars(active: Boolean) {
    val heights = listOf(18.dp, 30.dp, 24.dp, 42.dp, 28.dp, 48.dp, 34.dp, 22.dp, 38.dp, 26.dp, 44.dp, 20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(signalPanelColor(0.46f))
            .border(signalPanelBorder(0.62f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            heights.forEachIndexed { index, height ->
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(if (active || index % 2 == 0) height else height * 0.5f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    EchoAccent.copy(alpha = if (active) 0.95f else 0.4f),
                                    EchoAccent.copy(alpha = if (active) 0.7f else 0.3f),
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
internal fun DiagnosticLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun playbackStateLabel(state: EchoPlaybackState): String =
    when (state) {
        EchoPlaybackState.Idle -> "空闲"
        EchoPlaybackState.Loading -> "加载中"
        EchoPlaybackState.Playing -> "播放中"
        EchoPlaybackState.Paused -> "已暂停"
        EchoPlaybackState.Seeking -> "定位中"
        EchoPlaybackState.Buffering -> "缓冲中"
        EchoPlaybackState.Ended -> "已结束"
        EchoPlaybackState.Stopped -> "已停止"
        EchoPlaybackState.Error -> "错误"
    }

internal fun repeatModeLabel(mode: EchoRepeatMode): String =
    when (mode) {
        EchoRepeatMode.Off -> "循环关闭"
        EchoRepeatMode.All -> "列表循环"
        EchoRepeatMode.One -> "单曲循环"
    }

internal fun commandLabel(command: String?): String =
    when (command?.lowercase()) {
        null, "idle" -> "空闲"
        "play", "playpause" -> "播放"
        "pause" -> "暂停"
        "next" -> "下一首"
        "previous" -> "上一首"
        "seek" -> "跳转"
        "stop" -> "停止"
        else -> command
    }

