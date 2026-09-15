# PAUSE Keyboard — Implementation Plan

## Status: Planning Complete | Environment: No Android SDK available locally | Build: GitHub Actions only

## Goal

Production Android soft-keyboard (InputMethodService) in Kotlin using Jetpack Compose, Material 3, Coroutines, Hilt DI, clean modular architecture. Single APK with IME service + companion app. EN (QWERTY) + AR (RTL) with language switching, full key handling, PAUSE intervention hooks, and privacy-by-design.

## Architecture

```
pause-keyboard/
├── build.gradle.kts                  # Root: plugin versions
├── settings.gradle.kts               # Module declarations
├── gradle.properties                 # JVM + Compose flags
├── gradle/libs.versions.toml         # Version catalog
├── gradle/wrapper/gradle-wrapper.*   # Wrapper (jar downloaded via gradle wrapper task)
├── buildSrc/src/main/kotlin/com/pause/build/AndroidConfig.kt  # Shared constants
├── shared/                           # Library: models, interfaces, key codes
├── intelligence/                     # Library: local signal collection + pause detection
├── keyboard/                         # Library: IME service + Compose keyboard UI
├── app/                              # Application: onboarding, settings, dashboard
└── .github/workflows/ci.yml          # CI/CD
```

### Dependency graph (arrows = depends-on)

```
app → keyboard → shared
app → intelligence → shared
keyboard → intelligence → shared
```

### Key decisions

| Decision | Value |
|---|---|
| APK structure | Single APK (IME + companion app in `:app`) |
| minSdk / targetSdk | 21 / 35 |
| Kotlin | 2.0.21 |
| AGP | 8.5.2 |
| Compose compiler | 1.7.4 (Bill of Materials 2024.10.01) |
| DI | Hilt 2.52 |
| Settings persistence | DataStore Preferences (shared across processes) |
| State management | StateFlow + ViewModel (app) / Service-owned state holder (IME) |
| Testing | JUnit 5 (unit), Compose UI test (instrumented) |
| Gradle | 8.9 |

## Module Details

### 1. `:shared` (Library) — Common Models & Interfaces

**build.gradle.kts**: `com.android.library` + `kotlin-android`. No Compose/Hilt needed (pure Kotlin + Parcelable).

**Files:**

| File | Lines | Content |
|---|---|---|
| `Language.kt` | ~20 | `enum class Language(val code: String, val isRtl: Boolean) { ENGLISH, ARABIC }` with `fromCode()`, `next()` helpers |
| `KeyCodes.kt` | ~20 | Negative-value key code constants: SHIFT=-1, CAPS_LOCK=-2, BACKSPACE=-3, ENTER=-4, SPACE=-5, etc. |
| `KeyType.kt` | ~25 | `enum class KeyType { LETTER, FUNCTION, SYSTEM, MODIFIER, SHIFT, BACKSPACE, ENTER, SPACE, LANGUAGE, SYMBOL_TOGGLE, ARROW_LEFT, ARROW_RIGHT, ARROW_UP, ARROW_DOWN, DOT, COMMA, UNKNOWN }` |
| `KeySpec.kt` | ~40 | `@Parcelize data class KeySpec(code, label, type, widthRatio, longPressCodes, longPressLabels, requiresShift, capsLockAffect, rtlAdjust)` |
| `KeyboardDefinition.kt` | ~20 | `@Parcelize data class KeyboardDefinition(name, language, mode, rows, edgePaddingRatio, bottomEdgePaddingRatio)` |
| `KeyboardMode.kt` | ~12 | `enum class KeyboardMode { ALPHA, SYMBOLS, NUMBERS }` |
| `ShiftState.kt` | ~35 | `sealed interface ShiftState { object Lower, Upper, CapsLock }` with `next()`, `toggleCaps()`, `isUpperCase()` |
| `TypingMetrics.kt` | ~25 | `data class TypingMetrics(...)` — 15+ fields (WPM, pauses, deletions per word, rewrites per char, punctuation intensity, caps usage ratio, message length, inter-key latency, backspace long-presses, shift streak, impulsivity score, session start/duration, total counts). **No raw text.** |
| `InterventionType.kt` | ~12 | `enum class InterventionType { BREATHING_PROMPT, SUBTLE_HIGHLIGHT, MINIMAL_TIP }` |
| `InterventionProposal.kt` | ~20 | `@Parcelize data class InterventionProposal(id, type, priority, content, durationMs)` |
| `IntelligenceObserver.kt` | ~35 | Interface: `onKeyEvent()`, `onPauseStart()`, `onResumeFromPause()`, `onShiftStateChanged()`, `onLanguageChanged()`, `onModeChanged()`, `submitMetrics()`, `getMessageLength()` |
| `KeyboardSettings.kt` | ~35 | `@Parcelize data class KeyboardSettings(...)` — 13 fields with defaults. Plus `SettingsRanges` object for validation. |
| `Result.kt` | ~20 | `sealed class Result<T>` with `Success`, `Error` |
| `AndroidManifest.xml` | ~5 | Empty manifest for library |

