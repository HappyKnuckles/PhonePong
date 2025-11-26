import http from 'http';
import express from 'express';
import WebSocket from 'ws';
import config from './config/config';
import socketHandler from './src/handlers/SocketHandler';

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

socketHandler(wss);

server.listen(config.PORT, () => {
  console.log(`🚀 Server running on http://0.0.0.0:${config.PORT}`);
  if (config.HEADLESS) {
    console.log(
      '🤖 Running in HEADLESS mode (Waiting for players to connect...)'
    );
  }
});
