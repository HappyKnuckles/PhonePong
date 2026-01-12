// Configure webpack to exclude Node.js core modules for browser builds
config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};

// Set Node.js modules to false to exclude them from the browser bundle
config.resolve.fallback = {
    ...config.resolve.fallback,
    "zlib": false,
    "stream": false,
    "net": false,
    "tls": false,
    "crypto": false,
    "http": false,
    "https": false,
    "url": false,
    "bufferutil": false,
    "utf-8-validate": false,
    "fs": false,
    "path": false,
    "os": false
};

// Ignore optional dependencies that are Node.js-specific
config.externals = config.externals || [];
config.externals.push({
    'bufferutil': 'bufferutil',
    'utf-8-validate': 'utf-8-validate'
});

console.log('✅ Webpack configured to exclude Node.js modules from browser build');