### 2. `:intelligence` (Library) — Local Signal Engine

**build.gradle.kts**: `com.android.library` + Hilt + KSP + coroutines. Depends on `:shared`.

**Key classes:**

#### `TypingSignalCollector.kt`
- Implements `IntelligenceObserver` (Hilt `@Singleton`)
- Maintains `ArrayDeque<RawKeyEvent>` (rolling buffer, max 500)
- `RawKeyEvent` internal class: `code, isDeletion, isModifier, isLongPress, timestamp` — NO labels stored
- Counters: `backspaceLongPresss`, `shiftStreak`, `punctuationCount`, `wordCount`, `messageLength`
- `computeMetrics()` delegates to `MetricsAggregator`
- `reset()` clears all in-memory state

#### `MetricsAggregator.kt`
- `object` with pure function `compute(events: ArrayDeque<RawKeyEvent>, collector): TypingMetrics`
- Computes: inter-key latencies, pause durations, deletion ratio, rewrite patterns, punctuation intensity, caps usage, WPM, impulsivity score
- **Impulsivity formula**: `(latencyComponent * 0.5f + deletionComponent * 0.5f)` clamped to [0, 1]
- No I/O, fully testable

#### `PauseDetector.kt`
- `object` with `evaluate(metrics: TypingMetrics): InterventionProposal?`
- Rules:
  1. Rapid deletions (>25% deletion ratio AND >=5 deletions OR >=2 backspace long-presses) → priority 3
  2. High impulsivity (>0.7) + long pauses (>3s) + fast typing (<80ms latency) → priority 2
  3. Typing fatigue (session >30s, pauses >2s, WPM >30) → priority 1
- `resolveContent(proposal: InterventionProposal): String` — returns neutral text ("Take a breath", "You're doing great", "Pause?")

#### `InterventionScheduler.kt`
- Hilt `@Singleton`
- Takes `settingsProvider: () -> KeyboardSettings`
- `MutableStateFlow<InterventionProposal?>` exposed as `currentProposal`
- `post(metrics: TypingMetrics): Boolean` — evaluates, applies cooldown + max-per-session + dedup
- `clear()` and `resetSession()` methods

#### `LocalIntelligenceEngine.kt`
- Hilt `@Singleton`, orchestrator
- Holds `TypingSignalCollector`, `InterventionScheduler`
- Exposes `signalCollector: IntelligenceObserver` (for keyboard to call)
- Exposes `interventions: StateFlow<InterventionProposal?>` (for keyboard to observe)
- `onSessionEnd()` calls `collector.reset()` + `scheduler.resetSession()`

### 3. `:keyboard` (Library) — IME Service + Compose UI

**build.gradle.kts**: `com.android.library` + Compose + Hilt + KSP. Depends on `:shared`, `:intelligence`.

**AndroidManifest.xml**: Declares `PauseInputMethodService` as a service.

#### Core Service

| File | Responsibility |
|---|---|
| `PauseInputMethodService.kt` | Extends `InputMethodService()`. Overrides `onCreate()`, `onCreateInputView()`, `onStartInput()`, `onDestroy()`, `onKeyDown()`/`onKeyUp()`. Holds `KeyboardState`. Creates `ComposeView` with `ViewCompositionStrategy`. |
| `KeyboardState.kt` | State holder (not ViewModel). MutableStateFlow-backed. Manages `Language`, `ShiftState`, `KeyboardMode`. Methods: `pressKey()`, `cycleLanguage()`, `toggleShift()`, `handleBackspace()`, `switchMode()`, `moveCursor()`, `getLength()`. Calls `intelligence.signalCollector.onKeyEvent()` for each press. |

#### Compose UI

