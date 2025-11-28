import config from '../../config/config';
import GameState from '../core/GameState';
import PhysicsEngine from '../core/PhysicsEngine';
import NetworkManager, { ClientRole } from './NetworkManager';
import WebSocket from 'ws';

class GameManager {
  public state: GameState;
  public physics: PhysicsEngine;
  public net: NetworkManager;

  private physicsInterval: NodeJS.Timeout | null;
  private broadcastInterval: NodeJS.Timeout | null;

  constructor() {
    this.state = new GameState();
    this.physics = new PhysicsEngine();
    this.net = new NetworkManager();

    this.physicsInterval = null;
    this.broadcastInterval = null;
  }

  // --- Connectivity ---
  public registerClient(token: string, ws: WebSocket): void {
    this.net.register(token, ws);

    if (this.net.isReady() && !this.state.isRunning) {
      this.startGame();
    }
  }

  public removeClient(token: string): void {
    this.net.remove(token);
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

    console.log('😎 Game Initialized - Waiting for Player 1 Swing');
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

    console.log(`🏓 ${token} swung (Speed: ${speed})`);

    const playerNum = token === 'player1' ? 1 : 2;

    if (this.state.swingToStartPlayer === playerNum) {
      console.log('🏁 Serve Hit!');
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
    console.log(`⚠️ Ball Hit Floor (OUT)! Winner: Player ${winner}`);

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
    console.log(`🏆 Score: P1:${p1} - P2:${p2}`);

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

export default new GameManager();