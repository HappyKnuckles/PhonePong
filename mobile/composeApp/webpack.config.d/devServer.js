// Expose dev server to network for mobile device testing
config.devServer = config.devServer || {};
config.devServer.host = '0.0.0.0';
config.devServer.allowedHosts = 'all';
