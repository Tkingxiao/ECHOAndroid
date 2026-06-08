package app.echo.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFtsSearchTest {
    @Test
    fun blankQueryReturnsNull() {
        assertNull(sanitizeFtsQuery("   "))
    }

    @Test
    fun titleQueryUsesPrefixMatch() {
        assertEquals("hotel* AND california*", sanitizeFtsQuery("Hotel California"))
    }

    @Test
    fun artistQueryKeepsLettersAndNumbers() {
        assertEquals("artist* AND 2026*", sanitizeFtsQuery("Artist #2026!"))
    }

    @Test
    fun albumQueryDropsFtsReservedCharacters() {
        assertEquals("echo* AND final*", sanitizeFtsQuery("echo + final : * \""))
    }

    @Test
    fun chineseQueryUsesCharacterTokens() {
        assertEquals("青* AND 花* AND 瓷*", sanitizeFtsQuery("青花瓷"))
    }

    @Test
    fun specialCharactersDoNotCrash() {
        assertNotNull(sanitizeFtsQuery("\"*:+-()[]{} NEAR / \\"))
    }

    @Test
    fun ftsEntityContainsChineseCharacterTokens() {
        val entity = LibraryTrackEntity(
            id = "track-1",
            contentUri = "content://track/1",
            title = "青花瓷",
            artist = "周杰伦",
            album = "我很忙",
            albumArtist = "周杰伦",
            artworkUri = null,
            durationMs = 180_000L,
            trackNumber = 1,
            discNumber = null,
            year = 2007,
            mimeType = "audio/flac",
            sizeBytes = 1024L,
            dateModifiedSeconds = 1L,
        ).withScanMetadata()

        val fts = entity.toFtsEntity()

        assertEquals("track-1", fts.trackId)
        assertTrue(fts.normalizedText.contains("青花瓷"))
        assertTrue(fts.normalizedText.contains("青 花 瓷"))
        assertTrue(fts.normalizedText.contains("周 杰 伦"))
    }
}
