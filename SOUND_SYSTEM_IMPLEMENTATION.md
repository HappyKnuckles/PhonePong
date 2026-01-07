# Sound System Implementation - PhonePong

## Overview

This document describes the sound system scaffolding added to PhonePong. The system allows the web client to play sound effects when specific game events occur.

## What Was Implemented

### 1. **Web Client (Kotlin/JS)**

#### New Files Created:
- **`SoundManager.kt`** (`web/composeApp/src/jsMain/kotlin/org/tabletennis/project/audio/`)
  - Singleton class managing HTML5 Audio playback
  - Loads and plays 4 types of sounds: BALL_HIT, BALL_BOUNCE, SCORE, GAME_START
  - Supports volume control and mute functionality
  - Uses the Web Audio API through Kotlin/JS

- **`SoundEffectsPlayer.kt`** (`web/composeApp/src/commonMain/kotlin/org/tabletennis/project/audio/`)
  - Platform-agnostic interface using expect/actual pattern
  - Allows different implementations for different platforms

- **`SoundEffects.kt`** (`web/composeApp/src/jsMain/kotlin/org/tabletennis/project/audio/`)
  - JS implementation of SoundEffectsPlayer
  - Composable that listens to WebSocket events
  - Automatically plays sounds based on server events

#### Modified Files:
- **`WebSocketManager.kt`**
  - Added `SoundEvent` data class
  - Added `soundEvent` StateFlow to emit sound events
  - Added handler for "sound" message type from server

- **`GameScreen.kt`**
  - Integrated `SoundEffectsPlayer` composable
  - Now plays sounds automatically during gameplay

### 2. **Server (TypeScript)**

#### Modified Files:
- **`PhysicsEngine.ts`**
  - Updated `updateHeight()` to return `'BOUNCE'` when ball hits table
  - This triggers the bounce sound event

- **`GameManager.ts`**
  - Added `handleBounce()` method to send bounce sound to both players
  - Updated `updatePhysics()` to handle BOUNCE events
  - Modified `handleScore()` to send score sound to both players
  - Updated `applyHit()` to record lastHitTime (from previous fix)

- **`NetworkManager.ts`**
  - Updated `sendSound()` method to send sounds to both host (web) and player (mobile)
  - Ensures web clients receive sound notifications

- **`GameState.ts`** (from previous fix)
  - Added `lastHitTime` property to prevent double-hits

- **`config.ts`** (from previous fix)
  - Added `HIT_COOLDOWN` configuration

## Sound Events

The following sound events are now triggered:

1. **"hit"** - When paddle hits the ball
   - Already implemented in existing code via `sendSound()`
   
2. **"bounce"** - When ball bounces on the table
   - **NEW**: Triggered in `GameManager.handleBounce()`
   
3. **"score"** - When a point is scored
   - **NEW**: Triggered in `GameManager.handleScore()`
   
4. **"start"** - When game starts (placeholder for future use)

## Sound Files Needed

Create a directory at:
```
web/composeApp/src/commonMain/composeResources/files/sounds/
```

Add the following MP3 files:
- `ball_hit.mp3` - Played when paddle hits ball
- `ball_bounce.mp3` - Played when ball bounces on table
- `score.mp3` - Played when a point is scored
- `game_start.mp3` - Played when game starts

See `sounds/README.md` for detailed requirements and recommendations.

## How It Works

### Client-Side Flow:
1. `SoundManager` loads all sound files on initialization
2. `SoundEffectsPlayer` composable is added to `GameScreen`
3. It listens to `soundEvent` StateFlow from `WebSocketManager`
4. When a sound event arrives, it plays the corresponding sound

### Server-Side Flow:
1. Game events occur (bounce, score, hit)
2. `GameManager` calls `NetworkManager.sendSound(playerNum, soundName)`
3. Server sends JSON message: `{ type: 'sound', sound: 'bounce' }`
4. Web client receives message via WebSocket
5. `WebSocketManager` updates `soundEvent` StateFlow
6. `SoundEffectsPlayer` detects change and plays sound

## Architecture

```
┌─────────────────┐
│   GameManager   │
│    (Server)     │
└────────┬────────┘
         │ sendSound()
         ▼
┌─────────────────┐
│ NetworkManager  │
│    (Server)     │
└────────┬────────┘
         │ WebSocket
         ▼
┌─────────────────┐
│  WebSocketMgr   │
│    (Client)     │
└────────┬────────┘
         │ StateFlow
         ▼
┌─────────────────┐
│ SoundEffects    │
│   Player        │
└────────┬────────┘
         │ playSound()
         ▼
┌─────────────────┐
│  SoundManager   │
│  (Singleton)    │
└────────┬────────┘
         │ HTML5 Audio
         ▼
      🔊 Speakers
```

## Testing

### Without Sound Files:
- The game will still work normally
- Console warnings will appear: "Sound not found: [type]"
- No sounds will play

### With Sound Files:
1. Add MP3 files to the sounds directory
2. Rebuild the web app
3. Play the game
4. Listen for:
   - Bounce sound when ball hits table
   - Score sound when points are scored
   - Hit sound when paddle hits ball (already implemented)

## Future Enhancements

Possible improvements:
1. **Volume Controls**: Add UI to adjust volume
2. **Mute Button**: Add toggle in game UI
3. **More Sounds**: Add sounds for game start, game over, etc.
4. **Sound Variations**: Random variations of each sound for variety
5. **Spatial Audio**: Adjust volume/panning based on ball position
6. **Settings Persistence**: Remember volume settings in localStorage

## Bug Fixes Included

This implementation also includes the fix for the ball teleporting issue:
- Added collision cooldown mechanism (300ms)
- Prevents multiple hits from being registered rapidly
- Ball now behaves predictably when hit

## Files Changed Summary

### Created:
- `web/composeApp/src/jsMain/kotlin/org/tabletennis/project/audio/SoundManager.kt`
- `web/composeApp/src/jsMain/kotlin/org/tabletennis/project/audio/SoundEffects.kt`
- `web/composeApp/src/commonMain/kotlin/org/tabletennis/project/audio/SoundEffectsPlayer.kt`
- `web/composeApp/src/commonMain/composeResources/files/sounds/README.md`
- `web/composeApp/src/commonMain/composeResources/files/sounds/.gitkeep`

### Modified:
- `web/composeApp/src/commonMain/kotlin/org/tabletennis/project/network/WebSocketManager.kt`
- `web/composeApp/src/commonMain/kotlin/org/tabletennis/project/screens/game/ui/screens/GameScreen.kt`
- `Server/src/core/PhysicsEngine.ts`
- `Server/src/core/GameState.ts`
- `Server/src/managers/GameManager.ts`
- `Server/src/managers/NetworkManager.ts`
- `Server/config/config.ts`

## Next Steps

1. **Add Sound Files**: Create or download MP3 files and place them in the sounds directory
2. **Test**: Run the web app and verify sounds play at the correct times
3. **Adjust Volume**: Normalize sound files to appropriate levels
4. **Fine-tune**: Adjust timing or cooldowns if needed
5. **Polish**: Consider adding UI controls for sound settings

---

**Status**: ✅ Scaffolding Complete - Ready for sound files
**Date**: January 7, 2026
