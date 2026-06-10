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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import app.echo.android.design.LocalEchoDarkTheme
import app.echo.android.design.PageChrome
import app.echo.android.design.RoonInk
import app.echo.android.design.RoonMuted
import app.echo.android.model.connect.EchoRemoteConnectionState
import app.echo.android.model.library.LibraryScanPhase
import app.echo.android.model.library.LibraryScanProgress

@Composable
fun ConnectScreen(
    remoteState: EchoRemoteConnectionState,
    pcTitle: String,
    trackTitle: String,
    trackArtist: String,
    isPlaying: Boolean,
    discordPresenceEnabled: Boolean,
    discordPresenceReady: Boolean,
    discordPresenceTrackTitle: String?,
    subsonicServerUrl: String?,
    subsonicUsername: String?,
    subsonicPassword: String?,
    webDavServerUrl: String?,
    webDavUsername: String?,
    webDavPassword: String?,
    remoteScanState: LibraryScanProgress,
    onPairDemo: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onDisconnect: () -> Unit,
    onSyncSubsonicLibrary: (String, String, String) -> Unit,
    onSaveSubsonicCredentials: (String, String, String) -> Unit,
    onClearSubsonicCredentials: () -> Unit,
    onSyncWebDavLibrary: (String, String, String) -> Unit,
    onSaveWebDavCredentials: (String, String, String) -> Unit,
    onClearWebDavCredentials: () -> Unit,
    onCancelRemoteSync: () -> Unit,
) {
    val connected = remoteState == EchoRemoteConnectionState.Connected
    val scheme = MaterialTheme.colorScheme
    val dark = LocalEchoDarkTheme.current
    var subsonicServerInput by rememberSaveable(subsonicServerUrl) { mutableStateOf(subsonicServerUrl.orEmpty()) }
    var subsonicUserInput by rememberSaveable(subsonicUsername) { mutableStateOf(subsonicUsername.orEmpty()) }
    var subsonicPasswordInput by rememberSaveable(subsonicPassword) { mutableStateOf(subsonicPassword.orEmpty()) }
    var webDavServerInput by rememberSaveable(webDavServerUrl) { mutableStateOf(webDavServerUrl.orEmpty()) }
    var webDavUserInput by rememberSaveable(webDavUsername) { mutableStateOf(webDavUsername.orEmpty()) }
    var webDavPasswordInput by rememberSaveable(webDavPassword) { mutableStateOf(webDavPassword.orEmpty()) }
    PageChrome(title = "连接", subtitle = "串流服务 · PC 联动", badge = "互联", scrollable = true) {
        EchoSectionTitle("音乐服务", "连接你的曲库来源")
        Spacer(Modifier.height(12.dp))
        RemoteSourcePanel(
            title = "Subsonic / Navidrome",
            subtitle = "同步服务器曲库、封面和播放地址",
            serverLabel = "服务器地址",
            serverPlaceholder = "https://music.example.com",
            usernameLabel = "用户名",
            passwordLabel = "密码",
            serverUrl = subsonicServerInput,
            username = subsonicUserInput,
            password = subsonicPasswordInput,
            scanState = remoteScanState,
            onServerUrlChange = { subsonicServerInput = it },
            onUsernameChange = { subsonicUserInput = it },
            onPasswordChange = { subsonicPasswordInput = it },
            onSave = {
                onSaveSubsonicCredentials(subsonicServerInput, subsonicUserInput, subsonicPasswordInput)
            },
            onSync = {
                onSyncSubsonicLibrary(subsonicServerInput, subsonicUserInput, subsonicPasswordInput)
            },
            onCancel = onCancelRemoteSync,
            onClear = {
                subsonicServerInput = ""
                subsonicUserInput = ""
                subsonicPasswordInput = ""
                onClearSubsonicCredentials()
            },
        )
        Spacer(Modifier.height(10.dp))
        RemoteSourcePanel(
            title = "WebDAV / 网盘",
            subtitle = "按文件夹同步 NAS 或网盘音乐目录",
            serverLabel = "WebDAV 地址",
            serverPlaceholder = "https://dav.example.com/music",
            usernameLabel = "WebDAV 用户名",
            passwordLabel = "WebDAV 密码",
            serverUrl = webDavServerInput,
            username = webDavUserInput,
            password = webDavPasswordInput,
            scanState = remoteScanState,
            onServerUrlChange = { webDavServerInput = it },
            onUsernameChange = { webDavUserInput = it },
            onPasswordChange = { webDavPasswordInput = it },
            onSave = {
                onSaveWebDavCredentials(webDavServerInput, webDavUserInput, webDavPasswordInput)
            },
            onSync = {
                onSyncWebDavLibrary(webDavServerInput, webDavUserInput, webDavPasswordInput)
            },
            onCancel = onCancelRemoteSync,
            onClear = {
                webDavServerInput = ""
                webDavUserInput = ""
                webDavPasswordInput = ""
                onClearWebDavCredentials()
            },
        )
        Spacer(Modifier.height(10.dp))
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
        Spacer(Modifier.height(10.dp))
        ServiceCard(
            name = "Discord Rich Presence",
            subtitle = discordPresenceTrackTitle?.let { "手机播放：$it" } ?: "通过 PC ECHO 转发手机播放状态",
            icon = Icons.Rounded.GraphicEq,
            brandColor = Color(0xFF5865F2),
            statusLabel = when {
                !discordPresenceEnabled -> "未开启"
                discordPresenceReady -> "待转发"
                else -> "等待 PC"
            },
            active = discordPresenceEnabled && discordPresenceReady,
            locked = !discordPresenceEnabled,
            onClick = {},
        )
        Spacer(Modifier.height(20.dp))
        EchoSectionTitle("设备联动", if (connected) "手机控制，PC 输出" else "配对后接管 PC ECHO 播放")
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            scheme.surface.copy(alpha = if (dark) 0.88f else 0.70f),
                            if (dark) scheme.surfaceVariant.copy(alpha = 0.72f) else EchoHomeMist.copy(alpha = 0.62f),
                            scheme.primary.copy(alpha = if (dark) 0.14f else 0.08f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (dark) scheme.outlineVariant.copy(alpha = 0.58f) else EchoGlassBorder.copy(alpha = 0.86f),
                    ),
                    RoundedCornerShape(24.dp),
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
                            color = scheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            remoteConnectionLabel(remoteState),
                            color = scheme.onSurfaceVariant,
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

@Composable
private fun RemoteSourcePanel(
    title: String,
    subtitle: String,
    serverLabel: String,
    serverPlaceholder: String,
    usernameLabel: String,
    passwordLabel: String,
    serverUrl: String,
    username: String,
    password: String,
    scanState: LibraryScanProgress,
    onServerUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onSync: () -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
) {
    val ready = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    val scheme = MaterialTheme.colorScheme
    val dark = LocalEchoDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        scheme.surface.copy(alpha = if (dark) 0.88f else 0.70f),
                        EchoHomeBlue.copy(alpha = 0.12f),
                        if (dark) scheme.surfaceVariant.copy(alpha = 0.56f) else EchoHomeMist.copy(alpha = 0.28f),
                    ),
                ),
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) scheme.outlineVariant.copy(alpha = 0.58f) else EchoGlassBorder.copy(alpha = 0.86f),
                ),
                RoundedCornerShape(20.dp),
            )
            .padding(15.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Icon(Icons.Rounded.CloudQueue, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        title,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        remoteLibraryDetail(scanState, ready).takeIf { scanState.phase != LibraryScanPhase.Idle } ?: subtitle,
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ServiceStatusPill(
                    label = if (scanState.isScanning) "同步中" else if (ready) "可同步" else "待配置",
                    active = ready && scanState.phase != LibraryScanPhase.Error,
                    locked = !ready && !scanState.isScanning,
                )
            }
            RemoteTextInput(
                label = serverLabel,
                value = serverUrl,
                placeholder = serverPlaceholder,
                onValueChange = onServerUrlChange,
            )
            RemoteTextInput(
                label = usernameLabel,
                value = username,
                placeholder = "用户名",
                onValueChange = onUsernameChange,
            )
            RemoteTextInput(
                label = passwordLabel,
                value = password,
                placeholder = "密码或应用专用密码",
                secret = true,
                onValueChange = onPasswordChange,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                RemoteCompactAction(
                    text = "保存",
                    enabled = ready,
                    modifier = Modifier.weight(1f),
                    onClick = onSave,
                )
                RemoteCompactAction(
                    text = if (scanState.isScanning) "取消" else "同步",
                    enabled = ready || scanState.isScanning,
                    modifier = Modifier.weight(1f),
                    onClick = { if (scanState.isScanning) onCancel() else onSync() },
                )
                RemoteCompactAction(
                    text = "清除",
                    enabled = serverUrl.isNotBlank() || username.isNotBlank() || password.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = onClear,
                )
            }
        }
    }
}

