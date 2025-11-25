module.exports = {
    VSCALE: 300.0,        // scale velocity
    FREQUENCY_MS: 30,    // frequency of updates to hosts in milliseconds
    OUTERBOUND: 140,      // y boundary for scoring
    INNERBOUND: 70,       // y boundary for collision checks
    HEADLESS: false,      // test without hosts
    TEST: false,          // run test sequence on start
    USE_TIMEOUT: 1000,    // enable timeout after score
    PORT: process.env.PORT || 3000
};