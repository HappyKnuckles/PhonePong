# PhonePong

A real-time multiplayer ping pong game that transforms your smartphone into a motion-controlled racket. This distributed gaming system consists of three main components: a mobile controller app, a web-based game display, and a WebSocket server that synchronizes gameplay across devices.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Platform Support](#platform-support)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
  - [Server Setup](#server-setup)
  - [Web Display Setup](#web-display-setup)
  - [Mobile Controller Setup](#mobile-controller-setup)
- [Running the Application](#running-the-application)
- [How to Play](#how-to-play)
- [Technical Details](#technical-details)
- [Project Structure](#project-structure)
- [License](#license)

## Overview

PhonePong is an innovative multiplayer ping pong game that leverages modern web technologies and mobile sensors to create an immersive gaming experience. Players use their smartphones as physical controllers, swinging them like real ping pong rackets, while the game is displayed on a shared web browser.

The system uses WebSocket connections to synchronize real-time physics calculations between the server, game display, and player controllers, creating a seamless multiplayer experience across different platforms.

## Features

### Game Features

- **Real-Time Physics Simulation**: Server-side physics engine calculates ball trajectory, velocity, and collisions with millisecond precision
- **Parabolic Ball Trajectory**: Realistic 3D ball movement with perspective-based curves simulating depth and arc
- **Motion-Controlled Gameplay**: Transform your smartphone into a ping pong racket using accelerometer and gyroscope sensors
- **Swing Detection**: Advanced gesture recognition detects swing speed and direction for accurate ball control
- **Haptic Feedback**: Vibration feedback on successful hits (mobile devices)
- **Sound Effects**: Audio cues for swings and hits enhance the gaming experience
- **Score Tracking**: Automatic score calculation and display
- **Lobby System**: Create or join game lobbies using simple lobby codes
- **Multiplayer Matchmaking**: Automatic pairing of players and display hosts
- **Cross-Platform Compatibility**: Play with friends regardless of their device type
- **Singeplayer mode**: Don't have any friends? Just play against our NPC

### Mobile Controller Features

- **Motion Sensing**: Real-time orientation tracking using device sensors
- **Calibration System**: Calibrate controller to your preferred holding position
- **Debug Overlay**: Optional debug information showing sensor data and connection status
- **Connection Management**: Automatic reconnection handling and connection state monitoring
- **Low Latency**: Optimized data transmission for responsive gameplay

### Web Display Features

- **3D Perspective Rendering**: Isometric view of the ping pong table with realistic proportions
- **Smooth Animations**: Interpolated ball movement for fluid visual experience
- **Score Display**: Real-time score updates for both players
- **Game State Messages**: Visual feedback for game events (serves, points, waiting for players)
- **Responsive Design**: Adapts to different screen sizes and aspect ratios
- **Multi-Target Support**: Runs on browsers (JavaScript/WebAssembly) and desktop (JVM)

## Architecture

The system consists of three independent components:

1. **Server** (Node.js + TypeScript): Handles game logic, physics calculations, and WebSocket communication
2. **Web Display** (Kotlin Multiplatform + Compose): Renders the game table and ball for spectators/players
3. **Mobile Controller** (Kotlin Multiplatform + Compose): Captures motion data and sends control inputs

```
┌─────────────────┐
│  Mobile Phone   │
│  (Controller)   │ ──WebSocket──┐
└─────────────────┘              │
                                 ▼
┌─────────────────┐        ┌──────────┐
│  Mobile Phone   │        │  Server  │
│  (Controller)   │ ──WS──▶│ (Node.js)│
└─────────────────┘        └──────────┘
                                 │
┌─────────────────┐              │
│  Web Browser    │              │
│  (Display H1)   │ ◀───WebSocket┤
└─────────────────┘              │
                                 │
┌─────────────────┐              │
│  Web Browser    │              │
│  (Display H2)   │ ◀───WebSocket┘
└─────────────────┘

Note: The display can be shown on one or multiple devices simultaneously.
```

## Platform Support

### Mobile Controller App

- **Android**: Minimum SDK 24 (Android 7.0 Nougat) and above
- **iOS**: iOS 13.0 and above (requires motion sensor permissions)
- **Web Browser**: Modern browsers with DeviceOrientation API support (Chrome, Safari, Firefox)
  - Note: iOS Safari requires explicit user permission for motion sensors

### Web Display

- **Web Browsers**:
  - JavaScript target: Chrome, Firefox, Safari, Edge (latest versions)
  - WebAssembly target: Modern browsers with WASM support
- **Desktop**:
  - Windows (via JVM)
  - macOS (via JVM)
  - Linux (via JVM)

### Server

- **Any platform supporting Node.js 16.x or higher**
  - Windows
  - macOS
  - Linux

## Prerequisites

Before installing PhonePong, ensure you have the following installed:

### For Server

- Node.js (version 16.x or higher)
- npm (comes with Node.js)

### For Web Display

- JDK 11 or higher (for JVM target)
- Node.js and npm (for web targets)

### For Mobile Controller

- **For Android Development**:

  - Android Studio (latest stable version)
  - Android SDK with API level 24 or higher
  - Gradle 8.x

- **For iOS Development**:

  - macOS with Xcode 14 or higher
  - CocoaPods
  - iOS Simulator or physical iOS device

- **For Web Development**:
  - Node.js and npm

### General

- Git (for cloning the repository)

## Installation

### Important: Network Configuration

Before running the application, you need to configure the IP addresses across all components to match your local network setup:

1. **Find Your Local IP Address**:

   - Windows: Run `ipconfig` in PowerShell and look for IPv4 Address
   - macOS/Linux: Run `ifconfig` or `ip addr` and look for your local network IP

2. **Update IP Addresses in the Following Files**:

   - **Server Configuration**: `Server/config/config.ts` - Update the server host IP if needed
   - **Web Display WebSocket URL**: `web/composeApp/src/commonMain/kotlin/org/tabletennis/project/network/WebSocketManager.kt` - Update the WebSocket server URL to `wss://YOUR_IP:3000`
   - **Mobile Controller WebSocket URL**: `mobile/composeApp/src/commonMain/kotlin/com/example/phonepong/model/ControllerState.kt` - Update the default server URL to `wss://YOUR_IP:3000`

3. **Example Configuration**:
   - If your computer's IP is `192.168.1.100`, use `wss://192.168.1.100:3000` as the WebSocket URL
   - All devices (server, display, controllers) must be on the same network
   - Ensure firewall allows connections on port 3000

### Server Setup

1. Navigate to the Server directory:

```bash
cd Server
```

2. Install dependencies:

```bash
npm install
```

3. Generate SSL certificates (required for HTTPS/WSS connections from mobile devices):

This step is only needed to be able to run the mobile version in the browser as iOS only allows access to sensordata in an https context. On native devices you can run the full setup over http.

4. (Optional) Configure server settings by editing `config/config.ts`:

```typescript
export default {
  VSCALE: 150.0, // Velocity scale factor
  FREQUENCY_MS: 30, // Update frequency in milliseconds
  TABLE_X_LIMIT: 100, // X boundary for collision checks
  TABLE_Y_LIMIT: 100, // Y boundary for collision checks
  OUTERBOUND: 140, // Y boundary for scoring
  INNERBOUND: 70, // Y boundary for collision checks
  HEADLESS: false, // Test without hosts
  TEST: false, // Run test sequence on start
  USE_TIMEOUT: 1000, // Timeout after score (ms)
  PORT: 3000, // Server port
};
```

### Web Display Setup

1. Navigate to the web directory:

```bash
cd web
```

2. Install dependencies:

For JavaScript/WebAssembly targets:

```bash
cd composeApp
npm install
cd ..
```

3. Configure WebSocket URL (if needed):

Edit `web/composeApp/src/commonMain/kotlin/org/tabletennis/project/network/WebSocketManager.kt` to update the server URL if you're not using localhost:3000.

### Mobile Controller Setup

1. Navigate to the mobile directory:

```bash
cd mobile
```

2. Update server configuration:

Edit the default server URL in `mobile/composeApp/src/commonMain/kotlin/com/example/phonepong/model/ControllerState.kt` if needed.

3. **For Android**:

Open the `mobile` folder in Android Studio and let it sync the Gradle files automatically.

4. **For iOS**:

```bash
cd iosApp
pod install
cd ..
```

Then open `iosApp/iosApp.xcworkspace` in Xcode.

5. **For Web**:

No additional setup required beyond the initial project dependencies.

## Running the Application

### Starting the Server

1. Navigate to the Server directory:

```bash
cd Server
```

2. Run the development server:

```bash
npm run dev
```

The server will start on `https://localhost:3000` (HTTPS for mobile device compatibility).

3. (Optional) Run the emulator for testing without real controllers:

```bash
npm run emulator
```

### Running the Web Display

**Option 1: Run in Browser (WebAssembly - Recommended)**

```bash
cd web
./gradlew wasmJsBrowserRun
```

The game will open automatically in your default browser, typically at `http://localhost:8080`.

**Option 2: Run in Browser (JavaScript)**

```bash
cd web
./gradlew jsBrowserRun
```

**Option 3: Run as Desktop Application (JVM)**

```bash
cd web
./gradlew run
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

### Running the Mobile Controller

**For Android**:

1. Connect an Android device via USB or start an emulator
2. In Android Studio, select the device and click Run
3. Or use Gradle from command line:

```bash
cd mobile
./gradlew installDebug
```

**For iOS**:

1. Open `mobile/iosApp/iosApp.xcworkspace` in Xcode
2. Select your device or simulator
3. Click the Run button or press Cmd+R

**For Web (Browser-based Controller)**:

```bash
cd mobile
./gradlew jsBrowserRun
```

The controller interface will open in your browser. Note that motion sensors may require HTTPS and user permission on iOS devices.

## How to Play

### Setup

1. **Start the Server**: Run the WebSocket server as described above
2. **Open the Display**: Launch the web display on one or more devices:
   - Multiple browsers/devices showing the same game simultaneously
   - Each display will show mirrored real-time game state (each the own side of the table)
   - All displays must join the same lobby using the lobby code
3. **Connect Controllers**: Open the mobile controller app on each player's smartphone

### Gameplay Flow

1. **Lobby Creation**:

   - On the web display, click "Create New Lobby" or enter an existing lobby code
   - A unique lobby code will be generated
   - Share this code with other players

2. **Player Connection**:

   - On each mobile device, enter the lobby code
   - Select Player 1 or Player 2
   - Wait for all players and hosts to connect

3. **Starting the Game**:

   - The game starts automatically when both players and displays are connected
   - Multiple display devices can join the same lobby to show the game on different screens
   - One player will be designated to serve first

4. **Controls**:

   - **Serving**: Swing your phone downward to serve the ball
   - **Hitting**: Swing your phone to hit the ball back to your opponent
   - **Swing Speed**: The speed of your swing determines the ball's velocity
   - **Angle**: The angle of your phone affects the ball's direction

5. **Scoring**:
   - Points are scored when the opponent fails to return the ball
   - The first player to reach the target score wins
   - The serve alternates between players

### Tips

- Use controlled, deliberate swings for better accuracy
- Timing is crucial - swing just as the ball approaches your side
- Enable debug mode in settings to see sensor data and fine-tune your technique
- Ensure good WiFi connection for minimal latency

## Technical Details

### Communication Protocol

The system uses WebSocket (WSS) connections with JSON message payloads:

**Player to Server**:

- Swing events with speed and direction
- Connection handshake with token and lobby ID

**Server to Player**:

- Game state updates (score, serving player)
- Sound/vibration triggers for hits

**Server to Display**:

- Ball position and trajectory (x, y, z, velocity)
- Game state (score, messages)
- Physics updates at 30Hz

### Motion Sensing

The mobile controller uses:

- **Accelerometer**: Detects linear acceleration for swing speed
- **Gyroscope**: Tracks rotational velocity for swing direction
- **Device Orientation API**: Provides pitch, roll, and yaw angles

Swing detection algorithm:

1. Monitors acceleration magnitude
2. Applies threshold filtering (configurable)
3. Implements debounce to prevent double-detection
4. Calculates swing speed (normalized 0.0 - 1.0)
5. Transmits minimal payload for low latency

### Physics Engine

Server-side physics simulation includes:

- Parabolic trajectory calculation using quadratic Bezier curves
- Collision detection with table boundaries
- Bounce physics with configurable parameters
- Velocity scaling based on swing input
- Direction control based on paddle angle

### Performance Optimizations

- **Client-Side Prediction**: Display interpolates ball position between server updates
- **Delta Compression**: Only changed values are transmitted
- **Minimal Payloads**: Swing events contain only essential data
- **Adaptive Update Rate**: Server adjusts update frequency based on game state


## Troubleshooting

### Mobile Controller Cannot Connect

- Ensure the server is running and accessible from your network
- Verify that all IP addresses in the configuration files match your server's local IP
- Check that you're using HTTPS/WSS (required for mobile browser version)
- Verify firewall settings allow connections on port 3000
- On iOS, ensure you've granted motion sensor permissions
- Try accessing the server's root URL (`https://your-server-ip:3000`) in the mobile browser first to accept the SSL certificate
- Confirm all devices are connected to the same WiFi network

### Display Not Connecting

- Verify the WebSocket URL in the web display configuration matches your server's IP address
- Ensure you're using the correct protocol (wss:// for secure WebSocket)
- Check that the lobby code is entered correctly
- Refresh the browser and try reconnecting
- Check browser console for connection errors

### Motion Sensors Not Working

- On iOS 13+, you must explicitly grant permission through the browser
- Ensure the device is not in a restrictive mode (Low Power Mode, etc.)
- Check that the browser supports DeviceOrientation API

### Laggy Gameplay

- Reduce network latency by using a local network
- Close other applications using bandwidth
- Check server `FREQUENCY_MS` setting (lower = more updates but higher bandwidth)
- Ensure server has adequate CPU resources

### Display Not Updating

- Verify WebSocket connection status in browser console
- Check that you've joined the same lobby as the players
- Refresh the browser and reconnect
- Verify server is broadcasting updates (check server logs)

For more issues, you can contact me over <a href="mailto:nicolashoffmann01@protonmail.com">nicolashoffmann01@protonmail.com</a>
