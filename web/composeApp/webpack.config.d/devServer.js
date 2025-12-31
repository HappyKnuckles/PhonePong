// Enable HTTPS for webpack dev server
// This allows secure WebSocket connections (wss://) from the web client
config.devServer = config.devServer || {};
config.devServer.host = '0.0.0.0';
config.devServer.allowedHosts = 'all';
config.devServer.server = 'https';
