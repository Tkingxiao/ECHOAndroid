package app.echo.android.usbaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WavPcmHeaderParserTest {
    @Test
    fun parsesPcmWavHeader() {
        val header = wavHeader(
            formatCode = 1,
            channelCount = 2,
            sampleRate = 96_000,
            bitDepth = 24,
            dataSize = 960,
        )

        val descriptor = WavPcmHeaderParser.parse(header).getOrThrow()

        assertEquals(96_000, descriptor.sampleRateHz)
        assertEquals(2, descriptor.channelCount)
        assertEquals(24, descriptor.bitDepth)
        assertEquals(PcmEncoding.PcmInteger, descriptor.encoding)
        assertEquals(44, descriptor.dataOffset)
        assertEquals(960, descriptor.dataSize)
    }

    @Test
    fun rejectsUnsupportedFormatCode() {
        val header = wavHeader(
            formatCode = 0x0055,
            channelCount = 2,
            sampleRate = 44_100,
            bitDepth = 16,
            dataSize = 128,
        )

        assertTrue(WavPcmHeaderParser.parse(header).isFailure)
    }

    private fun wavHeader(
        formatCode: Int,
        channelCount: Int,
        sampleRate: Int,
        bitDepth: Int,
        dataSize: Int,
    ): ByteArray {
        val byteRate = sampleRate * channelCount * (bitDepth / 8)
        val blockAlign = channelCount * (bitDepth / 8)
        return buildList {
            addAscii("RIFF")
            addLe32(36 + dataSize)
            addAscii("WAVE")
            addAscii("fmt ")
            addLe32(16)
            addLe16(formatCode)
            addLe16(channelCount)
            addLe32(sampleRate)
            addLe32(byteRate)
            addLe16(blockAlign)
            addLe16(bitDepth)
            addAscii("data")
            addLe32(dataSize)
        }.map { it.toByte() }.toByteArray()
    }

    private fun MutableList<Int>.addAscii(value: String) {
        value.forEach { add(it.code) }
    }

    private fun MutableList<Int>.addLe16(value: Int) {
        add(value and 0xff)
        add((value shr 8) and 0xff)
    }

    private fun MutableList<Int>.addLe32(value: Int) {
        add(value and 0xff)
        add((value shr 8) and 0xff)
        add((value shr 16) and 0xff)
        add((value shr 24) and 0xff)
    }
}
