# How to install Mi TV on your TV (step by step)

*Versión en español: [INSTALAR.md](INSTALAR.md)*

> The app itself is in Spanish.

Works on TVs with **Android TV or Google TV** (Android 10 or newer): Hisense, TCL, Sony, Philips,
Chromecast with Google TV, Xiaomi Mi Box and others. It does not work on Samsung (Tizen),
LG (webOS) or Roku.

There are two parts:

1. **Install the app** (10 minutes, using only the TV remote).
2. **Make it your home screen** (15 minutes, you need a computer once).

If you only do part 1, Mi TV is just another app: open it from your apps list.

---

## Part 1: install the app (no computer)

### 1. Install "Downloader"
1. On the TV, open **Google Play Store**.
2. Search for **Downloader** (the orange icon, by *AFTVnews*) and install it.

### 2. Allow it to install apps
1. Go to **Settings** (gear icon) → **Apps** → **Security & restrictions** → **Unknown sources**.
2. Turn on **Downloader**.

Menu names vary a little by brand. If you can't find it, open Downloader and try step 3: the TV
will take you to that permission.

### 3. Download Mi TV
1. Open **Downloader**.
2. In the text box, type one of these addresses (with the on-screen keyboard):
   - **Living room** design (Apple TV style, with your own photos and videos as background):
     `github.com/DimitriMedNov/launcher-tv/releases/download/v0.1.0/mi-tv-sala.apk`
   - **City** design (each app is a building, with night mode and sleep timer):
     `github.com/DimitriMedNov/launcher-tv/releases/download/v0.1.0/mi-tv-cuarto.apk`
3. Press **Go**. When the download finishes, choose **Install**.
4. Choose **Done** (or **Open** to see it). Downloader will offer to delete the downloaded file:
   you can say yes.

You can only have one of the two designs at a time. To switch, install the other one on top.

---

## Part 2: make it your home screen (with a computer, once)

Google TV doesn't let you pick another home screen from its menus. You have to send one command
from a computer with **adb**, a free Google tool that talks to the TV over wifi.

### 1. Turn on developer mode on the TV
1. **Settings** → **System** → **About**.
2. Scroll to **Android TV OS build** and press **OK 7 times in a row**. You'll see
   "You are now a developer".
3. Go back to **System** → **Developer options** and turn on **USB debugging**
   (some TVs also have **Network debugging**: turn it on too).

### 2. Write down the TV's IP address
**Settings** → **Network & Internet** → select your wifi network. Write down the **IP address**
(something like `192.168.1.25`).

### 3. Install adb on your computer
- **Windows:** download
  [SDK Platform-Tools for Windows](https://developer.android.com/tools/releases/platform-tools),
  unzip it and open the `platform-tools` folder. Click the folder's address bar, type `cmd` and
  press Enter: a black window opens in that folder.
- **Mac:** download
  [SDK Platform-Tools for Mac](https://developer.android.com/tools/releases/platform-tools),
  unzip it, open **Terminal**, type `cd ` (with a space), drag the `platform-tools` folder into the
  window and press Enter. In the commands below, type `./adb` instead of `adb`.
- **Linux:** install your distribution's `android-tools` (or `adb`) package.

### 4. Connect the computer to the TV
Both must be on the **same wifi network**. Type (with your TV's IP):

```
adb connect 192.168.1.25:5555
```

The TV will ask **"Allow debugging?"**: check **Always allow** and choose **OK**.
If you see "failed to authenticate", run the command again.

### 5. Set Mi TV as the home screen

```
adb shell cmd package set-home-activity com.dimitrimednov.launchertv/.MainActivity
```

Press the **Home** button on the remote: Mi TV should appear.

**If the Google TV home screen still shows up**, disable it (it isn't deleted and can be restored):

```
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
```

### Optional: show what you're watching or listening to

```
adb shell cmd notification allow_listener com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.MediaListenerService
```

### Optional (city design): screensaver and sleep timer

```
adb shell settings put secure screensaver_components com.dimitrimednov.launchertv/.ui.cuarto.CityDream
adb shell settings put secure enabled_accessibility_services com.dimitrimednov.launchertv/com.dimitrimednov.launchertv.data.SleepService
adb shell settings put secure accessibility_enabled 1
```

The sleep timer uses an accessibility service because a regular app can't turn off the TV. It
doesn't read anything on screen. Note: this command replaces any other accessibility services you
already had enabled.

When you're done, you can turn off **USB debugging** on the TV.

---

## Go back to how it was

```
adb shell pm enable com.google.android.apps.tv.launcherx
adb uninstall com.dimitrimednov.launchertv
```

The first command is only needed if you disabled the Google TV home screen. After uninstalling
Mi TV, your usual home screen comes back on its own. You can also uninstall it from the TV:
**Settings** → **Apps** → **Mi TV** → **Uninstall**.

## Common problems
- **"App not installed":** you already have a version with a different signature. Uninstall Mi TV
  and install again.
- **No weather:** the TV has no internet; everything else still works.
- **The weather is for Progreso, Yucatán (Mexico):** the location is fixed in the app for now.
