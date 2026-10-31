# Cómo instalar Mi TV en tu tele (paso a paso)

*English version: [INSTALL.md](INSTALL.md)*

Funciona en teles con **Android TV o Google TV** (Android 10 o más nuevo): Hisense, TCL, Sony,
Philips, Chromecast con Google TV, Xiaomi Mi Box y otras. No sirve en Samsung (Tizen), LG (webOS)
ni Roku.

Son dos partes:

1. **Instalar la app** (10 minutos, solo con el control de la tele).
2. **Hacerla tu pantalla de inicio** (15 minutos, necesitas una computadora una sola vez).

Si solo haces la parte 1, Mi TV queda como una app más: la abres desde la lista de apps.

---

## Parte 1: instalar la app (sin computadora)

### 1. Instala "Downloader"
1. En la tele, abre **Google Play Store**.
2. Busca **Downloader** (el ícono naranja, de *AFTVnews*) e instálala.

### 2. Dale permiso para instalar apps
1. Ve a **Configuración** (el engrane) → **Apps** → **Seguridad y restricciones** →
   **Fuentes desconocidas**.
2. Activa **Downloader**.

El nombre del menú cambia un poco según la marca. Si no lo encuentras, abre Downloader e intenta
el paso 3: la tele te llevará sola a ese permiso.

### 3. Descarga Mi TV
1. Abre **Downloader**.
2. En el cuadro de texto escribe una de estas direcciones (con el teclado de la pantalla):
   - Diseño **sala** (estilo Apple TV, con tus fotos y videos de fondo):
     `github.com/DimitriMedNov/launcher-tv/releases/download/v0.1.0/mi-tv-sala.apk`
   - Diseño **ciudad** (cada app es un edificio, con modo noche y temporizador):
     `github.com/DimitriMedNov/launcher-tv/releases/download/v0.1.0/mi-tv-cuarto.apk`
3. Presiona **Go**. Cuando termine de descargar, elige **Instalar**.
4. Al final elige **Listo** (o **Abrir** para verla). Downloader te ofrecerá borrar el archivo
   descargado: puedes decir que sí.

Solo se puede tener uno de los dos diseños a la vez. Para cambiar, instala el otro encima.

---

## Parte 2: hacerla tu pantalla de inicio (con una computadora, una sola vez)

Google TV no deja elegir otra pantalla de inicio desde sus menús. Hay que darle una orden desde una
computadora con **adb**, una herramienta gratuita de Google que manda comandos a la tele por wifi.

### 1. Activa el modo de desarrollador en la tele
1. **Configuración** → **Sistema** → **Información**.
2. Baja hasta **Compilación del SO de Android TV** y presiona **OK 7 veces seguidas**.
   Saldrá el aviso "Ahora eres desarrollador".
3. Regresa a **Sistema** → **Opciones para desarrolladores** y activa **Depuración por USB**
   (en algunas teles también hay **Depuración por red**: actívala).

### 2. Anota la IP de la tele
**Configuración** → **Red e Internet** → toca tu red wifi. Anota la **dirección IP**
(algo como `192.168.1.25`).

### 3. Instala adb en la computadora
- **Windows:** descarga
  [SDK Platform-Tools para Windows](https://developer.android.com/tools/releases/platform-tools),
  descomprime el zip y abre la carpeta `platform-tools`. Haz clic en la barra de dirección de la
  carpeta, escribe `cmd` y presiona Enter: se abre una ventana negra en esa carpeta.
- **Mac:** descarga
  [SDK Platform-Tools para Mac](https://developer.android.com/tools/releases/platform-tools),
  descomprímelo, abre **Terminal** y escribe `cd ` (con espacio) y arrastra la carpeta
  `platform-tools` a la ventana; presiona Enter. En los comandos de abajo escribe `./adb` en vez de `adb`.
- **Linux:** instala el paquete `android-tools` (o `adb`) de tu distribución.

### 4. Conecta la computadora con la tele
La computadora y la tele deben estar en la **misma red wifi**. Escribe (cambiando la IP por la tuya):

```
adb connect 192.168.1.25:5555
```

La tele mostrará **"¿Permitir la depuración?"**: marca **Permitir siempre** y elige **Aceptar**.
Si sale "failed to authenticate", escribe el comando otra vez.

### 5. Pon Mi TV como pantalla de inicio

```
adb shell cmd package set-home-activity com.dimitrimednov.launchertv/.MainActivity
```

Presiona el botón **Home** del control: debe salir Mi TV.

**Si sigue saliendo la pantalla de Google TV**, desactívala (no se borra, se puede regresar):

```
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
```

### Opcional: que muestre lo que estás viendo o escuchando

```
adb shell cmd notification allow_listener com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.MediaListenerService
```

### Opcional (diseño ciudad): protector de pantalla y temporizador

```
adb shell settings put secure screensaver_components com.dimitrimednov.launchertv/.ui.cuarto.CityDream
adb shell settings put secure enabled_accessibility_services com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.SleepService
adb shell settings put secure accessibility_enabled 1
```

El temporizador usa un servicio de accesibilidad porque una app normal no puede apagar la tele.
No lee nada de la pantalla. Ojo: este comando reemplaza otros servicios de accesibilidad que ya
tuvieras activos.

Al terminar puedes apagar **Depuración por USB** en la tele.

---

## Regresar a como estaba

```
adb shell pm enable com.google.android.apps.tv.launcherx
adb uninstall com.dimitrimednov.launchertv
```

El primer comando solo hace falta si desactivaste la pantalla de Google TV. Al desinstalar Mi TV,
vuelve sola la pantalla de inicio de siempre. También puedes desinstalarla desde la tele:
**Configuración** → **Apps** → **Mi TV** → **Desinstalar**.

## Problemas comunes
- **"App no instalada":** ya tienes una versión con otra firma. Desinstala Mi TV y vuelve a instalar.
- **No aparece el clima:** la tele no tiene internet; el resto funciona igual.
- **El clima es de Progreso, Yucatán:** por ahora la ubicación viene fija en la app.
