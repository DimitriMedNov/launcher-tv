#!/usr/bin/env bash
# Prepara tus fotos y videos de fondo y los copia a la tele (o al emulador).
# Los archivos no se guardan en el repo: viven en la tele, en la carpeta de la app.
# Uso:
#   scripts/fondos.sh preparar carpeta1 [carpeta2 ...]   # fotos tal cual y videos a HEVC en ~/Videos/fondos-tv/tele
#   scripts/fondos.sh copiar [serial]                    # copia ~/Videos/fondos-tv/tele a la tele (por defecto el emulador)
#   scripts/fondos.sh borrar [serial]                    # quita los fondos de la tele; vuelve el degradado de anochecer
set -euo pipefail

APP=com.dimitrimednov.launchertv
DESTINO="/sdcard/Android/data/$APP/files/fondos"
LISTOS="${LISTOS:-$HOME/Videos/fondos-tv/tele}"
# Opcional: una línea por archivo, "nombre-original=nombre-en-la-tele" (sin extensión).
NOMBRES="${NOMBRES:-$HOME/Videos/fondos-tv/nombres.txt}"

# "gato-negro-moewalls-com.mp4" o "playa_5120x2880_xtrafondos.com.jpg" → "gato-negro"
limpiar_nombre() {
  local base="${1%.*}"
  base="${base%-moewalls-com}"
  base="$(sed -E 's/_[0-9]+x[0-9]+_xtrafondos\.com$//' <<<"$base")"
  if [ -f "$NOMBRES" ]; then
    local nuevo
    nuevo="$(awk -F= -v k="$base" '$1 == k { print $2; exit }' "$NOMBRES")"
    [ -n "$nuevo" ] && base="$nuevo"
  fi
  echo "$base"
}

case "${1:-}" in
  preparar)
    shift
    [ $# -gt 0 ] || { sed -n '4,7p' "$0"; exit 1; }
    mkdir -p "$LISTOS"
    for carpeta in "$@"; do
      for f in "$carpeta"/*; do
        [ -f "$f" ] || continue
        nombre="$(limpiar_nombre "$(basename "$f")")"
        ext="${f##*.}"; ext="${ext,,}"
        case "$ext" in
          mp4|mov|mkv|webm)
            # Se pasa a HEVC con la misma resolución y cuadros (calidad casi idéntica, CRF 17): el
            # chip de la tele lee HEVC 4K a 60 sin trabarse, y H.264 4K a 60 no siempre. Se quita el
            # audio y se pone el índice al principio para que empiece al instante.
            echo "Video: $nombre"
            ffmpeg -nostdin -loglevel error -y -i "$f" -map 0:v:0 -c:v libx265 -preset medium -crf 17 \
              -pix_fmt yuv420p -tag:v hvc1 -x265-params log-level=error -an \
              -movflags +faststart "$LISTOS/$nombre.mp4"
            ;;
          jpg|jpeg|png|webp)
            # La foto se copia tal cual; la app la lee al tamaño de la pantalla.
            echo "Foto:  $nombre"
            cp "$f" "$LISTOS/$nombre.${ext/jpeg/jpg}"
            ;;
        esac
      done
    done
    du -sh "$LISTOS"
    ;;
  copiar)
    serial="${2:-emulator-5554}"
    adb -s "$serial" shell mkdir -p "$DESTINO"
    adb -s "$serial" push "$LISTOS/." "$DESTINO/"
    echo "Copiados a $serial. Vuelve al inicio de la tele para verlos."
    ;;
  borrar)
    serial="${2:-emulator-5554}"
    adb -s "$serial" shell rm -rf "$DESTINO"
    ;;
  *)
    sed -n '4,7p' "$0"
    exit 1
    ;;
esac
