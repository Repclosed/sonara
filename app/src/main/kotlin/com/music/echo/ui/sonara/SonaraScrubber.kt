package echo.music.iad1tya.ui.sonara

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Sonara seek bar: a slim 4.dp cyan→ultraviolet track that thickens to 8.dp while dragging,
 * with a glowing thumb and monospaced `02:41 // 03:58` timestamps.
 */
@Composable
fun SonaraScrubber(
  position: Long,
  duration: Long,
  enabled: Boolean,
  onSeek: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isDragging by interactionSource.collectIsDraggedAsState()

  val trackHeight by
    animateDpAsState(
      targetValue = if (isDragging) 8.dp else 4.dp,
      animationSpec = spring(dampingRatio = 0.8f, stiffness = 900f),
      label = "SonaraScrubberHeight",
    )

  val safeDuration = duration.coerceAtLeast(0L)
  val safePosition = position.coerceIn(0L, if (safeDuration > 0L) safeDuration else position)

  Slider(
    value = safePosition.toFloat(),
    valueRange = 0f..(if (safeDuration > 0f) safeDuration.toFloat() else 1f),
    onValueChange = { if (enabled) onSeek(it.toLong()) },
    onValueChangeFinished = { if (enabled) onSeek(safePosition) },
    enabled = enabled,
    interactionSource = interactionSource,
    thumb = {
      Box(
        modifier =
          Modifier.width(trackHeight)
            .height(trackHeight)
            .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = SonaraNeon.Cyan)
            .sonaraGlowRing(shape = CircleShape, rimWidth = 1.dp),
      ) {
        Box(
          modifier =
            Modifier.size(trackHeight - 2.dp)
              .align(Alignment.Center)
              .background(Brush.radialGradient(listOf(Color.White, SonaraNeon.Cyan)), CircleShape)
        )
      }
    },
    track = { sliderState ->
      val fraction =
        ((sliderState.value - sliderState.valueRange.start) /
            (sliderState.valueRange.endInclusive - sliderState.valueRange.start))
          .coerceIn(0f, 1f)
      androidx.compose.foundation.Canvas(
        modifier = Modifier.fillMaxWidth().height(trackHeight)
      ) {
        val radius = size.height / 2f
        drawLine(
          color = Color.White.copy(alpha = 0.16f),
          start = Offset(radius, radius),
          end = Offset(size.width - radius, radius),
          strokeWidth = size.height,
          cap = StrokeCap.Round,
        )
        if (fraction > 0f) {
          drawLine(
            brush = SonaraNeon.AccentSweep,
            start = Offset(radius, radius),
            end = Offset(radius + (size.width - 2 * radius) * fraction, radius),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
          )
          // Soft bloom trailing the filled portion.
          drawCircle(
            brush =
              Brush.radialGradient(
                colors = listOf(SonaraNeon.Cyan.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(radius + (size.width - 2 * radius) * fraction, radius),
                radius = size.height * 2.4f,
              ),
            radius = size.height * 2.4f,
            center = Offset(radius + (size.width - 2 * radius) * fraction, radius),
          )
        }
        // Subtle inner rim so the track reads as glass.
        drawLine(
          color = Color.White.copy(alpha = 0.05f),
          start = Offset(radius, radius),
          end = Offset(size.width - radius, radius),
          strokeWidth = 0.6.dp.toPx(),
          cap = StrokeCap.Round,
          alpha = 0.6f,
        )
      }
    },
    modifier = modifier.fillMaxWidth(),
  )

  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "${formatClock(safePosition)} // ${formatClock(safeDuration)}",
      color = Color(0x99CFE9FF),
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.Monospace,
    )
  }
}

private fun formatClock(ms: Long): String {
  val totalSeconds = (ms.coerceAtLeast(0L) / 1000.0).roundToInt()
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
