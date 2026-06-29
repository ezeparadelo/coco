# 🥥 Coco v1.0.0

**Coco captures your notes instantly.** Zero friction between "I have a thought" and "it's saved".

Open the app → keyboard is already there → type → done. That's it.

---

## ✨ Features

- **⚡ Instant Capture** — The keyboard is ready the exact moment you open Coco. No extra taps, no menus, just start typing.
- **🎨 Warm & Organic Design** — Crafted with soft brown tones (`#8C6A4F`), inviting cream backgrounds (`#FFFBF4`), and spring-animated curves. Everything is tactile, rounded, and satisfying to interact with.
- **👆 Intuitive Swipe Gestures** — Swipe left on any note to delete, or right to archive. Pull down gently from the top of your history to reveal instant animated search. Swipe up on empty lists or search results to instantly dismiss and dismiss the keyboard.
- **🛡️ Undo & Safety Confirmations** — Deleted a note by accident? A floating neumorphic pill lets you restore it instantly. Deleting permanently or importing JSON backups triggers clear safety confirmation dialogs indicating exact loss counts before any data is replaced.
- **📌 Pin Your Favorites** — Long-press any note to pin it right at the top. Your most important thoughts stay front and center.
- **🌈 Pastel Note Palette** — Tap any note to organize your ideas visually with four curated pastel tones: Warm Sand, Coco Green, Lavender, and Nordic Blue.
- **📦 Archive & Organize** — Keep your main feed clean by tucking finished thoughts into the Archive tab. Access your counts directly from the pull-down header.
- **💾 JSON Backup & Restore** — Fully compatible with the Storage Access Framework (SAF). Export all your notes to a clean `.json` backup or safely import them anytime with full confirmation protection.
- **🚀 Ultra-Fast Performance** — Built with Kotlin 2.0, Jetpack Compose, Room DB, and 120 FPS GPU-accelerated rendering. Includes an optional "No animations" toggle for instant startup speed.
- **🌍 Bilingual Support** — Fully localized in English and Spanish, adapting seamlessly to your system preference.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose (Material 3 BOM 2024.10.01) + Custom Neumorphic & Curved Bounding System
- **Database**: Room 2.6.1 (KSP) + Flow / Coroutines Unidirectional Data Flow
- **Architecture**: Single-module MVVM (`:app`), manual dependency injection via `CocoApplication`
- **Minimum SDK**: Android 8.0 (API 26) · **Target SDK**: Android 15 (API 35)

---

## 🚀 Building Locally

Ensure you have JDK 17+ installed and configured.

```bash
# Clone the repository
git clone https://github.com/ezeparadelo/coco.git
cd coco

# Build Debug APK
./gradlew :app:assembleDebug

# Run Unit Tests
./gradlew :app:testDebugUnitTest

# Install directly on a connected device/emulator
./gradlew :app:installDebug
```

> **Note for Windows users**: Use `./gradlew.bat` in PowerShell or Command Prompt.

---

## 📄 License

A passion project designed and developed by [Ezequiel Paradelo](https://github.com/ezeparadelo) 🤎
