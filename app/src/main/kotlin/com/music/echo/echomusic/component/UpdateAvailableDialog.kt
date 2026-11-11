package echo.music.iad1tya.echomusic.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import echo.music.iad1tya.R
import echo.music.iad1tya.echomusic.updater.ChangelogSection
import echo.music.iad1tya.ui.utils.parseMarkdownToSections
import echo.music.iad1tya.ui.utils.parseSimpleMarkdown

private val VoidCanvas = Color(0xFF05060A)
private val LaserCyan = Color(0xFF00F5FF)
private val Ultraviolet = Color(0xFFBD00FF)
private val GlassFill = Color(0x220D111A)
private val GlassRim =
  Brush.linearGradient(
    colors =
      listOf(
        Color(0x8000F5FF),
        Color(0x40BD00FF),
        Color(0x10FFFFFF),
      )
  )

/** Only release links that originate from github.com/repclosed/sonara are actionable. */
internal fun isTrustedSonaraReleaseUrl(url: String?): Boolean {
  if (url.isNullOrBlank()) return false
  return try {
    val parsed = Uri.parse(url)
    val host = parsed.host?.lowercase()
    val segments = parsed.pathSegments
    (host == "github.com" || host == "www.github.com") &&
      segments.size >= 2 &&
      segments[0] == "repclosed" &&
      segments[1] == "sonara"
  } catch (e: Exception) {
    false
  }
}

/**
 * Cyberpunk-glass dialog announcing a newer Sonara release.
 *
 * @param version Release tag reported by the GitHub Releases API.
 * @param changelog Parsed Markdown sections from the release body.
 * @param description Raw Markdown / description shown above the sections.
 * @param title Human-readable release title from the GitHub release `name`.
 * @param downloadUrl Release page or direct `.apk` asset URL. Only
 *   `github.com/repclosed/sonara` URLs are opened.
 * @param onDismiss Invoked when the dialog is dismissed.
 */
@Composable
fun UpdateAvailableDialog(
  version: String,
  changelog: List<ChangelogSection>,
  description: String?,
  onDismiss: () -> Unit,
  title: String? = null,
  downloadUrl: String? = null,
) {
  val context = LocalContext.current
  val effectiveUrl =
    downloadUrl
      ?.takeIf { isTrustedSonaraReleaseUrl(it) }
      ?: "https://github.com/repclosed/sonara/releases"

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.padding(horizontal = 18.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(VoidCanvas)
          .background(GlassFill)
          .border(1.2.dp, GlassRim, RoundedCornerShape(24.dp))
          .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier.size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LaserCyan.copy(alpha = 0.12f))
                .border(1.dp, LaserCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              painter = painterResource(R.drawable.update),
              contentDescription = null,
              tint = LaserCyan,
              modifier = Modifier.size(22.dp),
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.update_available_title),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
            )
            Text(
              text = version,
              style = MaterialTheme.typography.labelMedium,
              color = LaserCyan,
              fontFamily = FontFamily.Monospace,
            )
          }
        }

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .weight(1f, fill = false)
              .heightIn(max = 360.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(Color(0x33060A12))
              .border(1.dp, Ultraviolet.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
              .verticalScroll(rememberScrollState())
              .padding(14.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = stringResource(R.string.changelog),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Ultraviolet,
            )

            val (effectiveDescription, effectiveSections) =
              remember(changelog, description) {
                if (changelog.isNotEmpty()) {
                  Pair(description?.takeIf { it.isNotBlank() }, changelog)
                } else if (!description.isNullOrBlank()) {
                  parseMarkdownToSections(description)
                } else {
                  Pair(null, emptyList())
                }
              }

            if (!effectiveDescription.isNullOrBlank()) {
              Text(
                text = parseSimpleMarkdown(effectiveDescription, LaserCyan),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCDD8EA),
              )
            }

            effectiveSections.forEach { section ->
              if (section.title.isNotBlank()) {
                Text(
                  text = parseSimpleMarkdown(section.title, LaserCyan),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = LaserCyan,
                  modifier = Modifier.padding(top = 6.dp),
                )
              }
              section.items.forEach { item ->
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = "▸",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LaserCyan,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = parseSimpleMarkdown(item.trim(), LaserCyan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCDD8EA),
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }

            if (effectiveDescription.isNullOrBlank() && effectiveSections.isEmpty()) {
              Text(
                text = stringResource(R.string.no_changelog_available),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0x99A0B0D0),
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          TextButton(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
            Text(text = stringResource(R.string.later), color = Color(0x99A0B0D0))
          }
          Spacer(Modifier.weight(1f))
          Button(
            onClick = {
              onDismiss()
              runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effectiveUrl)))
              }
            },
            shape = RoundedCornerShape(18.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = LaserCyan.copy(alpha = 0.16f),
                contentColor = LaserCyan,
              ),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LaserCyan.copy(alpha = 0.55f)),
          ) {
            Text(
              text = "Download Update",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
          }
        }
      }
    }
  }
}
