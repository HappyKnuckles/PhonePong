# The image for the VPS: the Node server, which also serves the screen at / and the browser
# controller at /controller/. Both web parts are built with Gradle before `docker build`
# (the Deploy workflow does that):
#   dist/screen      <- web:    gradle :composeApp:wasmJsBrowserDistribution
#   dist/screen/js   <- web:    gradle :composeApp:jsBrowserDistribution (fallback without WebAssembly GC)
#   dist/controller  <- mobile: gradle :composeApp:jsBrowserDistribution

FROM --platform=$BUILDPLATFORM node:22-alpine AS build
WORKDIR /app
COPY Server/package*.json ./
RUN npm ci
COPY Server/ ./
RUN npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production \
    PORT=3000 \
    PUBLIC_DIR=/app/public
COPY Server/package*.json ./
RUN npm ci --omit=dev && npm cache clean --force
COPY --from=build /app/dist ./dist
COPY dist/screen ./public/screen
COPY dist/controller ./public/controller
USER node
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s CMD wget -qO- http://127.0.0.1:3000/health || exit 1
CMD ["node", "dist/server.js"]
