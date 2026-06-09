package app.echo.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.echoSettings by preferencesDataStore(name = "echo-settings")

data class EchoAppSettings(
    val preferOffload: Boolean = true,
    val lastOutputRoute: String = "system",
    val dynamicArtworkEnabled: Boolean = true,
    val compactModeEnabled: Boolean = false,
    val pcHandoffEnabled: Boolean = true,
    val showLyricsControlDeck: Boolean = false,
    val onlineLyricsEnabled: Boolean = false,
    val usbExclusiveEnabled: Boolean = false,
    val customBackgroundMode: String = EchoBackgroundMode.Default,
    val customBackgroundUri: String? = null,
    val customBackgroundBlur: Float = 32f,
    val customBackgroundBrightness: Float = 0.72f,
    val customBackgroundGlass: Float = 0.62f,
)

object EchoBackgroundMode {
    const val Default = "default"
    const val Image = "image"
    const val Video = "video"
}

class EchoSettingsStore(
    private val context: Context,
) {
    val appSettings: Flow<EchoAppSettings> =
        context.echoSettings.data.map { preferences ->
            EchoAppSettings(
                preferOffload = preferences[Keys.PreferOffload] ?: true,
                lastOutputRoute = preferences[Keys.LastOutputRoute] ?: "system",
                dynamicArtworkEnabled = preferences[Keys.DynamicArtworkEnabled] ?: true,
                compactModeEnabled = preferences[Keys.CompactModeEnabled] ?: false,
                pcHandoffEnabled = preferences[Keys.PcHandoffEnabled] ?: true,
                showLyricsControlDeck = preferences[Keys.ShowLyricsControlDeck] ?: false,
                onlineLyricsEnabled = preferences[Keys.OnlineLyricsEnabled] ?: false,
                usbExclusiveEnabled = preferences[Keys.UsbExclusiveEnabled] ?: false,
                customBackgroundMode = preferences[Keys.CustomBackgroundMode] ?: EchoBackgroundMode.Default,
                customBackgroundUri = preferences[Keys.CustomBackgroundUri],
                customBackgroundBlur = (preferences[Keys.CustomBackgroundBlur] ?: 32f).coerceIn(0f, 80f),
                customBackgroundBrightness = (preferences[Keys.CustomBackgroundBrightness] ?: 0.72f).coerceIn(0.35f, 1.15f),
                customBackgroundGlass = (preferences[Keys.CustomBackgroundGlass] ?: 0.62f).coerceIn(0.18f, 0.90f),
            )
        }

    suspend fun setPreferOffload(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.PreferOffload] = enabled }
    }

    suspend fun setLastOutputRoute(route: String) {
        context.echoSettings.edit { it[Keys.LastOutputRoute] = route }
    }

    suspend fun setDynamicArtworkEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.DynamicArtworkEnabled] = enabled }
    }

    suspend fun setCompactModeEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.CompactModeEnabled] = enabled }
    }

    suspend fun setPcHandoffEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.PcHandoffEnabled] = enabled }
    }

    suspend fun setShowLyricsControlDeck(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.ShowLyricsControlDeck] = enabled }
    }

    suspend fun setOnlineLyricsEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.OnlineLyricsEnabled] = enabled }
    }

    suspend fun setUsbExclusiveEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.UsbExclusiveEnabled] = enabled }
    }

    suspend fun setCustomBackground(mode: String, uri: String?) {
        context.echoSettings.edit {
            it[Keys.CustomBackgroundMode] = mode
            if (uri.isNullOrBlank()) {
                it.remove(Keys.CustomBackgroundUri)
            } else {
                it[Keys.CustomBackgroundUri] = uri
            }
        }
    }

    suspend fun setCustomBackgroundBlur(value: Float) {
        context.echoSettings.edit { it[Keys.CustomBackgroundBlur] = value.coerceIn(0f, 80f) }
    }

    suspend fun setCustomBackgroundBrightness(value: Float) {
        context.echoSettings.edit { it[Keys.CustomBackgroundBrightness] = value.coerceIn(0.35f, 1.15f) }
    }

    suspend fun setCustomBackgroundGlass(value: Float) {
        context.echoSettings.edit { it[Keys.CustomBackgroundGlass] = value.coerceIn(0.18f, 0.90f) }
    }

    private object Keys {
        val PreferOffload = booleanPreferencesKey("prefer_offload")
        val LastOutputRoute = stringPreferencesKey("last_output_route")
        val DynamicArtworkEnabled = booleanPreferencesKey("dynamic_artwork_enabled")
        val CompactModeEnabled = booleanPreferencesKey("compact_mode_enabled")
        val PcHandoffEnabled = booleanPreferencesKey("pc_handoff_enabled")
        val ShowLyricsControlDeck = booleanPreferencesKey("show_lyrics_control_deck")
        val OnlineLyricsEnabled = booleanPreferencesKey("online_lyrics_enabled")
        val UsbExclusiveEnabled = booleanPreferencesKey("usb_exclusive_enabled")
        val CustomBackgroundMode = stringPreferencesKey("custom_background_mode")
        val CustomBackgroundUri = stringPreferencesKey("custom_background_uri")
        val CustomBackgroundBlur = floatPreferencesKey("custom_background_blur")
        val CustomBackgroundBrightness = floatPreferencesKey("custom_background_brightness")
        val CustomBackgroundGlass = floatPreferencesKey("custom_background_glass")
    }
}
