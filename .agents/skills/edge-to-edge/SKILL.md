---
name: edge-to-edge
description: Use this skill to migrate your Jetpack Compose app to add adaptive edge-to-edge support and troubleshoot common issues. Use this skill to fix UI components (like buttons or lists) that are obscured by or overlapping with the navigation bar or status bar, fix IME insets, and fix system bar legibility.
license: Apache License 2.0
metadata:
  author: Google LLC
  last-updated: '2026-04-01'
  keywords:
  - android
  - compose
  - system bars
  - edge-to-edge
  - status bar
  - navigation bar
---

## Prerequisites

- Project **MUST** use Android Jetpack Compose.
- Project **MUST** target SDK 35 or later. If the SDK is lower than 35, increase the SDK to 35.

## Step 1: plan

1. Locate and analyze all Activity classes to detect which have existing edge-to-edge support. For every Activity without edge-to-edge, plan to make each Activity edge-to-edge.
2. In each Activity, Locate and analyze all lists and FAB components to detect which have existing edge-to-edge support. For every component without edge-to-edge support, plan to make each of these components edge-to-edge.
3. In each Activity, scan for `TextField`, `OutlinedTextField`, or `BasicTextField`. If found, then you **MUST** verify the IME doesn't hide the input field by following the IME section of this skill.

## Step 2: add edge-to-edge support

1. Add `enableEdgeToEdge` before `setContent` in `onCreate` in each Activity that does not already call `enableEdgeToEdge`.
2. Add `android:windowSoftInputMode="adjustResize"` in the AndroidManifest.xml for all Activities that use a soft keyboard.

## Step 3: apply insets

- The app **MUST** apply system insets, or align content to rulers, so critical UI remains tappable. Choose only one method to avoid double padding:

  1. **PREFERRED:** When available, use `Scaffold`s and pass `PaddingValues` to the content lambda.
  
  ```kotlin
  Scaffold { innerPadding ->
      // innerPadding accounts for system bars and any Scaffold components
      LazyColumn(
          modifier = Modifier
              .fillMaxSize()
              .consumeWindowInsets(innerPadding),
          contentPadding = innerPadding
      ) { /* Content */ }
  }
  ```

  2. **PREFERRED:** When available, use the automatic inset handling or padding modifiers in material components.
     - Material 3 Components manages safe areas for its own components, including:
       - `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar`
       - `BottomAppBar`, `ModalBottomSheet`, `NavigationBar`, `NavigationRail`
     - For Material 2 Components, use the `windowInsets` parameter to apply insets manually. **DO NOT** apply padding to the parent container; instead, pass insets directly to the App Bar component.
     
  3. For components outside a Scaffold, use padding modifiers, such as `Modifier.safeDrawingPadding()` or `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.

  ```kotlin
  Box(
      modifier = Modifier
          .fillMaxSize()
          .safeDrawingPadding()
  ) {
      Button(
          onClick = {},
          modifier = Modifier.align(Alignment.BottomCenter)
      ) {
          Text("Login")
      }
  }
  ```
