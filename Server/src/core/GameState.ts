import config from '../../config/config';
import { Ball } from './types';

export default class GameState {
  public score!: { p1: number; p2: number };
  public swingToStartPlayer!: 0 | 1 | 2; // 0 = Active, 1 = P1 Start, 2 = P2 Start
  public isRunning!: boolean;
  public lastHitDirection!: number;
  public hitTimeout!: number | null;
  public ball!: Ball;

  constructor() {
    this.reset();
  }

  public reset(): void {
    this.resetBall();
    this.score = { p1: 0, p2: 0 };
    this.swingToStartPlayer = 0;
    this.isRunning = false;
    this.hitTimeout = null;
  }

  public resetBall(): void {
    this.ball = {
      x: 0,
      y: 0,
      z: 0,
      v: 0,
      startY: 0,
      bounceY: 0,
      state: 'FLIGHT',
      goal: (Math.random() - 0.5) * 1.9 * config.OUTERBOUND,
      d: -1, // Direction: -1 = Up (P1), 1 = Down (P2)
      lastUpdate: Date.now(),
    };
    this.lastHitDirection = this.ball.d;
  }
}