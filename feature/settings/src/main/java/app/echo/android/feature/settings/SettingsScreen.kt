package app.echo.android.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentDeep
import app.echo.android.design.EchoAccentText
import app.echo.android.design.EchoGlassBorder
import app.echo.android.design.EchoHomeBlue
import app.echo.android.design.EchoHomeMist
import app.echo.android.design.EchoSectionTitle
import app.echo.android.design.PageChrome
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.model.playback.EchoPlaybackStatus
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    status: EchoPlaybackStatus,
    trackCount: Int,
    albumCount: Int,
    artistCount: Int,
    dynamicArtworkEnabled: Boolean,
    compactModeEnabled: Boolean,
    pcHandoffEnabled: Boolean,
    showLyricsControlDeck: Boolean,
    onlineLyricsEnabled: Boolean,
    usbExclusiveEnabled: Boolean,
    customBackgroundMode: String,
    customBackgroundUri: String?,
    customBackgroundBlur: Float,
    customBackgroundBrightness: Float,
    customBackgroundGlass: Float,
    onDynamicArtworkEnabledChange: (Boolean) -> Unit,
    onCompactModeEnabledChange: (Boolean) -> Unit,
    onPcHandoffEnabledChange: (Boolean) -> Unit,
    onShowLyricsControlDeckChange: (Boolean) -> Unit,
    onOnlineLyricsEnabledChange: (Boolean) -> Unit,
    onUsbExclusiveEnabledChange: (Boolean) -> Unit,
    onPickImageBackground: () -> Unit,
    onPickVideoBackground: () -> Unit,
    onClearCustomBackground: () -> Unit,
    onCustomBackgroundBlurChange: (Float) -> Unit,
    onCustomBackgroundBrightnessChange: (Float) -> Unit,
    onCustomBackgroundGlassChange: (Float) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenConnect: () -> Unit,
) {
    val sectionGap = if (compactModeEnabled) 8.dp else 14.dp

    PageChrome(
        title = "设置",
        subtitle = "移动端偏好与 ECHO 互联",
        badge = "设置",
        scrollable = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(sectionGap)) {
            SettingsHeroCard(
                status = status,
                trackCount = trackCount,
                albumCount = albumCount,
                artistCount = artistCount,
                dynamicArtwork = dynamicArtworkEnabled,
            )
            SettingsSectionCard(title = "界面") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Palette,
                    title = "动态封面氛围",
                    detail = "播放页和迷你播放器跟随封面微调背景",
                    checked = dynamicArtworkEnabled,
                    onCheckedChange = onDynamicArtworkEnabledChange,
                )
                SettingsSwitchRow(
                    icon = Icons.Rounded.Speed,
                    title = "紧凑显示",
                    detail = "为小屏幕减少卡片间距",
                    checked = compactModeEnabled,
                    onCheckedChange = onCompactModeEnabledChange,
                )
            }
            SettingsSectionCard(title = "自定义背景") {
                SettingsActionRow(
                    icon = Icons.Rounded.Image,
                    title = "背景图片",
                    detail = backgroundDetail(customBackgroundMode, customBackgroundUri, "image"),
                    onClick = onPickImageBackground,
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Movie,
                    title = "视频壁纸",
                    detail = backgroundDetail(customBackgroundMode, customBackgroundUri, "video"),
                    onClick = onPickVideoBackground,
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Settings,
                    title = "恢复默认背景",
                    detail = if (customBackgroundUri.isNullOrBlank()) {
                        "正在使用默认 ECHO 背景"
                    } else {
                        "移除当前自定义背景"
                    },
                    enabled = !customBackgroundUri.isNullOrBlank(),
                    onClick = onClearCustomBackground,
                )
                SettingsSliderRow(
                    icon = Icons.Rounded.Palette,
                    title = "毛玻璃模糊",
                    detail = "${customBackgroundBlur.roundToInt()} dp",
                    value = customBackgroundBlur,
                    valueRange = 0f..80f,
                    steps = 15,
                    onValueChange = onCustomBackgroundBlurChange,
                )
                SettingsSliderRow(
                    icon = Icons.Rounded.Speed,
                    title = "背景亮度",
                    detail = "${(customBackgroundBrightness * 100f).roundToInt()}%",
                    value = customBackgroundBrightness,
                    valueRange = 0.35f..1.15f,
                    steps = 15,
                    onValueChange = onCustomBackgroundBrightnessChange,
                )
                SettingsSliderRow(
                    icon = Icons.Rounded.Palette,
                    title = "玻璃覆盖",
                    detail = "${(customBackgroundGlass * 100f).roundToInt()}%",
                    value = customBackgroundGlass,
                    valueRange = 0.18f..0.90f,
                    steps = 11,
                    onValueChange = onCustomBackgroundGlassChange,
                )
            }
            SettingsSectionCard(title = "播放") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Lyrics,
                    title = "歌词同步工具",
                    detail = "在歌词页显示导入、格式和偏移微调面板",
                    checked = showLyricsControlDeck,
                    onCheckedChange = onShowLyricsControlDeckChange,
                )
                SettingsSwitchRow(
                    icon = Icons.Rounded.Lyrics,
                    title = "网络歌词",
                    detail = "无内嵌或本地歌词时自动尝试网易云和 LRCLIB",
                    checked = onlineLyricsEnabled,
                    onCheckedChange = onOnlineLyricsEnabledChange,
                )
                SettingsSwitchRow(
                    icon = Icons.Rounded.Usb,
                    title = "USB 独占输出",
                    detail = usbExclusiveDetail(status),
                    checked = usbExclusiveEnabled,
                    onCheckedChange = onUsbExclusiveEnabledChange,
                )
            }
            SettingsSectionCard(title = "互联") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Devices,
                    title = "PC 接力入口",
                    detail = "保留桌面端连接与远程控制入口",
                    checked = pcHandoffEnabled,
                    onCheckedChange = onPcHandoffEnabledChange,
                )
                SettingsActionRow(
                    icon = Icons.Rounded.Devices,
                    title = "连接 PC ECHO",
                    detail = if (pcHandoffEnabled) "管理配对和远程播放状态" else "PC 接力入口已关闭",
                    enabled = pcHandoffEnabled,
                    onClick = onOpenConnect,
                )
            }
            SettingsSectionCard(title = "曲库") {
                SettingsActionRow(
                    icon = Icons.Rounded.LibraryMusic,
                    title = "本地音乐",
                    detail = "查看歌曲、专辑、艺术家和扫描入口",
                    onClick = onOpenLibrary,
                )
            }
            Spacer(Modifier.height(156.dp))
        }
    }
}

