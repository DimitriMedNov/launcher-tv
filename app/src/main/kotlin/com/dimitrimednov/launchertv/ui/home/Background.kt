package com.dimitrimednov.launchertv.ui.home

import android.graphics.ImageDecoder
import android.net.Uri
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.dimitrimednov.launchertv.R
import com.dimitrimednov.launchertv.data.BackgroundItem
import com.dimitrimednov.launchertv.ui.theme.Motion
import com.dimitrimednov.launchertv.ui.theme.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

private val KenBurnsEasing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
private const val CROSSFADE_MS = 1_400
/** El fondo ocupa 1.1 veces la pantalla para que el paralaje no deje ver la orilla. */
private const val ZOOM = 1.1f
/** Acercamiento máximo de una foto: el del fondo por 1.07 del Ken Burns. */
private const val MAX_PHOTO_ZOOM = ZOOM * 1.07f

/**
 * Fondo del inicio y del protector (es la misma capa: así nunca hay dos videos a la vez).
 * Lleva un velo ligero para que se lean los textos sin tapar la foto: en el inicio, a la izquierda
 * (títulos) y abajo (apps); en el protector, solo arriba (clima) y abajo (hora). Se oscurece más
 * cuando se abre la barra lateral.
 */
@Composable
fun HomeBackground(
    item: BackgroundItem,
    loop: Boolean,
    onVideoEnded: (BackgroundItem) -> Unit,
    dimmed: Boolean,
    faded: Boolean,
    saver: Boolean,
    gridColumn: Int?,
) {
    // El video no acepta transparencia de Compose: en vez de aclarar el fondo se le pone encima el color de fondo.
    val fade by animateFloatAsState(if (faded && !saver) .7f else 0f, tween(900), label = "fondo")
    val dim by animateFloatAsState(if (dimmed && !saver) .4f else 0f, tween(350), label = "oscurecer")
    val shift by animateFloatAsState((gridColumn ?: 0) * -8f, Motion.ease(900), label = "paralaje")
    val homeVeil by animateFloatAsState(if (saver) 0f else 1f, tween(800), label = "velo inicio")

    // El color de fondo ya lo pinta DesignCanvas debajo.
    Box(Modifier.fillMaxSize()) {
        BackgroundMedia(item, loop, onVideoEnded, Modifier.fillMaxSize().zoomed { shift })
        Box(Modifier.fillMaxSize().veils({ fade }, { homeVeil }, { dim }))
    }
}

/**
 * Todos los velos en una sola capa. Cada uno se pinta solo en la parte de la pantalla que cubre y
 * solo si se ve: la tele repinta la pantalla entera en cada cuadro y su chip gráfico es justo.
 * Inicio: izquierda (55 % → 0 en el 70 % del ancho) y abajo (0 → 45 % desde la mitad).
 * Protector: arriba (30 % → 0 en el 20 % del alto) y abajo (0 → 50 % desde el 60 %).
 */
private fun Modifier.veils(fade: () -> Float, homeVeil: () -> Float, dim: () -> Float) = drawWithCache {
    val w = size.width
    val h = size.height
    val homeLeft = Brush.horizontalGradient(
        0f to Palette.Bg.copy(alpha = .55f), .5f to Palette.Bg.copy(alpha = .3f), 1f to Color.Transparent, endX = w * .7f,
    )
    val homeBottom = Brush.verticalGradient(listOf(Color.Transparent, Palette.Bg.copy(alpha = .45f)), startY = h * .45f, endY = h)
    val saverTop = Brush.verticalGradient(listOf(Palette.Bg.copy(alpha = .3f), Color.Transparent), endY = h * .2f)
    val saverBottom = Brush.verticalGradient(listOf(Color.Transparent, Palette.Bg.copy(alpha = .5f)), startY = h * .6f, endY = h)
    onDrawBehind {
        val f = fade()
        val home = homeVeil()
        val d = dim()
        if (f > 0f) drawRect(Palette.Bg.copy(alpha = f))
        if (home < 1f) {
            drawRect(saverTop, size = Size(w, h * .2f), alpha = 1f - home)
            drawRect(saverBottom, Offset(0f, h * .6f), Size(w, h * .4f), alpha = 1f - home)
        }
        if (home > 0f) {
            drawRect(homeLeft, size = Size(w * .7f, h), alpha = home)
            drawRect(homeBottom, Offset(0f, h * .45f), Size(w, h * .55f), alpha = home)
        }
        if (d > 0f) drawRect(Color.Black.copy(alpha = d))
    }
}

