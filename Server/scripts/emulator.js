const WebSocket = require('ws');

// --- CONFIGURATION ---
const PORT = 3000;
const SERVER_URL = `ws://localhost:${PORT}`;

// HIT ZONES
const HIT_THRESHOLD = 95;
const SWING_COOLDOWN = 250;
const MISS_CHANCE = 0.01;

// --- STATE ---
let ball = { x: 0, y: 0, v: 0 };
let prevY = 0;
let dy = 0;

let lastSwingTime = { player1: 0, player2: 0 };
let lobbyId = null;

// Declare clients (initialized later)
let hostClient = null;
let p1Client = null;
let p2Client = null;

// --- INITIALIZATION ---

function startBot() {
  console.log('🤖 Bot starting... Attempting to create lobby as Player 1');

  // 1. Connect Player 1 first to create the lobby
  p1Client = new WebSocket(`${SERVER_URL}/?token=player1&action=create`);

  p1Client.on('open', () => console.log(`✅ Player 1 connected (Creator)`));

  p1Client.on('message', (data) => {
    try {
      const parsed = JSON.parse(data);

      // 2. Listen for Lobby ID
      if (parsed.type === 'lobby_created') {
        lobbyId = parsed.lobbyId;
        console.log(`🎉 Lobby Created: ${lobbyId}`);
        connectOthers(lobbyId);
      }
    } catch (e) {
      // Ignore non-JSON or unrelated messages during setup
    }
  });

  p1Client.on('error', (err) => console.error(`❌ P1 Error:`, err.message));
}

function connectOthers(id) {
  console.log(`🔗 Connecting Player 2 and Host 2 to lobby ${id}...`);

  // 3. Connect remaining clients to the specific lobby
  p2Client = new WebSocket(`${SERVER_URL}/?token=player2&lobby=${id}`);
  hostClient = new WebSocket(`${SERVER_URL}/?token=host2&lobby=${id}`);

  setupClient(p2Client, 'player2');
  setupClient(hostClient, 'host2');

  // 4. Attach Game Logic Listeners once Host is ready
  hostClient.on('message', handleHostMessage);
}

function setupClient(ws, token) {
  ws.on('open', () => console.log(`✅ ${token} connected to lobby ${lobbyId}`));
  ws.on('error', (err) => console.error(`❌ ${token} error:`, err.message));
}

// --- GAME LOGIC LISTENERS ---

function handleHostMessage(data) {
  try {
    const parsed = JSON.parse(data);

    if (parsed.type === 'coordinates') {
      prevY = ball.y;
      ball = parsed.data;
      dy = ball.y - prevY;
    }

    if (parsed.type === 'score') {
      console.log(`📢 Score: ${parsed.message}`);
      ball.v = 0;
      setTimeout(attemptServe, 1500);
    }
  } catch (e) {
    console.error('Error parsing host data', e);
  }
}

// --- HELPER: Random Gyro Data ---
function getRandomSensorData() {
  return {
    speed: Math.random() * 0.5 + 1,
    gamma: Math.random() * 90 - 45,
    beta: Math.random() * 60 - 30,
  };
}

// --- SWING LOGIC ---

function swing(client, playerName) {
  if (!client || client.readyState !== WebSocket.OPEN) return;

  const now = Date.now();
  if (now - lastSwingTime[playerName] < SWING_COOLDOWN) return;

  const sensor = getRandomSensorData();

  client.send(
    JSON.stringify({
      speed: sensor.speed,
      gamma: sensor.gamma,
      beta: sensor.beta,
    })
  );

  console.log(`⚔️ ${playerName} swung at Y:${ball.y.toFixed(1)}`);
  lastSwingTime[playerName] = now;
}

function tryToSwing(client, playerName) {
  const now = Date.now();

  if (now - lastSwingTime[playerName] < SWING_COOLDOWN) return;

  if (Math.random() < MISS_CHANCE) {
    console.log(`❌ ${playerName} WHIFFED (Missed)!`);
    lastSwingTime[playerName] = now;
    return;
  }

  swing(client, playerName);
}

function attemptServe() {
  if (Math.abs(ball.v) < 0.05 && lobbyId) {
    console.log('🏸 Attempting to serve...');
    if (p1Client) swing(p1Client, 'player1');
    setTimeout(() => {
      if (Math.abs(ball.v) < 0.05 && p2Client) swing(p2Client, 'player2');
    }, 500);
  }
}

// --- MAIN LOOP ---
setInterval(() => {
  // Wait until lobby is set up
  if (!lobbyId || !p1Client || !p2Client) return;

  const isEdgeBounce = Math.abs(ball.y) >= 99;

  // 100% hit rate on edge, 5% hit rate on volley/short bounce
  const shouldHit = isEdgeBounce || Math.random() < 0.05;

  if (!shouldHit) return;

  if (dy < -0.1 && ball.y < -HIT_THRESHOLD && ball.y > -110) {
    tryToSwing(p1Client, 'player1');
  }

  if (dy > 0.1 && ball.y > HIT_THRESHOLD && ball.y < 110) {
    tryToSwing(p2Client, 'player2');
  }
}, 16);

// Watchdog to keep game running
setInterval(attemptServe, 3000);

// --- START ---
startBot();