@Composable
private fun RemoteTextInput(
    label: String,
    value: String,
    placeholder: String,
    secret: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RemoteCompactAction(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = LocalEchoDarkTheme.current
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (enabled) scheme.primary.copy(alpha = 0.13f) else scheme.surfaceVariant.copy(alpha = 0.28f),
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) scheme.outlineVariant.copy(alpha = 0.50f) else EchoGlassBorder.copy(alpha = 0.62f),
                ),
                RoundedCornerShape(13.dp),
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (enabled) scheme.primary else scheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private fun remoteLibraryDetail(scanState: LibraryScanProgress, ready: Boolean): String =
    when {
        scanState.isScanning -> buildString {
            append(remoteScanPhaseLabel(scanState.phase))
            append(" · ")
            append(scanState.totalCount?.let { "${scanState.scannedCount}/$it" } ?: scanState.scannedCount.toString())
            scanState.currentTitle?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
        }
        scanState.phase == LibraryScanPhase.Completed -> {
            "同步完成：${scanState.scannedCount} 首，新增 ${scanState.insertedCount}，更新 ${scanState.updatedCount}，删除 ${scanState.deletedCount}"
        }
        scanState.phase == LibraryScanPhase.Error -> scanState.error ?: "远程曲库同步失败"
        ready -> "可同步到云端专辑墙"
        else -> "填写服务器、用户名和密码"
    }

private fun remoteScanPhaseLabel(phase: LibraryScanPhase): String =
    when (phase) {
        LibraryScanPhase.Preparing -> "准备同步"
        LibraryScanPhase.QueryingMediaStore -> "读取远程曲库"
        LibraryScanPhase.Diffing -> "对比索引"
        LibraryScanPhase.WritingDatabase -> "写入曲库"
        LibraryScanPhase.CleaningRemoved -> "清理旧索引"
        LibraryScanPhase.Completed -> "同步完成"
        LibraryScanPhase.Cancelled -> "已取消"
        LibraryScanPhase.Error -> "同步失败"
        LibraryScanPhase.Idle -> "等待同步"
    }

