---
name: android-dev
description: Guidelines for compilation, testing, environment setup, and running filtered Gradle commands to save agent tokens.
---

# Coco Android Tooling & DevOps Skill

This skill guides AI agents and developers building, testing, and debugging the Coco Android Application.

## ⚡ Token-Saving Gradle Builds
Standard Gradle outputs can consume hundreds of thousands of tokens due to verbose task indicators.
- **Rule**: Whenever you need to build, compile, or run tests in the terminal, **do not** run plain `./gradlew.bat` or `./gradlew` commands.
- **Execution**: Run commands using the custom repository filter script:
  ```powershell
  # Windows PowerShell
  .\.agents\scripts\run_gradle_filtered.ps1 :app:assembleDebug
  .\.agents\scripts\run_gradle_filtered.ps1 :app:testDebugUnitTest
  ```
  This script intercepts outputs and discards status notices (`UP-TO-DATE`, `FROM-CACHE`, etc.), outputting only error stacks, warnings, and build summary lines, which saves up to 90% of prompt tokens.

---

## 🛠️ Main Gradle Tasks
Use these arguments with the filtering script:
- `:app:assembleDebug` — Compiles and builds the debug APK (`app/build/outputs/apk/debug/app-debug.apk`).
- `:app:installDebug` — Builds and installs the debug package onto an active device or emulator.
- `:app:testDebugUnitTest` — Runs local JVM JUnit unit tests (e.g. testing NoteDao or ViewModels in memory).
- `:app:connectedDebugAndroidTest` — Executes instrumented tests requiring a physical or emulated Android device.
- `:app:lintDebug` — Inspects codebase for Kotlin style rules and static lint anomalies.

---

## 📡 ADB Commands & Logs
- **Clearing App State**: To reset user preferences and clear Room database tables when testing onboarding flows, use:
  ```bash
  adb shell pm clear com.coco.app
  ```
- **Error Tracking**: When capturing application logs, isolate critical JVM crashes to avoid token noise:
  ```bash
  adb logcat -d -s AndroidRuntime:E
  ```
  Or trace application logs using:
  ```bash
  adb logcat -d -s "CocoApp:*" "*:E"
  ```
