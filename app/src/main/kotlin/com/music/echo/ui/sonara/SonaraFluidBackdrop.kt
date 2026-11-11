package echo.music.iad1tya.ui.sonara

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated "fluid mesh" backdrop for the Sonara Now Playing surface.
 *
 * Three blurred radial spheres drift on independent orbits over a near-black canvas, tinted by
 * Palette/Coil artwork colors when available and falling back to the Sonara neon accents.
 * A light scrim keeps text contrast intact.
 */
@Composable
fun SonaraFluidBackdrop(
  colors: List<Color>,
  alphaProvider: () -> Float,
  modifier: Modifier = Modifier,
  isPlaying: Boolean = true,
) {
  val base1 = colors.getOrNull(0) ?: SonaraNeon.Ultraviolet
  val base2 = colors.getOrNull(1) ?: SonaraNeon.Cyan
  val base3 = colors.getOrNull(2) ?: SonaraNeon.Magenta

  val tint1 = lerp(base1, SonaraNeon.Ultraviolet, 0.55f)
  val tint2 = lerp(base2, SonaraNeon.Cyan, 0.6f)
  val tint3 = lerp(base3, SonaraNeon.Magenta, 0.5f)

  val transition = rememberInfiniteTransition(label = "SonaraFluidBackdrop")
  val speed = if (isPlaying) 1f else 0.35f

  val phaseA by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * Math.PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = (26_000 / speed).toInt(), easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "sphereA",
    )
  val phaseB by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * Math.PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = (34_000 / speed).toInt(), easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "sphereB",
    )
  val phaseC by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * Math.PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = (42_000 / speed).toInt(), easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "sphereC",
    )

  Box(modifier = modifier.fillMaxSize().background(Color(0xFF05060A))) {
    Box(
      modifier =
        Modifier.fillMaxSize().graphicsLayer { alpha = alphaProvider().coerceIn(0f, 1f) }
    ) {
      FluidSphere(
        color = tint2,
        sphereSize = 380.dp,
        x = 0.5f + 0.34f * cos(phaseA),
        y = 0.22f + 0.16f * sin(phaseA),
      )
      FluidSphere(
        color = tint1,
        sphereSize = 460.dp,
        x = 0.42f + 0.30f * cos(phaseB + 1.9f),
        y = 0.62f + 0.22f * sin(phaseB + 0.7f),
      )
      FluidSphere(
        color = tint3,
        sphereSize = 300.dp,
        x = 0.5f + 0.40f * cos(phaseC + 4.1f),
        y = 0.82f + 0.14f * sin(phaseC + 2.3f),
      )

      // Dark scrim (10–15%) keeps the artwork glow from washing out lyrics and controls.
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(
                  Color.Black.copy(alpha = 0.10f),
                  Color.Black.copy(alpha = 0.15f),
                  Color.Black.copy(alpha = 0.12f),
                )
              )
            )
      )
    }
  }
}

@Composable
private fun FluidSphere(
  color: Color,
  sphereSize: androidx.compose.ui.unit.Dp,
  x: Float,
  y: Float,
) {
  Box(
    modifier =
      Modifier.fillMaxSize().graphicsLayer {
          val w = this.size.width
          val h = this.size.height
          translationX = x * w - (sphereSize.toPx() / 2f)
          translationY = y * h - (sphereSize.toPx() / 2f)
        },
    contentAlignment = Alignment.TopStart,
  ) {
    Box(
      modifier =
        Modifier.size(sphereSize)
          .blur(64.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors =
                listOf(
                  color.copy(alpha = 0.55f),
                  color.copy(alpha = 0.28f),
                  Color.Transparent,
                )
            )
          )
    )
  }
}
