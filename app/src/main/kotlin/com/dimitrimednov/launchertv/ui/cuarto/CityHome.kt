package com.dimitrimednov.launchertv.ui.cuarto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dimitrimednov.launchertv.data.LauncherApp
import com.dimitrimednov.launchertv.data.NowPlaying

private const val SLAT_W = 66
private const val SLAT_GAP = 6
private const val OPEN_W = 552
private const val SKY_H = 806
private const val ROW_W = 1632
private val Heights = listOf(420, 520, 360, 600, 460, 540, 380, 640, 480, 400, 560, 440, 620, 370, 500, 450)

/** Lo que el inicio necesita para dibujarse. */
class HomeModel(
    val apps: List<LauncherApp>?,
    val nowPlaying: Map<String, NowPlaying>,
    val state: CityState,
    val zone: HomeZone?,
)

/** Desplazamiento para que la app abierta quede a la vista cuando hay más apps de las que caben. */
private fun scrollFor(n: Int, gi: Int): Int {
    val total = (n - 1) * (SLAT_W + SLAT_GAP) + OPEN_W
    if (total <= ROW_W) return 0
    return (gi * (SLAT_W + SLAT_GAP) - (ROW_W - OPEN_W) / 2).coerceIn(0, total - ROW_W)
}

@Composable
fun HomeContent(m: HomeModel) {
    val c = city
    val apps = m.apps
    val s = m.state
    if (apps != null && apps.isEmpty()) return EmptyCity(focused = s.base && s.screen == CityScreen.Home)
    val n = apps?.size ?: 16
    val gi = s.grid.coerceIn(0, (n - 1).coerceAtLeast(0))
    val scroll by animateDpAsState(scrollFor(n, gi).dp, cityTween(320), label = "scroll")

    // Luna junto al edificio abierto.
    val oL = 192 + gi * (SLAT_W + SLAT_GAP) - scrollFor(n, gi)
    val oR = oL + OPEN_W
    val moonX by animateDpAsState((if (oR + 40 + 170 <= 1824) oR + 40 else oL - 40 - 170).dp, cityTween(420), label = "luna")
    Moon(170, Modifier.offset { IntOffset(moonX.roundToPx(), 206.dp.roundToPx()) })

    Row(Modifier.offset(192.dp, 160.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        CText("La ciudad", 34, weight = FontWeight.Bold, condensed = true, upper = true, spacing = .05f)
        if (apps != null) CText("${apps.size} ${if (apps.size == 1) "edificio" else "edificios"} · una app en cada uno", 24, color = c.mu)
    }

    Box(Modifier.offset(176.dp, 196.dp).size(1656.dp, 850.dp).clipToBounds()) {
        Row(
            Modifier
                .offset(16.dp, 24.dp)
                .height(SKY_H.dp)
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .graphicsLayer { translationX = -scroll.toPx() },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(SLAT_GAP.dp),
        ) {
            if (apps == null) {
                repeat(16) { i ->
                    Box(Modifier.size((if (i == 0) OPEN_W else SLAT_W).dp, (if (i == 0) SKY_H else Heights[i]).dp).background(c.sf, RoundedCornerShape(4.dp, 4.dp)))
                }
            } else {
                apps.forEachIndexed { i, app ->
                    key(app.packageName) { Building(app, i, n, i == gi, m) }
                }
            }
        }
    }
    m.state.radial?.let { r ->
        val vi = apps?.indexOfFirst { it.packageName == r.packageName } ?: -1
        if (vi >= 0) RadialMenu(r, apps!![vi].name, (oL + OPEN_W / 2).coerceIn(192 + 340, 1824 - 340))
    }
}

@Composable
private fun Building(app: LauncherApp, index: Int, n: Int, open: Boolean, m: HomeModel) {
    val c = city
    val s = m.state
    val focused = open && m.zone == HomeZone.Grid && s.base && s.screen == CityScreen.Home
    val moving = open && s.moving
    val color = c.brand(appColor(app))
    // Se leen al medir, no al componer: así crecer no vuelve a componer el panel en cada cuadro.
    val w = animateDpAsState((if (open) OPEN_W else SLAT_W).dp, cityTween(320), label = "ancho")
    val h = animateDpAsState((if (open) SKY_H else Heights[index % Heights.size]).dp, cityTween(320), label = "alto")
    val lift by animateFloatAsState(if (moving) -18f else 0f, cityTween(240), label = "mover")
    Box(
        Modifier
            .graphicsLayer { translationY = lift * density }
            .focusGlow(focused || moving, if (moving) c.ac2 else c.ac, c.glow)
            .layout { measurable, _ ->
                val p = measurable.measure(Constraints.fixed(w.value.roundToPx(), h.value.roundToPx()))
                layout(p.width, p.height) { p.place(0, 0) }
            }
            .clipToBounds(),
    ) {
        if (open) {
            Column(Modifier.fillMaxSize().tiled(c.sf, rememberDotTile())) {
                Box(Modifier.fillMaxWidth().height(6.dp).background(color))
                OpenPanel(app, index, n, m)
            }
        } else {
            val light = lerp(c.sf, color, if (c.night) .16f else .24f)
            Column(Modifier.fillMaxSize().tiled(light, rememberWindowTile(c.sf))) {
                Box(Modifier.fillMaxWidth().height(8.dp).background(color))
                Box(Modifier.fillMaxWidth().background(c.sf).padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                    CText(app.name, 28, weight = FontWeight.SemiBold, condensed = true, upper = true, spacing = .06f, modifier = Modifier.vertical())
                }
            }
        }
    }
}

@Composable
private fun OpenPanel(app: LauncherApp, index: Int, n: Int, m: HomeModel) {
    val c = city
    val s = m.state
    val media = panelMedia(app, m.nowPlaying[app.packageName])
    Column(
        Modifier.wrapContentWidth(Alignment.Start, unbounded = true).width(OPEN_W.dp).fillMaxHeight()
            .padding(start = 34.dp, end = 34.dp, top = 28.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CText("%02d / %02d".format(index + 1, n), 26, color = c.mu, weight = FontWeight.SemiBold, condensed = true, spacing = .08f)
            CText("Edificio en foco", 24, color = c.mu, weight = FontWeight.SemiBold, condensed = true, upper = true, spacing = .08f)
        }
        CText(app.name, 76, weight = FontWeight.Bold, condensed = true, upper = true, lineHeight = 1f)
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.lnS))
        if (media != null) {
            Box(Modifier.background(c.ac, R2).padding(horizontal = 12.dp, vertical = 3.dp)) {
                CText(media.label, 24, color = c.onAc, weight = FontWeight.Bold, condensed = true, upper = true, spacing = .06f)
            }
            CText(media.title, 48, weight = FontWeight.Bold, condensed = true, upper = true, lineHeight = 1.05f, maxLines = 2)
            if (media.sub.isNotEmpty()) CText(media.sub, 26, color = c.mu2, lineHeight = 1.3f, maxLines = 2)
            if (media.big != null) {
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CText(media.big, 132, weight = FontWeight.Bold, condensed = true, tabular = true, lineHeight = .9f)
                    CText(media.unit, 44, color = c.ac, weight = FontWeight.SemiBold, condensed = true, modifier = Modifier.padding(bottom = 6.dp))
                    CText(media.bigLabel, 24, color = c.mu, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.fillMaxWidth().height(6.dp).background(c.lnS)) {
                        Box(Modifier.fillMaxWidth(media.progress).fillMaxHeight().background(c.ac))
                    }
                    CText(media.meta, 24, color = c.mu2, tabular = true)
                }
            }
        } else {
            Box(Modifier.border(1.dp, c.lnS, R2).padding(horizontal = 12.dp, vertical = 2.dp)) {
                CText("Sin actividad reciente", 24, weight = FontWeight.Bold, condensed = true, upper = true, spacing = .06f)
            }
            CText("Ábrela para empezar a ver o escuchar.", 28, color = c.mu2, lineHeight = 1.3f)
            Box(
                Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)
                    .tiled(lerp(c.sf, c.brand(appColor(app), .6f), if (c.night) .08f else .12f), rememberWindowTile(c.sf, 34, 40, 14, 20))
                    .border(1.dp, c.ln),
            )
        }
        if (media != null) Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PanelButton.entries.forEachIndexed { i, b ->
                CityButton(b.label, s.base && m.zone == HomeZone.Panel && s.panelButton == i)
            }
        }
    }
}

