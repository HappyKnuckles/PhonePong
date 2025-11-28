import config from '../../config/config';
import { Ball } from './types';

const BALL_RADIUS = 5;
const PADDLE_HIT_HEIGHT = 50;
const ARC_MAX_HEIGHT = 135;
const BOUNCE_PEAK = 180;

class PhysicsEngine {

  public update(ball: Ball, lastHitDirection: number): Ball {
    if (Math.abs(ball.y) >= config.OUTERBOUND && lastHitDirection === ball.d) {
      ball.y = 99 * Math.sign(ball.y);
      ball.lastUpdate = Date.now();
    }

    const now = Date.now();
    const dt = (now - ball.lastUpdate) / 1000;

    const distX = ball.goal - ball.x;
    const distY = ball.d * config.OUTERBOUND - ball.y;
    const distance = Math.sqrt(distX ** 2 + distY ** 2) || 1;
    const velocity = config.VSCALE * ball.v * dt;

    ball.x += velocity * (distX / distance);
    ball.y += velocity * (distY / distance);

    this.updateHeight(ball);

    ball.lastUpdate = now;
    return ball;
  }

  private updateHeight(ball: Ball): void {
    const currentDistTraveled = Math.abs(ball.y - ball.startY);

    const totalFlightDist = Math.abs(ball.bounceY - ball.startY);

    const progress = currentDistTraveled / totalFlightDist;

    if (ball.state === 'FLIGHT') {

      if (progress >= 1.0) {
        const TABLE_WIDTH = 60;
        if (Math.abs(ball.x) <= TABLE_WIDTH) {
          ball.state = 'BOUNCE'; 
        } else {
          ball.z = Math.max(0, ball.z - 2);
          return;
        }
      }

      const linearHeight = PADDLE_HIT_HEIGHT + (BALL_RADIUS - PADDLE_HIT_HEIGHT) * progress;

      const shiftedProgress = Math.pow(progress, 0.85);
      const arcHeight = ARC_MAX_HEIGHT * Math.sin(shiftedProgress * Math.PI);

      ball.z = linearHeight + arcHeight;
    }

    else if (ball.state === 'BOUNCE') {

      const distPastBounce = Math.abs(ball.y - ball.bounceY);

      const halfTableLen = config.OUTERBOUND;
      const virtualLandSpot = halfTableLen * 2.8;
      const totalBounceDist = virtualLandSpot - Math.abs(ball.bounceY);

      const t = distPastBounce / totalBounceDist;

      ball.z = BALL_RADIUS + (BOUNCE_PEAK * 4 * t * (1 - t));

      if (ball.z < 0) ball.z = 0;
    }
  }

  public applyHit(ball: Ball, velocity: number, specificGoal: number | null = null): Ball {
    ball.v = velocity;

    ball.goal = specificGoal !== null
      ? specificGoal
      : (Math.random() - 0.5) * 1.9 * config.OUTERBOUND;

    ball.startY = ball.y;
    ball.state = 'FLIGHT';

    const tableLen = config.OUTERBOUND;
    const powerFactor = Math.min(1.5, Math.max(0, velocity - 1));
    let bounceDepth = (tableLen * 0.4) + (tableLen * 0.5 * powerFactor);

    const minBounce = tableLen * 0.2;
    bounceDepth = Math.max(minBounce, Math.min(bounceDepth, tableLen));

    const newDir = ball.d * -1;
    ball.bounceY = bounceDepth * newDir;

    ball.d = newDir;
    ball.lastUpdate = Date.now();

    return ball;
  }

  public isCollision(ballY: number): boolean {
    const absY = Math.abs(ballY);
    return absY >= config.INNERBOUND && absY <= config.OUTERBOUND;
  }
}

export default PhysicsEngine;