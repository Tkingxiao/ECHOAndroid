package app.echo.android.playback

import app.echo.android.model.playback.EchoPlaybackDiagnostics

fun EchoPlaybackDiagnostics.toReadableLines(): List<String> = buildList {
    add("route=$outputRoute")
    add("offload=$offloadActive")
    add("usbExclusiveEnabled=$usbExclusiveEnabled")
    add("usbConnected=$usbConnected")
    usbDeviceName?.let { add("usbDevice=$it") }
    add("usbBitPerfectSupported=$usbBitPerfectSupported")
    add("usbBitPerfectActive=$usbBitPerfectActive")
    if (usbSupportedSampleRates.isNotEmpty()) {
        add("usbSupportedSampleRates=${usbSupportedSampleRates.joinToString(",")}")
    }
    usbLastRequestedSampleRateHz?.let { add("usbLastRequestedSampleRateHz=$it") }
    usbLastRequestError?.let { add("usbLastRequestError=${it.kind}:${it.message}") }
    add("bufferedMs=$bufferedMs")
    add("requestToken=$requestToken")
    codec?.let { add("codec=$it") }
    sampleRateHz?.let { add("sampleRateHz=$it") }
    channelCount?.let { add("channelCount=$it") }
    bitrate?.let { add("bitrate=$it") }
    lastCommand?.let { add("lastCommand=$it") }
    lastError?.let { add("lastError=${it.kind}:${it.message}") }
}
