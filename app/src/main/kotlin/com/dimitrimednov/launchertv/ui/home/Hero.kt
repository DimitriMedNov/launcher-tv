package com.dimitrimednov.launchertv.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.dimitrimednov.launchertv.data.LIVE_TV_PACKAGE
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.data.NowPlaying
import com.dimitrimednov.launchertv.data.SPOTIFY_PACKAGE
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import com.dimitrimednov.launchertv.ui.theme.rememberSettlingLoop

/** Lo que muestra la franja de arriba para la app enfocada. */
data class HeroModel(
    val app: LauncherApp,
    val eyebrow: String,
    val live: Boolean,
    val music: Boolean,
    val musicPlaying: Boolean,
    val title: String,
    val meta: String,
    val progress: Float?,
    val primaryLabel: String,
    val primaryIcon: ImageVector,
)

/**
 * "Seguir viendo" permanente no se puede (confirmado por adb): se usa la sesión de medios
 * de la app mientras siga abierta; si no hay, la tarjeta invita a abrir la app.
 */
fun heroModel(app: LauncherApp, playing: NowPlaying?): HeroModel = when {
    app.packageName == LIVE_TV_PACKAGE -> HeroModel(
        app, "Ahora", live = true, music = false, musicPlaying = false,
        title = playing?.title ?: "Televisión en vivo",
        meta = playing?.artist ?: "Antena · Presiona OK para ver",
        progress = playing?.progress?.takeIf { playing.durationMs > 0 },
        primaryLabel = "Ver canal", primaryIcon = Icons.Rounded.PlayArrow,
    )
    playing != null && app.packageName == SPOTIFY_PACKAGE -> HeroModel(
        app, "Sonando", live = false, music = true, musicPlaying = playing.isPlaying,
        title = playing.title,
        meta = listOfNotNull(playing.artist, playing.album, timeRange(playing)).joinToString(" · "),
        progress = playing.progress.takeIf { playing.durationMs > 0 },
        primaryLabel = if (playing.isPlaying) "Pausar" else "Reproducir",
        primaryIcon = if (playing.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
    )
    playing != null -> HeroModel(
        app, "Seguir viendo", live = false, music = false, musicPlaying = false,
        title = playing.title,
        meta = listOfNotNull(playing.artist, remaining(playing)).joinToString(" · ").ifEmpty { app.name },
        progress = playing.progress.takeIf { playing.durationMs > 0 },
        primaryLabel = "Reanudar", primaryIcon = Icons.Rounded.PlayArrow,
    )
    else -> HeroModel(
        app, "App", live = false, music = false, musicPlaying = false,
        title = app.name, meta = "Presiona OK para abrir", progress = null,
        primaryLabel = "Abrir", primaryIcon = Icons.Rounded.PlayArrow,
    )
}

private fun mmss(ms: Long) = "%d:%02d".format(ms / 60_000, (ms / 1000) % 60)

private fun timeRange(p: NowPlaying) = if (p.durationMs > 0) "${mmss(p.positionMs)} / ${mmss(p.durationMs)}" else null

private fun remaining(p: NowPlaying): String? {
    if (p.durationMs <= 0) return null
    val min = ((p.durationMs - p.positionMs) / 60_000).coerceAtLeast(1)
    return if (min >= 60) "Quedan ${min / 60} h ${min % 60} min" else "Quedan $min min"
}

/** Franja del Inicio: cada línea entra escalonada (55 ms) al cambiar de app, como en el diseño. */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Hero(model: HeroModel, dir: Dir, focusedButton: Int?, pressed: String?, accent: Color, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = model,
        contentKey = { it.app.packageName + it.title },
        transitionSpec = { fadeIn(tween(0)) togetherWith fadeOut(tween(160)) },
        label = "hero",
        modifier = modifier,
    ) { m ->
        Column(Modifier.width(1100.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                lineModifier(0, dir),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    m.app.name, color = m.app.brand.foreground, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1,
                    modifier = Modifier
                        .height(44.dp)
                        .background(Brush.linearGradient(m.app.brand.background.ifSingle()), CircleShape)
                        .border(1.dp, Palette.glass(.15f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                )
                Text(m.eyebrow, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Palette.Label)
                if (m.live) LiveBadge()
                if (m.music) EqualizerBars(accent, m.musicPlaying)
            }
            Text(
                m.title, fontSize = 88.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2.8).sp, lineHeight = 86.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = lineModifier(1, dir).cssLineHeight(88.sp, 86.sp),
            )
            Text(m.meta, fontSize = 28.sp, color = Palette.Secondary, maxLines = 1, modifier = lineModifier(2, dir))
            if (m.progress != null) ProgressBar(m.progress, accent, lineModifier(3, dir))
            Row(lineModifier(4, dir).padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                PillButton(m.primaryLabel, m.primaryIcon, focusedButton == 0, pressed == "hero0", accent)
                PillButton("Más info", Icons.Rounded.Info, focusedButton == 1, pressed == "hero1", accent)
            }
        }
    }
}

