import config from '../../config/config';
import GameState from '../core/GameState';
import PhysicsEngine from '../core/PhysicsEngine';
import NetworkManager, { ClientRole } from './NetworkManager';
import WebSocket from 'ws';

export default class GameManager {
  public lobbyId: string;
  public state: GameState;
  public physics: PhysicsEngine;
  public net: NetworkManager;

  private physicsInterval: NodeJS.Timeout | null;
  private broadcastInterval: NodeJS.Timeout | null;
  private onDestroy: () => void;

  constructor(lobbyId: string, onDestroy: () => void) {
    this.lobbyId = lobbyId;
    this.onDestroy = onDestroy;

    this.state = new GameState();
    this.physics = new PhysicsEngine();
    this.net = new NetworkManager();

    this.physicsInterval = null;
    this.broadcastInterval = null;
  }

  public cleanup(): void {
    this.stopGameLoop();
  }

  // --- Connectivity ---
  public registerClient(token: string, ws: WebSocket): void {
    console.log(`[Lobby ${this.lobbyId}] 📝 Registering client: ${token}`);
    this.net.register(token, ws);

    // Log current state
    const occupied = this.net.getOccupiedRoles();
    console.log(`[Lobby ${this.lobbyId}] 📊 Occupied slots after registration: ${occupied.join(', ') || 'none'}`);
    console.log(`[Lobby ${this.lobbyId}] 🎯 isReady: ${this.net.isReady()}, isRunning: ${this.state.isRunning}`);

    // 1. Send specific info to the new client
    this.net.sendJSON(token as ClientRole, {
      type: 'lobby_info',
      lobbyId: this.lobbyId
    });

    // 2. Broadcast the updated "Occupied Slots" list to EVERYONE
    this.net.broadcastLobbyState(this.lobbyId);

    // 3. If game is already running, send "start" to the reconnecting client
    if (this.state.isRunning && this.net.isReady()) {
      // Send start to the newly connected client so they can join the game in progress
      this.net.sendJSON(token as ClientRole, { type: 'game_in_progress' });
      const client = ws;
      if (client.readyState === WebSocket.OPEN) {
        client.send('start');
      }
      console.log(`[Lobby ${this.lobbyId}] 🔄 Client ${token} rejoined running game`);
    } else if (this.net.isReady() && !this.state.isRunning) {
      // All clients connected for the first time, start the game
      this.startGame();
    }
  }

  public removeClient(token: string): void {
    this.net.remove(token);

    // Broadcast update so others know a slot opened up
    this.net.broadcastLobbyState(this.lobbyId);

    // Optional: If no players/hosts left, destroy lobby
    const occupied = this.net.getOccupiedRoles();
    if (occupied.length === 0) {
      console.log(`[Lobby ${this.lobbyId}] Empty, destroying...`);
      this.onDestroy();
    }
  }


  // --- Game Flow ---
  public startGame(): void {
    if (this.state.isRunning) return;

    this.state.resetBall();
    this.net.broadcast('start');

    this.state.currentServer = 1;
    this.state.swingToStartPlayer = 1;
    this.state.ball.y = -99;
    this.state.ball.d = -1;

    this.state.isRunning = true;

    console.log(`[Lobby ${this.lobbyId}] 😎 Game Initialized - Waiting for Player 1 Swing`);
    this.startGameLoop();
  }

  public resetGame(): void {
    this.state.reset();
    this.stopGameLoop();
  }

  public startGameLoop(): void {
    this.stopGameLoop();

    this.physicsInterval = setInterval(() => {
      this.updatePhysics();
      this.checkRules();
    }, 16);

    this.broadcastInterval = setInterval(() => {
      if (this.net.isReady()) {
        this.net.syncHosts(this.state.ball);
      }
    }, config.FREQUENCY_MS);
  }

  public stopGameLoop(): void {
    if (this.physicsInterval) clearInterval(this.physicsInterval);
    if (this.broadcastInterval) clearInterval(this.broadcastInterval);
  }


