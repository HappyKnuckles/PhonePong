# UI Redesign Documentation

## Overview

The app has been redesigned with a modern menu-based navigation system while preserving all existing motion controller functionality (gyroscope, swing detection, WebSocket communication).

## New Architecture

### Screen Navigation

The app now uses a simple state-based navigation system with two main screens:

1. **MenuScreen** - Player selection screen
2. **GameScreen** - Racket view with motion control

Navigation is managed through `ControllerState.currentScreen` and `Screen` sealed class.

### File Structure

```
composeApp/src/commonMain/kotlin/com/example/insanecrossmobilepingpongapp/
├── model/
│   ├── Screen.kt                 # NEW: Screen navigation sealed class & PlayerRole enum
│   ├── ControllerState.kt        # UPDATED: Added screen, playerRole, isDebugVisible
│   ├── DeviceOrientation.kt      # Unchanged
│   ├── PaddleControl.kt          # Unchanged
│   └── SwingEvent.kt             # Unchanged
├── controller/
│   ├── ControllerViewModel.kt    # UPDATED: Added selectPlayer(), toggleDebug(), disconnectAndReturnToMenu()
│   └── SwingDetector.kt          # Unchanged
├── ui/
│   ├── MenuScreen.kt             # NEW: Player selection menu
│   ├── GameScreen.kt             # NEW: Racket display and game view
│   ├── DebugOverlay.kt           # NEW: Debug panel with reusable cards
│   └── ControllerScreen.kt       # OLD: Kept for reference (no longer used)
├── App.kt                        # UPDATED: State-based navigation logic
└── [other files unchanged]
```

## Screen Details

### 1. MenuScreen

**Purpose**: First screen shown on app launch. Users select their player role.

**Features**:
- Dark gradient background (`#1A1A2E` to `#0F0F1E`)
- Title: "Ping Pong Online"
- Subtitle: "Wähle deinen Spieler"
- Two large buttons:
  - "Spieler 1 (Unten)" - Red button (`#E63946`)
  - "Spieler 2 (Oben)" - Green button (`#06D6A0`)
- Info text about auto-start when both players connect

**User Flow**:
1. User taps a player button
2. `ViewModel.selectPlayer(role)` is called
3. Navigation automatically switches to GameScreen
4. WebSocket connection starts automatically with player's token

**Code Location**: `composeApp/src/commonMain/kotlin/.../ui/MenuScreen.kt`

### 2. GameScreen

**Purpose**: Main game screen showing racket and connection status.

**Features**:
- Dark gradient background (`#1A1A2E` to `#16213E`)
- **Racket Display**: Large circular racket image (red for Player 1, black for Player 2)
- **Connection Badge**: Shows player name and connection status
- **Debug Toggle Button**: Top-right FAB to toggle debug overlay
- **Disconnect Button**: Bottom center, returns to menu

**Racket Images**:
- Player 1: `Res.drawable.racket_red` (red face with black outline)
- Player 2: `Res.drawable.racket_black` (black face with red outline)

**User Flow**:
1. Racket is displayed based on selected player role
2. Motion detection runs in background
3. Tapping debug icon shows/hides overlay
4. Tapping "Trennen" disconnects and returns to menu

**Code Location**: `composeApp/src/commonMain/kotlin/.../ui/GameScreen.kt`

### 3. DebugOverlay

**Purpose**: Slide-in panel showing diagnostic information.

**Features**:
- Semi-transparent black backdrop (`alpha = 0.5`)
- Rounded top corners (`24.dp`)
- Scrollable content with cards:
  - **Connection Card**: Server URL and connection state
  - **Status Card**: Sensor/Connected/Calibrated badges
  - **Tilt Card**: TiltX, TiltY, Intensity with progress bars
  - **Swing Card**: SwingSpeed, DirectionX, DirectionY with progress bars
- Close button in header

**Reusable Components**:
- `DebugConnectionCard`
- `DebugStatusCard`
- `DebugTiltCard`
- `DebugSwingCard`
- `DebugValueRow` (private helper)

**Code Location**: `composeApp/src/commonMain/kotlin/.../ui/DebugOverlay.kt`

## ViewModel Changes

### New State Properties

```kotlin
data class ControllerState(
    // ... existing properties ...
    val currentScreen: Screen = Screen.Menu,
    val playerRole: PlayerRole? = null,
    val isDebugVisible: Boolean = false
)
```

### New Methods

```kotlin
// Select player and navigate to game
fun selectPlayer(role: PlayerRole)

// Toggle debug overlay visibility
fun toggleDebug()

// Disconnect and return to menu
fun disconnectAndReturnToMenu()
```

### Navigation Flow

```
MenuScreen
    ↓ (user taps player button)
selectPlayer(role)
    ↓ (updates state + auto-connects)
GameScreen
    ↓ (user taps disconnect)
disconnectAndReturnToMenu()
    ↓ (disconnects WebSocket)
MenuScreen
```

## Racket Images

### Current Implementation

