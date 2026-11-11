package com.music.echo.ui.sonara

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Blurred duplicate of the album art that sits behind the artwork, giving the Sonara Now Playing
 * "liquid glow" halo. Purely decorative.
 */
@Composable
fun SonaraArtworkGlow(
  artworkUri: Any?,
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 22.dp,
) {
  if (artworkUri == null) return
  Box(
    modifier =
      modifier.fillMaxSize().graphicsLayer {
        scaleX = 1.1f
        scaleY = 1.1f
        alpha = 0.5f
      }
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current).data(artworkUri).crossfade(true).build(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize().blur(32.dp).clip(RoundedCornerShape(cornerRadius)),
    )
  }
}