| File | Responsibility |
|---|---|
| `compose/PauseKeyboard.kt` | Root Composable. Observes `KeyboardState` (via StateFlow). Renders `KeyboardLayout` + `InterventionOverlay`. |
| `compose/KeyComposable.kt` | Single key rendering with `Button`/`Box`. Click handling, ripple, long-press detection via `pointerInput`. |
| `compose/KeyboardRow.kt` | `Row` of keys with `weight()` sizing. Reverses order for RTL. |
| `compose/KeyboardLayout.kt` | Maps `KeyboardDefinition.rows` to `KeyboardRow`s. Responsive width distribution. |
| `compose/LongPressPopup.kt` | Popup menu for long-press alternates. |
| `compose/InterventionOverlay.kt` | Subtle overlay over spacebar area. Animated pulse/glow. Tappable to dismiss. Never blocks. |
| `compose/modifiers/GestureModifiers.kt` | `pointerInput`-based modifier for tap/long-press detection. |

#### Layout Providers

| File | Layout |
|---|---|
| `EnglishKeyboardProvider.kt` | QWERTY: QWERTYUIOP / ASDFGHJKL / Shift ZXCVSBNM Enter / Space |
| `ArabicKeyboardProvider.kt` | Arabic letters in RTL order: برلماخص؟ / وعرستمغ / Shift ضصقرشتم، Enter / Space |
| `SymbolsKeyboardProvider.kt` | Punctuation row, emoji toggle, return-to-letters |
| `NumbersKeyboardProvider.kt` | 1-9 grid + 0 wide + symbols toggle |

#### Input Processing

| File | Responsibility |
|---|---|
| `input/InputEventProcessor.kt` | Maps key codes to `InputConnection` operations. `commitText()`, `deleteSurroundingText()`, `sendKeyEvent()`. |
| `input/BackspaceHandler.kt` | Single press deletes one char; long-press repeats with configurable delay/interval via `Handler.postDelayed`. |
| `input/ShiftHandler.kt` | Single tap → Upper (reverts on next key); double-tap within 300ms → CapsLock. |
| `input/LanguageSwitcher.kt` | Cycles EN↔AR, updates DataStore, calls `intelligence.onLanguageChanged()`. |
| `cursor/CursorController.kt` | Arrow keys call `InputConnection.setSelection()`. |

#### Feedback

| File | Responsibility |
|---|---|
| `feedback/HapticFeedback.kt` | Uses `Vibrator` with `VibrationEffect.createOneShot()`. Key-specific patterns. |
| `feedback/SoundPlayer.kt` | `SoundPool` for key press sounds. Respects `soundEnabled`. |
| `feedback/VibrationController.kt` | Reads `vibrationIntensity` setting. |

#### Settings

| File | Responsibility |
|---|---|
| `settings/SettingsManager.kt` | Reads `KeyboardSettings` from DataStore Proto. Exposes `Flow<KeyboardSettings>`. |

#### DI

| File | Responsibility |
|---|---|
| `di/KeyboardModule.kt` | Hilt module: provides `KeyboardState`, `SettingsManager`, `LocalIntelligenceEngine`. |

### 4. `:app` (Application) — Companion App

**build.gradle.kts**: `com.android.application`. Depends on `:keyboard`, `:intelligence`, `:shared`.

**AndroidManifest.xml**: Declares `MainActivity` (launcher), `SettingsActivity`, `DashboardActivity`. Also declares `PauseInputMethodService` from `:keyboard`.

| File | Responsibility |
|---|---|
| `PauseApplication.kt` | Application class, sets up Hilt |
| `MainActivity.kt` | Checks if keyboard is enabled. If not → onboarding. If yes → dashboard. |
| `onboarding/OnboardingScreen.kt` | 3-step pager: intro, privacy, enable keyboard |
| `onboarding/EnableKeyboardScreen.kt` | Opens `ACTION_INPUT_METHOD_SETTINGS` intent. Shows enabled-state check. |
| `settings/SettingsActivity.kt` | Host Activity |
| `settings/SettingsScreen.kt` | Material3 preference screen (appearance, feedback, privacy, about) |
| `settings/SettingsViewModel.kt` | DataStore-backed ViewModel |
| `dashboard/DashboardScreen.kt` | Shows aggregate metrics (WPM, deletions, impulsivity) — no text |
| `dashboard/DashboardViewModel.kt` | Observes `TypingMetrics` from intelligence engine |
| `di/AppModule.kt` | Hilt module for app-level dependencies |

