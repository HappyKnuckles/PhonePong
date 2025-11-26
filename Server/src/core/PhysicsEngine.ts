import config from '../../config/config';
import { Ball } from './types';

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

    ball.lastUpdate = now;

    return ball;
  }

  public applyHit(ball: Ball, velocity: number, specificGoal: number | null = null): Ball {
    ball.v = velocity;

    ball.goal =
      specificGoal !== null
        ? specificGoal
        : (Math.random() - 0.5) * 1.9 * config.OUTERBOUND;

    ball.d *= -1; // Reverse direction
    ball.lastUpdate = Date.now();

    return ball;
  }

  public isCollision(ballY: number): boolean {
    const absY = Math.abs(ballY);
    return absY >= config.INNERBOUND && absY <= config.OUTERBOUND;
  }
}

export default PhysicsEngine;