private fun backgroundDetail(mode: String, uri: String?, rowMode: String): String =
    if (mode == rowMode && !uri.isNullOrBlank()) {
        "已选择 ${uri.substringAfterLast('/').takeLast(28)}"
    } else {
        "从本机文件选择"
    }

@Composable
private fun SettingsHeroCard(
    status: EchoPlaybackStatus,
    trackCount: Int,
    albumCount: Int,
    artistCount: Int,
    dynamicArtwork: Boolean,
) {
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val heroColors = if (dynamicArtwork) {
        if (dark) {
            listOf(
                scheme.surface.copy(alpha = 0.92f),
                scheme.surfaceVariant.copy(alpha = 0.78f),
                scheme.primary.copy(alpha = 0.22f),
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.88f),
                EchoHomeMist.copy(alpha = 0.82f),
                EchoAccentDeep.copy(alpha = 0.16f),
            )
        }
    } else {
        if (dark) {
            listOf(
                scheme.surface.copy(alpha = 0.92f),
                scheme.surfaceVariant.copy(alpha = 0.72f),
                scheme.surface.copy(alpha = 0.88f),
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.92f),
                EchoHomeMist.copy(alpha = 0.76f),
                Color.White.copy(alpha = 0.88f),
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(heroColors),
            )
            .border(BorderStroke(1.dp, EchoGlassBorder), RoundedCornerShape(26.dp))
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "ECHO Mobile",
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        status.track?.title ?: "本机播放就绪",
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SettingsIconBubble(Icons.Rounded.Settings)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SettingsStatTile("歌曲", trackCount.toString(), Modifier.weight(1f))
                SettingsStatTile("专辑", albumCount.toString(), Modifier.weight(1f))
                SettingsStatTile("艺术家", artistCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(scheme.surface.copy(alpha = if (dark) 0.78f else 0.74f))
            .border(
                BorderStroke(1.dp, if (dark) scheme.outlineVariant.copy(alpha = 0.62f) else EchoGlassBorder),
                RoundedCornerShape(22.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            title,
            color = scheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRowShell(icon = icon, title = title, detail = detail) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    detail: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    SettingsRowShell(
        icon = icon,
        title = title,
        detail = detail,
        modifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Text(
            if (enabled) "进入" else "关闭",
            color = if (enabled) EchoAccentText else RoonMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingsSliderRow(
    icon: ImageVector,
    title: String,
    detail: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface.copy(alpha = if (dark) 0.70f else 0.64f))
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) scheme.outlineVariant.copy(alpha = 0.52f) else EchoGlassBorder.copy(alpha = 0.78f),
                ),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBubble(icon)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    color = scheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    detail,
                    color = scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
            )
        }
    }
}

@Composable
private fun SettingsRowShell(
    icon: ImageVector,
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface.copy(alpha = if (dark) 0.70f else 0.64f))
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) scheme.outlineVariant.copy(alpha = 0.52f) else EchoGlassBorder.copy(alpha = 0.78f),
                ),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBubble(icon)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                title,
                color = scheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                detail,
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

@Composable
private fun SettingsIconBubble(icon: ImageVector) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.primary.copy(alpha = if (dark) 0.18f else 0.14f))
            .border(BorderStroke(1.dp, scheme.primary.copy(alpha = 0.20f)), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surface.copy(alpha = if (dark) 0.70f else 0.64f))
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) scheme.outlineVariant.copy(alpha = 0.52f) else EchoGlassBorder.copy(alpha = 0.78f),
                ),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = scheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(value, color = scheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun usbExclusiveDetail(status: EchoPlaybackStatus): String {
    val diagnostics = status.diagnostics
    return when {
        diagnostics.usbBitPerfectActive -> "USB DAC 已使用 bit-perfect 链路"
        diagnostics.usbBitPerfectSupported -> "插入的 USB DAC 支持 bit-perfect，可按曲目采样率请求"
        diagnostics.usbConnected -> "已连接 USB DAC，但当前只走 Android mixer"
        else -> "高级输出模式；无 USB DAC 或不支持时自动回退"
    }
}
