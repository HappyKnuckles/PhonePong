export default {
  VSCALE: 150.0,        // scale velocity
  FREQUENCY_MS: 30,     // frequency of updates to hosts in milliseconds
  TABLE_X_LIMIT: 100,  // x boundary for collision checks
  TABLE_Y_LIMIT: 100,  // y boundary for collision checks
  OUTERBOUND: 140,      // y boundary for scoring
  INNERBOUND: 70,       // y boundary for collision checks
  HEADLESS: false,      // test without hosts
  TEST: false,          // run test sequence on start
  USE_TIMEOUT: 1000,    // enable timeout after score
  HIT_COOLDOWN: 200,    // minimum time (ms) between consecutive hits to prevent double-hitting
  PORT: process.env.PORT ? Number(process.env.PORT) : 3000
};