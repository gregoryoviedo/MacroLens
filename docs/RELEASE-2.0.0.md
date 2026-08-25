# MacroLens 2.0.0

This release rebrands the project from **Lupa Free** to **MacroLens** and ships four new accessibility-focused features. See the [migration notes](#migration-from-1-0-0) if you are upgrading from a 1.x build.

## Highlights

- **New identity.** New app name, new launcher icon, and a `MacroLens` namespace. The signing key is the same as 1.0.0, so future updates will install cleanly on top of this build once the package name on the device matches.
- **Front and rear cameras.** A new toggle in the top bar switches between the rear (default) and front cameras. The torch button is automatically hidden on the front camera.
- **Reading line.** A draggable horizontal guide helps you keep your place while reading magnified text.
- **Color filters for the frozen frame.** Three chips — *Normal*, *B&W*, *Inverted* — appear whenever a frame is frozen, so you can push contrast without leaving the app.
- **Focus haptic.** A short vibration confirms when tap-to-focus locks, which is useful when the focus reticle is small or briefly out of view.

## What's in the box

- Live magnifier powered by CameraX (rear or front camera).
- Hardware-supported zoom shown as a live ratio, via slider or pinch.
- Tap-to-focus with a temporary reticle and a lock-confirmation haptic.
- Flashlight control on the rear camera when the device exposes a flash unit.
- In-memory frame freeze with three color filters.
- Draggable reading line for low-vision reading.
- Camera error banner with retry.
- Translations: English, Spanish, German, Portuguese.
- OLED-friendly black interface with glass-effect controls.
- No network, no analytics, no ads, no storage, no background.

## Breaking changes

- **Application ID changed.** From `com.example.lupafree` to `com.example.macrolens`. The Google Play listing and the on-device package are now `com.example.macrolens`. Existing 1.x installs are seen by Android as a different app and will not auto-update — they must be uninstalled first.
- **Signing environment variables renamed.** `LUPAFREE_KEYSTORE_*` is now `MACROLENS_KEYSTORE_*`. The keystore file can stay the same; only the property names changed.

## Upgrade notes

- For the Play Console, this is a major version bump (1.0.0 → 2.0.0) because the application ID changed.
- For sideloaded 1.x APKs, uninstall the old build before installing this one. The signing certificate is identical, so once the package name matches, future updates will install in place.
- Contributors: read the new `PRODUCT.md` and `AGENTS.md` files in the repository. The rebrand touched every layer of the project, including the package directory and the signing config.

## What's next

- Optional image capture with explicit storage behavior.
- Improved focus and exposure feedback.
- Additional accessibility refinements.
- More device compatibility testing.

## Downloads

- `MacroLens-2.0.0.apk` — universal release APK, signed, R8 + resource shrinking enabled. About 5.4 MB.
- `app-release.aab` (build locally with `./gradlew :app:bundleRelease`) — App Bundle for the Play Console.

## Verification

- Built with Android Gradle Plugin 9.3.0 and Kotlin 2.4.10.
- Min SDK 30 (Android 11). Target SDK 36.
- Signed with the v2 APK signature scheme. Certificate SHA-256: `BE:7F:0D:71:E4:15:54:1D:17:E0:65:86:B1:8B:78:3B:C2:49:F5:FC:EB:0C:C5:95:C7:C2:8C:F9:18:F5:D6:E4`.
- `./gradlew :app:testDebugUnitTest` passes (6 tests).
- `./gradlew :app:assembleRelease` produces a working APK.
