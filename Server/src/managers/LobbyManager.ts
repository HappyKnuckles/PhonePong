import GameManager, { GameMode } from './GameManager';
import { BotDifficulty } from '../core/BotAI';

class LobbyManager {
  private lobbies: Map<string, GameManager>;

  constructor() {
    this.lobbies = new Map();
  }

  public createLobby(gameMode: GameMode = 'multiplayer', botDifficulty: BotDifficulty = 'medium'): string {
    const lobbyId = this.generateLobbyId();
    const game = new GameManager(lobbyId, () => this.removeLobby(lobbyId), gameMode, botDifficulty);
    this.lobbies.set(lobbyId, game);
    console.log(`✨ Lobby created: ${lobbyId} (Mode: ${gameMode})`);
    return lobbyId;
  }

  public getLobby(lobbyId: string): GameManager | undefined {
    return this.lobbies.get(lobbyId);
  }

  public removeLobby(lobbyId: string): void {
    if (this.lobbies.has(lobbyId)) {
      this.lobbies.get(lobbyId)?.cleanup(); // Stop loops
      this.lobbies.delete(lobbyId);
      console.log(`🗑️ Lobby removed: ${lobbyId}`);
    }
  }

  private generateLobbyId(): string {
    // Generate a 4-letter uppercase code (e.g., "ABCD")
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let result = '';
    do {
      result = '';
      for (let i = 0; i < 4; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
    } while (this.lobbies.has(result)); // Ensure uniqueness
    return result;
  }
}

export default new LobbyManager();