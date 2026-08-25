# MacroLens

MacroLens is a small, open-source Android magnifier focused on privacy, clarity, and everyday accessibility. It has no ads, accounts, analytics, or network permission.

## Features

- Live magnifier powered by CameraX, with front and rear cameras.
- Hardware-supported zoom shown as a live ratio, controlled by slider or pinch gesture.
- Tap-to-focus with a temporary focus reticle and a confirmation haptic on focus lock.
- Flashlight control when the device provides a flash unit (rear camera only).
- Freeze the current frame in memory without writing it to storage.
- Three color filters for the frozen frame — Normal, B&W, and Inverted — to support low-vision reading.
- Draggable horizontal reading line to keep your place while reading magnified text.
- Camera error feedback and retry action.
- Spanish, English, German, and Portuguese translations.
- Automatic device-language selection with English as the fallback.
- OLED-friendly black interface with glass-effect controls.
- MIT license, GitHub access, and Binance Pay donation information.

The app deliberately uses CameraX only. The maximum zoom depends on the camera capabilities exposed by Android and the device manufacturer. It does not attempt to reproduce proprietary camera processing from vendor camera applications.

## Screenshots

Screenshots will be added under `docs/screenshots/`.

## Technology

| Layer | Technology |
| --- | --- |
| Language | Kotlin 2.4.10 |
| UI | Jetpack Compose |
| Material | Material 3 |
| Camera | CameraX 1.6.1 |
| Lifecycle | AndroidX Lifecycle 2.11.0 |
| Build | Android Gradle Plugin 9.3.0 |
| Minimum Android | Android 11 (API 30) |
| Compile SDK | 37 |
| Target SDK | 36 |

## Project Structure

```text
MacroLens/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/macrolens/
│       │   │   ├── MainActivity.kt
│       │   │   └── ui/
│       │   │       ├── MagnifierScreen.kt
│       │   │       ├── MagnifierViewModel.kt
│       │   │       └── theme/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       └── values*/
│       ├── test/
│       └── androidTest/
├── docs/
├── gradle/libs.versions.toml
├── AGENTS.md
├── CONTRIBUTING.md
├── LICENCE.md
├── PRODUCT.md
└── README.md
```

## Requirements

- Android Studio with support for AGP 9.3.0.
- JDK 11 or newer.
- Android SDK platform 37.
- A physical Android device or emulator with at least one camera.

## Build and Install

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Run the unit tests with:

```bash
./gradlew :app:testDebugUnitTest
```

Run the Android tests on a connected device or emulator with:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Release Builds

Release signing reads credentials from `~/.gradle/gradle.properties` or environment variables. Never commit a keystore or passwords.

```properties
MACROLENS_KEYSTORE_PATH=/path/to/macrolens-release.keystore
MACROLENS_KEYSTORE_PASSWORD=your-password
MACROLENS_KEY_ALIAS=macrolens
MACROLENS_KEY_PASSWORD=your-password
```

Build a signed release when the keystore exists:

```bash
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

The release build enables R8 and resource shrinking. Verify the generated artifact on a physical device before distribution.

## Usage

1. Open MacroLens and grant camera access.
2. Adjust zoom with the slider or pinch gesture. The current ratio is shown above the slider.
3. Tap the preview to focus on a specific point. A short haptic confirms the focus lock.
4. Use the flashlight button (rear camera only) when additional light is needed.
5. Use the camera switch button to toggle between rear and front cameras for self-examination.
6. Tap the reading line button to show a draggable horizontal guide, useful for tracking your place while reading.
7. Freeze and resume the current frame with the pause button. While frozen, choose Normal, B&W, or Inverted to make the frame easier to read.
8. Open More options for the license, permissions, GitHub, or donation information.

## Privacy and Permissions

The app requests only camera access at runtime. The flashlight permission is declared for device compatibility. No Internet, storage, location, microphone, account, or tracking permission is used.

Frozen frames remain in memory and are not saved to disk. The color filters applied to a frozen frame run entirely on the device. The app contains no analytics, advertising SDK, or remote service.

## Donation

Donations are supported through Binance Pay.

**Binance Pay ID:** `371811579`

## Configuration

The application links and the freeze color matrices are defined in `app/src/main/java/com/example/macrolens/ui/MagnifierScreen.kt`. Camera state, focus handling, and feature toggles live in `app/src/main/java/com/example/macrolens/ui/MagnifierViewModel.kt`.

Translations are stored in:

- `app/src/main/res/values/strings.xml` for English and fallback text.
- `app/src/main/res/values-es/strings.xml` for Spanish.
- `app/src/main/res/values-de/strings.xml` for German.
- `app/src/main/res/values-pt/strings.xml` for Portuguese.

Android selects the closest matching device locale. English is used when no supported translation matches. When adding a new user-facing string, add it to all four files.

## Known Limitations

- Zoom is limited to the range reported by CameraX and the device camera HAL.
- The app does not use manufacturer-specific camera APIs or proprietary super-resolution.
- The front camera has no torch support and is selected only when the device exposes one.
- Color filters apply to the frozen frame only; the live preview is not filtered.
- Frozen frames are intentionally not persisted, exported, or shared.
- Video recording and image export are not included.

## Roadmap

- Optional image capture with explicit storage behavior.
- Improved focus and exposure feedback.
- Additional accessibility refinements.
- More device compatibility testing.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for build, testing, and design guidelines. The product intent and non-goals are documented in [PRODUCT.md](PRODUCT.md). The architecture and contribution conventions for AI-assisted work are described in [AGENTS.md](AGENTS.md).

## License

MacroLens is distributed under the MIT License. See [LICENCE.md](LICENCE.md).

Copyright (c) 2026 Gregory Oviedo.

## Credits

- CameraX, Jetpack Compose, Material 3, and AndroidX are provided by Google and the Android Open Source Project.
- The launcher icon is an original asset for MacroLens.
