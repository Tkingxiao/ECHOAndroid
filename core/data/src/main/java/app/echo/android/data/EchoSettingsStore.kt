package app.echo.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
    val uiFontFamily: String = EchoFontFamilyMode.System,
    val uiFontScale: Float = 1f,
    val uiDensityScale: Float = 1f,
    val lyricsFontFamily: String = EchoFontFamilyMode.System,
    val lyricsFontScale: Float = 1f,
    val importedFontUri: String? = null,
    val themeMode: String = EchoThemeMode.System,
    val scheduledDarkModeEnabled: Boolean = false,
    val scheduledDarkStartMinute: Int = 22 * 60,
    val scheduledDarkEndMinute: Int = 7 * 60,
    val lastFmEnabled: Boolean = false,
    val lastFmApiKey: String? = null,
    val lastFmSharedSecret: String? = null,
    val lastFmUsername: String? = null,
    val lastFmSessionKey: String? = null,
)

object EchoBackgroundMode {
    const val Default = "default"
    const val Image = "image"
    const val Video = "video"
}

object EchoFontFamilyMode {
    const val System = "system"
    const val Outfit = "outfit"
    const val Serif = "serif"
    const val Monospace = "monospace"
    const val Imported = "imported"
}

object EchoThemeMode {
    const val System = "system"
    const val Light = "light"
    const val Dark = "dark"
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
                uiFontFamily = preferences[Keys.UiFontFamily] ?: EchoFontFamilyMode.System,
                uiFontScale = (preferences[Keys.UiFontScale] ?: 1f).coerceIn(0.88f, 1.18f),
                uiDensityScale = (preferences[Keys.UiDensityScale] ?: 1f).coerceIn(0.90f, 1.12f),
                lyricsFontFamily = preferences[Keys.LyricsFontFamily] ?: EchoFontFamilyMode.System,
                lyricsFontScale = (preferences[Keys.LyricsFontScale] ?: 1f).coerceIn(0.82f, 1.28f),
                importedFontUri = preferences[Keys.ImportedFontUri],
                themeMode = preferences[Keys.ThemeMode] ?: EchoThemeMode.System,
                scheduledDarkModeEnabled = preferences[Keys.ScheduledDarkModeEnabled] ?: false,
                scheduledDarkStartMinute = (preferences[Keys.ScheduledDarkStartMinute] ?: 22 * 60).coerceIn(0, 23 * 60 + 59),
                scheduledDarkEndMinute = (preferences[Keys.ScheduledDarkEndMinute] ?: 7 * 60).coerceIn(0, 23 * 60 + 59),
                lastFmEnabled = preferences[Keys.LastFmEnabled] ?: false,
                lastFmApiKey = preferences[Keys.LastFmApiKey],
                lastFmSharedSecret = preferences[Keys.LastFmSharedSecret],
                lastFmUsername = preferences[Keys.LastFmUsername],
                lastFmSessionKey = preferences[Keys.LastFmSessionKey],
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

    suspend fun setUiFontFamily(value: String) {
        context.echoSettings.edit { it[Keys.UiFontFamily] = value }
    }

    suspend fun setUiFontScale(value: Float) {
        context.echoSettings.edit { it[Keys.UiFontScale] = value.coerceIn(0.88f, 1.18f) }
    }

    suspend fun setUiDensityScale(value: Float) {
        context.echoSettings.edit { it[Keys.UiDensityScale] = value.coerceIn(0.90f, 1.12f) }
    }

    suspend fun setLyricsFontFamily(value: String) {
        context.echoSettings.edit { it[Keys.LyricsFontFamily] = value }
    }

    suspend fun setLyricsFontScale(value: Float) {
        context.echoSettings.edit { it[Keys.LyricsFontScale] = value.coerceIn(0.82f, 1.28f) }
    }

    suspend fun setImportedFontUri(uri: String?) {
        context.echoSettings.edit {
            if (uri.isNullOrBlank()) {
                it.remove(Keys.ImportedFontUri)
                if (it[Keys.UiFontFamily] == EchoFontFamilyMode.Imported) {
                    it[Keys.UiFontFamily] = EchoFontFamilyMode.System
                }
                if (it[Keys.LyricsFontFamily] == EchoFontFamilyMode.Imported) {
                    it[Keys.LyricsFontFamily] = EchoFontFamilyMode.System
                }
            } else {
                it[Keys.ImportedFontUri] = uri
            }
        }
    }

    suspend fun setThemeMode(value: String) {
        context.echoSettings.edit { it[Keys.ThemeMode] = value }
    }

    suspend fun setScheduledDarkModeEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.ScheduledDarkModeEnabled] = enabled }
    }

    suspend fun setScheduledDarkStartMinute(value: Int) {
        context.echoSettings.edit { it[Keys.ScheduledDarkStartMinute] = value.coerceIn(0, 23 * 60 + 59) }
    }

    suspend fun setScheduledDarkEndMinute(value: Int) {
        context.echoSettings.edit { it[Keys.ScheduledDarkEndMinute] = value.coerceIn(0, 23 * 60 + 59) }
    }

    suspend fun setLastFmEnabled(enabled: Boolean) {
        context.echoSettings.edit { it[Keys.LastFmEnabled] = enabled }
    }

    suspend fun setLastFmCredentials(
        apiKey: String,
        sharedSecret: String,
        username: String,
        sessionKey: String,
    ) {
        context.echoSettings.edit {
            it[Keys.LastFmApiKey] = apiKey.trim()
            it[Keys.LastFmSharedSecret] = sharedSecret.trim()
            it[Keys.LastFmUsername] = username.trim()
            it[Keys.LastFmSessionKey] = sessionKey.trim()
            it[Keys.LastFmEnabled] = true
        }
    }

    suspend fun clearLastFmCredentials() {
        context.echoSettings.edit {
            it[Keys.LastFmEnabled] = false
            it.remove(Keys.LastFmUsername)
            it.remove(Keys.LastFmSessionKey)
        }
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
        val UiFontFamily = stringPreferencesKey("ui_font_family")
        val UiFontScale = floatPreferencesKey("ui_font_scale")
        val UiDensityScale = floatPreferencesKey("ui_density_scale")
        val LyricsFontFamily = stringPreferencesKey("lyrics_font_family")
        val LyricsFontScale = floatPreferencesKey("lyrics_font_scale")
        val ImportedFontUri = stringPreferencesKey("imported_font_uri")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val ScheduledDarkModeEnabled = booleanPreferencesKey("scheduled_dark_mode_enabled")
        val ScheduledDarkStartMinute = intPreferencesKey("scheduled_dark_start_minute")
        val ScheduledDarkEndMinute = intPreferencesKey("scheduled_dark_end_minute")
        val LastFmEnabled = booleanPreferencesKey("lastfm_enabled")
        val LastFmApiKey = stringPreferencesKey("lastfm_api_key")
        val LastFmSharedSecret = stringPreferencesKey("lastfm_shared_secret")
        val LastFmUsername = stringPreferencesKey("lastfm_username")
        val LastFmSessionKey = stringPreferencesKey("lastfm_session_key")
    }
}
