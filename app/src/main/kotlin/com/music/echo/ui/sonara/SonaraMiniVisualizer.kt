package echo.music.iad1tya.ui.sonara

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Tiny live frequency visualizer for the floating Sonara mini player.
 * Purely animated — it does not consume the audio pipeline.
 */
@Composable
fun SonaraMiniVisualizer(
  isPlaying: Boolean,
  modifier: Modifier = Modifier,
  bars: Int = 4,
) {
  val transition = rememberInfiniteTransition(label = "SonaraMiniVisualizer")
  val phase by
    transition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2400, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "SonaraMiniVisualizerPhase",
    )

  Row(
    modifier = modifier.height(18.dp),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(bars) { index ->
      val wave = kotlin.math.sin((phase * 2f * Math.PI).toFloat() + index * 1.1f)
      val intensity = if (isPlaying) (0.25f + 0.75f * abs(wave)) else 0.18f
      Box(
        modifier =
          Modifier.width(2.dp)
            .height((4 + 14 * intensity).dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(
              Brush.verticalGradient(
                listOf(
                  SonaraNeon.Cyan.copy(alpha = 0.35f + 0.65f * intensity),
                  SonaraNeon.Ultraviolet.copy(alpha = 0.25f + 0.5f * intensity),
                )
              )
            ),
      )
    }
  }
}
