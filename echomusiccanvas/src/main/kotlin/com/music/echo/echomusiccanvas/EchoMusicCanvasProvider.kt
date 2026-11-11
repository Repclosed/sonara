package echo.music.iad1tya.echomusiccanvas

import echo.music.iad1tya.canvas.CanvasArtwork
import kotlinx.serialization.Serializable

@Serializable
data class echomusicCanvasManifest(val items: List<echomusicCanvasItem> = emptyList())

@Serializable data class echomusicCanvasItem(val song: String, val artist: String, val url: String)

/**
 * Canvas artwork provider stub.
 *
 * Sonara no longer consumes the upstream Echo Music canvas feed. The request path is neutralized
 * locally, so callers simply fall through to the next artwork provider (Apple Music / YouTube).
 * The public signature is intentionally preserved for binary/source compatibility.
 */
object echomusicCanvasProvider {
  suspend fun getBySongArtist(
    song: String,
    artist: String,
  ): CanvasArtwork? = null
}
