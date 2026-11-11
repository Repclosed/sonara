package echo.music.iad1tya.utils

import android.media.MediaMetadataRetriever
import android.util.LruCache
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import timber.log.Timber

/**
 * Sonara native performance core bridge.
 *
 * Attempts to load `libsonara_core.so` once. If the native library is missing (unsupported ABI,
 * source-only build, CI without the Rust toolchain) every entry point transparently delegates to
 * the pure-Kotlin fallback below — the app must never crash because of the optional native module.
 */
object SonaraNativeCore {

  private const val LIBRARY_NAME = "sonara_core"

  /** True when the JNI library loaded and reported a healthy initialization. */
  val isAvailable: Boolean

  private val nativeVersion: String?
  private var initialized = false

  private val fallbackCache = object : LruCache<Long, ShortArray>(MAX_FALLBACK_ENTRIES) {
    override fun sizeOf(key: Long, value: ShortArray): Int = value.size * 2
  }

  init {
    var loaded = false
    try {
      System.loadLibrary(LIBRARY_NAME)
      loaded = true
    } catch (e: UnsatisfiedLinkError) {
      Timber.tag("SonaraNativeCore").d(e, "Native core unavailable, using Kotlin fallback")
    } catch (e: SecurityException) {
      Timber.tag("SonaraNativeCore").d(e, "Native core blocked, using Kotlin fallback")
    }

    nativeVersion =
      if (loaded) {
        runCatching { nativeVersion() }.getOrNull()
      } else {
        null
      }

    initialized =
      if (loaded) {
        runCatching { nativeInit() > 0 }.getOrDefault(false)
      } else {
        false
      }

    isAvailable = loaded && initialized
  }

  // ---------------------------------------------------------------------------------------------
  // JNI surface (only invoked when [isAvailable] is true)
  // ---------------------------------------------------------------------------------------------

  private external fun nativeInit(): Int

  private external fun nativeCachePut(songId: Long, pcm: ByteArray): Int

  private external fun nativeCacheGet(songId: Long): ByteArray?

  private external fun nativeCacheRelease(songId: Long): Int

  private external fun nativeCacheClear()

  private external fun nativeParseTags(path: String): String?

  private external fun nativeSpectrum(pcm: ByteArray, bands: Int): FloatArray?

  private external fun nativeVersion(): String

  // ---------------------------------------------------------------------------------------------
  // Public safe API
  // ---------------------------------------------------------------------------------------------

  fun version(): String = nativeVersion ?: "kotlin-fallback"

  /** Caches decoded PCM16 samples for a song. Returns true when stored. */
  fun cacheBuffer(songId: Long, pcm: ShortArray): Boolean {
    if (pcm.isEmpty()) return false
    if (isAvailable) {
      return runCatching { nativeCachePut(songId, pcm.toLittleEndianBytes()) >= 0 }
        .getOrElse { false }
    }
    fallbackCache.put(songId, pcm.copyOf())
    return true
  }

  /** Retrieves cached PCM16 samples, or null when the buffer is not resident. */
  fun getBuffer(songId: Long): ShortArray? {
    if (isAvailable) {
      return runCatching { nativeCacheGet(songId)?.toShortArray() }.getOrNull()
    }
    return fallbackCache.get(songId)
  }

  fun releaseBuffer(songId: Long) {
    if (isAvailable) {
      runCatching { nativeCacheRelease(songId) }
      return
    }
    fallbackCache.remove(songId)
  }

  fun clearBuffers() {
    if (isAvailable) {
      runCatching { nativeCacheClear() }
      return
    }
    fallbackCache.evictAll()
  }

  data class NativeTags(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
  )

  /** Parses tags natively when possible, otherwise via [MediaMetadataRetriever]. */
  fun parseTags(path: String): NativeTags? {
    if (isAvailable) {
      val raw = runCatching { nativeParseTags(path) }.getOrNull()
      if (!raw.isNullOrEmpty()) {
        val parts = raw.split('\u0000')
        if (parts.size >= 4) {
          return NativeTags(
            title = parts[0],
            artist = parts[1],
            album = parts[2],
            durationMs = parts[3].toLongOrNull() ?: 0L,
          )
        }
      }
    }
    return fallbackTags(path)
  }

  /**
   * Magnitude spectrum normalized to 0..1, sized to [bands].
   * Used by the 60/120 FPS visualizers.
   */
  fun spectrum(pcm: ShortArray, bands: Int = 48): FloatArray {
    val safeBands = bands.coerceIn(8, 512)
    if (isAvailable) {
      val native = runCatching { nativeSpectrum(pcm.toLittleEndianBytes(), safeBands) }
        .getOrNull()
      if (native != null && native.size == safeBands) return native
    }
    return fallbackSpectrum(pcm, safeBands)
  }

  // ---------------------------------------------------------------------------------------------
  // Kotlin fallback
  // ---------------------------------------------------------------------------------------------

  private fun fallbackTags(path: String): NativeTags? {
    val retriever = MediaMetadataRetriever()
    return try {
      retriever.setDataSource(path)
      NativeTags(
        title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty(),
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty(),
        album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty(),
        durationMs =
          retriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?: 0L,
      )
    } catch (e: Exception) {
      null
    } finally {
      runCatching { retriever.release() }
    }
  }

  private fun fallbackSpectrum(pcm: ShortArray, bands: Int): FloatArray {
    if (pcm.size < 8) return FloatArray(bands)
    val window = pcm.size.coerceAtMost(4096)
    val result = FloatArray(bands)

    // Goertzel-style band energy: cheap, allocation-free and good enough for a visualizer.
    for (band in 0 until bands) {
      val bin = (band + 1).toFloat() / bands
      val omega = 2.0 * PI * (0.5 + bin * (window / 2.0 - 0.5)) / window
      val coeff = 2.0 * cos(omega)
      var s0 = 0.0
      var s1 = 0.0
      var s2 = 0.0
      for (index in 0 until window) {
        s0 = pcm[index] / 32768.0 + coeff * s1 - s2
        s2 = s1
        s1 = s0
      }
      val magnitude = hypot(s1 - s2 * cos(omega), s2 * sin(omega)) / window
      result[band] = (magnitude * 4.0).toFloat().coerceIn(0f, 1f)
    }
    return result
  }

  private fun ShortArray.toLittleEndianBytes(): ByteArray {
    val buffer = ByteBuffer.allocate(size * 2).order(ByteOrder.LITTLE_ENDIAN)
    for (sample in this) buffer.putShort(sample)
    return buffer.array()
  }

  private fun ByteArray.toShortArray(): ShortArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val samples = ShortArray(size / 2)
    for (index in samples.indices) samples[index] = buffer.short
    return samples
  }

  private const val MAX_FALLBACK_ENTRIES = 8 * 1024 * 1024
}
