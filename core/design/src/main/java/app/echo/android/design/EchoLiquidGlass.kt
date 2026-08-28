package app.echo.android.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Control-layer glass: translucent fill, top-left specular, hairline rim.
 * Sits above content; do not use as a page background.
 *
 * [dark] defaults to the app theme; pass an explicit value when the glass sits on a
 * surface whose luminance is independent of the theme (e.g. artwork backdrops).
 */
@Composable
fun EchoLiquidGlass(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    strength: Float = 1f,
    luminous: Boolean = false,
    elevation: Dp = 16.dp,
    dark: Boolean = LocalEchoDarkTheme.current,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val lightweight = LocalEchoEffectivePerformanceMode.current.isLightweight
    val s = strength.coerceIn(0.45f, 1.4f)
    val fill = if (luminous) {
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = if (dark) 0.94f else 0.96f),
            0.55f to Color.White.copy(alpha = if (dark) 0.86f else 0.92f),
            1f to Color.White.copy(alpha = if (dark) 0.74f else 0.84f),
        )
    } else if (dark) {
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.16f * s),
            0.42f to Color.White.copy(alpha = 0.08f * s),
            1f to Color.Black.copy(alpha = 0.20f * s),
        )
    } else {
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.72f),
            1f to Color.White.copy(alpha = 0.48f),
        )
    }
    val specular = Brush.linearGradient(
        0f to Color.White.copy(alpha = if (luminous) 0.42f else if (dark) 0.20f * s else 0.28f),
        0.48f to Color.Transparent,
    )
    val rim = when {
        luminous -> Color.White.copy(alpha = if (dark) 0.58f else 0.90f)
        dark -> Color.White.copy(alpha = 0.22f * s)
        else -> Color.White.copy(alpha = 0.86f)
    }
    val showEdgeHighlight = !lightweight && shape !== CircleShape
    Box(
        modifier = modifier
            .then(
                if (lightweight || elevation <= 0.dp) {
                    Modifier
                } else {
                    Modifier.shadow(
                        elevation = elevation * s,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = if (dark) 0.30f else 0.10f),
                        spotColor = Color.Black.copy(alpha = if (dark) 0.18f else 0.08f),
                    )
                },
            )
            .clip(shape)
            .background(fill)
            .then(if (lightweight) Modifier else Modifier.background(specular))
            .border(BorderStroke(1.dp, rim), shape),
        contentAlignment = contentAlignment,
    ) {
        if (showEdgeHighlight) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (dark) 0.42f * s else 0.70f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        content()
    }
}
