package app.echo.android.usbaudio

object WavPcmHeaderParser {
    fun parse(header: ByteArray): Result<PcmStreamDescriptor> =
        runCatching {
            require(header.size >= RIFF_HEADER_SIZE) { "WAV header is too short" }
            require(header.ascii(0, 4) == "RIFF") { "Missing RIFF marker" }
            require(header.ascii(8, 4) == "WAVE") { "Missing WAVE marker" }

            var offset = RIFF_HEADER_SIZE
            var fmt: WavFormat? = null
            var dataOffset = -1
            var dataSize = 0

            while (offset + CHUNK_HEADER_SIZE <= header.size) {
                val chunkId = header.ascii(offset, 4)
                val chunkSize = header.le32(offset + 4)
                val chunkDataOffset = offset + CHUNK_HEADER_SIZE
                require(chunkSize >= 0) { "Invalid WAV chunk size" }

                when (chunkId) {
                    "fmt " -> {
                        if (chunkDataOffset + chunkSize > header.size) break
                        fmt = parseFormat(header, chunkDataOffset, chunkSize)
                    }
                    "data" -> {
                        dataOffset = chunkDataOffset
                        dataSize = chunkSize
                        break
                    }
                }

                offset = chunkDataOffset + chunkSize + (chunkSize and 1)
            }

            val format = requireNotNull(fmt) { "Missing WAV fmt chunk" }
            require(dataOffset >= 0 && dataSize > 0) { "Missing WAV data chunk" }
            require(format.encoding == PcmEncoding.PcmInteger || format.encoding == PcmEncoding.PcmFloat) {
                "Unsupported WAV encoding"
            }
            require(format.sampleRateHz > 0 && format.channelCount > 0 && format.bitDepth > 0) {
                "Invalid WAV PCM format"
            }

            PcmStreamDescriptor(
                sampleRateHz = format.sampleRateHz,
                channelCount = format.channelCount,
                bitDepth = format.bitDepth,
                encoding = format.encoding,
                dataOffset = dataOffset,
                dataSize = dataSize,
            )
        }

    private fun parseFormat(data: ByteArray, offset: Int, size: Int): WavFormat {
        require(size >= 16) { "WAV fmt chunk is too short" }
        val audioFormat = data.le16(offset)
        val channelCount = data.le16(offset + 2)
        val sampleRate = data.le32(offset + 4)
        val bitDepth = data.le16(offset + 14)
        val encoding = when (audioFormat) {
            WAVE_FORMAT_PCM -> PcmEncoding.PcmInteger
            WAVE_FORMAT_IEEE_FLOAT -> PcmEncoding.PcmFloat
            WAVE_FORMAT_EXTENSIBLE -> parseExtensibleEncoding(data, offset, size)
            else -> error("Unsupported WAV format code $audioFormat")
        }
        return WavFormat(
            sampleRateHz = sampleRate,
            channelCount = channelCount,
            bitDepth = bitDepth,
            encoding = encoding,
        )
    }

    private fun parseExtensibleEncoding(data: ByteArray, offset: Int, size: Int): PcmEncoding {
        require(size >= 40) { "WAV extensible fmt chunk is too short" }
        return when (data.le16(offset + 24)) {
            WAVE_FORMAT_PCM -> PcmEncoding.PcmInteger
            WAVE_FORMAT_IEEE_FLOAT -> PcmEncoding.PcmFloat
            else -> error("Unsupported WAV extensible subformat")
        }
    }

    private data class WavFormat(
        val sampleRateHz: Int,
        val channelCount: Int,
        val bitDepth: Int,
        val encoding: PcmEncoding,
    )

    private fun ByteArray.ascii(offset: Int, size: Int): String =
        decodeToString(offset, offset + size)

    private fun ByteArray.le16(offset: Int): Int =
        (this[offset].toInt() and 0xff) or ((this[offset + 1].toInt() and 0xff) shl 8)

    private fun ByteArray.le32(offset: Int): Int =
        (this[offset].toInt() and 0xff) or
            ((this[offset + 1].toInt() and 0xff) shl 8) or
            ((this[offset + 2].toInt() and 0xff) shl 16) or
            ((this[offset + 3].toInt() and 0xff) shl 24)

    private const val RIFF_HEADER_SIZE = 12
    private const val CHUNK_HEADER_SIZE = 8
    private const val WAVE_FORMAT_PCM = 0x0001
    private const val WAVE_FORMAT_IEEE_FLOAT = 0x0003
    private const val WAVE_FORMAT_EXTENSIBLE = 0xfffe
}
