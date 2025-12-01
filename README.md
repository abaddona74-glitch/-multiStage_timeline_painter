## Folder Structure

```
multiStage_timeline_painter/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/timelinepainter/
│   │       │   ├── MainActivity.kt       # Entry point
│   │       │   ├── TimelineScreen.kt     # Main UI & Canvas drawing logic
│   │       │   ├── TimelineState.kt      # Zoom/Pan state management
│   │       │   └── TimelineModels.kt     # Data classes & Sample data
│   │       └── res/                      # Resources (values, xml, etc.)
│   └── build.gradle.kts                  # App module configuration
├── gradle/
│   └── wrapper/                          # Gradle Wrapper files
├── build.gradle.kts                      # Project configuration
├── settings.gradle.kts                   # Module settings
└── README.md                             # Project documentation
```

## Demo Video

https://github.com/user-attachments/assets/2481aa78-41d6-40a6-ab52-b01e06551898



## How to Run

### Prerequisites
*   **Android Studio** (Ladybug or newer recommended)
*   **Android SDK** (minSdk 26, targetSdk 34)
*   **JDK 17** or newer

### Steps
1.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select **Open** and navigate to the project root directory.
    *   Wait for Gradle sync to complete. If prompted to generate a Gradle Wrapper, allow it.

2.  **Run the App:**
    *   Connect an Android device or create an Android Virtual Device (AVD).
    *   Click the green **Run** button (Shift+F10).

### Deploy Over Wi‑Fi (ADB)

If you prefer installing over your device's IP (after a one‑time USB setup):

1. One‑time (with USB plugged):
     - Enable Developer options and USB debugging on the phone.
     - Verify device:
         - `adb devices`
     - Switch to TCP/IP:
         - `adb tcpip 5555`
     - Find the phone IP (or use device Wi‑Fi settings):
         - `adb shell ip addr show wlan0`
     - Connect over Wi‑Fi:
         - `adb connect <PHONE_IP>:5555`

2. Build an APK (first time or after changes):
     - In Android Studio: Build → Build APK(s). The debug APK will be at `app\build\outputs\apk\debug\app-debug.apk`.

3. Install to the Wi‑Fi device:
     - Replace with your actual IP (example shown):
         - `adb -s 192.168.1.181:5555 install -r app\build\outputs\apk\debug\app-debug.apk`

4. Reconnect after device/PC reboot (if needed):
     - `adb connect <PHONE_IP>:5555`

Alternatively, use the provided Windows helper script:

- `scripts\install_over_ip.cmd <PHONE_IP> [port] [apk_path]`
    - Example:
        - `scripts\install_over_ip.cmd 192.168.1.181 5555 app\build\outputs\apk\debug\app-debug.apk`
    - If you omit the IP, the script will try to use the first connected `:5555` device from `adb devices`.

