package app.echo.android.usbaudio

object UsbPcmFormatSelector {
    fun chooseFormat(
        descriptor: UsbAudioDescriptorInfo,
        spec: UsbPcmFormatSpec,
    ): UsbAudioStreamingFormat? =
        descriptor.streamingFormats
            .asSequence()
            .filter { it.isIsochronousOut || it.endpointTransferType == UsbEndpointTransferType.Bulk }
            .filter { it.endpointDirection == UsbEndpointDirection.Out }
            .filter { format -> format.bitResolution == null || format.bitResolution == spec.bitDepth }
            .filter { format -> format.channelCount == null || format.channelCount == spec.channelCount }
            .filter { format ->
                format.sampleRates.isEmpty() || spec.sampleRateHz in format.sampleRates
            }
            .sortedWith(compareByDescending<UsbAudioStreamingFormat> { it.isIsochronousOut }
                .thenByDescending { it.sampleRates.isNotEmpty() }
                .thenByDescending { it.bitResolution == spec.bitDepth }
                .thenByDescending { it.channelCount == spec.channelCount }
                .thenBy { it.interfaceNumber }
                .thenBy { it.alternateSetting })
            .firstOrNull()
}
