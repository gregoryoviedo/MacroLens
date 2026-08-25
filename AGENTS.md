# MacroLens — Agents

A working guide for humans and AI coding agents contributing to MacroLens. Read this before changing code. When something here conflicts with what the code appears to do, the code is the source of truth and this document is stale — please update it.

## What this project is

A single-screen Android magnifier. Open source, no network, no analytics, no storage, no background. Product intent and the explicit list of non-goals live in [PRODUCT.md](PRODUCT.md). The user's guide and build instructions are in [README.md](README.md). Contribution process is in [CONTRIBUTING.md](CONTRIBUTING.md).

## Quick start

```bash
./gradlew :app:assembleDebug          # build a debug APK
./gradlew :app:installDebug           # install on a connected device
./gradlew :app:testDebugUnitTest      # run JVM unit tests
./gradlew :app:connectedDebugAndroidTest  # run instrumented tests on a device
```

JDK 11 or newer is required. Android SDK 37 must be installed. The `local.properties` file points at the SDK and is not tracked.

## Architecture

The app is intentionally flat. There are three runtime classes:

```
MainActivity
└── MagnifierScreen (Composable, holds UI state like sheet/dialog visibility)
    └── MagnifierViewModel (single ViewModel — camera + feature state)
```

`MainActivity` only configures the window and hosts the single Compose root. It does not own state.

`MagnifierViewModel` exposes a single `StateFlow<MagnifierUiState>`. All UI state — zoom, torch, frozen frame, freeze filter, focus indicator, reading line, selected camera — lives in `MagnifierUiState`. UI calls intent functions (`onZoomChange`, `toggleFreeze`, `toggleCamera`, …); the ViewModel updates the state; Compose recomposes.

The camera is bound once per `PreviewView` instance. CameraX unbinds happen in `onCleared` and before every rebind. Re-binds happen when the user switches front/back camera or taps **Retry** on the error banner.

### State additions

When adding a new feature flag, follow this pattern:

1. Add a field with a default value to `MagnifierUiState`. Default values keep the data class safe to construct in tests.
2. Add a `fun setX(...)` or `fun toggleX()` on the ViewModel. Keep it pure: update state, then optionally call a side-effecting method (`bindCamera`, `toggleTorch`, …).
3. Render the new affordance in `MagnifierScreen` (or a new private composable in the same file).
4. Add a string in all four `values*/strings.xml` files. Do not hard-code user-facing text.
5. Add a unit test in `app/src/test/.../MagnifierViewModelTest.kt` that exercises the state transition.

### File map

| Concern | File |
| --- | --- |
| Compose theme (colors, typography, dark mode) | `app/src/main/java/com/example/macrolens/ui/theme/` |
| Camera state and intent functions | `app/src/main/java/com/example/macrolens/ui/MagnifierViewModel.kt` |
| All Compose UI, including overlays and dialogs | `app/src/main/java/com/example/macrolens/ui/MagnifierScreen.kt` |
| Launcher icon (adaptive) | `app/src/main/res/mipmap-anydpi/ic_launcher.xml` |
| Launcher icon legacy bitmaps | `app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher{,_round,_foreground}.png` |
| Launcher icon background color | `app/src/main/res/drawable/ic_launcher_background.xml` |
| Monochrome silhouette for Android 13+ themed icons | `app/src/main/res/drawable/ic_launcher_monochrome.png` |
| All user-facing text (en, es, de, pt) | `app/src/main/res/values{,-es,-de,-pt}/strings.xml` |
| Window theme, status/nav bar styling | `app/src/main/res/values/themes.xml` |
| Release signing credentials (env var names) | `app/build.gradle.kts` |
| ProGuard / R8 keep rules | `app/proguard-rules.pro` |
| Unit tests | `app/src/test/java/com/example/macrolens/MagnifierViewModelTest.kt` |

## Conventions

