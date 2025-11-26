export interface Ball {
  x: number;
  y: number;
  v: number;          // Velocity magnitude (normalized usually)
  goal: number;       // Target X coordinate
  d: number;          // Direction: -1 (Up/P1) or 1 (Down/P2)
  lastUpdate: number; // Timestamp (ms)
}