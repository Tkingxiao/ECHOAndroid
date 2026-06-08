package app.echo.android.lyrics

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.echoImportedLyrics by preferencesDataStore(name = "echo-imported-lyrics")

class ImportedLyricsStore(
    private val context: Context,
) {
    suspend fun lyricsUriForTrack(trackId: String): Uri? =
        context.echoImportedLyrics.data
            .map { preferences ->
                preferences[Keys.Bindings]
                    ?.let(::parseBindings)
                    ?.optString(trackId)
                    ?.takeIf(String::isNotBlank)
                    ?.let(Uri::parse)
            }
            .first()

    suspend fun bindLyrics(trackId: String, uri: Uri) {
        context.echoImportedLyrics.edit { preferences ->
            val bindings = preferences[Keys.Bindings]?.let(::parseBindings) ?: JSONObject()
            bindings.put(trackId, uri.toString())
            preferences[Keys.Bindings] = bindings.toString()
        }
    }

    private fun parseBindings(raw: String): JSONObject =
        runCatching { JSONObject(raw) }.getOrDefault(JSONObject())

    private object Keys {
        val Bindings = stringPreferencesKey("track_lyrics_uri_bindings")
    }
}
