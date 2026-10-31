# Auditoría: app contra el diseño de Claude Design

Fecha: 30-oct-2026. Diseño: `diseno/Launcher TV v2 recreation/Launcher TV v5.dc.html`.

## Cómo se hizo
1. **Diseño:** se abrió el HTML en Brave sin ventana (headless) a 1920×1080 y se le mandaron las mismas teclas que al control. Salen 8 pantallas.
2. **App:** se abrió en el emulador de Android TV 12 en **modo demo**, con los mismos datos de ejemplo del diseño (18 apps, 5 entradas, "Noticias del mediodía", 27° Cielo claro). Se activa con:
   ```bash
   adb shell am start -S -n com.dimitrimednov.launchertv/.MainActivity --ez demo true
   ```
3. Se compararon lado a lado (diseño a la izquierda, app a la derecha; las imágenes se quitaron del repo público porque mostraban una foto de fondo sin licencia para redistribuir). También se midió en qué fila y columna de píxeles cae cada texto.

**Límite:** se compararon imágenes fijas. Las animaciones (inclinación, rebotes, entrada escalonada) usan las mismas duraciones y curvas del código del diseño, pero no se compararon cuadro por cuadro.

## Resultado
| Pantalla | Comparación | Coincide |
|---|---|---|
| Inicio (cuadrícula con foco) | `inicio.jpg` | ✅ |
| Franja de arriba con foco en botones | `hero.jpg` | ✅ |
| Barra lateral abierta | `barra.jpg` | ✅ |
| Netflix "Seguir viendo" | `netflix.jpg` | ✅ |
| Spotify "Sonando" | `spotify.jpg` | ✅ |
| Entradas | `entradas.jpg` | ✅ con 1 diferencia de datos (ver abajo) |
| Ajustes rápidos | `ajustes.jpg` | ✅ con 2 diferencias de datos (ver abajo) |
| Protector | `protector.jpg` | ✅ (en la captura del diseño los textos no alcanzaron a aparecer; se comparó contra su código) |

**Medidas** (fila de píxeles donde empieza y termina cada texto, diseño → app):

| Pantalla | Píldora de la app | Título | Botones / tarjetas |
|---|---|---|---|
| Franja con foco | 172–187 → 171–187 | 230–295 → 230–295 | 428–505 → 428–505 |
| Inicio | 169–183 → 170–183 | 216–278 → 217–278 | 418–441 → 418–440 |
| Entradas | 165–183 → 166–183 | 219–290 → 220–290 | 503–569 → 503–569 |
| Ajustes (título) | — | 163–262 → 162–262 | — |

En horizontal, "Noticias del mediodía" va de 222 a 1085 px en el diseño y de 221 a 1084 px en la app.

## Diferencias encontradas y corregidas
| # | Diferencia | Causa | Arreglo |
|---|---|---|---|
| 1 | Títulos grandes 17 px más abajo y lo de debajo 34 px más abajo | El CSS usa `line-height: .98` / `1` / `.9` y deja que la letra se salga de su renglón; Compose no lo hace con un solo renglón | `Modifier.cssLineHeight()` en `Common.kt` |
| 2 | Textos un poco más anchos y con otro interlineado | `tv-material` pone separación entre letras e interlineado propios | Estilo base igual al CSS en `DesignCanvas.kt` |
| 3 | Íconos de Live TV y del logo "Mi TV" en contorno | Material Icons no trae el `live_tv` / `tv` relleno de Material Symbols | Íconos propios en `ui/icons/SymbolIcons.kt` |
| 4 | Barra lateral con todos los íconos rellenos | El diseño rellena solo la sección actual | Íconos en contorno para las demás (`Section.outlinedIcon`) |
| 5 | Flecha del valor en todas las filas de Ajustes | El diseño la muestra solo en la fila enfocada | Aparece con el foco |
| 6 | Texto del clima pálido sobre el cielo claro | El diseño usa desenfoque detrás de la píldora | Fondo un poco más oscuro (ver límites) |
| 7 | La hora salía en negro | Al agregar sombra o números tabulares se perdía el estilo base | Se combina con el estilo actual |

## Diferencias que se quedan
| Diferencia | Por qué |
|---|---|
| Sin desenfoque (blur) en píldoras, barra lateral y paneles | Android no puede difuminar lo que hay detrás de un elemento, y la tele del cuarto tiene Android 11. Se usa fondo semitransparente; la píldora del clima queda un poco más oscura que en el diseño. |
| Ajustes muestra "Abrir" en lugar de "Estándar", "Estéreo", "Apagado" | Hisense no deja que otra app lea ni cambie el modo de imagen, sonido o temporizador. Cada opción abre la pantalla de la tele. |
| Entradas: "HDMI 1 · Con señal" en lugar de "HDMI 1 · 1080p · 60 Hz" | Android no da la resolución de la entrada a otras apps. |
| Live TV real: "Televisión en vivo" en lugar del programa actual | La guía de canales de Hisense no se pudo leer (ver `brief-desarrollo-launcher.md`). En modo demo sí se ve "Noticias del mediodía". |
| "Seguir viendo" solo mientras la app sigue abierta | Netflix y las demás no publican esa lista para otras apps. |
| Sin la píldora de atajos de teclas arriba a la izquierda (cambio posterior, 30-oct-2026) | Lo pidió Dimitri: con el control se entiende solo. |
| Fila "Apps ocultas" en Ajustes | Se agregó a propósito: menú al mantener OK, modo Mover y apps ocultas no estaban en el diseño. |
| Sin tarjeta de FOX One | No está instalada en la tele. Aparece solo en modo demo. |
| Velo más ligero que el del diseño y sin brillo de color (cambio posterior a la auditoría) | Lo pidió Dimitri: que sus fotos y videos se vean, pero que los textos se lean. |
| Fondo con fotos y videos propios que rotan, en lugar de una sola foto | Se agregó después de la auditoría. Ver "Fondos propios" en el README. |