- **Kotlin style.** Default Kotlin official style. The `kotlin.code.style=official` line in `gradle.properties` is intentional.
- **No comments in code.** The code is short enough to read. Commit messages and these docs carry the rationale. (Existing file-level and section comments in `proguard-rules.pro` and the colors file are fine because they document non-obvious rules.)
- **Compose state.** `remember { mutableStateOf(...) }` for view-local UI flags (dialog visibility, sheet visibility, permission denial counter). Everything that survives a recomposition and reflects behavior of the camera goes in the ViewModel.
- **Strings.** Every visible string lives in `strings.xml` and has all four translations. The exception is the `Binance Pay ID: 371811579` literal in `DonationDialog` — that is a data value, not a label.
- **Icons.** The project depends on `androidx.compose.material.icons.extended`, so all Material icons are available. Use `Icons.Filled.*` for primary actions, `Icons.AutoMirrored.Filled.*` for icons that need RTL handling.
- **Glass surfaces.** The three private constants at the top of `MagnifierScreen.kt` — `GlassBackground`, `GlassBorderColor`, `GlassBorder` — are the only allowed control colors. New floating controls should use `GlassFab` (or extend it) and new chip-like controls should use `FilterChip` (or a similar helper) to stay visually consistent.
- **Conventional commits.** `feat:` for new behavior, `fix:` for bugs, `build:` for build/R8/keystore work, `style:` for cosmetic non-behavior changes, `refactor:` for internal restructuring, `docs:` for documentation only, `chore:` for tooling and housekeeping. Use the imperative mood. Add a body when the change is more than a one-liner.

## Things that are easy to get wrong

- **The package directory must match the namespace.** The Kotlin source lives at `app/src/main/java/com/example/macrolens/...` and the `namespace`/`applicationId` in `app/build.gradle.kts` is `com.example.macrolens`. If you move a file, the `package` declaration has to move with it. The `git mv` command preserves history across the move.
- **The release signing block reads environment variables or `~/.gradle/gradle.properties`.** It does not read the project's `gradle.properties`. The block is a no-op when the keystore is missing, so the unsigned debug build is unaffected.
- **`MagnifierViewModel` cannot bind a camera from a unit test.** The `ProcessCameraProvider` future needs a real `Context`. Keep ViewModel logic that does not need a camera as plain state transitions and put them in `setX`/`toggleX` methods so they can be unit-tested without instrumentation.
- **The `bindCamera` short-circuit checks the bound `LifecycleOwner` and `PreviewView`.** When you change the camera selector (`useFrontCamera`), the same callback path is followed. `toggleCamera` updates the flag and then calls `bindCamera` again so the rebind is always with the fresh selector.
- **The frozen frame filter is applied at the composable layer, not in the camera pipeline.** It only affects the bitmap shown by `FreezeOverlay`. Do not try to apply a `ColorFilter` to the `PreviewView` — it does not have one and adding one would require dropping to `ImageAnalysis` + `SurfaceTexture`, which is a large change.
- **The front camera has no torch.** `bindCamera` forces `isTorchOn = false` and `hasFlashUnit = false` when the front camera is active, and `toggleTorch` is a no-op in that case. Do not surface a torch button for the front camera.
- **`HapticFeedbackConstants.CONFIRM` requires API 30.** `minSdk` is 30, so this is safe. If `minSdk` is ever lowered, switch to `LONG_PRESS` or gate the call.
- **`local.properties` is generated by Android Studio and contains an absolute SDK path.** Do not edit it. Do not commit it (it is already in `.gitignore`).
- **`.idea/workspace.xml` is IDE state.** It will be rewritten on the next save in Android Studio. The non-historical references (VCS URL, module name, Play dynamic filter appId) are kept in sync as a courtesy, but the file is not authoritative.

## What "done" looks like

Before opening a pull request, all of the following should be true:

- `./gradlew :app:testDebugUnitTest` passes.
- `./gradlew :app:assembleDebug` produces a working APK.
- The README, PRODUCT, AGENTS, and CONTRIBUTING files still describe the project accurately. If you added a feature, update the features list in `README.md` and check the non-goals in `PRODUCT.md`.
- New user-facing strings are translated in all four `values*` folders.
- New state additions have a unit test in `MagnifierViewModelTest`.
- The commit message uses a conventional prefix and describes both the user-visible change and the files that moved.