/** Agranda el fondo a [ZOOM] y lo corre [shift] px; con medidas, así solo se recoloca y no se repinta. */
private fun Modifier.zoomed(shift: () -> Float) = layout { measurable, constraints ->
    val width = (constraints.maxWidth * ZOOM).roundToInt()
    val height = (constraints.maxHeight * ZOOM).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place((constraints.maxWidth - width) / 2 + shift().roundToInt(), (constraints.maxHeight - height) / 2)
    }
}

/** Lo que se dibuja encima del video: una foto o el último cuadro del video anterior. */
private sealed interface Picture {
    val key: String

    class Photo(val item: BackgroundItem) : Picture {
        override val key get() = item.id
    }

    class Frame(val bitmap: ImageBitmap) : Picture {
        override val key = "cuadro-${System.identityHashCode(bitmap)}"
    }
}

/**
 * El video va abajo y encima las fotos, que se funden entre sí; para pasar a un video se
 * desvanecen en cuanto el video tiene su primera imagen. De video a video se congela el último
 * cuadro encima, así nunca se ve negro y solo hay un reproductor a la vez: dos videos 4K juntos
 * ahogan a la tele.
 */
@Composable
private fun BackgroundMedia(item: BackgroundItem, loop: Boolean, onVideoEnded: (BackgroundItem) -> Unit, modifier: Modifier) {
    var below by remember { mutableStateOf<Picture?>(null) }
    var top by remember { mutableStateOf<Picture?>(null) }
    var video by remember { mutableStateOf<BackgroundItem?>(null) }
    var surface by remember { mutableStateOf<TextureView?>(null) }
    // El fondo pedido ahora: una foto que termina de cargar tarde no debe quitar un video pedido después.
    var wanted by remember { mutableStateOf(item.id) }
    // reveal: aparición de la foto de arriba sobre la de abajo. cover: las fotos encima del video.
    val reveal = remember { Animatable(1f) }
    val cover = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(item.id) {
        wanted = item.id
        if (item.isVideo) {
            if (item.id == video?.id) return@LaunchedEffect
            // Si ya se veía un video, su último cuadro tapa el cambio.
            if (top == null) top = surface?.grabFrame()?.let(Picture::Frame)
            below = null
            reveal.snapTo(1f)
            cover.snapTo(1f)
            video = item
        } else {
            if ((top as? Picture.Photo)?.item?.id == item.id) return@LaunchedEffect
            val nothingShown = top == null && video == null
            // Primero se cambia la capa y luego se oculta: si fuera al revés, la de abajo parpadearía.
            below = top
            top = Picture.Photo(item)
            reveal.snapTo(if (nothingShown) 1f else 0f)
            cover.snapTo(1f)
        }
    }
    fun onPhotoReady(picture: Picture) {
        if (picture != top || picture.key != wanted || reveal.isRunning) return
        scope.launch {
            reveal.animateTo(1f, tween(CROSSFADE_MS, easing = FastOutSlowInEasing))
            below = null
            video = null
        }
    }
    fun onVideoReady() {
        if (cover.value == 0f || cover.isRunning) return
        scope.launch {
            cover.animateTo(0f, tween(CROSSFADE_MS, easing = FastOutSlowInEasing))
            below = null
            top = null
        }
    }

    Box(modifier) {
        VideoPlane(
            file = video?.file,
            surface = surface,
            loop = loop,
            onSurface = { surface = it },
            onReady = ::onVideoReady,
            onEnded = { video?.let(onVideoEnded) },
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = cover.value }) {
            for (picture in listOfNotNull(below, top)) {
                key(picture.key) {
                    val layerModifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (picture == top) reveal.value else 1f }
                    when (picture) {
                        is Picture.Photo -> PhotoLayer(picture.item.file, onReady = { onPhotoReady(picture) }, modifier = layerModifier)
                        is Picture.Frame -> Image(picture.bitmap, null, contentScale = ContentScale.FillBounds, modifier = layerModifier)
                    }
                }
            }
        }
    }
}

