package app.echo.android

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        requestHighRefreshRate()
        setContent {
            EchoMobileApp()
        }
    }

    override fun onResume() {
        super.onResume()
        requestHighRefreshRate()
    }

    private fun requestHighRefreshRate() {
        val preferredMode = bestSupportedHighRefreshMode() ?: return

        val attributes = window.attributes
        attributes.preferredDisplayModeId = preferredMode.modeId
        attributes.preferredRefreshRate = preferredMode.refreshRate
        window.attributes = attributes
    }

    @Suppress("DEPRECATION")
    private fun bestSupportedHighRefreshMode(): DisplayModePreference? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            windowManager.defaultDisplay
        }
        val currentMode = activeDisplay?.mode ?: return null
        val preferredMode = activeDisplay
            .supportedModes
            .filter {
                it.refreshRate >= MinHighRefreshRate &&
                    it.physicalWidth == currentMode.physicalWidth &&
                    it.physicalHeight == currentMode.physicalHeight
            }
            .maxByOrNull { it.refreshRate }
            ?: return null
        return DisplayModePreference(
            modeId = preferredMode.modeId,
            refreshRate = preferredMode.refreshRate,
        )
    }

    private data class DisplayModePreference(
        val modeId: Int,
        val refreshRate: Float,
    )

    private companion object {
        const val MinHighRefreshRate = 90f
    }
}
