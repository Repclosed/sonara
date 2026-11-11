package echo.music.iad1tya.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score
import echo.music.iad1tya.constants.AppFont
import echo.music.iad1tya.constants.SelectedFontKey
import echo.music.iad1tya.utils.rememberPreference

val DefaultThemeColor = Color(0xFF00F5FF)

/**
 * Sonara cyberpunk × liquid-glass palette.
 *
 * The theme always paints the obsidian void canvas; the user-selected accent
 * only feeds [accent]-driven slots, so every surface keeps its neon identity.
 */
object SonaraPalette {
  val VoidCanvas = Color(0xFF05060A)
  val LaserCyan = Color(0xFF00F5FF)
  val Ultraviolet = Color(0xFFBD00FF)
  val CircuitAmber = Color(0xFFFF0055)
  val AcidLime = Color(0xFF00FF66)
  val GlassFill = Color(0x220D111A)
  val GlassRimStart = Color(0x8000F5FF)
  val GlassRimMid = Color(0x40BD00FF)
  val GlassRimEnd = Color(0x10FFFFFF)
  val TextPrimary = Color(0xFFEAF7FF)
  val TextSecondary = Color(0xFFA0B0D0)
  val SurfaceElevated = Color(0xFF0D111A)
  val SurfaceHigh = Color(0xFF141A26)
  val SurfaceHighest = Color(0xFF1B2331)
  val OutlineNeon = Color(0x3300F5FF)
}

private fun sonaraColorScheme(accent: Color, pureBlack: Boolean): ColorScheme {
  val canvas = if (pureBlack) Color.Black else SonaraPalette.VoidCanvas
  val surfaceLow = if (pureBlack) Color(0xFF050505) else Color(0xFF0A0D14)
  return androidx.compose.material3.darkColorScheme(
    primary = accent,
    onPrimary = Color(0xFF001014),
    primaryContainer = accent.copy(alpha = 0.18f),
    onPrimaryContainer = SonaraPalette.LaserCyan,
    inversePrimary = SonaraPalette.Ultraviolet,
    secondary = SonaraPalette.Ultraviolet,
    onSecondary = Color.White,
    secondaryContainer = SonaraPalette.Ultraviolet.copy(alpha = 0.20f),
    onSecondaryContainer = Color(0xFFF4D9FF),
    tertiary = SonaraPalette.CircuitAmber,
    onTertiary = Color.White,
    tertiaryContainer = SonaraPalette.CircuitAmber.copy(alpha = 0.20f),
    onTertiaryContainer = Color(0xFFFFD6E2),
    background = canvas,
    onBackground = SonaraPalette.TextPrimary,
    surface = canvas,
    onSurface = SonaraPalette.TextPrimary,
    surfaceVariant = SonaraPalette.SurfaceHigh,
    onSurfaceVariant = SonaraPalette.TextSecondary,
    surfaceTint = accent,
    inverseSurface = SonaraPalette.TextPrimary,
    inverseOnSurface = canvas,
    error = SonaraPalette.CircuitAmber,
    onError = Color.White,
    errorContainer = SonaraPalette.CircuitAmber.copy(alpha = 0.2f),
    onErrorContainer = Color(0xFFFFD6E2),
    outline = SonaraPalette.OutlineNeon,
    outlineVariant = Color(0x1AFFFFFF),
    scrim = Color.Black,
    surfaceBright = SonaraPalette.SurfaceHighest,
    surfaceDim = surfaceLow,
    surfaceContainer = SonaraPalette.SurfaceElevated,
    surfaceContainerHigh = SonaraPalette.SurfaceHigh,
    surfaceContainerHighest = SonaraPalette.SurfaceHighest,
    surfaceContainerLow = surfaceLow,
    surfaceContainerLowest = if (pureBlack) Color.Black else Color(0xFF07090F),
  )
}

