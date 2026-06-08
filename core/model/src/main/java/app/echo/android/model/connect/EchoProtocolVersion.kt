package app.echo.android.model.connect

data class EchoProtocolVersion(
    val major: Int,
    val minor: Int,
) {
    companion object {
        val Current = EchoProtocolVersion(1, 0)
    }
}
