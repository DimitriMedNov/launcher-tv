package com.dimitrimednov.launchertv.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dimitrimednov.launchertv.ui.icons.SymbolIcons

/** Cómo se dibuja la tarjeta de una app: fondo (uno o dos colores), color del texto y acento del brillo. */
data class Brand(
    val background: List<Color>,
    val foreground: Color,
    val accent: Color,
    val icon: ImageVector? = null,
    /** Dirección del degradado en grados CSS (135 = de arriba-izquierda a abajo-derecha). */
    val gradientAngle: Float = 135f,
)

/** Una app conocida del diseño, con su paquete real en las teles Hisense. */
data class KnownApp(val packageName: String, val name: String, val brand: Brand)

private fun solid(bg: Long, fg: Long, accent: Long, icon: ImageVector? = null) =
    Brand(listOf(Color(bg)), Color(fg), Color(accent), icon)

const val LIVE_TV_PACKAGE = "com.vt.tv.tvcenter"
const val SPOTIFY_PACKAGE = "com.spotify.tv.android"

/** Orden y colores del diseño. Lo que no está aquí va después, en orden alfabético. */
val KnownApps: List<KnownApp> = listOf(
    KnownApp(
        LIVE_TV_PACKAGE, "Live TV",
        Brand(listOf(Color(0xFF0B3D91), Color(0xFF0099FE)), Color.White, Color(0xFF0099FE), SymbolIcons.LiveTv),
    ),
    KnownApp("com.apple.atve.androidtv.appletv", "Apple TV", solid(0xFF000000, 0xFFFFFFFF, 0xFFA1A1A6)),
    KnownApp("com.netflix.ninja", "Netflix", solid(0xFF141414, 0xFFE50914, 0xFFE50914)),
    KnownApp("com.disney.disneyplus", "Disney+", solid(0xFF0B1E5B, 0xFFFFFFFF, 0xFF2F6BFF)),
    KnownApp("com.google.android.youtube.tv", "YouTube", solid(0xFFFFFFFF, 0xFFFF0000, 0xFFFF0000)),
    KnownApp("com.amazon.amazonvideo.livingroom", "Prime Video", solid(0xFF00A8E1, 0xFFFFFFFF, 0xFF00A8E1)),
    KnownApp(SPOTIFY_PACKAGE, "Spotify", solid(0xFF191414, 0xFF1DB954, 0xFF1DB954)),
    KnownApp("com.kick.mobile", "Kick", solid(0xFF0B0B0B, 0xFF53FC18, 0xFF53FC18)),
    KnownApp(
        "com.neilturner.aerialviews", "Aerial Views",
        Brand(listOf(Color(0xFF2E5C7A), Color(0xFF8FB8C9)), Color.White, Color(0xFF8FB8C9), gradientAngle = 160f),
    ),
    KnownApp("com.amco.clarovideo_atv", "Claro video", solid(0xFFDA291C, 0xFFFFFFFF, 0xFFDA291C)),
    KnownApp("com.dazn", "DAZN", solid(0xFF0C161C, 0xFFF7FF1A, 0xFFF7FF1A)),
    KnownApp("com.wbd.stream", "HBO Max", solid(0xFF002BE7, 0xFFFFFFFF, 0xFF3D5CFF)),
    KnownApp("com.univision.prendetv", "ViX", solid(0xFFFF4C00, 0xFFFFFFFF, 0xFFFF4C00)),
    KnownApp("com.mercadolibre.android.mplay_tv", "Mercado Play", solid(0xFFFFE600, 0xFF2D3277, 0xFFFFE600)),
    KnownApp("tv.twitch.android.app", "Twitch", solid(0xFF9146FF, 0xFFFFFFFF, 0xFF9146FF)),
    KnownApp("com.android.vending", "Play Store", solid(0xFF2C2C2E, 0xFFFFFFFF, 0xFFFFFFFF, Icons.Rounded.Shop)),
    KnownApp("com.android.tv.settings", "Configuración", solid(0xFF3A3A3C, 0xFFFFFFFF, 0xFFFFFFFF, Icons.Rounded.Settings)),
)

/** Tarjeta para las apps que el diseño no trae. */
val DefaultBrand = solid(0xFF2C2C2E, 0xFFFFFFFF, 0xFFFFFFFF)

/** Apps del sistema de Hisense o del launcher anterior que empiezan ocultas (se pueden mostrar desde Ajustes). */
val HiddenByDefault = setOf(
    "com.spocky.projengmenu",
    "com.vt.hishow",
    "com.vt.tv.ai_sense",
    "com.vt.tv.mediacenter",
    "com.vt.tv.mobiledeviceconnectiontips",
    "com.vt.tv.protector",
    "com.vt.tv.singlelisten",
    // Tele del cuarto: sin antena ni Aerial Views (el fondo es de color).
    "com.hisense.tv.protector",
    "com.mediatek.wwtv.tvcenter",
    "com.neilturner.aerialviews",
    "com.vt.miracast",
)
