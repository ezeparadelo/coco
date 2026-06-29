---
name: frontend
description: Front-end UI developer guidelines for Jetpack Compose, neumorphism, shapes, gestures, animations, and splash transitions in Coco.
---

# Coco Frontend & UI Custom Skill

This skill guides AI agents and developers implementing UI changes on Coco using Jetpack Compose.

## 🎨 Theme & Tokens ("Coco Fresco")
Always use tokens defined in `com.coco.app.ui.theme.Color.kt` instead of arbitrary hex codes:
- **Cream** (`CocoCream` = `#FFFBF4`): Used as the primary background or container surface.
- **Brown** (`CocoBrown` = `#8C6A4F`): Primary branding color (arch top, default text detail).
- **Dark Brown** (`CocoBrownDark` = `#3A2A1D`): Strong elements, text shadows, high contrast headers.
- **Green** (`CocoGreen` = `#5E8C61`): Active states, confirmation actions, Done buttons.
- **Ink** (`CocoInk` = `#2E2018`): Text color when drawing on Cream backgrounds.
- **NeoLight** (`#FFFFFF`) and **NeoShadow** (`#ECE0CC`): Custom neumorphic shadows.

---

## 🛠️ Neumorphism & Click Effects
Coco UI relies heavily on organic, tactile surfaces:
- Use `Modifier.neumorphic(shape, lightShadowColor, darkShadowColor)` defined in `Neumorphic.kt` to apply soft shadows rather than standard flat cards.
- Use `Modifier.pressBounce(interactionSource)` defined in `Neumorphic.kt` for interactive buttons. This adds a physical scale press (0.94x) and spring animation feedback.
- Every note card must transition colors smoothly using `animateColorAsState` across the 4 pastel options (Arena cálida, Verde Coco, Lavanda, Celeste nórdico).

---

## 🖐️ Gesture & Animation Invariants
Do not break the following behaviors:
1. **Vertical Drag Sheet**: The main header or history pull tab allows dragging the capture panel up and down. The `dragModifier` should always remain constant on the component so `onDragStopped` executes the final snap velocity checks (>600px/s) correctly.
2. **WritePill Gestures**: The floating "Escribir" pill uses separate pointer detection gestures: `pointerInput` with `detectTapGestures` and `detectVerticalDragGestures` separately. **Do not** combine `.clickable` with `.draggable` as it crashes touch recognition.
3. **Card Swipes**: Note cards in the history list support horizontal swipes:
   - **Left Swipe**: Deletes the note (shows a red background + trash bin).
   - **Right Swipe**: Archives the note (shows a brown background + archive box).
   - Both must trigger a brief `3.5s` neumorphic **Undo Snackbar** (Undo bar allows recovering deleted notes).
4. **fastMode**: If `fastMode` is true in `SettingsStore`, disable all UI spring physics, arch wave drawings, and color animations to maximize performance.

---

## 🚀 Splash Screen Handoff (Dual-Phase)
Coco coordinates OS-level themes with Compose.
- **Compose Phase**: Runs in `HomeScreen.kt` via `morphProgress` (0f -> 1f) over 1200ms.
- **Handoff**: Native splash waits for `composeReady.get()` in `MainActivity.kt`.
- **Invariants**:
  - Do not change native circle color from `#5E4130` (must match Compose start circle).
  - Do not use Popup/Dialog for onboarding overlays (inline rendering is required to cover status/navigation bars).
  - The transparent touch-blocking overlay must cover the screen during the splash transition.
