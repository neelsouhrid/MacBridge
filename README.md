# MacBridge 🌉
*A seamless, native bridge to control your Mac directly from your Android phone.*

MacBridge consists of two parts: a lightweight macOS menu bar server, and a sleek Android companion app. Together, they allow you to remotely control your Mac's core functions over your local Wi-Fi network.

## ✨ Features
*   **App & Window Management**: See all running apps, switch to specific windows, launch new apps, and force-quit them.
*   **System Controls**: Adjust your Mac's screen brightness and system volume natively.
*   **Power & Audio**: Put your Mac to sleep or instantly mute the audio.
*   **Universal Search**: Search through all installed applications on your Mac and launch them remotely.
*   **Sleek UI**: The Mac app lives quietly in your menu bar with real-time command logging, while the Android app features a beautiful, OLED-friendly dark mode interface.

---

## 📥 Installation Guide

### 1. The macOS Menu Bar App
Because Mac apps are technically "folders", they must be downloaded as a `.zip` file to preserve their internal structure.
1. Go to the **[Releases](https://github.com/neelsouhrid/MacBridge/releases)** page on this repository.
2. Download the latest `MacBridge-Mac.zip` file.
3. Double-click the `.zip` file to extract the `MacBridge` app.
4. Drag and drop the extracted `MacBridge` app into your Mac's **Applications** folder.
5. Open it! It will appear as an icon in your top-right menu bar.
*(Note: Upon first launch, macOS will ask you to grant Accessibility permissions so it can read window titles and control system settings).*

### 2. The Android Companion App
1. Go to the **[Releases](https://github.com/neelsouhrid/MacBridge/releases)** page.
2. Download the latest `MacBridge.apk` file to your phone.
3. Open the file on your Android device to install it (you may need to allow "Install from Unknown Sources" in your Android settings).
4. Make sure your phone is connected to the **same Wi-Fi network** as your Mac.
5. Open the Android app, enter the IP address and Port shown in your Mac's menu bar, and hit Connect!

---

## 🛠 Building from Source
If you prefer to compile the project yourself:

**For Mac:**
1. Open `MacBridge.xcodeproj` in Xcode 14+.
2. Ensure you have the [Swifter](https://github.com/httpswift/swifter) package added.
3. Select your Mac as the destination and press `Cmd + R` to run.

**For Android:**
1. Open the Android project folder in Android Studio.
2. Sync Gradle to fetch dependencies (Retrofit, Coroutines, Jetpack Compose).
3. Build and run on your physical Android device.


## Mac IMAGE
<img width="358" height="439" alt="Screenshot 2026-05-18 at 5 20 29 PM" src="https://github.com/user-attachments/assets/f702c7e2-3589-4534-9385-aaa998f7c3ce" />

## Android IMAGES
<p align="left">
  <img width="270" height="560" alt="1001429227" src="https://github.com/user-attachments/assets/3616e2c6-52f2-415a-8f3c-b14b2edb07e1" />
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <img width="270" height="555" alt="1001429228" src="https://github.com/user-attachments/assets/bb89bbdf-6c4c-4225-bb89-7dde74a1f378" />
</p>