/**
 * Video sin sonido. El reproductor se suelta al abrir otra app (la tele del cuarto tiene
 * 1.8 GB de RAM) o al dejar de verse, y se crea de nuevo al volver. Si el video no se puede leer, se salta.
 */
@OptIn(UnstableApi::class)
@Composable
private fun VideoPlane(
    file: File?,
    surface: TextureView?,
    loop: Boolean,
    onSurface: (TextureView) -> Unit,
    onReady: () -> Unit,
    onEnded: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val ready by rememberUpdatedState(onReady)
    val ended by rememberUpdatedState(onEnded)
    var player by remember { mutableStateOf<ExoPlayer?>(null) }

    LifecycleStartEffect(file, surface) {
        if (file == null || surface == null) return@LifecycleStartEffect onStopOrDispose {}
        val p = ExoPlayer.Builder(context).setLoadControl(smallBuffer()).build().apply {
            volume = 0f
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() = ready()
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) ended()
                }
                override fun onPlayerError(error: PlaybackException) = ended()
            })
            setVideoTextureView(surface)
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
            playWhenReady = true
        }
        player = p
        onStopOrDispose {
            p.release()
            player = null
        }
    }
    LaunchedEffect(player, loop) { player?.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF }

    // TextureView y no SurfaceView: en la Hisense de la sala la capa de video aparte (SurfaceView)
    // activa el procesamiento de imagen de la tele y todo el sistema va más lento (medido 30-oct-2026).
    AndroidView(factory = { TextureView(it).also(onSurface) }, modifier = modifier)
}

/**
 * El video es un archivo local: basta con leer unos segundos adelante. Por defecto ExoPlayer guarda
 * hasta 50 s (unos 130 MB en 4K) y la tele de la sala, con 2.5 GB, se queda sin memoria.
 */
@OptIn(UnstableApi::class)
private fun smallBuffer() = DefaultLoadControl.Builder()
    .setBufferDurationsMs(2_000, 4_000, 500, 1_000)
    .setTargetBufferBytes(24 * 1024 * 1024)
    .setPrioritizeTimeOverSizeThresholds(false)
    .build()

/** Copia lo que muestra el video ahora. Devuelve null si todavía no tiene imagen. */
private fun TextureView.grabFrame(): ImageBitmap? = if (isAvailable) bitmap?.asImageBitmap() else null

/** Foto propia (o el degradado de anochecer si `file` es null o no se pudo leer) con acercamiento lento. */
@Composable
private fun PhotoLayer(file: File?, onReady: () -> Unit, modifier: Modifier) {
    val window = LocalWindowInfo.current.containerSize
    val ready by rememberUpdatedState(onReady)
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var loaded by remember { mutableStateOf(file == null) }
    LaunchedEffect(file) {
        if (file != null) {
            bitmap = decodePhoto(file, (maxOf(window.width, window.height) * MAX_PHOTO_ZOOM).toInt())
        }
        loaded = true
        ready()
    }
    if (!loaded) return
    val decoded = bitmap
    val painter = if (decoded != null) remember(decoded) { BitmapPainter(decoded) } else painterResource(R.drawable.fondo_anochecer)
    KenBurnsPhoto(painter, periodMs = 50_000, modifier)
}

@Composable
private fun KenBurnsPhoto(painter: Painter, periodMs: Int, modifier: Modifier) {
    // Un solo acercamiento y la foto se queda quieta: así la tele no repinta la pantalla sin parar.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(periodMs, easing = KenBurnsEasing)) }
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.graphicsLayer {
            val p = progress.value
            val s = 1f + .07f * p
            scaleX = s
            scaleY = s
            translationX = -size.width * .01f * p
            translationY = size.height * .01f * p
        },
    )
}

/**
 * La foto se lee al ancho que de verdad ocupa con el acercamiento ([targetWidth]): se ve nítida
 * y no se carga entera (una de 8K ocuparía 130 MB en memoria). Más chica que eso, se deja igual.
 */
private suspend fun decodePhoto(file: File, targetWidth: Int): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
            val width = info.size.width
            if (targetWidth in 1 until width) decoder.setTargetSize(targetWidth, info.size.height * targetWidth / width)
        }.asImageBitmap()
    }.getOrNull()
}
