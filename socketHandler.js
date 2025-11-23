const WebSocket = require('ws');
const config = require('./config');
const gameManager = require('./GameManager');

module.exports = (wss) => {
    
    wss.on('connection', (ws, req) => {
        const token = req.url.replace('/?token=', '');
        console.log('✅ Client connected: ' + token);

        gameManager.registerClient(token, ws);
        gameManager.broadcast("start");

        // --- Test Mode Trigger ---
        if (config.TEST && gameManager.clients.host1 && gameManager.clients.host2) {
            gameManager.sendToHost('host1', {
                type: "collision",
                data: { x: -12.0, y: -100.0, v: 0.7532, goal_x: 43.5 }
            });
            console.log("😎 Game started - TEST event");
        }

        // --- Auto Start Logic ---
        if (gameManager.isReady() && !gameManager.started) {
            gameManager.broadcast("start");
            gameManager.swingtostart = 1;
            console.log("😎 Player 1 Swing to start");
            gameManager.started = true;
        }

        // --- PLAYER LOGIC ---
        if (token.startsWith("player")) {
            ws.on('message', (message) => {
                if (!gameManager.isReady()) return;

                if (gameManager.timeout != null && config.USE_TIMEOUT != 0 && Date.now() < gameManager.timeout) {
                    return;
                } else {
                    gameManager.timeout = null;
                }

                const parsed = JSON.parse(message.toString());
                const speed = parsed.speed;
                if (speed == 1.0) return;

                console.log(`🏓 Player ${token} swung with speed: ${speed}`);

                // Start Movement logic
                if (gameManager.swingtostart == 0 || token == "player" + gameManager.swingtostart) {
                    gameManager.updateBallState();
                }

                // First Serve Logic
                if (token == "player" + gameManager.swingtostart) {
                    console.log("🏁 Started");
                    gameManager.updateBallStateAfterCollision(speed, null);
                    
                    if (!config.HEADLESS) {
                        console.log("🔄 First Collision detected");
                        const data = {
                            x: gameManager.ballstate.x,
                            y: gameManager.ballstate.y,
                            v: gameManager.ballstate.v * config.VSCALE,
                            goal_x: gameManager.ballstate.goal,
                        };
                        
                        gameManager.sendToHost('host1', { from: "collision", data });
                        gameManager.sendToHost('host2', { type: "collision", data });
                    }
                    gameManager.swingtostart = 0;
                }

                // Normal Return Logic
                if (token == "player" + gameManager.to() && 
                   (token == "player" + gameManager.swingtostart || gameManager.swingtostart == 0)) {
                    console.log("Checking collision...");
                    gameManager.checkCollision(speed);
                }
            });
        } 
        
        // --- HOST LOGIC ---
        else if (token.startsWith("host")) {
            // Set up the interval for this specific host
            gameManager.intervals[token] = setInterval(() => {
                gameManager.updateBallState();
                gameManager.checkOutOfBounds();

                const bs = gameManager.ballstate;
                const prev = gameManager.previous[token];

                // Only send update if ball moved
                if (gameManager.isReady() && ws.readyState === WebSocket.OPEN && 
                   (bs.x != prev.x || bs.y != prev.y)) {
                    
                    console.log(`Ball Position - x: ${bs.x.toFixed(3)}, y: ${bs.y.toFixed(3)}`);

                    ws.send(JSON.stringify({
                        type: "coordinates",
                        data: {
                            x: bs.x,
                            y: bs.y,
                            v: bs.v * config.VSCALE,
                            goal_x: bs.goal != null ? bs.goal : 0,
                        }
                    }));
                }

                gameManager.previous[token].x = bs.x;
                gameManager.previous[token].y = bs.y;

            }, config.FREQUENCY_MS);
        }

        // --- Disconnect Logic ---
        ws.on("close", () => {
            gameManager.removeClient(token);
            console.log('❌ Client disconnected: ' + token);
        });
    });
};