  // --- Core Logic ---
  public handlePlayerSwing(token: ClientRole, speed: number): void {
    if (!this.net.isReady()) return;
    if (this.state.hitTimeout && Date.now() < this.state.hitTimeout) return;
    if (speed <= 1.0) return;

    console.log(`[Lobby ${this.lobbyId}] 🏓 ${token} swung (Speed: ${speed})`);

    const playerNum = token === 'player1' ? 1 : 2;

    if (this.state.swingToStartPlayer === playerNum) {
      console.log(`[Lobby ${this.lobbyId}] 🏁 Serve Hit!`);
      this.applyHit(speed, null, true);
      this.state.swingToStartPlayer = 0;
      return;
    }

    if (this.state.swingToStartPlayer === 0) {
      const targetPlayer = this.getTargetPlayer();

      if (playerNum === targetPlayer) {
        if (this.physics.isCollision(this.state.ball.y)) {
          this.applyHit(speed, null, false);
          this.net.sendSound(playerNum, 'hit');
        }
      }
    }
  }

  private updatePhysics(): void {
    const result = this.physics.update(this.state.ball, this.state.lastHitDirection);

    if (result === 'FLOOR_HIT') {
      this.handleSideMiss();
    }
  }

  private handleSideMiss(): void {
    const winner = this.getTargetPlayer();
    console.log(`[Lobby ${this.lobbyId}] ⚠️ Ball Hit Floor (OUT)! Winner: Player ${winner}`);

    if (winner === 1) this.state.score.p1++;
    else this.state.score.p2++;

    this.handleScore();
  }

  private applyHit(velocity: number, specificGoal: number | null, isServe: boolean = false): void {
    this.physics.applyHit(this.state.ball, velocity, specificGoal, isServe);
    this.state.lastHitDirection = this.state.ball.d;
    this.notifyCollision();
  }

  private checkRules(): void {
    if (Math.abs(this.state.ball.y) >= config.OUTERBOUND) {
      const scorer = this.getFromPlayer();

      if (scorer === 1) this.state.score.p1++;
      else this.state.score.p2++;

      this.handleScore();
    }
  }

  private handleScore(): void {
    const { p1, p2 } = this.state.score;
    console.log(`[Lobby ${this.lobbyId}] 🏆 Score: P1:${p1} - P2:${p2}`);

    const totalPoints = p1 + p2;
    if (totalPoints % 2 === 0) {
      this.state.currentServer = this.state.currentServer === 1 ? 2 : 1;
    }

    const server = this.state.currentServer;

    this.net.sendJSON('host1', {
      type: 'score',
      score: [p1, p2],
      message: server === 1 ? 'Swing to start' : 'Opponent starts',
    });
    this.net.sendJSON('host2', {
      type: 'score',
      score: [p2, p1],
      message: server === 2 ? 'Swing to start' : 'Opponent starts',
    });

    this.state.hitTimeout = Date.now() + config.USE_TIMEOUT;

    this.state.resetBall();
    this.state.swingToStartPlayer = server;

    if (server === 1) {
      this.state.ball.y = -99;
      this.state.ball.d = -1;
    } else {
      this.state.ball.y = 99;
      this.state.ball.d = 1;
    }

    this.state.lastHitDirection = this.state.ball.d;
  }

  // --- Helpers ---
  private getTargetPlayer(): number {
    return (3 + this.state.ball.d) / 2;
  }

  private getFromPlayer(): number {
    return (3 - this.state.ball.d) / 2;
  }

  private notifyCollision(): void {
    if (config.HEADLESS) return;

    const data = {
      from: 'collision',
      data: {
        x: this.state.ball.x,
        y: this.state.ball.y,
        v: this.state.ball.v * config.VSCALE,
        goal_x: this.state.ball.goal,
      }
    };
    this.net.sendJSON('host1', data);
    this.net.sendJSON('host2', { ...data, type: 'collision' });
  }
}