## Build Configuration

### Root `build.gradle.kts`
```kotlin
plugins {
    id 'com.android.application' version '8.5.2' apply false
    id 'com.android.library' version '8.5.2' apply false
    id 'org.jetbrains.kotlin.android' version '2.0.21' apply false
    id 'com.google.dagger.hilt.android' version '2.52' apply false
    id 'com.google.devtools.ksp' version '2.0.21-1.2.0' apply false
}
```

### DataStore Schema
Create `app/src/main/java/com/pause/shared/data/SettingsSerializer.kt` — proto or Preferences-based. Recommendation: Preferences (simpler, no proto compiler needed).

### Gradle Wrapper
`gradle/wrapper/gradle-wrapper.properties`:
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
```

## CI/CD — `.github/workflows/ci.yml`

```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Download Android SDK
        uses: android-actions/setup-android@v3
        with:
          api-level: 35
          ndk: false
      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        with:
          gradle-version: 8.9
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest --no-daemon
      - name: Run lint
        run: ./gradlew lintDebug --no-daemon
      - name: Build debug APK
        run: ./gradlew assembleDebug --no-daemon
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: pause-keyboard-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

## Testing Strategy

### Unit tests (JUnit 5, JVM)

| Module | File | Test cases |
|---|---|---|
| `:shared` | `LanguageTest.kt` | fromCode, next, isRtl for EN/AR |
| `:shared` | `KeySpecTest.kt` | Parcelable round-trip, width defaults |
| `:intelligence` | `MetricsAggregatorTest.kt` | Compute from synthetic event buffer; verify WPM, deletion ratio, impulsivity |
| `:intelligence` | `PauseDetectorTest.kt` | Each rule triggers correctly; edge cases (empty metrics) |
| `:intelligence` | `InterventionSchedulerTest.kt` | Cooldown blocks duplicate; max per session; enable/disable |
| `:keyboard` | `EnglishKeyboardProviderTest.kt` | Verify QWERTY row layout, key codes |
| `:keyboard` | `ArabicKeyboardProviderTest.kt` | Verify RTL reversal |
| `:keyboard` | `ShiftHandlerTest.kt` | Lower→Upper→Lower, Lower→CapsLock→Lower |
| `:keyboard` | `BackspaceHandlerTest.kt` | Single press, long-press repeat timing |

### Instrumented tests (Android device)

| Module | File | Coverage |
|---|---|---|
| `:app` | `OnboardingFlowTest.kt` | Navigation, Settings intent |
| `:app` | `SettingsScreenTest.kt` | Save/change settings |
| `:keyboard` | `KeyboardInputTest.kt` | Type text, verify InputConnection calls |

## Implementation Order

1. Root Gradle files + buildSrc + version catalog
2. `:shared` module — all data models, interfaces, key codes
3. `:intelligence` module — signal collector, metrics aggregator, pause detector, scheduler, engine
4. `:keyboard` module — service, state, Compose UI, layout providers, input processing, feedback
5. `:app` module — application, onboarding, settings, dashboard
6. Resources — strings (EN+AR), themes, colors, drawables
7. Hilt DI — modules for all layers
8. Tests — unit tests (all modules), instrumented tests (app/keyboard)
9. CI/CD — GitHub Actions workflow
10. Build verification — `./gradlew assembleDebug`, `./gradlew test`

## Privacy Enforcement

- `TypingSignalCollector` discards all `label` values immediately after event processing begins
- No `INTERNET` permission in any manifest
- `MetricsAggregator` only sees `RawKeyEvent` (code, booleans, timestamp) — no text
- Dashboard shows only aggregate numbers, never message content
- Lint check in CI for network security config absence

## Files Already Created

- `:shared` module — all 12 source files (Language, KeySpec, KeyType, KeyCodes, KeyboardDefinition, KeyboardMode, ShiftState, TypingMetrics, InterventionType, InterventionProposal, IntelligenceObserver, KeyboardSettings, Result) + AndroidManifest.xml + build.gradle.kts
- `:intelligence` module — `TypingSignalCollector.kt` + AndroidManifest.xml + build.gradle.kts
- Root files — build.gradle.kts, settings.gradle.kts, gradle.properties, libs.versions.toml, gradle-wrapper.properties, buildSrc/AndroidConfig.kt
