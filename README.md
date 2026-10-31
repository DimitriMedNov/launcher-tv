# Mi TV: launcher para Android TV

Pantalla de inicio propia (launcher) para dos teles Hisense con Google TV, con estilo Apple TV:
barra lateral, cuadrícula de apps con colores de marca, foco con anillo e inclinación, clima a color,
entradas HDMI con detección de señal, fondos propios (fotos y videos en calidad original que rotan)
y protector de pantalla. Reemplaza a Projectivy.

Proyecto personal. Kotlin + Jetpack Compose for TV.

**¿Quieres instalarla en tu tele?** Sigue la [guía paso a paso](INSTALAR.md) ([English](INSTALL.md)). No necesitas saber programar.

**Estado (30-oct-2026):** la app compila, pasa lint (0 errores) y funciona en el emulador de
Android TV 12 (navegación, menú, Mover, ocultar, protector y sin internet probados; usa unos 46 MB de RAM).
Desde el 30-oct-2026 es la pantalla de inicio de la tele de la sala, con fondos propios. La tele del cuarto usa otro diseño, en la rama `cuarto-batman`.

Las capturas de esta versión (sala) se están rehaciendo con el fondo nuevo. La comparación
contra el diseño está en [`docs/auditoria-diseno.md`](docs/auditoria-diseno.md). Las capturas de la
versión del cuarto están más abajo, en "Tele del cuarto".

---

## Qué hace

