import { Server, WebSocket } from 'ws'; 
import { IncomingMessage } from 'http';
import gameManager from '../managers/GameManager';
import { ClientRole } from '../managers/NetworkManager';

interface PlayerInput {
  speed?: number;
}

export default (wss: Server) => {
  wss.on('connection', (ws: WebSocket, req: IncomingMessage) => {
    const url = req.url || '';
    const token = url.replace('/?token=', '');

    if (!isValidRole(token)) {
      console.log(`⚠️ Unknown client rejected: ${token}`);
      ws.close();
      return;
    }

    gameManager.registerClient(token, ws);


    ws.on('message', (message: Buffer | string) => {
      try {
        if (!token.startsWith('player')) return;

        const msgString = message.toString();
        const parsed = JSON.parse(msgString) as PlayerInput;

        if (typeof parsed.speed === 'number') {
          gameManager.handlePlayerSwing(token, parsed.speed);
        }
      } catch (e: any) {
        console.error(`Error handling message from ${token}:`, e.message);
      }
    });

    ws.on('close', () => {
      console.log(`❌ Disconnected: ${token}`);
      gameManager.removeClient(token);
    });

    ws.on('error', (err: Error) => {
      console.error(`🔴 Error on ${token}:`, err.message);
    });
  });
};

function isValidRole(token: string): token is ClientRole {
  return ['host1', 'host2', 'player1', 'player2'].includes(token);
}
