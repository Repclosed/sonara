@file:OptIn(ExperimentalMaterial3Api::class)

package echo.music.iad1tya.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import echo.music.iad1tya.BuildConfig
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.utils.backToMain

/* ---------------------------------------------------------------------------
 * Sonara cyberpunk-glass palette. Kept local so the About surface keeps its
 * identity even when the user picks a dynamic Material theme colour.
 * ------------------------------------------------------------------------- */
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

private const val SONARA_PORTFOLIO = "https://repclosed.github.io"
private const val SONARA_REPOSITORY = "https://github.com/repclosed/sonara"
private const val SONARA_GITHUB_PROFILE = "https://github.com/repclosed"
private const val SONARA_AVATAR = "https://github.com/repclosed.png?size=240"

/** Opens [url] in the user's browser through an explicit ACTION_VIEW intent. */
internal fun openSonaraLink(context: Context, url: String) {
  val intent =
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  runCatching { context.startActivity(intent) }
}

@Composable
fun AboutScreen(
  navController: NavController,
  scrollBehavior: TopAppBarScrollBehavior,
  onBack: (() -> Unit)? = null,
  highlightKey: String? = null,
) {
  val context = LocalContext.current

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = VoidCanvas,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.about),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = { onBack?.invoke() ?: navController.navigateUp() },
            onLongClick = navController::backToMain,
          ) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        },
        windowInsets = TopAppBarDefaults.windowInsets,
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = VoidCanvas,
            scrolledContainerColor = VoidCanvas,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
          ),
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(
            LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal)
          ),
      contentPadding =
        PaddingValues(
          start = 16.dp,
          top = innerPadding.calculateTopPadding() + 8.dp,
          end = 16.dp,
          bottom =
            WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 32.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      item { SonaraIdentityCard() }

      item {
        SonaraGlassCard {
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SonaraLinkRow(
              icon = R.drawable.website,
              title = "Portfolio / Bio",
              subtitle = SONARA_PORTFOLIO,
              onClick = { openSonaraLink(context, SONARA_PORTFOLIO) },
            )
            SonaraDivider()
            SonaraLinkRow(
              icon = R.drawable.github,
              title = "Project Repository",
              subtitle = "repclosed/sonara",
              onClick = { openSonaraLink(context, SONARA_REPOSITORY) },
            )
            SonaraDivider()
            SonaraLinkRow(
              icon = R.drawable.github_main,
              title = "GitHub Profile",
              subtitle = "@repclosed",
              onClick = { openSonaraLink(context, SONARA_GITHUB_PROFILE) },
            )
          }
        }
      }

      item {
        Text(
          text = "Crafted with the Sonara native performance engine.",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0x59A0B0D0),
          fontFamily = FontFamily.Monospace,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun SonaraIdentityCard() {
  val context = LocalContext.current
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (pressed) 0.97f else 1f,
      animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
      label = "sonaraAboutScale",
    )

  SonaraGlassCard {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        // Ambient neon halo projected behind the avatar.
        Box(
          modifier =
            Modifier.size(118.dp)
              .blur(28.dp)
              .background(
                Brush.radialGradient(
                  colors =
                    listOf(
                      LaserCyan.copy(alpha = 0.42f),
                      Ultraviolet.copy(alpha = 0.22f),
                      Color.Transparent,
                    )
                ),
                CircleShape,
              )
        )
        Box(
          modifier =
            Modifier.size(92.dp)
              .graphicsLayer {
                scaleX = scale
                scaleY = scale
              }
              .clip(CircleShape)
              .border(1.2.dp, GlassRim, CircleShape)
              .clickable(
                interactionSource = interaction,
                indication = null,
              ) { openSonaraLink(context, SONARA_PORTFOLIO) },
          contentAlignment = Alignment.Center,
        ) {
          AsyncImage(
            model = SONARA_AVATAR,
            contentDescription = "Cyan, creator of Sonara",
            placeholder = painterResource(R.drawable.ic_launcher_nobg),
            error = painterResource(R.drawable.ic_launcher_nobg),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }

      Spacer(Modifier.height(2.dp))

      Text(
        text = "Sonara",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = Color.White,
      )
      Text(
        text = "Next-Gen High-Performance Music Client",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFA0B0D0),
        textAlign = TextAlign.Center,
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SonaraBadge(text = "v" + BuildConfig.VERSION_NAME, accent = LaserCyan)
        SonaraBadge(text = "Creator: Cyan", accent = Ultraviolet)
      }

      Spacer(Modifier.height(4.dp))

      OutlinedButton(
        onClick = { openSonaraLink(context, SONARA_GITHUB_PROFILE) },
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, LaserCyan.copy(alpha = 0.55f)),
        colors =
          ButtonDefaults.outlinedButtonColors(
            contentColor = LaserCyan,
            containerColor = Color(0x1100F5FF),
          ),
      ) {
        Icon(
          painter = painterResource(R.drawable.github),
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = LaserCyan,
        )
        Spacer(Modifier.width(8.dp))
        Text("github.com/repclosed", fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun SonaraBadge(text: String, accent: Color) {
  Box(
    modifier =
      Modifier.clip(RoundedCornerShape(8.dp))
        .background(accent.copy(alpha = 0.12f))
        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
        .padding(horizontal = 9.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = accent,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.Monospace,
      maxLines = 1,
    )
  }
}

@Composable
private fun SonaraGlassCard(content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(GlassFill)
        .border(1.2.dp, GlassRim, RoundedCornerShape(24.dp))
        .padding(horizontal = 6.dp, vertical = 6.dp),
    content = content,
  )
}

@Composable
private fun SonaraLinkRow(
  icon: Int,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val alpha by
    animateFloatAsState(
      targetValue = if (pressed) 0.6f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMedium),
      label = "sonaraLinkAlpha",
    )

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .graphicsLayer { this.alpha = alpha }
        .clip(RoundedCornerShape(18.dp))
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Box(
      modifier =
        Modifier.size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(LaserCyan.copy(alpha = 0.10f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = LaserCyan,
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0x99A0B0D0),
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Icon(
      painter = painterResource(R.drawable.arrow_forward),
      contentDescription = null,
      modifier = Modifier.size(18.dp),
      tint = Ultraviolet.copy(alpha = 0.8f),
    )
  }
}

@Composable
private fun SonaraDivider() {
  HorizontalDivider(
    modifier = Modifier.padding(start = 66.dp, end = 18.dp),
    thickness = 0.6.dp,
    color = Color(0x1AFFFFFF),
  )
}
