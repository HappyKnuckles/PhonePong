import WebSocket from 'ws';
import config from '../../config/config';
import { Ball } from '../core/types';

export type ClientRole = 'host1' | 'host2' | 'player1' | 'player2';

interface BallState extends Partial<Ball> { }

class NetworkManager {
  private clients: Record<ClientRole, WebSocket | null>;

  constructor() {
    this.clients = {
      host1: null,
      host2: null,
      player1: null,
      player2: null,
    };
  }

  public register(token: string, ws: WebSocket): void {
    if (this.isValidRole(token)) {
      this.clients[token] = ws;
      console.log(`✅ Registered ${token}`);
    } else {
      console.warn(`⚠️ Attempted to register invalid token: ${token}`);
    }
  }

  public remove(token: string): void {
    if (this.isValidRole(token)) {
      this.clients[token] = null;
    }
  }

  public isReady(): boolean {
    if (config.HEADLESS) {
      return !!(this.clients.player1 && this.clients.player2);
    }
    return !!(
      this.clients.host1 &&
      this.clients.host2 &&
      this.clients.player1 &&
      this.clients.player2
    );
  }

  public broadcast(msg: string): void {
    Object.values(this.clients).forEach((client) => {
      if (client && client.readyState === WebSocket.OPEN) {
        client.send(msg);
      }
    });
  }

  public sendJSON(token: ClientRole, data: object): void {
    const client = this.clients[token];
    if (client && client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  }

  public sendSound(playerNum: 1 | 2, soundName: string): void {
    const token = `player${playerNum}` as ClientRole;
    this.sendJSON(token, { type: 'sound', sound: soundName });
  }

  public syncHosts(ball: BallState): void {
    const data = {
      type: 'coordinates',
      data: {
        x: ball.x,
        y: ball.y,
        v: ball.v! * config.VSCALE,
        goal_x: ball.goal || 0,
      },
    };

    this.sendJSON('host1', data);
    this.sendJSON('host2', data);
  }

  private isValidRole(token: string): token is ClientRole {
    return ['host1', 'host2', 'player1', 'player2'].includes(token);
  }
}

export default NetworkManager;