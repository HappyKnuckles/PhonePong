const WebSocket = require('ws');

// --- CONFIGURATION ---
const PORT = 3000;
const SERVER_URL = `ws://localhost:${PORT}`;

// HIT ZONES
const HIT_THRESHOLD = 80;

const SWING_SPEED = 1.05;
const SWING_COOLDOWN = 250;

// 10% chance to miss a ball
const MISS_CHANCE = 0.001;

// --- STATE ---
let ball = { x: 0, y: 0, v: 0 };
let prevY = 0;
let dy = 0;

let lastSwingTime = { player1: 0, player2: 0 };

// --- CONNECTIONS ---
const hostClient = new WebSocket(`${SERVER_URL}/?token=host2`);
const p1Client = new WebSocket(`${SERVER_URL}/?token=player1`);
const p2Client = new WebSocket(`${SERVER_URL}/?token=player2`);

function setupPlayer(ws, token) {
  ws.on('open', () => console.log(`✅ ${token} connected`));
  ws.on('error', (err) => console.error(`❌ ${token} error:`, err.message));
}

setupPlayer(p1Client, 'player1');
setupPlayer(p2Client, 'player2');

// --- HOST LISTENER ---
hostClient.on('open', () => console.log(`👀 Host2 connected`));

hostClient.on('message', (data) => {
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
});

// --- HELPER: Random Gyro Data ---
function getRandomSensorData() {
  return {
    gamma: Math.random() * 90 - 45,
    beta: Math.random() * 60 - 30,
  };
}

// --- SWING LOGIC ---

function swing(client, playerName) {
  const now = Date.now();
  if (now - lastSwingTime[playerName] < SWING_COOLDOWN) return;

  if (client.readyState === WebSocket.OPEN) {
    const sensor = getRandomSensorData();

    client.send(
      JSON.stringify({
        speed: SWING_SPEED,
        gamma: sensor.gamma,
        beta: sensor.beta,
      })
    );

    console.log(`⚔️ ${playerName} swung at Y:${ball.y.toFixed(1)}`);
    lastSwingTime[playerName] = now;
  }
}

// Helper to process the decision to swing or miss
function tryToSwing(client, playerName) {
  const now = Date.now();

  // Check cooldown first to avoid calculating randoms unnecessarily
  if (now - lastSwingTime[playerName] < SWING_COOLDOWN) return;

  // Random Miss Logic
  if (Math.random() < MISS_CHANCE) {
    console.log(`❌ ${playerName} WHIFFED (Missed)!`);
    lastSwingTime[playerName] = now;
    return;
  }

  swing(client, playerName);
}

function attemptServe() {
  if (Math.abs(ball.v) < 0.05) {
    console.log('🏸 Attempting to serve...');
    swing(p1Client, 'player1');
    setTimeout(() => {
      if (Math.abs(ball.v) < 0.05) swing(p2Client, 'player2');
    }, 500);
  }
}

// --- MAIN LOOP ---
setInterval(() => {
  if (dy < -0.1 && ball.y < -HIT_THRESHOLD && ball.y > -110) {
    tryToSwing(p1Client, 'player1');
  }

  if (dy > 0.1 && ball.y > HIT_THRESHOLD && ball.y < 110) {
    tryToSwing(p2Client, 'player2');
  }
}, 16);

// Watchdog to keep game running
setInterval(attemptServe, 3000);