/** Aviso "Sin conexión" en lugar de la franja. Su único botón abre los ajustes de wifi. */
@Composable
fun OfflineHero(focused: Boolean, pressed: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.width(1100.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Icon(Icons.Rounded.WifiOff, contentDescription = null, tint = Palette.Tertiary, modifier = Modifier.size(56.dp))
        Text(
            "Sin conexión", fontSize = 88.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2.8).sp,
            modifier = Modifier.cssLineHeight(88.sp, 86.sp),
        )
        Text(
            "Revisa tu red. Mientras tanto puedes usar las entradas HDMI y la antena.",
            fontSize = 28.sp, color = Palette.Secondary,
        )
        PillButton("Abrir ajustes de wifi", null, focused, pressed, Palette.Accent, Modifier.padding(top = 22.dp))
    }
}

/** Esqueleto con la forma de la franja (estado "Cargando"). */
@Composable
fun HeroSkeleton(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.size(260.dp, 44.dp).shimmer(22.dp))
        Box(Modifier.size(820.dp, 86.dp).shimmer(20.dp, 80))
        Box(Modifier.size(420.dp, 28.dp).shimmer(14.dp, 160))
        Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.size(240.dp, 72.dp).shimmer(36.dp, 240))
            Box(Modifier.size(220.dp, 72.dp).shimmer(36.dp, 300))
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, accent: Color, modifier: Modifier) {
    val p by animateFloatAsState(progress, Motion.ease(700), label = "progreso")
    Box(modifier.padding(top = 4.dp).size(420.dp, 8.dp).clip(CircleShape).background(Palette.glass(.16f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(p).background(accent, CircleShape).shine(true, 4.dp, .55f))
    }
}

@Composable
private fun LiveBadge() {
    val a by rememberSettlingLoop(800, 1f, .35f, rest = 1f)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(12.dp).graphicsLayer { alpha = a }.background(Palette.Live, CircleShape))
        Text("EN VIVO", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Palette.Live)
    }
}

@Composable
private fun EqualizerBars(color: Color, playing: Boolean) {
    Row(Modifier.height(24.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        listOf(520, 380, 640).forEachIndexed { i, ms ->
            val h by rememberSettlingLoop(ms, .25f, 1f, rest = .3f, key = playing, delayMs = i * 60)
            Box(Modifier.width(5.dp).fillMaxHeight(if (playing) h else .3f).background(color, CircleShape))
        }
    }
}

/** Entrada escalonada de cada línea: desde la dirección de la flecha, 55 ms entre líneas. */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedVisibilityScope.lineProgress(index: Int): Float {
    val p by transition.animateFloat(
        transitionSpec = { tween(640, delayMillis = index * 55, easing = Motion.Ease) },
        label = "línea $index",
    ) { if (it == EnterExitState.Visible) 1f else 0f }
    return p
}

@Composable
private fun androidx.compose.animation.AnimatedContentScope.lineModifier(index: Int, dir: Dir): Modifier {
    val p = lineProgress(index)
    val (vx, vy) = when (dir) { Dir.Right -> 1f to 0f; Dir.Left -> -1f to 0f; Dir.Down -> 0f to 1f; Dir.Up -> 0f to -1f }
    return Modifier.graphicsLayer {
        alpha = p
        translationX = (1 - p) * vx * 44f
        translationY = (1 - p) * vy * 28f
    }
}

private fun List<Color>.ifSingle() = if (size == 1) listOf(first(), first()) else this
