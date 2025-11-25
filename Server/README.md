# 🏓 WebSocket Ping Pong Backend

A real-time, event-driven Node.js backend for a 1-vs-1 ping pong game. This server handles physics calculations, collision detection, and state synchronization between "Host" displays (browsers) and "Player" controllers (mobile phones) via WebSockets.

## 🚀 Features

* **Real-time Physics:** Server-side calculation of ball movement, including velocity and arc trajectory.
* **Parabolic Curve Logic:** The ball travels in a perspective-based curve (quadratic Bezier) rather than a straight line to simulate 3D depth.
* **Role-Based Architecture:** Distinct logic for `Host` (Display) and `Player` (Controller) clients.
* **Automatic Matchmaking:** Automatically pairs Host 1 with Host 2 and Player 1 with Player 2.
* **Modular Codebase:** Clean separation of concerns (Config, State, Networking, Server).
* **Headless Mode:** Debugging mode to run physics loops without connected clients.

## 📂 Project Structure

```text
├── config.js          # Central configuration for physics and game constants
├── GameManager.js     # "The Brain": Handling state, physics, score, and logic
├── socketHandler.js   # "The Router": Handling WebSocket connections and messages
├── server.js          # Entry point and server initialization
└── README.md          # Documentation
```

## 🛠️ Installation

1. **Clone the repository** (or copy the files into a folder).
2. **Install dependencies:**
   ```bash
   npm install express ws
   ```
3. **Run the server:**
   ```bash
   node server.js
   ```

## ⚙️ Configuration (`config.js`)

You can tweak the game feel by modifying `config.js`. No server restart is required if using `nodemon`, otherwise restart node after changes.

| Variable | Default | Description |
| :--- | :--- | :--- |
| `VSCALE` | `250.0` | Velocity multiplier for the ball speed. |
| `FREQUENCY_MS` | `400` | How often the server syncs coordinates with the Hosts. |
| `MAX_CURVE` | `50.0` | **New:** The maximum width of the parabolic arc. |
| `OUTERBOUND` | `140` | The Y-coordinate boundary for scoring a point. |
| `INNERBOUND` | `70` | The Y-coordinate boundary where collisions are checked. |
| `HEADLESS` | `false` | Set to `true` to debug physics in the console without clients. |

## 🔌 WebSocket Protocol

Clients connect via WebSocket using a query parameter to identify their role.

**Connection URL:**
`ws://YOUR_SERVER_IP:3000/?token={ROLE_ID}`

### Available Roles (`{ROLE_ID}`)
* `host1` - Display for Player 1 side.
* `host2` - Display for Player 2 side.
* `player1` - Controller for Player 1.
* `player2` - Controller for Player 2.

### 📤 Sending Data (Client -> Server)

**From Player Controller:**
When a player swings their phone, send a JSON object with the swing speed.

```json
{
  "speed": 0.85
}
```

### 📥 Receiving Data (Server -> Client)

**To Host (Coordinates):**
Sent every `FREQUENCY_MS` or on major events.

```json
{
  "type": "coordinates",
  "data": {
    "x": 12.5,       // Horizontal Position (includes curve offset)
    "y": 80.0,       // Depth Position
    "v": 200.5,      // Velocity
    "goal_x": 40.0   // Where the ball is ending up
  }
}
```

**To Host (Collision/Hit):**
Sent immediately when a player successfully hits the ball.

```json
{
  "type": "collision", // or "from": "collision"
  "data": { ... }      // Contains x, y, v, goal_x
}
```

**To Host (Score):**
Sent when the ball passes the `OUTERBOUND`.

```json
{
  "type": "score",
  "score": [1, 0],     // [Player1 Score, Player2 Score]
  "message": "Swing to start the next round"
}
```

## 🧠 Physics Logic

The ball movement is calculated in `GameManager.js` using a parametric approach:

1. **Y-Axis (Depth):** Moves linearly based on velocity `v`.
2. **X-Axis (Width):** Moves along a quadratic curve.
   * **Linear Path:** Calculates the straight line between `startX` and `goal`.
   * **Curve Offset:** Applies a quadratic formula `4 * p * (1 - p)` where `p` is the percentage of distance traveled.
   * **Result:** The ball arcs out to the side (up to `MAX_CURVE` width) and curves back in exactly to the target X position.

## 🧪 Testing

You can enable **Headless Mode** in `config.js` to see the physics loop running in your terminal without needing to connect 4 devices.

```javascript
// config.js
HEADLESS: true
```