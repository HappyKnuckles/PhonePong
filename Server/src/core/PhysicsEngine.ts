import config from '../../config/config';
import { Ball, PhysicsResult } from './types';

const BALL_RADIUS = 5;
const PADDLE_HIT_HEIGHT = 50;
const ARC_MAX_HEIGHT = 135;
const BOUNCE_PEAK = 180;
const SERVE_ARC_HEIGHT = 40;
const TABLE_X_LIMIT = config.TABLE_X_LIMIT;
const TABLE_Y_LIMIT = config.TABLE_Y_LIMIT;

class PhysicsEngine {

  public update(ball: Ball, lastHitDirection: number): PhysicsResult {
    if (Math.abs(ball.y) >= config.OUTERBOUND && lastHitDirection === ball.d) {
      ball.y = 99 * Math.sign(ball.y);
    }

    const now = Date.now();
    const dt = (now - ball.lastUpdate) / 1000;

    const distX = ball.goal - ball.x;
    const distY = ball.d * config.OUTERBOUND - ball.y;
    const distance = Math.sqrt(distX ** 2 + distY ** 2) || 1;
    const velocity = config.VSCALE * ball.v * dt;

    ball.x += velocity * (distX / distance);
    ball.y += velocity * (distY / distance);

    const status = this.updateHeight(ball);

    ball.lastUpdate = now;
    return status;
  }

  private updateHeight(ball: Ball): PhysicsResult {
    const currentDistTraveled = Math.abs(ball.y - ball.startY);
    const totalFlightDist = Math.abs(ball.bounceY - ball.startY);
    const progress = totalFlightDist > 0 ? currentDistTraveled / totalFlightDist : 0;

    if (ball.state === 'FLIGHT' || ball.state === 'SERVE_FLIGHT') {
      if (progress >= 1.0) {
        const onTableX = Math.abs(ball.x) <= TABLE_X_LIMIT;
        const onTableY = Math.abs(ball.y) <= TABLE_Y_LIMIT;

        if (onTableX && onTableY) {

          if (ball.state === 'SERVE_FLIGHT') {
            ball.state = 'FLIGHT';
            ball.startY = ball.y;
            ball.z = 0;
            const tableLen = config.OUTERBOUND;

            const nextBounceDepth = tableLen * 0.6;

            ball.v = ball.v * 0.85;

            ball.bounceY = nextBounceDepth * ball.d;
            return 'CONTINUE';
          }
          else {
            ball.state = 'BOUNCE';
            ball.z = 0;
          }
        } else {
          ball.z = Math.max(0, ball.z - 15);
          if (ball.z <= 0) return 'FLOOR_HIT';
          return 'CONTINUE';
        }
      }
    }

    if (ball.state === 'SERVE_FLIGHT') {
      const linearHeight = PADDLE_HIT_HEIGHT + (BALL_RADIUS - PADDLE_HIT_HEIGHT) * progress;
      const arcHeight = SERVE_ARC_HEIGHT * Math.sin(progress * Math.PI);
      ball.z = linearHeight + arcHeight;
    }
    else if (ball.state === 'FLIGHT') {
      const startH = (Math.abs(ball.startY) < config.OUTERBOUND * 0.9) ? BALL_RADIUS : PADDLE_HIT_HEIGHT;

      const linearHeight = startH + (BALL_RADIUS - startH) * progress;
      const shiftedProgress = Math.pow(progress, 0.85);
      const arcHeight = ARC_MAX_HEIGHT * Math.sin(shiftedProgress * Math.PI);
      ball.z = linearHeight + arcHeight;
    }
    else if (ball.state === 'BOUNCE') {
      const distPastBounce = Math.abs(ball.y - ball.bounceY);
      const halfTableLen = config.OUTERBOUND;
      const virtualLandSpot = halfTableLen * 2.8;
      const totalBounceDist = virtualLandSpot - Math.abs(ball.bounceY);
      const t = totalBounceDist > 0 ? distPastBounce / totalBounceDist : 0;

      ball.z = BALL_RADIUS + (BOUNCE_PEAK * 4 * t * (1 - t));
      if (ball.z < 0) ball.z = 0;
    }

    return 'CONTINUE';
  }

  public applyHit(ball: Ball, velocity: number, specificGoal: number | null = null, isServe: boolean = false): Ball {
    ball.v = velocity;
    ball.goal = specificGoal !== null ? specificGoal : (Math.random() - 0.5) * 1.9 * config.OUTERBOUND;
    ball.startY = ball.y;

    const newDir = ball.d * -1;
    ball.d = newDir;

    if (isServe) {
      ball.state = 'SERVE_FLIGHT';
      ball.bounceY = ball.startY * 0.5;
    } else {
      ball.state = 'FLIGHT';
      const tableLen = config.OUTERBOUND;
      const powerFactor = Math.min(1.5, Math.max(0, velocity - 1));
      let bounceDepth = (tableLen * 0.4) + (tableLen * 0.5 * powerFactor);
      const minBounce = tableLen * 0.2;
      bounceDepth = Math.max(minBounce, Math.min(bounceDepth, tableLen));
      ball.bounceY = bounceDepth * newDir;
    }

    ball.lastUpdate = Date.now();
    return ball;
  }

  public isCollision(ballY: number): boolean {
    const absY = Math.abs(ballY);
    return absY >= config.INNERBOUND && absY <= config.OUTERBOUND;
  }
}

export default PhysicsEngine;