package app.echo.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.echo.android.design.EchoAccent
import app.echo.android.design.EchoAccentDeep
import app.echo.android.design.EchoAccentText
import app.echo.android.design.EchoGlassBorder
import app.echo.android.design.EchoHomeMist
import app.echo.android.design.RoonMuted

enum class EchoTab(
    val label: String,
    val icon: ImageVector,
) {
    Now("播放", Icons.Rounded.MusicNote),
    Library("曲库", Icons.Rounded.LibraryMusic),
    Connect("连接", Icons.Rounded.Devices),
    Diagnostics("状态", Icons.Rounded.GraphicEq),
}

@Composable
fun BottomDock(
    selectedTab: Int,
    onLightSurface: Boolean,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(26.dp)
    val dockBackground = if (onLightSurface) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.42f),
                EchoHomeMist.copy(alpha = 0.24f),
                EchoAccentDeep.copy(alpha = 0.14f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.07f),
            ),
        )
    }
    val borderColor = if (onLightSurface) EchoGlassBorder.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.24f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (onLightSurface) 18.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (onLightSurface) 0.12f else 0.22f),
                spotColor = Color.Black.copy(alpha = if (onLightSurface) 0.10f else 0.18f),
            )
            .clip(shape)
            .background(dockBackground)
            .border(BorderStroke(1.dp, borderColor), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EchoTab.entries.forEach { tab ->
                DockItem(
                    tab = tab,
                    selected = selectedTab == tab.ordinal,
                    onLightSurface = onLightSurface,
                    onClick = { onSelectTab(tab.ordinal) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    tab: EchoTab,
    selected: Boolean,
    onLightSurface: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = when {
        selected && onLightSurface -> Color(0xFFFFBCD5)
        selected -> EchoAccentText
        onLightSurface -> RoonMuted.copy(alpha = 0.78f)
        else -> Color.White.copy(alpha = 0.68f)
    }
    val selectedBackground = if (onLightSurface) {
        Brush.verticalGradient(listOf(EchoAccentDeep.copy(alpha = 0.16f), Color.White.copy(alpha = 0.16f)))
    } else {
        Brush.verticalGradient(listOf(EchoAccent.copy(alpha = 0.28f), EchoAccentDeep.copy(alpha = 0.12f)))
    }
    val selectedBorder = if (onLightSurface) Color.White.copy(alpha = 0.42f) else EchoAccent.copy(alpha = 0.38f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 46.dp, minHeight = 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .then(
                    if (selected) {
                        Modifier
                            .background(selectedBackground)
                            .border(BorderStroke(1.dp, selectedBorder), RoundedCornerShape(15.dp))
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tab.icon, contentDescription = tab.label, tint = contentColor, modifier = Modifier.size(20.dp))
        }
    }
}
