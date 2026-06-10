package app.echo.android

internal object LastFmApiConfig {
    val apiKey: String
        get() = BuildConfig.LASTFM_API_KEY.trim()

    val sharedSecret: String
        get() = BuildConfig.LASTFM_SHARED_SECRET.trim()

    val hasApiKey: Boolean
        get() = apiKey.isNotBlank()

    val hasSharedSecret: Boolean
        get() = sharedSecret.isNotBlank()
}
