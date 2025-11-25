const WebSocket = require('ws');
const config = require('./config');

class GameManager {
    constructor() {
        // State containers
        this.clients = {
            host1: null,
            host2: null,
            player1: null,
            player2: null
        };

        this.intervals = {};
        this.previous = { host1: { x: 0, y: 0 }, host2: { x: 0, y: 0 } };
        
        this.ballstate = {};
        this.score1 = 0;
        this.score2 = 0;
        
        this.started = false;
        this.swingtostart = 0;
        this.timeout = null;
        this.checked = 1;

        // Initial Setup
        this.initializeGame();
    }

    initializeGame() {
        this.ballstate = {
            x: 0,
            y: 0,
            v: 0,
            goal: (Math.random() - .5) * 1.9 * config.OUTERBOUND,
            d: -1,
            t: Date.now(),
        };
        this.score1 = 0;
        this.score2 = 0;
        this.checked = this.ballstate.d;
        this.timeout = null;
        this.swingtostart = 0;
        this.started = false;
    }

    resetGame() {
        this.initializeGame();
        this.broadcast("start");
        this.swingtostart = 1;
        console.log("😎 Player 1 Swing to start");
        this.started = true;
    }

    // --- Networking Helpers ---

    registerClient(token, ws) {
        this.clients[token] = ws;
    }

    removeClient(token) {
        this.clients[token] = null;
        if (this.intervals[token]) {
            clearInterval(this.intervals[token]);
            delete this.intervals[token];
        }
    }

    isReady() {
        if (config.HEADLESS) {
            return this.clients.player1 !== null && this.clients.player2 !== null;
        }
        return this.clients.host1 !== null && 
               this.clients.host2 !== null && 
               this.clients.player1 !== null && 
               this.clients.player2 !== null && 
               this.clients.host1.readyState === WebSocket.OPEN && 
               this.clients.host2.readyState === WebSocket.OPEN;
    }

    broadcast(message) {
        Object.values(this.clients).forEach(client => {
            if (client && client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    }

    sendToHost(hostKey, data) {
        const client = this.clients[hostKey];
        if (client && client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(data));
        }
    }

    // --- Physics / Math Helpers ---

    to() { return (3 + this.ballstate.d) / 2; }
    from() { return (3 - this.ballstate.d) / 2; }

    // --- Core Logic ---

    updateBallState() {
        if (Math.abs(this.ballstate.y) >= config.OUTERBOUND && this.checked == this.ballstate.d) {
            this.ballstate.y = 99 * Math.sign(this.ballstate.y);
            this.ballstate.t = Date.now();
        }

        let dt = (Date.now() - this.ballstate.t) / 1000;
        let norm = Math.sqrt(
            (this.ballstate.goal - this.ballstate.x) ** 2 +
            (this.ballstate.d * config.OUTERBOUND - this.ballstate.y) ** 2
        );

        this.ballstate.x += config.VSCALE * this.ballstate.v * dt * (this.ballstate.goal - this.ballstate.x) / norm;
        this.ballstate.y += config.VSCALE * this.ballstate.v * dt * (this.ballstate.d * config.OUTERBOUND - this.ballstate.y) / norm;
        this.ballstate.t = Date.now();
    }

    updateBallStateAfterCollision(velocity, goal) {
        if (goal == null) {
            goal = (Math.random() - .5) * 1.9 * config.OUTERBOUND;
        }
        // Note: dt calculation was in original but unused in this function, removed for clarity
        this.ballstate.v = velocity;
        this.ballstate.goal = goal;
        this.ballstate.d *= -1;
        this.ballstate.t = Date.now();
    }

    checkOutOfBounds() {
        if (Math.abs(this.ballstate.y) >= config.OUTERBOUND) {
            if (this.from() == 1) this.score1 += 1;
            else this.score2 += 1;

            if (this.clients.host1 && this.clients.host2) {
                this.sendToHost('host1', {
                    type: "score",
                    score: [this.score1, this.score2],
                    message: "Swing to start the next round"
                });
                this.sendToHost('host2', {
                    type: "score",
                    score: [this.score2, this.score1],
                    message: "Your opponent will start the next round"
                });
            } else {
                this.resetGame();
                return;
            }

            this.timeout = Date.now() + config.USE_TIMEOUT;
            console.log(`🏆 Score Update - P1: ${this.score1}, P2: ${this.score2}`);

            // Reset ball for next round
            this.ballstate = {
                x: 0, y: 0, v: 0,
                goal: (Math.random() - .5) * 1.9 * config.OUTERBOUND,
                d: -1, t: Date.now(),
            };

            this.checked = this.ballstate.d;
            console.log("😎 Player 1 Swing to start");

            this.swingtostart = this.to();
            this.ballstate.y = this.to() == 1 ? -99 : 99;
            this.ballstate.t = Date.now();
            this.ballstate.d = 1;
            this.checked = this.ballstate.d;
        }
    }

    checkCollision(velocity) {
        if (config.OUTERBOUND >= Math.abs(this.ballstate.y) && Math.abs(this.ballstate.y) >= config.INNERBOUND) {
            this.updateBallStateAfterCollision(velocity, null);
            console.log("🔄 Collision detected");

            if (!config.HEADLESS) {
                const collisionData = {
                    x: this.ballstate.x,
                    y: this.ballstate.y,
                    v: this.ballstate.v,
                    goal_x: this.ballstate.goal,
                };

                this.sendToHost('host1', { from: "collision", data: collisionData });
                this.sendToHost('host2', { type: "collision", data: collisionData });
                this.sendToHost("player" + this.from(), { type: "sound", sound: "hit" });
            }
        }
    }
}

module.exports = new GameManager();