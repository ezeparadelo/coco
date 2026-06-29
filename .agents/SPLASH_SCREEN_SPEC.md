# Splash Screen System Specification

## Overview
Coco uses a two-phase splash screen system that creates a seamless transition from the native Android splash to a Compose-animated entry. The goal is zero visual discontinuity between the OS splash and the app UI.

## Architecture

### Phase 1: Native Splash (OS-level)
**File:** `app/src/main/res/values/themes.xml`

The native splash is defined by `Theme.Coco.Starting`:
```xml
<style name="Theme.Coco.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/coco_cream</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash_circle</item>
    <item name="postSplashScreenTheme">@style/Theme.Coco</item>
</style>
```

**Circle specifications:**
- **Color:** `#5E4130` (CocoArchTop, NOT CocoBrown)
- **Size:** 170dp (calibrated to match Compose circle)
- **File:** `app/src/main/res/drawable/ic_splash_circle.xml`

### Phase 2: Compose Animation (1200ms)
**File:** `app/src/main/java/com/coco/app/ui/home/HomeScreen.kt`

The animation is controlled by `morphProgress` (Animatable 0f → 1f) with three phases:

| Progress | Phase | Action |
|----------|-------|--------|
| 0.0 - 0.5 | Circle descent | Circle moves from center to bottom |
| 0.5 - 0.6 | Circle fade-out | Circle alpha 1.0 → 0.0 |
| 0.6 - 1.0 | Arch rise | Arch animates from below screen to final position |

**Timing:** 1200ms total with `FastOutSlowInEasing`

## Seamless Handoff Mechanism

**File:** `app/src/main/java/com/coco/app/MainActivity.kt`

```kotlin
val composeReady = AtomicBoolean(false)
installSplashScreen().setKeepOnScreenCondition { !composeReady.get() }
```

The native splash remains visible until Compose signals readiness:
1. `HomeScreen` calls `onComposeReady()` after the first frame with the overlay drawn
2. This happens in `LaunchedEffect(initialized)` before starting the animation
3. The native splash dismisses, revealing the Compose circle at the exact same position

## Animation Variables

**In HomeScreen.kt:**

```kotlin
val morphProgress = remember { Animatable(0f) }
var startupComplete by remember { mutableStateOf(false) }

val archPhase by remember {
    derivedStateOf { ((morphProgress.value - 0.6f) / 0.4f).coerceIn(0f, 1f) }
}

val effectiveOffsetY by remember {
    derivedStateOf {
        if (!startupComplete && initialized) {
            val targetOffset = if (startInHistory) historyOffset else normalOffset
            targetOffset + (heightPx - targetOffset) * (1f - archPhase)
        } else offsetY
    }
}
```

**Circle overlay:**
```kotlin
val circleSizeDp = 170f
val centerY = heightPx / 2f - (circleSizeDp / 2 * density)
val bottomY = heightPx - (circleSizeDp / 2 * density)
val circlePhase = (p / 0.5f).coerceIn(0f, 1f)
val circleAlpha = if (p < 0.5f) 1f else ((0.6f - p) / 0.1f).coerceIn(0f, 1f)
```

## startInHistory Behavior

**When `startInHistory = true`:**
- The arch animates from `heightPx` (below screen) up to `historyOffset` (peeking position)
- This happens during the 60-100% phase (archPhase 0.0 → 1.0)
- The arch is ready to show notes immediately after animation completes

**When `startInHistory = false` (default):**
- The arch animates from `heightPx` (below screen) up to `normalOffset` (writing position)
- Same timing as above

**Key insight:** Both cases use the same animation formula:
```kotlin
targetOffset + (heightPx - targetOffset) * (1f - archPhase)
```
Where `targetOffset` is either `historyOffset` or `normalOffset`.

## fastMode Behavior

**When `fastMode = true`:**
- Skips the entire animation sequence
- `morphProgress` snaps to 1f immediately
- `startupComplete` is set to true without delay
- UI appears instantly without circle or arch animation

## Onboarding Integration

**File:** `app/src/main/java/com/coco/app/ui/onboarding/OnboardingDialog.kt`

The onboarding dialog appears after `startupComplete = true`:
```kotlin
val showOnboarding by remember { 
    derivedStateOf { !hasSeenOnboarding && startupComplete } 
}
```

**Keyboard timing:**
```kotlin
canRequestFocus = startupComplete && !showOnboarding
```
This prevents the keyboard from opening during the animation if it's the first launch.

**Rendering:** Onboarding is rendered inline within the main Box (not as Dialog/Popup) to ensure edge-to-edge coverage including the status bar.

## Touch Blocking

During the animation, a transparent overlay blocks all touch input:
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {}
)
```

This prevents accidental interactions during the splash sequence.

## Calibration Guide

### If the circle "jumps" when appearing:
1. Verify `ic_splash_circle.xml` has the correct radius and color (`#5E4130`)
2. Verify `HomeScreen.kt` uses `circleSizeDp = 170f`
3. Adjust the native circle size if needed (each unit ≈ 4.7dp on screen)

### If the arch doesn't align properly:
- Check that `historyOffset = heightPx - (115 * density)`
- Check that `normalOffset = heightPx * 0.46f`
- Verify the arch phase calculation uses 0.6-1.0 range

## Common Mistakes to Avoid

❌ **Don't** change the native circle color to `CocoBrown` (must be `CocoArchTop`)
❌ **Don't** use Dialog/Popup for onboarding (breaks status bar coverage)
❌ **Don't** remove the touch-blocking overlay during animation
❌ **Don't** make `startInHistory` skip the arch animation (it should animate to historyOffset)
❌ **Don't** change the arch phase timing from 0.6-1.0 without updating circle fade-out

## Debugging Commands

```bash
# Clear app data to test onboarding
adb shell pm clear com.coco.app

# View crash logs
adb logcat -d -s AndroidRuntime:E

# Install and test
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## File Reference

| File | Purpose |
|------|---------|
| `app/src/main/res/drawable/ic_splash_circle.xml` | Native splash circle vector |
| `app/src/main/res/values/themes.xml` | Theme.Coco.Starting definition |
| `app/src/main/AndroidManifest.xml` | Applies splash theme, enables Predictive Back |
| `app/src/main/java/com/coco/app/MainActivity.kt` | AtomicBoolean handoff mechanism |
| `app/src/main/java/com/coco/app/ui/home/HomeScreen.kt` | Complete animation logic |
| `app/src/main/java/com/coco/app/ui/onboarding/OnboardingDialog.kt` | Inline onboarding with animations |
| `app/src/main/java/com/coco/app/ui/components/CocoArch.kt` | The actual arch with animated dome |
