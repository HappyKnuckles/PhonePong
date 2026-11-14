import https from 'https';
import fs from 'fs';
import path from 'path';
import express from 'express';
import WebSocket from 'ws';
import config from './config/config';
import socketHandler from './src/handlers/SocketHandler';

const app = express();

app.get('/health', (req, res) => {
  res.send('ok');
});

// In the container, the built screen (web) and browser controller (mobile js) are served from here too:
// the screen at /, the controller at /controller/. Locally PUBLIC_DIR is unset and / shows the certificate page.
const publicDir = process.env.PUBLIC_DIR;
if (publicDir) {
  app.use('/controller', express.static(path.join(publicDir, 'controller')));
  app.use(express.static(path.join(publicDir, 'screen')));
}

// Serve a simple page to help accept the certificate on mobile devices
app.get('/', (req, res) => {
  res.send(`
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>PhonePong Server</title>
      <style>
        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 20px; background: #1a1a2e; color: #eee; }
        h1 { color: #4ecca3; }
        .success { background: #4ecca3; color: #1a1a2e; padding: 20px; border-radius: 10px; margin: 20px 0; }
        .info { background: #393e46; padding: 15px; border-radius: 8px; margin: 10px 0; }
        .btn { display: inline-block; background: #4ecca3; color: #1a1a2e; padding: 15px 30px; border-radius: 8px; text-decoration: none; font-weight: bold; margin: 10px 0; }
        code { background: #222; padding: 2px 6px; border-radius: 4px; }
      </style>
    </head>
    <body>
      <h1>🏓 PhonePong Server</h1>
      <div class="success">
        <h2>✅ Certificate Accepted!</h2>
        <p>You can now close this page and open the PhonePong app.</p>
        <p>The WebSocket connection (wss://) will now work.</p>
      </div>
      <div class="info">
        <p><strong>Server:</strong> Running on port ${config.PORT}</p>
        <p><strong>Protocol:</strong> HTTPS + WSS</p>
      </div>
      <div class="info">
        <h3>📱 iOS: If WebSocket still doesn't work</h3>
        <p>Download and install the certificate:</p>
        <a class="btn" href="/cert">Download Certificate</a>
        <p>Then go to: Settings → General → About → Certificate Trust Settings → Enable trust</p>
      </div>
    </body>
    </html>
  `);
});

// Endpoint to download the certificate for iOS
app.get('/cert', (req, res) => {
  const certPath = path.join(__dirname, 'certs', 'server.crt');
  if (fs.existsSync(certPath)) {
    res.setHeader('Content-Type', 'application/x-x509-ca-cert');
    res.setHeader('Content-Disposition', 'attachment; filename="phonepong-server.crt"');
    res.sendFile(certPath);
  } else {
    res.status(404).send('Certificate not found');
  }
});

// SSL Certificate options
const certPath = path.join(__dirname, 'certs', 'server.crt');
const keyPath = path.join(__dirname, 'certs', 'server.key');

let server;

if (fs.existsSync(certPath) && fs.existsSync(keyPath)) {
  // HTTPS mode with SSL certificates
  const sslOptions = {
    key: fs.readFileSync(keyPath),
    cert: fs.readFileSync(certPath),
  };
  server = https.createServer(sslOptions, app);
  console.log('🔒 SSL certificates loaded - running in HTTPS mode');
} else {
  // Fallback to HTTP if no certificates found
  console.warn('⚠️  SSL certificates not found. Run: cd certs && powershell -ExecutionPolicy Bypass -File generate-certs.ps1');
  console.warn('   Falling back to HTTP mode (mobile HTTPS clients may not connect)');
  const http = require('http');
  server = http.createServer(app);
}

const wss = new WebSocket.Server({ server });

socketHandler(wss);

server.listen(config.PORT, () => {
  const protocol = fs.existsSync(certPath) ? 'https' : 'http';
  console.log(`🚀 Server running on ${protocol}://0.0.0.0:${config.PORT}`);
  if (config.HEADLESS) {
    console.log(
      '🤖 Running in HEADLESS mode (Waiting for players to connect...)'
    );
  }
});