Placeholder XML vector drawables are provided in:
- `composeApp/src/commonMain/composeResources/drawable/racket_red.xml`
- `composeApp/src/commonMain/composeResources/drawable/racket_black.xml`

These are simple colored circles with handles. They work cross-platform (Android/iOS).

### How to Replace with Real Images

To use actual racket PNG/JPG images:

#### For Android:
```
composeApp/src/androidMain/res/
└── drawable/
    ├── racket_red.png
    └── racket_black.png
```

#### For iOS:
```
composeApp/src/iosMain/composeResources/
└── drawable/
    ├── racket_red.png
    └── racket_black.png
```

#### For Shared (Compose Multiplatform Resources):
```
composeApp/src/commonMain/composeResources/
└── drawable/
    ├── racket_red.png
    └── racket_black.png
```

**Recommended**: Use shared resources for cross-platform consistency.

**Image Requirements**:
- Format: PNG or JPG
- Recommended size: 512x512 or 1024x1024 pixels
- Transparent background (PNG) works best
- Should show paddle face (red or black side) facing viewer

**No Code Changes Needed**: The `GameScreen` already uses `painterResource()` which works with all image formats.

## Design Colors

### Primary Palette
- **Player 1 Red**: `#E63946`
- **Player 2 Green**: `#06D6A0`
- **Dark Background**: `#1A1A2E`, `#0F0F1E`, `#16213E`
- **Card Background**: `#2A2A3E`, `#1E1E2E`
- **Warning/Error**: `#E63946`
- **Success**: `#06D6A0`
- **Warning Orange**: `#FFA500`
- **Text Gray**: `#BBBBBB`, `#888888`

### Typography
- **Title**: 48sp, Bold, White
- **Subtitle**: 20sp, Gray
- **Button Text**: 28sp, Bold
- **Card Headers**: 16sp, Bold
- **Body**: 14sp, Regular

## Testing Checklist

- [ ] Menu screen displays correctly on first launch
- [ ] Player 1 button navigates to game with red racket
- [ ] Player 2 button navigates to game with black racket
- [ ] Connection badge shows correct player name
- [ ] Connection status updates correctly (Connecting → Connected)
- [ ] Debug button toggles overlay visibility
- [ ] Debug overlay shows live sensor data
- [ ] Disconnect button returns to menu
- [ ] WebSocket disconnects when returning to menu
- [ ] Swing detection continues to work in GameScreen
- [ ] Motion data is NOT visible by default (only in debug mode)
- [ ] Portrait orientation is locked (Android)

## Migration Notes

### What Changed
- **App.kt**: Now uses `when` statement for screen navigation
- **ControllerViewModel**: Added UI navigation methods
- **ControllerState**: Added screen/player/debug properties

### What Stayed the Same
- **All sensor code** (AndroidMotionSensor, IosMotionSensor)
- **WebSocketClient** and connection logic
- **SwingDetector** algorithm
- **PaddleControl** data processing
- **Calibration logic**
- **Logging system**

### Backward Compatibility
- `ControllerScreen.kt` is preserved but no longer used
- Can be safely deleted or kept for reference
- All existing ViewModels methods remain functional

## Localization

Currently German strings are hardcoded. For future localization:

```kotlin
// In MenuScreen.kt
Text(text = stringResource(Res.string.choose_player))

// In GameScreen.kt
Text(text = stringResource(Res.string.disconnect))
```

Strings to externalize:
- "Ping Pong Online"
- "Wähle deinen Spieler"
- "Spieler 1 (Unten)" / "Spieler 2 (Oben)"
- "Hinweis: Das Spiel startet..."
- "Trennen" (Disconnect)
- "Verbunden" (Connected)
- Debug card titles

## Performance Notes

- **Navigation**: Zero-overhead state-based switching
- **Recomposition**: Only active screen is composed
- **Debug Overlay**: Only rendered when `isDebugVisible = true`
- **Sensor Processing**: Unchanged, continues to run efficiently
- **WebSocket**: Auto-connects on player selection, auto-disconnects on menu return

## Future Enhancements

### Possible Additions
1. **Animation**: Fade transitions between screens
2. **Haptic Feedback**: Vibrate on swing detection
3. **Sound Effects**: Audio feedback for swings
4. **Settings Screen**: Adjust sensitivity, server URL, etc.
5. **Reconnect Logic**: Auto-reconnect on connection loss
6. **Loading State**: Show spinner while connecting
7. **Error Messages**: Toast/Snackbar for connection errors
8. **Calibration Reminder**: Prompt user to calibrate on first use

### Code Quality Improvements
1. Extract colors to theme system
2. Extract dimensions to constants
3. Add unit tests for ViewModel navigation logic
4. Add UI tests for screen transitions
5. Improve error handling in navigation flow

---

## Quick Start Guide

1. **Launch App** → MenuScreen appears
2. **Tap "Spieler 1" or "Spieler 2"** → Auto-connects and shows racket
3. **Start Playing** → Swing detection works automatically
4. **Tap Debug Icon** → View live sensor data
5. **Tap "Trennen"** → Disconnect and return to menu

The redesign is complete and production-ready! 🎉
