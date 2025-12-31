import { Server, WebSocket } from 'ws';
import { IncomingMessage } from 'http';
import lobbyManager from '../managers/LobbyManager';
import { ClientRole } from '../managers/NetworkManager';
import { URLSearchParams } from 'url';

interface PlayerInput {
  speed?: number;
}

export default (wss: Server) => {
  wss.on('connection', (ws: WebSocket, req: IncomingMessage) => {
    const urlParts = (req.url || '').split('?');
    const params = new URLSearchParams(urlParts[1] || '');

    let token = params.get('token') as string;
    const action = params.get('action');
    let lobbyId = params.get('lobby');

    const isAutoAssignPlayer = token === 'player';
    const isAutoAssignHost = token === 'host';

    if (!isAutoAssignPlayer && !isAutoAssignHost && !isValidRole(token)) {
      console.log(`⚠️ Unknown client role rejected: ${token}`);
      ws.close();
      return;
    }

    // --- LOBBY LOGIC ---
    let gameInstance;

    if (action === 'create') {
      lobbyId = lobbyManager.createLobby();
      gameInstance = lobbyManager.getLobby(lobbyId);

      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'lobby_created', lobbyId }));
      }
    } else if (lobbyId) {
      lobbyId = lobbyId.toUpperCase();
      gameInstance = lobbyManager.getLobby(lobbyId);
    }

    // --- VALIDATION ---
    if (!gameInstance || !lobbyId) {
      console.log(`⚠️ Connection rejected: Invalid or missing Lobby ID (${lobbyId})`);
      ws.send(JSON.stringify({ type: 'error', message: 'Lobby not found' }));
      ws.close();
      return;
    }

    // --- CHECK IF LOBBY IS FULL ---
    if (gameInstance.net.isFull()) {
      console.log(`⚠️ Connection rejected: Lobby ${lobbyId} is full (all slots occupied)`);
      ws.send(JSON.stringify({ type: 'error', message: 'Lobby is full' }));
      ws.close();
      return;
    }

    // --- AUTO-ASSIGN PLAYER/HOST SLOT ---
    if (isAutoAssignPlayer) {
      const availableSlot = gameInstance.net.getAvailablePlayerSlot();
      if (!availableSlot) {
        console.log(`⚠️ Connection rejected: No player slots available in lobby ${lobbyId}`);
        ws.send(JSON.stringify({ type: 'error', message: 'No player slots available' }));
        ws.close();
        return;
      }
      token = availableSlot;
      console.log(`🎯 Auto-assigned player slot: ${token}`);
    } else if (isAutoAssignHost) {
      const availableSlot = gameInstance.net.getAvailableHostSlot();
      if (!availableSlot) {
        console.log(`⚠️ Connection rejected: No host slots available in lobby ${lobbyId}`);
        ws.send(JSON.stringify({ type: 'error', message: 'No host slots available' }));
        ws.close();
        return;
      }
      token = availableSlot;
      console.log(`🎯 Auto-assigned host slot: ${token}`);
    }

    // Register client
    gameInstance.registerClient(token, ws);

    // Send the assigned role back to the client
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'role_assigned', role: token }));
    }

    // --- MESSAGE HANDLING ---
    ws.on('message', (message: Buffer | string) => {
      try {
        if (!token.startsWith('player')) return;

        const msgString = message.toString();
        const parsed = JSON.parse(msgString) as PlayerInput;

        if (typeof parsed.speed === 'number') {
          gameInstance?.handlePlayerSwing(token, parsed.speed);
        }
      } catch (e: any) {
        console.error(`Error handling message from ${token} in lobby ${lobbyId}:`, e.message);
      }
    });

    ws.on('close', () => {
      console.log(`❌ Disconnected: ${token} from lobby ${lobbyId}`);
      if (lobbyId && token) {
        const game = lobbyManager.getLobby(lobbyId);
        if (game) game.removeClient(token);
      }
    });

    ws.on('error', (err: Error) => {
      console.error(`🔴 Error on ${token} in lobby ${lobbyId}:`, err.message);
    });
  });
};

function isValidRole(token: string): token is ClientRole {
  return ['host1', 'host2', 'player1', 'player2'].includes(token);
}