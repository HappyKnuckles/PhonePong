import config from '../../config/config';
import { Ball } from './types';

export default class GameState {
  public score!: { p1: number; p2: number };
  public swingToStartPlayer!: 0 | 1 | 2;
  public currentServer!: 1 | 2;
  public isRunning!: boolean;
  public lastHitDirection!: number;
  public hitTimeout!: number | null;
  public lastHitTime!: number;
  public ball!: Ball;

  constructor() {
    this.reset();
  }

  public reset(): void {
    this.score = { p1: 0, p2: 0 };
    this.swingToStartPlayer = 0;
    this.currentServer = 1; // Default P1 starts
    this.isRunning = false;
    this.hitTimeout = null;
    this.lastHitTime = 0;
    this.resetBall();
  }

  public resetBall(): void {
    this.ball = {
      x: 0,
      y: -99,
      z: 50,
      v: 0,
      startY: -99,
      bounceY: -50,
      state: 'FLIGHT',
      goal: (Math.random() - 0.5) * 1.9 * config.OUTERBOUND,
      d: 1,
      lastUpdate: Date.now(),
    };
    this.lastHitDirection = this.ball.d;
  }
}