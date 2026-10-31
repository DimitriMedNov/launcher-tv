# Launcher propio para las teles Hisense: brief de desarrollo

Fecha: 26-oct-2026. Estado (30-oct-2026): diseño aún sin decidir, Claude Design está haciendo propuestas; el desarrollo empieza cuando se elija.

## Objetivo
Una app de Android TV propia que reemplace a Projectivy como pantalla de inicio en las dos teles, con estilo Apple TV (tvOS): video aéreo de fondo, íconos con efecto parallax, franja superior (Top Shelf) por app, clima a color, entradas HDMI/Antena y protector de pantalla propio. Proyecto personal público para el portafolio (GitHub DimitriMedNov).

## Diseño
- Brief inicial: `brief-claude-design-launcher.md` (esta carpeta).
- Ajustes pedidos después en Claude Design:
  1. Más moderno y fiel a tvOS: cristal translúcido, foco con parallax (escala 112 %, inclinación máx. 6°, brillo especular, "respiración" en reposo), transiciones encadenadas (Top Shelf con crossfade de 350 ms, Ken Burns en el video, entrada en cascada de 40 ms, paneles con resorte), microinteracciones, esqueletos de carga y movimiento reducido. Duraciones de 150 a 400 ms. Pide una hoja de movimiento con disparador, duración, curva y valores.
  2. Íconos del clima a color, estilo Apple (sol amarillo, nubes, gotas azules, rayo, luna de noche, calor, viento, neblina), con animación sutil y en SVG. Íconos de entradas según el aparato conectado (consola, decodificador, laptop, barra de sonido, antena), con punto verde de señal, "Sin señal" al 40 % y la opción "Renombrar y elegir ícono".
- Falta: guardar aquí el link o la exportación final de Claude Design (prototipo + hoja de estilos + hoja de movimiento).

## Stack
- Kotlin + Jetpack Compose for TV (`androidx.tv:tv-material`), minSdk 29 (Android 10). Las teles tienen Android 11 y 12.
- Clima: Open-Meteo (gratis, sin API key). Progreso, Yucatán: lat 21.2822136, lon -89.6636637, °C.
- Video aéreo: las mismas fuentes de Apple que usa Aerial Views.
- Letra: Inter (licencia libre), incluida en la app.
- Las librerías web de /viernes (Animate UI, Anime.js, Bencho) no aplican aquí. Su nivel de animación se replica con springs/tweens de Compose.

## Fases y tiempo estimado (10 a 14 h, unos 2 días)
1. **Preparar (30–45 min):** JDK 17 (`sudo pacman -S --needed jdk17-openjdk`, lo corre el usuario), Android SDK en `~/.local/opt` (unos 3 GB, sin sudo; pedir permiso antes), proyecto creado, tokens de diseño y la hoja de movimiento pasados a código.
2. **Primera versión en la tele de la sala (3–4 h):** Inicio con video aéreo, hora, clima a color, fila de apps en orden con parallax, abrir apps con transición, registrado como launcher (HOME). Projectivy se queda como respaldo.
3. **Resto del diseño (4–6 h):** Top Shelf por app, fila de entradas + panel rápido, menú de app (Mover/Ocultar/Info), los 4 estados (cargando, sin internet, sin apps, error).
4. **Protector y pulido (3–4 h):** protector propio (DreamService), pruebas en las dos teles (vigilar la RAM del cuarto), README y repositorio en GitHub.

## Orden de apps
Live TV (`com.vt.tv.tvcenter`, solo en la sala) → Apple TV → Netflix → Disney+ → YouTube → Prime Video → Spotify → Kick → Aerial Views → el resto.

