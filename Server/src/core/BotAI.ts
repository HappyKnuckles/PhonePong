import config from '../../config/config';
import { Ball } from './types';

export type BotDifficulty = 'easy' | 'medium' | 'hard';

interface BotConfig {
  reactionDelay: number;      // ms delay before bot reacts
  hitAccuracy: number;        // 0-1, how accurate the hit is
  missChance: number;         // 0-1, chance to miss entirely
  speedVariation: number;     // variation in swing speed
  baseSpeed: number;          // base swing speed
}

const DIFFICULTY_CONFIGS: Record<BotDifficulty, BotConfig> = {
  easy: {
    reactionDelay: 300,
    hitAccuracy: 0.5,
    missChance: 0.15,
    speedVariation: 0.3,
    baseSpeed: 1,
  },
  medium: {
    reactionDelay: 150,
    hitAccuracy: 0.75,
    missChance: 0.05,
    speedVariation: 0.2,
    baseSpeed: 1.2,
  },
  hard: {
    reactionDelay: 50,
    hitAccuracy: 0.95,
    missChance: 0.01,
    speedVariation: 0.1,
    baseSpeed: 1.6,
  },
};

export default class BotAI {
  private difficulty: BotDifficulty;
  private config: BotConfig;
  private lastSwingTime: number = 0;
  private pendingSwing: NodeJS.Timeout | null = null;

  constructor(difficulty: BotDifficulty = 'medium') {
    this.difficulty = difficulty;
    this.config = DIFFICULTY_CONFIGS[difficulty];
  }

  /**
   * Check if the bot should swing based on ball position and state
   * Returns the swing speed if bot should swing, null otherwise
   */
  public shouldSwing(ball: Ball, isServing: boolean): number | null {
    const now = Date.now();
    
    // Prevent rapid swings
    if (now - this.lastSwingTime < 500) {
      return null;
    }

    // Bot is player 2, so ball must be heading towards player 2 (d = 1)
    if (ball.d !== 1) {
      return null;
    }

    // Check if ball is in hitting zone for player 2
    const absY = Math.abs(ball.y);
    const inHitZone = absY >= config.INNERBOUND && absY <= config.OUTERBOUND;
    
    if (isServing) {
      // Bot needs to serve
      this.lastSwingTime = now;
      return this.calculateSwingSpeed();
    }

    if (!inHitZone) {
      return null;
    }

    // Random miss based on difficulty
    if (Math.random() < this.config.missChance) {
      return null;
    }

    this.lastSwingTime = now;
    return this.calculateSwingSpeed();
  }

  /**
   * Calculate swing speed with some variation based on difficulty
   */
  private calculateSwingSpeed(): number {
    const variation = (Math.random() - 0.5) * 2 * this.config.speedVariation;
    const speed = this.config.baseSpeed + variation;
    return Math.max(0.5, speed); // Minimum speed of 0.5
  }

  /**
   * Get reaction delay for this difficulty
   */
  public getReactionDelay(): number {
    // Add some randomness to reaction time
    const variation = Math.random() * 50 - 25; // ±25ms
    return Math.max(0, this.config.reactionDelay + variation);
  }

  /**
   * Clean up any pending timeouts
   */
  public cleanup(): void {
    if (this.pendingSwing) {
      clearTimeout(this.pendingSwing);
      this.pendingSwing = null;
    }
  }

  public getDifficulty(): BotDifficulty {
    return this.difficulty;
  }
}
