package app.echo.android.playback

import app.echo.android.model.settings.EchoEffectivePerformanceMode
import java.util.concurrent.atomic.AtomicLong

object EchoPlaybackCachePolicy {
    const val BalancedMaxBytes = 256L * 1024L * 1024L
    const val LightweightMaxBytes = 128L * 1024L * 1024L

    private val maxBytes = AtomicLong(BalancedMaxBytes)

    val maxCacheBytes: Long
        get() = maxBytes.get()

    fun setEffectivePerformanceMode(mode: EchoEffectivePerformanceMode) {
        maxBytes.set(if (mode.isLightweight) LightweightMaxBytes else BalancedMaxBytes)
    }
}
