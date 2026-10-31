# Launcher TV

Pantalla de inicio propia (launcher) para Android TV, estilo Apple TV, para las dos teles Hisense con Google TV de Dimitri. Reemplazará a Projectivy, el launcher que usan hoy. Proyecto personal y público para su portafolio (GitHub DimitriMedNov).

## Estado (30-oct-2026)
- App escrita en `app/` (ver README): compila, lint sin errores, probada en emulador Android TV 12 (`scripts/emulador.sh`, AVD en `~/.local/opt/android-avd`). Falta probarla en las teles.
- Diseño elegido: `diseno/Launcher TV v2 recreation/Launcher TV v5.dc.html` (barra lateral Inicio/Entradas/Ajustes, cuadrícula de apps de 6 columnas, foco de anillo, letra Manrope, fondo foto con Ken Burns). Se construye tal cual, con tarjetas de color con nombre.
- Se agrega aunque el diseño no lo trae: menú al mantener OK (Abrir, Mover, Ocultar, Información) y los 4 estados (cargando, sin internet, sin apps, error), con el mismo estilo.
- Fondos: fotos propias en calidad original y videos en su resolución original pasados a HEVC (lo autorizó Dimitri el 30-oct-2026; pesan la mitad y van algo más fluidos), copiados a la tele con `scripts/fondos.sh`. Inicio: fotos 5 min, videos hasta que terminan, al azar, con velo ligero (izquierda 55 %→0, abajo 0→45 %) para leer textos. Protector: empieza con video y alterna video/foto; fotos 90 s y videos repetidos hasta 90 s (sin cortar la vuelta); velo solo arriba y abajo.
- No abrir el emulador sin preguntar: pide unos 2 GB de RAM y ya se cerró todo por falta de memoria.
- Ajustes rápidos: Wifi, Imagen, Sonido y Temporizador abren la pantalla de ajustes de la tele (no se pueden cambiar desde una app).

## Documentos
- `docs/brief-desarrollo-launcher.md`: stack, fases (10 a 14 h), riesgos y datos de las teles. Es la fuente de verdad del plan.
- `docs/brief-claude-design-launcher.md`: lo que se le pidió a Claude Design (colores, tipografía, foco, área segura, pantallas).

## Stack decidido
- Kotlin + Jetpack Compose for TV (`androidx.tv:tv-material`), minSdk 29. Las teles tienen Android 11 (cuarto) y Android 12 (sala).
- Clima: Open-Meteo (sin API key), Progreso, Yucatán: lat 21.2822136, lon -89.6636637, °C.
- Letra Manrope (la del diseño elegido) incluida en la app. Las animaciones se hacen con springs y tweens de Compose. Las librerías web (React, Tailwind, Anime.js) no aplican aquí.
- Entorno (sin sudo): JDK 17 Temurin en `~/.local/opt/jdk-17` y Android SDK en `~/.local/opt/android-sdk` (platform 36, build-tools 36.0.0). Para compilar: `JAVA_HOME=~/.local/opt/jdk-17 ANDROID_HOME=~/.local/opt/android-sdk`.

## Teles (adb por wifi, puerto 5555, ya autorizado)
- Sala: IP local de la tele (ver `adb devices`), Android 12, con antena (Live TV = `com.vt.tv.tvcenter`). Ahí se prueba primero.
- Cuarto: IP local de la tele, Android 11, solo 1.8 GB de RAM: vigilar el rendimiento del video de fondo.
- Sala (30-oct-2026): Mi TV ya es la pantalla de inicio. Projectivy está desactivado (`pm disable-user`), no borrado; su servicio de accesibilidad está apagado. Aerial Views desinstalado y protector del sistema apagado (`screensaver_enabled 0`); `screen_off_timeout` subido de 60 s a 20 min para que salga el protector propio.
- Cuarto (30-oct-2026): rama `cuarto-batman` con el diseño "La ciudad" (`diseno/Launcher TV Batman/`). Mi TV es la pantalla de inicio y el protector del sistema (`CityDream`); Projectivy desactivado (`pm disable-user`), Aerial Views sigue instalado pero ya no es el protector. Falta activar el servicio de accesibilidad del temporizador y el permiso de notificaciones (comandos en el README). `screen_off_timeout` sigue en 60 s.
- La rama `main` es la sala y `cuarto-batman` el cuarto: no mezclar sin preguntar.

## Reglas
- Español, pasos literales y sin jerga sin explicar.
- Commits con autor DimitriMedNov. Nunca usar el correo de la cuenta de Claude.
- Antes de instalar algo o tocar las teles por adb: preguntar.
