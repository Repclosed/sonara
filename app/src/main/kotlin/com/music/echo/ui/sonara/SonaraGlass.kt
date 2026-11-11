package echo.music.iad1tya.ui.sonara

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sonara "cyberpunk liquid glass" design tokens.
 *
 * These are intentionally kept outside the Material scheme so the Now Playing surface can stay
 * obsidian-dark regardless of the user's selected accent palette.
 */
object SonaraNeon {
  val Cyan = Color(0xFF00F5FF)
  val Ultraviolet = Color(0xFFBD00FF)
  val Magenta = Color(0xFFFF0055)
  val Lime = Color(0xFF00FF66)

  val Void = Color(0xFF05060A)
  val GlassFill = Color(0x220D111A)
  val GlassFillDeep = Color(0x33101621)

  val RimCyan = Color(0x8000F5FF)
  val RimViolet = Color(0x40BD00FF)
  val RimWhite = Color(0x10FFFFFF)

  /** Cyan → ultraviolet sweep used across tracks, rims and illuminated controls. */
  val AccentSweep: Brush
    get() = Brush.horizontalGradient(listOf(Cyan, Ultraviolet, Magenta))

  /** Glass body fill. */
  val GlassBody: Brush
    get() = Brush.verticalGradient(listOf(GlassFill, GlassFillDeep))

  /** Neon rim gradient (cyan → ultraviolet → white highlight). */
  val Rim: Brush
    get() = Brush.linearGradient(listOf(RimCyan, RimViolet, RimWhite, RimCyan))

  /** Pill/capsule shape used by floating docks and the mini player. */
  val CapsuleShape: Shape
    get() = RoundedCornerShape(percent = 50)
}

/** Default Sonara squircle used for glass cards and album artwork. */
val SonaraCardShape: Shape
  get() = RoundedCornerShape(24.dp)

/**
 * Applies the Sonara glass treatment: translucent obsidian fill plus a 1.2.dp neon rim.
 * Purely visual — no state, no side effects.
 */
fun Modifier.sonaraGlass(
  shape: Shape = SonaraCardShape,
  rimWidth: Dp = 1.2.dp,
  fill: Brush = SonaraNeon.GlassBody,
): Modifier = this.clip(shape).background(fill).border(rimWidth, SonaraNeon.Rim, shape)

/** Neon rim without a fill, for controls layered over other surfaces. */
fun Modifier.sonaraNeonRim(
  shape: Shape = SonaraNeon.CapsuleShape,
  rimWidth: Dp = 1.2.dp,
): Modifier = this.border(rimWidth, SonaraNeon.Rim, shape)

/** Soft cyan glow ring, used by the illuminated play/pause disc. */
fun Modifier.sonaraGlowRing(
  shape: Shape = SonaraNeon.CapsuleShape,
  rimWidth: Dp = 1.6.dp,
): Modifier =
  this.border(
    rimWidth,
    Brush.sweepGradient(
      listOf(
        SonaraNeon.Cyan.copy(alpha = 0.95f),
        SonaraNeon.Ultraviolet.copy(alpha = 0.75f),
        SonaraNeon.RimWhite,
        SonaraNeon.Cyan.copy(alpha = 0.95f),
      )
    ),
    shape,
  )
