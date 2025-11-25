// server.js
const http = require('http');
const express = require('express');
const WebSocket = require('ws');
const config = require('./config');
const socketHandler = require('./socketHandler');
const gameManager = require('./GameManager');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Initialize Socket Handler
socketHandler(wss);

// Optional: Headless Loop logic (if needed based on config)
if (config.HEADLESS) {
    console.log("🤖 Running in HEADLESS mode");
    setInterval(() => {
        gameManager.updateBallState();
        console.log(`Ball Position - x: ${gameManager.ballstate.x.toFixed(3)}, y: ${gameManager.ballstate.y.toFixed(3)}`);
        gameManager.checkOutOfBounds();
    }, 300);
}

// Start Server
server.listen(config.PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running on http://0.0.0.0:${config.PORT}`);
});