@Composable
fun echomusicTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  pureBlack: Boolean = false,
  isMonochrome: Boolean = false,
  themeColor: Color = DefaultThemeColor,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val selectedFontValue by rememberPreference(SelectedFontKey, AppFont.SYSTEM.value)
  val customFontPathValue by rememberPreference(echo.music.iad1tya.constants.CustomFontPathKey, "")

  val brandFont =
    remember(selectedFontValue, customFontPathValue) {
      when (AppFont.fromValue(selectedFontValue)) {
        AppFont.SYSTEM -> FontFamily.Default
        AppFont.GOOGLE_SANS -> GoogleSansFontFamily
        AppFont.SANS_FLEX -> SansFlexFontFamily
        AppFont.OUTFIT -> OutfitFontFamily
        AppFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
        AppFont.CUSTOM -> {
          try {
            if (customFontPathValue.isNotEmpty() && java.io.File(customFontPathValue).exists()) {
              val typeface = android.graphics.Typeface.createFromFile(customFontPathValue)
              FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
            } else {
              FontFamily.Default
            }
          } catch (e: Exception) {
            FontFamily.Default
          }
        }
      }
    }

  val colorScheme =
    remember(themeColor, pureBlack, isMonochrome) {
      if (isMonochrome) {
        sonaraColorScheme(Color(0xFF9AA4B2), pureBlack).toGrayscaleColorScheme()
      } else {
        sonaraColorScheme(themeColor, pureBlack)
      }
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = getTypography(brandFont),
    shapes =
      androidx.compose.material3.MaterialTheme.shapes.copy(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
      ),
    content = content
  )
}

fun Bitmap.extractThemeColor(): Color {
  val colorsToPopulation =
    Palette.from(this).maximumColorCount(8).generate().swatches.associate {
      it.rgb to it.population
    }
  val rankedColors = Score.score(colorsToPopulation)
  return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
  val extractedColors =
    Palette.from(this).maximumColorCount(64).generate().swatches.associate {
      it.rgb to it.population
    }

  val orderedColors =
    Score.score(extractedColors, 2, 0xff4285f4.toInt(), true).sortedByDescending {
      Color(it).luminance()
    }

  return if (orderedColors.size >= 2) listOf(Color(orderedColors[0]), Color(orderedColors[1]))
  else listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

fun ColorScheme.pureBlack(apply: Boolean) =
  if (apply) copy(
    surface = Color.Black,
    background = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0C0C0C),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1B1B1B),
    surfaceContainerHighest = Color(0xFF222222)
  ) else this

fun ColorScheme.toGrayscaleColorScheme(): ColorScheme {
  fun convert(color: Color): Color {
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[1] = 0f
    return Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl)).copy(alpha = color.alpha)
  }

  return copy(
    primary = convert(primary),
    onPrimary = convert(onPrimary),
    primaryContainer = convert(primaryContainer),
    onPrimaryContainer = convert(onPrimaryContainer),
    inversePrimary = convert(inversePrimary),
    secondary = convert(secondary),
    onSecondary = convert(onSecondary),
    secondaryContainer = convert(secondaryContainer),
    onSecondaryContainer = convert(onSecondaryContainer),
    tertiary = convert(tertiary),
    onTertiary = convert(onTertiary),
    tertiaryContainer = convert(tertiaryContainer),
    onTertiaryContainer = convert(onTertiaryContainer),
    background = convert(background),
    onBackground = convert(onBackground),
    surface = convert(surface),
    onSurface = convert(onSurface),
    surfaceVariant = convert(surfaceVariant),
    onSurfaceVariant = convert(onSurfaceVariant),
    surfaceTint = convert(surfaceTint),
    inverseSurface = convert(inverseSurface),
    inverseOnSurface = convert(inverseOnSurface),
    error = convert(error),
    onError = convert(onError),
    errorContainer = convert(errorContainer),
    onErrorContainer = convert(onErrorContainer),
    outline = convert(outline),
    outlineVariant = convert(outlineVariant),
    scrim = convert(scrim),
    surfaceBright = convert(surfaceBright),
    surfaceDim = convert(surfaceDim),
    surfaceContainer = convert(surfaceContainer),
    surfaceContainerHigh = convert(surfaceContainerHigh),
    surfaceContainerHighest = convert(surfaceContainerHighest),
    surfaceContainerLow = convert(surfaceContainerLow),
    surfaceContainerLowest = convert(surfaceContainerLowest)
  )
}

val ColorSaver =
  object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)

    override fun SaverScope.save(value: Color): Int = value.toArgb()
  }
