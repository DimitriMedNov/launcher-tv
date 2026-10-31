#!/usr/bin/env bash
# Prueba el launcher en un emulador de Android TV 12 en la laptop.
# Uso:
#   scripts/emulador.sh crear      # una sola vez: crea la tele virtual "tv12" (1920x1080, 2 GB)
#   scripts/emulador.sh abrir      # abre la ventana del emulador y espera a que arranque
#   scripts/emulador.sh instalar   # compila el release, lo instala y abre la app
#   scripts/emulador.sh captura nombre   # guarda capturas/nombre.png
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-$HOME/.local/opt/jdk-17}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/.local/opt/android-sdk}"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.local/opt/android-avd}"
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
APP=com.dimitrimednov.launchertv
SERIAL=emulator-5554
IMAGEN="system-images;android-31;android-tv;x86"

case "${1:-}" in
  crear)
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" emulator "$IMAGEN"
    mkdir -p "$ANDROID_AVD_HOME"
    echo no | "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd -n tv12 -k "$IMAGEN" -d tv_1080p --force
    cfg="$ANDROID_AVD_HOME/tv12.avd/config.ini"
    sed -i -e 's/^hw.ramSize=.*/hw.ramSize=2048/' -e 's/^hw.keyboard=.*/hw.keyboard=yes/' "$cfg"
    ;;
  abrir)
    "$ANDROID_HOME/emulator/emulator" -avd tv12 -no-snapshot -no-boot-anim -gpu host -no-audio >/dev/null 2>&1 &
    adb -s "$SERIAL" wait-for-device shell 'while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 2; done'
    echo "Emulador listo."
    ;;
  instalar)
    (cd "$RAIZ/app" && ./gradlew assembleRelease -q)
    adb -s "$SERIAL" install -r "$RAIZ/app/build/outputs/apk/release/launcher-tv-release.apk"
    adb -s "$SERIAL" shell cmd notification allow_listener "$APP/.data.MediaListenerService"
    adb -s "$SERIAL" shell am start -n "$APP/.MainActivity"
    ;;
  captura)
    mkdir -p "$RAIZ/capturas"
    adb -s "$SERIAL" exec-out screencap -p > "$RAIZ/capturas/${2:?Falta el nombre}.png"
    echo "Guardada en capturas/$2.png"
    ;;
  *)
    sed -n '2,8p' "$0"
    exit 1
    ;;
esac