## Confirmado en la tele de la sala por adb (30-oct-2026)
- "Seguir viendo" permanente: NO. La lista Watch Next está vacía y leer la de otras apps exige `ACCESS_ALL_EPG_DATA` (signature|privileged). Decisión: el Top Shelf muestra "lo último que viste" leyendo la sesión de medios de la app (Netflix, Disney+ y Prime la publican con título y posición) mientras siga viva; si no hay, video aéreo.
- Señal de HDMI: SÍ. Los HDMI son entradas estándar (`com.vt.source.external/.hdmi.HdmiTvInputService/HW2..HW5`, HW2 = HDMI 1). `TvInputManager.getInputState` pasa de 1 (sin señal) a 0 (con señal) al prender el Xbox en HDMI 1. `cable_connection_status` no sirve: Hisense siempre reporta 2.
- Canción de Spotify: SÍ. `com.spotify.tv.android` publica sesión de medios con título, artista y estado. La app necesita acceso a notificaciones, que se concede por adb.
- Imagen, Sonido y Temporizador: solo se abre la pantalla de ajustes (`android.settings.DISPLAY_SETTINGS`, `android.settings.SOUND_SETTINGS`, `android.settings.TIMER_SETTINGS`); no hay forma pública de cambiar el valor desde la app.

## Riesgos por confirmar en la tele
- Vista previa en vivo de HDMI y antena (TvView): depende de que el servicio de Hisense acepte sesiones de otras apps. Se prueba con una app mínima. Si no, la entrada solo cambia de fuente.
- Canales de antena: la lista no se pudo leer desde adb; se prueba con la misma app mínima.
- Tele del cuarto (1.8 GB de RAM): los fondos son video 4K a 60 cuadros en HEVC. Falta probar si va fluido; si no, dejar solo fotos en esa tele (basta con no copiarle videos).

## Cómo quedaron los fondos (30-oct-2026)
En lugar del video aéreo de Apple, el fondo usa fotos y videos propios que se copian a la tele con `scripts/fondos.sh` (carpeta `Android/data/com.dimitrimednov.launchertv/files/fondos`, fuera del repo).
- Inicio: fotos 5 min y videos hasta que terminan, mezclados al azar, con un velo ligero a la izquierda y abajo para que se lean los textos (más oscuro con la barra lateral abierta).
- Protector: el mismo fondo; siempre empieza con un video y alterna video, foto, video… Las fotos duran 90 s y cada video se repite hasta cumplir 90 s, terminando la vuelta en curso. Sale tras 90 s sin tocar el control o con Atrás dos veces en la barra lateral.
- Videos en su resolución y cuadros originales, pasados a HEVC (CRF 17): pesan la mitad y en la sala la navegación con video subió de 34 a 37 cuadros/s (30-oct-2026). Fotos en calidad original leídas al tamaño de la pantalla.
- Rendimiento medido en la sala (30-oct-2026): el video va en `TextureView`, no en `SurfaceView` (con la capa de video aparte la tele activa su procesamiento de imagen y todo el sistema va más lento). Probar siempre el APK release: el debug va mucho más lento. Ninguna animación es infinita (`rememberSettlingLoop`): clima, EN VIVO, ecualizador y señal dan unas vueltas y se quedan quietos, y el Ken Burns hace un solo acercamiento; en reposo con foto la app no repinta (1 cuadro en 10 s).
- Reproductor: Media3 ExoPlayer 1.11.1, uno solo a la vez; se suelta al abrir otra app.
- Aerial Views se desinstaló de la sala (30-oct-2026) porque salía encima al minuto; el único protector es el de Mi TV (a los 90 s sin tocar el control).
- Al primer arranque nunca se repite el fondo con el que empezó la vez anterior.

## Datos de las teles
- Sala: Android 12, con antena.
- Cuarto: Android 11.
- adb por wifi en el puerto 5555 (ya autorizado en las dos).
- Android 12 bloquea `adb backup/restore` y Projectivy no importó su `.plbackup`. Toda configuración se hace por UI (uiautomator + teclas) o por la propia app.

## Definición de terminado
1. El proyecto compila sin errores (`./gradlew assembleRelease`) y sin advertencias nuevas de lint.
2. Instalado en las dos teles y funcionando como pantalla de inicio.
3. Probado con el control: navegación completa, Atrás, apertura de apps, entradas y protector.
4. Revertible: Projectivy sigue instalado y se vuelve a él con un comando.
