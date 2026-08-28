package app.echo.android.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.IOException

object EchoPlaybackArtwork {
    fun load(
        context: Context,
        artworkUri: String?,
        embeddedSourceUri: String?,
        maxEdgePx: Int,
        maxBytes: Int = DefaultMaxBytes,
    ): Bitmap? {
        decodeUri(context, artworkUri, maxEdgePx, maxBytes)?.let { return it }
        return decodeEmbedded(context, embeddedSourceUri, maxEdgePx, maxBytes)
    }

    private fun decodeUri(
        context: Context,
        rawUri: String?,
        maxEdgePx: Int,
        maxBytes: Int,
    ): Bitmap? {
        val uri = rawUri?.takeIf { it.isNotBlank() }?.let(Uri::parse) ?: return null
        if (uri.scheme != "content" && uri.scheme != "file" && uri.scheme != "android.resource") {
            return null
        }
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                // 限长读取:超过 maxBytes 直接放弃,避免超大封面整文件进堆
                val data = input.readBytesCapped(maxBytes) ?: return@use null
                decodeBytes(data, maxEdgePx, maxBytes)
            }
        }.getOrNull()
    }

    /** 限长读取并按 maxEdgePx 降采样解码;超限返回 null。供 HTTP 等外部流复用。 */
    fun decodeCapped(
        input: java.io.InputStream,
        maxEdgePx: Int,
        maxBytes: Int = DefaultMaxBytes,
    ): Bitmap? {
        val data = input.readBytesCapped(maxBytes) ?: return null
        return decodeBytes(data, maxEdgePx, maxBytes)
    }

    private fun java.io.InputStream.readBytesCapped(maxBytes: Int): ByteArray? {
        val output = java.io.ByteArrayOutputStream(DefaultReadBufferBytes)
        val buffer = ByteArray(DefaultReadBufferBytes)
        var total = 0
        while (true) {
            val read = read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) return null
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun decodeEmbedded(
        context: Context,
        rawUri: String?,
        maxEdgePx: Int,
        maxBytes: Int,
    ): Bitmap? {
        val uri = rawUri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
            ?.takeIf { it.scheme == "content" || it.scheme == "file" }
            ?: return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val data = retriever.embeddedPicture ?: return null
            decodeBytes(data, maxEdgePx, maxBytes)
        } catch (_: RuntimeException) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: IOException) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun decodeBytes(data: ByteArray, maxEdgePx: Int, maxBytes: Int): Bitmap? {
        if (data.isEmpty() || data.size > maxBytes) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val longest = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        var sampleSize = 1
        val edge = maxEdgePx.coerceAtLeast(1)
        while (longest / sampleSize > edge) {
            sampleSize *= 2
        }
        val decode = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(data, 0, data.size, decode)
    }

    const val DefaultMaxBytes = 8 * 1024 * 1024
    private const val DefaultReadBufferBytes = 64 * 1024
    const val NotificationMaxEdgePx = 512
    const val WidgetMaxEdgePx = 256
}
