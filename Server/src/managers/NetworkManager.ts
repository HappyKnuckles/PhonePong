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

  // --- MESSAGING ---

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
        z: ball.z,
        y: ball.y,
        v: ball.v! * config.VSCALE,
        goal_x: ball.goal || 0,
      },
    };

    this.sendJSON('host1', data);
    this.sendJSON('host2', data);
  }

  // --- LOBBY STATE LOGIC ---

  /**
   * Returns a list of roles that are currently connected and open.
   */
  public getOccupiedRoles(): ClientRole[] {
    return (Object.keys(this.clients) as ClientRole[]).filter((role) => {
      const ws = this.clients[role];
      return ws !== null && ws.readyState === WebSocket.OPEN;
    });
  }

  /**
   * Sends the list of taken spots to everyone in the lobby.
   */
  public broadcastLobbyState(lobbyId: string): void {
    const occupied = this.getOccupiedRoles();
    const message = JSON.stringify({
      type: 'lobby_state',
      lobbyId,
      occupied // e.g. ['host1', 'player1']
    });

    Object.values(this.clients).forEach((client) => {
      if (client && client.readyState === WebSocket.OPEN) {
        client.send(message);
      }
    });
  }

  public hasHost(): boolean {
    return !!(this.clients.host1 || this.clients.host2);
  }

  /**
   * Returns the first available player slot ('player1' or 'player2').
   * Returns null if both slots are taken.
   */
  public getAvailablePlayerSlot(): 'player1' | 'player2' | null {
    const p1 = this.clients.player1;
    const p2 = this.clients.player2;

    // Check if player1 slot is free (null or closed connection)
    if (p1 === null || p1.readyState !== WebSocket.OPEN) {
      return 'player1';
    }
    // Check if player2 slot is free
    if (p2 === null || p2.readyState !== WebSocket.OPEN) {
      return 'player2';
    }
    // Both slots taken
    return null;
  }

  /**
   * Returns the first available host slot ('host1' or 'host2').
   * Returns null if both slots are taken.
   */
  public getAvailableHostSlot(): 'host1' | 'host2' | null {
    const h1 = this.clients.host1;
    const h2 = this.clients.host2;

    // Check if host1 slot is free (null or closed connection)
    if (h1 === null || h1.readyState !== WebSocket.OPEN) {
      return 'host1';
    }
    // Check if host2 slot is free
    if (h2 === null || h2.readyState !== WebSocket.OPEN) {
      return 'host2';
    }
    // Both slots taken
    return null;
  }

  /**
   * Returns true if all four slots (host1, host2, player1, player2) are filled.
   */
  public isFull(): boolean {
    const h1 = this.clients.host1;
    const h2 = this.clients.host2;
    const p1 = this.clients.player1;
    const p2 = this.clients.player2;

    return (
      h1 !== null && h1.readyState === WebSocket.OPEN &&
      h2 !== null && h2.readyState === WebSocket.OPEN &&
      p1 !== null && p1.readyState === WebSocket.OPEN &&
      p2 !== null && p2.readyState === WebSocket.OPEN
    );
  }

  private isValidRole(token: string): token is ClientRole {
    return ['host1', 'host2', 'player1', 'player2'].includes(token);
  }
}

export default NetworkManager;