@Composable
fun Moon(size: Int, modifier: Modifier = Modifier) {
    val c = city
    Canvas(modifier.size(size.dp)) {
        val k = this.size.width / 170f
        for (i in 1..3) drawCircle(c.moon.copy(alpha = if (c.night) .05f else .07f), this.size.width / 2 + i * 10 * k)
        drawCircle(c.moon)
        val crater = Color.Black
        drawCircle(crater.copy(alpha = .10f), 17 * k, Offset(54 * k, 65 * k))
        drawCircle(crater.copy(alpha = .08f), 10 * k, Offset(104 * k, 41 * k))
        drawCircle(crater.copy(alpha = .09f), 22 * k, Offset(107 * k, 121 * k))
        drawCircle(crater.copy(alpha = .08f), 7.5f * k, Offset(55.5f * k, 119.5f * k))
    }
}

@Composable
private fun RadialMenu(r: Radial, name: String, cx: Int) {
    val c = city
    val cy = 623
    val ring = remember { Animatable(0f) }
    LaunchedEffect(r.ring) {
        if (r.ring) ring.animateTo(1f, tween(1100, easing = LinearEasing)) else ring.snapTo(0f)
    }
    AnimatedVisibility(true, enter = fadeIn(cityTween(220)) + scaleIn(cityTween(220), initialScale = .88f)) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.offset((cx - 110).dp, (cy - 110).dp).size(220.dp).background(c.sf2, CircleShape).border(1.dp, c.lnS, CircleShape))
            Canvas(Modifier.offset((cx - 122).dp, (cy - 122).dp).size(244.dp)) {
                val rr = 116.dp.toPx()
                drawCircle(c.ln, rr, style = Stroke(4.dp.toPx()))
                drawArc(
                    c.ac2, -90f, 360f * ring.value, false,
                    topLeft = Offset(center.x - rr, center.y - rr), size = androidx.compose.ui.geometry.Size(rr * 2, rr * 2),
                    style = Stroke(6.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            Column(
                Modifier.offset((cx - 95).dp, (cy - 110).dp).size(190.dp, 220.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CText(name, 26, weight = FontWeight.SemiBold)
                CText(if (r.option == RadialOption.Hide) (if (r.ring) "Sigue…" else "Mantén OK") else "Elige con flechas", 24, color = c.mu, tabular = true)
            }
            val places = mapOf(
                RadialOption.Open to (0 to -180), RadialOption.Move to (270 to 0),
                RadialOption.Hide to (0 to 180), RadialOption.Info to (-290 to 0),
            )
            RadialOption.entries.forEach { o ->
                val (dx, dy) = places.getValue(o)
                val f = r.option == o
                val fill = if (o == RadialOption.Hide) c.ac2 else c.ac
                val scale by animateFloatAsState(if (f) 1.08f else 1f, cityTween(240), label = "radial")
                Box(Modifier.offset((cx + dx).dp, (cy + dy).dp)) {
                    Row(
                        Modifier
                            .layoutCentered()
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .focusGlow(f, Color.Transparent, c.glow)
                            .height(64.dp)
                            .background(if (f) fill else c.sf2, R4)
                            .border(1.dp, if (f) fill else c.lnS, R4)
                            .padding(horizontal = 26.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val tc = if (f) c.onAc else c.tx
                        CText(o.arrow, 24, color = tc, tabular = true)
                        CText(o.label, 26, color = tc, weight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** Centra el elemento en su punto de origen (`translate(-50%, -50%)`). */
private fun Modifier.layoutCentered(): Modifier = layout { measurable, constraints ->
    val p = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(0, 0) { p.place(-p.width / 2, -p.height / 2) }
}

@Composable
private fun EmptyCity(focused: Boolean) {
    val c = city
    Column(
        Modifier.offset(192.dp, 136.dp).size(1632.dp, 880.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CText("0 apps visibles", 24, color = c.mu, tabular = true)
        CText("No hay apps en el inicio", 56, weight = FontWeight.SemiBold)
        CText("La ciudad se quedó a oscuras. Tus apps ocultas esperan en Ajustes.", 28, color = c.mu)
        CityButton("Mostrar apps ocultas", focused, Modifier.padding(top = 16.dp))
    }
}

/** Lo que se muestra en el panel de la app abierta si está reproduciendo algo. */
class PanelMedia(
    val label: String,
    val title: String,
    val sub: String,
    val big: String?,
    val unit: String,
    val bigLabel: String,
    val progress: Float,
    val meta: String,
)

private val MusicApps = setOf("com.spotify.tv.android", "deezer.android.tv", "com.google.android.youtube.tvmusic")

private fun mmss(ms: Long): String {
    val t = ms / 1000
    return "${t / 60}:${"%02d".format(t % 60)}"
}

fun panelMedia(app: LauncherApp, np: NowPlaying?): PanelMedia? {
    np ?: return null
    val known = np.durationMs > 0
    return if (app.packageName in MusicApps) {
        PanelMedia(
            label = if (np.isPlaying) "Sonando ahora" else "En pausa",
            title = np.title,
            sub = listOfNotNull(np.artist, np.album).joinToString(" · "),
            big = if (known) mmss(np.positionMs) else null, unit = "",
            bigLabel = "de ${mmss(np.durationMs)}",
            progress = np.progress,
            meta = "${mmss(np.positionMs)} / ${mmss(np.durationMs)}",
        )
    } else {
        val left = ((np.durationMs - np.positionMs) / 60_000).coerceAtLeast(0)
        PanelMedia(
            label = "Seguir viendo",
            title = np.title,
            sub = listOfNotNull(np.artist, np.album).joinToString(" · "),
            big = if (known) (np.progress * 100).toInt().toString() else null, unit = "%",
            bigLabel = "visto",
            progress = np.progress,
            meta = if (left >= 60) "Quedan ${left / 60} h ${left % 60} min" else "Quedan $left min",
        )
    }
}

/** Colores del diseño para las apps del cuarto; las demás usan el acento de su marca. */
private val DesignColors = mapOf(
    "com.apple.atve.androidtv.appletv" to 0xFFA7A9B4, "com.netflix.ninja" to 0xFFE50914,
    "com.disney.disneyplus" to 0xFF2453E6, "com.google.android.youtube.tv" to 0xFFFF2D2D,
    "com.amazon.amazonvideo.livingroom" to 0xFF1BA7E0, "com.spotify.tv.android" to 0xFF1DB954,
    "com.kick.mobile" to 0xFF53FC18, "com.amco.clarovideo_atv" to 0xFFE4002B, "com.dazn" to 0xFFE8E337,
    "com.wbd.stream" to 0xFF6A3DFF, "com.univision.prendetv" to 0xFFFF6A13,
    "com.mercadolibre.android.mplay_tv" to 0xFFFFD93D, "tv.twitch.android.app" to 0xFF9146FF,
    "com.android.vending" to 0xFF34A853, "com.android.tv.settings" to 0xFF7C8BA3,
    "com.cbs.ca" to 0xFF0064FF, "deezer.android.tv" to 0xFFA238FF, "com.google.android.videos" to 0xFF4285F4,
    "com.calientetv" to 0xFFFF3B6B, "com.digitalproshare.filmapp" to 0xFFFFA62B,
)

private val Fallback = listOf(0xFF7FA3C4, 0xFFC48F6A, 0xFF8FC4A0, 0xFFB98AD0, 0xFFD0C27F)

fun appColor(app: LauncherApp): Color {
    DesignColors[app.packageName]?.let { return Color(it) }
    val accent = app.brand.accent
    if (accent != Color.White) return accent
    return Color(Fallback[(app.packageName.hashCode() and 0x7fffffff) % Fallback.size])
}