| Parte | Qué muestra | De dónde salen los datos |
|---|---|---|
| **Barra lateral "Mi TV"** | Inicio, Entradas y Ajustes. Angosta con íconos; se abre al darle foco. | — |
| **Inicio: franja de arriba** | Nombre de la app enfocada, título grande, progreso y botones. | Sesión de medios de la app (ver abajo). |
| **Inicio: "Tus apps"** | Cuadrícula de 6 columnas. La tarjeta enfocada crece al 106 %, gana un anillo blanco, se inclina 7° hacia la flecha y cruza un destello. | Apps de tele instaladas (`LEANBACK_LAUNCHER`). |
| **Entradas** | HDMI 1 a 4 y Antena, con punto verde cuando hay señal. | `TvInputManager` de Android. |
| **Ajustes rápidos** | Wifi, Imagen, Sonido, Temporizador, Apps ocultas, Ajustes de Android y pronóstico de 3 días. | Open-Meteo y estado del wifi. |
| **Arriba a la derecha** | Clima a color ("27° Cielo claro") y hora ("4:12 p. m."). | Open-Meteo, Progreso, Yucatán. |
| **Fondo** | Tus fotos y videos, mezclados y en orden al azar, con un velo oscuro ligero a la izquierda y abajo para que se lean títulos y apps. Se oscurece más al abrir la barra lateral. | Carpeta `fondos` de la app en la tele (ver [Fondos propios](#fondos-propios)). |
| **Protector** | El mismo fondo a pantalla completa, con hora grande, fecha, clima y nombre del fondo. Sale tras 90 s sin tocar el control. | — |

### Franja de arriba según la app
- **Spotify:** canción, artista, álbum, tiempo y botón **Pausar/Reproducir** que controla la música sin salir del inicio.
- **Netflix, Disney+, Prime y otras:** "Seguir viendo" con título y "Quedan N min", **solo mientras la app siga abierta en segundo plano**.
- **Live TV:** "Ahora · EN VIVO" y botón "Ver canal".
- **Cualquier otra app:** su nombre y "Presiona OK para abrir".

### Control remoto
| Tecla | Acción |
|---|---|
| Flechas | Mover el foco. Izquierda desde la primera columna abre la barra lateral. |
| OK | Abrir la app, cambiar de entrada o abrir el ajuste. |
| OK mantenido (0.6 s) | Menú de la app (Abrir, Mover, Ocultar, Información) o de la entrada (Ver, Elegir ícono, Información). |
| Atrás | Regresa un nivel. En la barra lateral, dos veces seguidas abre el protector. |
| Home | Vuelve al principio del inicio. |

**Modo Mover:** la tarjeta se levanta y se mueve con las 4 flechas; OK guarda el orden y Atrás cancela.

### Fondos propios
| | Inicio | Protector |
|---|---|---|
| Foto | 5 min, con acercamiento lento (Ken Burns) | 90 s, con acercamiento lento |
| Video | Hasta que termina, sin sonido | Se repite hasta cumplir 90 s y termina la vuelta en curso (nunca se corta), sin sonido |
| Orden | Al azar, mezclado | Siempre empieza con un video y luego alterna foto, video, foto… |

- Fotos y videos se mezclan y el orden se revuelve cada vez que abre la app, así que arranca con uno al azar.
- Entre un fondo y otro hay un fundido de 1.4 s. El nuevo aparece solo cuando ya tiene su primera imagen, para que nunca se vea negro.
- Si hay un solo fondo y es video, se repite sin parar. Sin fondos propios se ve un degradado de anochecer que trae la app.
- Videos en su resolución y cuadros originales (4K a 60) pasados a HEVC (pesan la mitad y la tele los mueve algo mejor); fotos en su calidad original, leídas al tamaño de la pantalla de la tele. Solo hay un reproductor a la vez, con unos segundos de búfer.
- Inicio y protector comparten la misma capa de fondo: nunca hay dos videos reproduciéndose a la vez. El video se detiene al abrir otra app.

### Estados del inicio
1. **Cargando:** esqueletos con la forma de la franja y de la cuadrícula.
2. **Sin internet:** aviso "Sin conexión" con el botón "Abrir ajustes de wifi".
3. **Sin apps visibles:** "No hay apps en el inicio" con el botón "Agregar apps" (abre Apps ocultas).
4. **Error de clima:** se oculta solo el bloque del clima y el pronóstico; lo demás sigue igual.

---

## Lo que Android TV no permite (confirmado por adb en la tele, 30-oct-2026)

| Idea del diseño | Resultado | Cómo quedó |
|---|---|---|
| "Seguir viendo" permanente de Netflix/Disney+ | No se puede. La lista Watch Next está vacía y leer la de otras apps exige `ACCESS_ALL_EPG_DATA`, un permiso solo para apps del sistema. | Se usa la sesión de medios mientras la app está abierta. |
| Apagar el wifi o cambiar el modo de Imagen/Sonido desde el panel | Desde Android 10 una app no puede apagar el wifi, y Hisense no da una forma pública de cambiar esos modos. | Cada opción abre la pantalla de ajustes correspondiente de la tele. |
| Desenfoque de los paneles | Compose solo lo hace en Android 12+, y la tele del cuarto tiene Android 11. | Paneles con fondo semitransparente, sin desenfoque. |
| Vista previa en vivo de HDMI/antena | **Sin confirmar.** Depende de que Hisense deje a otras apps usar su reproductor. | Por ahora la entrada solo cambia de fuente. |

Detalle técnico en [`docs/brief-desarrollo-launcher.md`](docs/brief-desarrollo-launcher.md).

---

## Cómo está hecho

```
app/src/main/kotlin/com/dimitrimednov/launchertv/
├── MainActivity.kt            Pantalla de inicio (responde al botón Home)
├── data/                      Lectura de datos, sin interfaz
│   ├── AppsRepository.kt        Apps instaladas, orden y ocultas (SharedPreferences)
│   ├── Brands.kt                Colores y orden de cada app conocida
│   ├── WeatherRepository.kt     Clima de Open-Meteo y códigos WMO → ícono y texto
│   ├── InputsRepository.kt      Entradas HDMI/antena y su señal
│   ├── NowPlayingRepository.kt  Qué reproduce cada app (sesiones de medios)
│   ├── BackgroundsRepository.kt Fotos y videos de la carpeta `fondos` de la tele
│   └── DeviceStatus.kt          Internet y wifi
└── ui/
    ├── theme/                 Colores, letra, curvas de movimiento y lienzo 1920×1080
    ├── icons/                 Íconos de clima y de entradas dibujados con Canvas
    └── home/
        ├── NavState.kt          Estado del foco (zonas, paneles, modo Mover)
        ├── Navigator.kt         Máquina de estados del control remoto
        ├── RemoteKeys.kt        Teclas → Navigator (incluye OK mantenido)
        ├── HomeViewModel.kt     Une datos y navegación; abre apps, entradas y ajustes
        ├── HomeScreen.kt        Pantalla completa
        ├── BackgroundRotation.kt Qué fondo toca y cuánto dura (inicio y protector)
        ├── Background.kt         Capa de fondo: foto o video, con fundido entre ellos
        └── Rail, Hero, AppGrid, AppTile, InputsSection, SettingsSection,
            SidePanel, Overlays, Screensaver, TopBar, Background, Common
```

- **La lógica no vive en la interfaz.** `Navigator` decide qué pasa con cada tecla y produce un `NavState` nuevo. Las pantallas solo dibujan ese estado.
- **Las medidas se copian tal cual del diseño.** `DesignCanvas` hace que 1 dp sea 1 px del lienzo de 1920×1080 de Claude Design, sin importar la densidad de la tele.
- **Pensado para la tele de 1.8 GB de RAM.** Un solo reproductor de video a la vez, que se suelta al abrir otra app; sin desenfoque; y las posiciones animadas se aplican al dibujar, sin recalcular la pantalla en cada cuadro. El video 4K todavía no se ha probado en esa tele.
- **Pocas librerías.** AndroidX, Compose, `tv-material`, los íconos de Material y Media3 ExoPlayer (el reproductor de video oficial de Android) para los fondos. El clima se lee con `HttpURLConnection` y `org.json`.

| Pieza | Versión |
|---|---|
| Android Gradle Plugin | 9.5.0 |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |
| `androidx.tv:tv-material` | 1.1.0 |
| `androidx.media3:media3-exoplayer` | 1.11.1 |
| compileSdk / targetSdk / minSdk | 37.2 / 36 / 29 |
| Gradle | 9.8.2 (wrapper incluido) |

---

## Compilar

Requisitos: JDK 17 y Android SDK con `platforms;android-37.2` y `build-tools;37.0.0`.

```bash
cd app
export JAVA_HOME=~/.local/opt/jdk-17
export ANDROID_HOME=~/.local/opt/android-sdk
./gradlew assembleRelease      # APK en app/build/outputs/apk/release/
./gradlew lintDebug            # reporte en app/build/reports/lint-results-debug.html
```

El release se firma con la llave de depuración (proyecto personal, se instala por adb).

## Probar en la laptop (emulador de Android TV)

Necesita KVM (aceleración por hardware de Linux) y unos 1.4 GB para el emulador y la imagen de Android TV 12.
El emulador pide unos 2 GB de RAM: si la laptop tiene poca libre, se puede quedar sin memoria y cerrarse todo. Mejor probar directo en la tele.

```bash
scripts/emulador.sh crear      # una sola vez
scripts/emulador.sh abrir      # abre la ventana y espera a que arranque
scripts/emulador.sh instalar   # compila, instala y abre la app
scripts/emulador.sh captura inicio   # guarda capturas/inicio.png
```

Para ver la app con los mismos datos de ejemplo del diseño (18 apps, entradas, Spotify sonando):
`adb shell am start -S -n com.dimitrimednov.launchertv/.MainActivity --ez demo true`

En la ventana del emulador: **flechas** = mover, **Enter** = OK (mantenerlo abre el menú), **Esc** = Atrás.
El emulador no tiene Netflix, Spotify ni entradas HDMI. Esas partes solo se prueban en la tele.

## Instalar en la tele

La tele debe tener la depuración por red activada (Ajustes → Sistema → Opciones de desarrollador).

```bash
adb connect <IP-de-la-tele>:5555
adb install -r app/build/outputs/apk/release/launcher-tv-release.apk

# Permiso para leer qué suena (Spotify, Netflix…). Una sola vez:
adb shell cmd notification allow_listener com.dimitrimednov.launchertv/.data.MediaListenerService

# Probarla sin cambiar la pantalla de inicio:
adb shell am start -n com.dimitrimednov.launchertv/.MainActivity
```

### Copiar tus fondos a la tele

Las fotos y videos no se guardan en el repo: viven en la tele, en `/sdcard/Android/data/com.dimitrimednov.launchertv/files/fondos`.
Formatos: fotos `jpg`, `png`, `webp`; videos `mp4`, `mkv`, `webm`. El nombre del archivo es el que muestra el protector
(`gato-mirando-la-lluvia.mp4` → "Gato mirando la lluvia").

```bash
# 1. Juntar tus carpetas en ~/Videos/fondos-tv/tele sin bajar la calidad
#    (al video solo se le quita el audio, sin volver a comprimir)
scripts/fondos.sh preparar ~/Descargas/fondos-anime ~/Imágenes/paisajes

# 2. Copiarlos a la tele (sin serial va al emulador)
scripts/fondos.sh copiar <IP-de-la-tele>:5555

# Quitarlos y volver al degradado de anochecer
scripts/fondos.sh borrar <IP-de-la-tele>:5555
```

Requiere `ffmpeg`. Para cambiar el nombre que se ve, agrega líneas `nombre-original=nombre-nuevo` en `~/Videos/fondos-tv/nombres.txt`.

### Ponerla como pantalla de inicio (y volver a Projectivy)

```bash
# Usar Mi TV al presionar Home
adb shell cmd package set-home-activity com.dimitrimednov.launchertv/.MainActivity

# Regresar a Projectivy (en la sala está desactivado: primero hay que activarlo)
adb shell pm enable com.spocky.projengmenu
adb shell cmd package set-home-activity com.spocky.projengmenu/.ui.home.MainActivity
```

Projectivy también atrapa el botón Home con un servicio de accesibilidad. En la sala se resolvió así (30-oct-2026):

```bash
adb shell settings delete secure enabled_accessibility_services   # apaga el servicio de Projectivy
adb shell pm disable-user --user 0 com.spocky.projengmenu         # lo desactiva sin borrarlo
adb shell settings put secure screensaver_enabled 0               # sin protector del sistema (Aerial Views)
adb shell settings put system screen_off_timeout 1200000          # 20 min: deja salir el protector de Mi TV
```

---

## Tele del cuarto: "La ciudad" (rama `cuarto-batman`)

Diseño propio para la tele del cuarto (`diseno/Launcher TV Batman/Launcher Cuarto v6.dc.html`):
una ciudad de noche donde cada app es un edificio. El edificio enfocado se abre y muestra lo que
estás viendo o escuchando; a su lado sale la luna. De vez en cuando pasa una tormenta (lluvia,
relámpagos y murciélagos) que dura unos segundos y se detiene, para que la tele no repinte sin parar.

| Inicio | Menú radial (mantener OK) |
|---|---|
| ![Inicio](capturas/cuarto/inicio.jpg) | ![Menú radial](capturas/cuarto/menu-radial.jpg) |
| **Ajustes** | **Entradas** |
| ![Ajustes](capturas/cuarto/ajustes.jpg) | ![Entradas](capturas/cuarto/entradas.jpg) |
| **Información de la app** | **Protector** |
| ![Información](capturas/cuarto/informacion.jpg) | ![Protector](capturas/cuarto/protector.jpg) |

- **Inicio:** izquierda/derecha recorre los edificios, abajo va a Abrir / Más info, izquierda en el
  primero abre el menú. Mantener OK abre el menú radial: Abrir (arriba), Mover (derecha),
  Ocultar (abajo, se confirma manteniendo OK hasta llenar el anillo) e Información (izquierda).
- **Ajustes:** accesos rápidos, temporizador para dormir (15, 30, 60 y 90 min, con cuenta regresiva
  arriba a la derecha), modo noche (Automático de 10 p. m. a 7 a. m., Siempre o Nunca; cambia a
  tonos ámbar con un fundido de 2 s), pronóstico de 5 días como gráfica y apps ocultas.
- **Entradas:** HDMI 1, 2 y 3 con su señal. Sin antena.
- **Protector:** sale a los 2 min sin tocar nada dentro del launcher y, como protector del sistema,
  también encima de cualquier app. La hora cambia de lugar cada minuto con un fundido.
- **Rendimiento medido en la tele del cuarto (30-oct-2026):** al navegar, 29 de 1,240 cuadros
  llegaron tarde (2 %); en reposo no se dibuja nada; la app usa unos 87 MB de memoria.

### Instalar en la tele del cuarto

```bash
export ANDROID_SERIAL=IP-DE-TU-TELE:5555
adb install -r app/build/outputs/apk/release/launcher-tv-release.apk

# Pantalla de inicio (Projectivy queda desactivado, no borrado)
adb shell pm disable-user --user 0 com.spocky.projengmenu
adb shell cmd package set-home-activity com.dimitrimednov.launchertv/.MainActivity

# Protector del sistema: la ciudad (antes era Aerial Views)
adb shell settings put secure screensaver_components com.dimitrimednov.launchertv/.ui.cuarto.CityDream

# Temporizador para dormir: permiso para apagar la tele (servicio de accesibilidad)
adb shell settings put secure enabled_accessibility_services com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.SleepService
adb shell settings put secure accessibility_enabled 1

# Para ver qué suena o qué estabas viendo en el panel
adb shell cmd notification allow_listener com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.MediaListenerService

# Volver a como estaba
adb shell pm enable com.spocky.projengmenu
adb shell cmd package set-home-activity com.spocky.projengmenu/.ui.home.MainActivity
adb shell settings put secure screensaver_components com.neilturner.aerialviews/.ui.screensaver.DreamActivity
adb shell settings put secure enabled_accessibility_services com.spocky.projengmenu/com.spocky.projengmenu.services.ProjectivyAccessibilityService
```

Una app normal no puede apagar la tele. El temporizador lo hace con un servicio de accesibilidad
que, al terminar la cuenta, pide a Android "bloquear la pantalla", lo que manda la tele a reposo.
No lee nada de la pantalla.

---

## Carpetas

| Carpeta | Contenido |
|---|---|
| `app/` | Proyecto Android. |
| `scripts/emulador.sh` | Crear, abrir e instalar en el emulador de Android TV. |
| `scripts/fondos.sh` | Preparar tus fotos y videos y copiarlos a la tele. |
| `capturas/` | Capturas que usa este README. |
| `diseno/Launcher TV v2 recreation/` | Diseño elegido de Claude Design. Abre `Launcher TV v5.dc.html` en el navegador para navegarlo con las flechas. |
| `docs/brief-desarrollo-launcher.md` | Plan, fases, riesgos y lo confirmado en las teles. |
| `docs/brief-claude-design-launcher.md` | Lo que se le pidió a Claude Design. |
| `docs/auditoria-diseno.md` | Comparación píxel a píxel de la app contra el diseño, con lo que se corrigió y lo que no se puede igualar. |

## Licencia

Código bajo licencia [MIT](LICENSE). Las letras incluidas tienen su propia licencia (SIL Open Font License 1.1), en [`licencias/`](licencias/).

## Créditos
- **Letras:** [Manrope](https://github.com/googlefonts/manrope) (sala) y [Barlow y Barlow Condensed](https://github.com/jpt/barlow) (cuarto), licencia SIL Open Font License 1.1.
- **Clima:** [Open-Meteo](https://open-meteo.com/), datos CC BY 4.0.
- **Íconos:** Material Icons (Apache 2.0). Los de clima y entradas se dibujaron a partir del diseño.
- **Fondo incluido:** un degradado de anochecer hecho para este proyecto (`app/src/main/res/drawable-nodpi/fondo_anochecer.jpg`).
- **Fondos propios:** no vienen en el repo. Cada quien copia los suyos a su tele con `scripts/fondos.sh`.
- Nombres y colores de Netflix, Disney+, Spotify y las demás marcas pertenecen a sus dueños. Se usan solo para reconocer cada app en el inicio.
