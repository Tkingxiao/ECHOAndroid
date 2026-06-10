package app.echo.android.feature.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echo.android.design.ArtworkTile
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentDeep
import app.echo.android.design.LocalEchoDarkTheme
import app.echo.android.design.progressFraction
import app.echo.android.model.playback.EchoPlaybackState
import app.echo.android.model.playback.EchoPlaybackStatus
import app.echo.android.model.playback.PlaybackPositionState
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MiniPlayer(
    status: EchoPlaybackStatus,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    positionState: PlaybackPositionState? = null,
    onHideDock: (() -> Unit)? = null,
    onExpand: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(20.dp)
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val dark = LocalEchoDarkTheme.current
    val offsetX = remember { Animatable(0f) }
    var widthPx by remember { mutableStateOf(1f) }
    val canSwitch = onNext != null && onPrevious != null && status.track != null
    val activePositionMs = positionState?.positionMs ?: status.positionMs
    val activeDurationMs = positionState?.durationMs?.takeIf { it > 0L } ?: status.durationMs
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = scheme.primary.copy(alpha = 0.16f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (dark) {
                        listOf(
                            Color(0xFF24262D),
                            Color(0xFF20222A),
                            Color(0xFF24262D),
                        )
                    } else {
                        listOf(
                            Color.White,
                            Color(0xFFFAFAFA),
                            Color(0xFFF4F4F5),
                        )
                    },
                ),
            )
            .border(BorderStroke(1.dp, if (dark) scheme.outlineVariant.copy(alpha = 0.58f) else Color(0xFFE9E9EC)), shape)
            .padding(start = 14.dp, top = 5.dp, end = 8.dp, bottom = 5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
                    .graphicsLayer { translationX = offsetX.value }
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = onExpand != null) { onExpand?.invoke() }
                    .then(
                        if (canSwitch) {
                            Modifier.pointerInput(status.track?.id) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                                    },
                                    onDragEnd = {
                                        val threshold = widthPx * 0.24f
                                        val settled = offsetX.value
                                        scope.launch {
                                            when {
                                                settled <= -threshold -> {
                                                    offsetX.animateTo(-widthPx, tween(160))
                                                    onNext()
                                                    offsetX.snapTo(widthPx)
                                                    offsetX.animateTo(0f, tween(300))
                                                }
                                                settled >= threshold -> {
                                                    offsetX.animateTo(widthPx, tween(160))
                                                    onPrevious()
                                                    offsetX.snapTo(-widthPx)
                                                    offsetX.animateTo(0f, tween(300))
                                                }
                                                else -> offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ArtworkTile(
                    artworkUri = status.track?.artworkUri,
                    modifier = Modifier.size(40.dp),
                    accent = EchoAccent,
                    showSignal = false,
                    cornerRadius = 11.dp,
                    elevation = 2.dp,
                    placeholderIconSize = 22.dp,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        status.track?.title ?: "ECHO Mobile",
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 700,
                            repeatDelayMillis = 1600,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        status.track?.artist ?: "就绪",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LinearProgressIndicator(
                        progress = { progressFraction(activePositionMs, activeDurationMs) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = scheme.primary,
                        trackColor = scheme.outlineVariant.copy(alpha = if (dark) 0.44f else 0.90f),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = EchoAccent.copy(alpha = 0.30f),
                        spotColor = EchoAccentDeep.copy(alpha = 0.34f),
                    )
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(EchoAccent, EchoAccentDeep)))
                    .clickable(
                        enabled = status.state != EchoPlaybackState.Idle || status.track != null,
                        onClick = onPlayPause,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (status.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "播放或暂停",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (onHideDock != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onHideDock),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "隐藏底栏",
                        tint = scheme.onSurfaceVariant.copy(alpha = 0.84f),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}
