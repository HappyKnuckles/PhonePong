export interface Ball {
  x: number;
  y: number; // Depth (Table Length)
  z: number; // Height (Up/Down) - This is the "Y Animation"
  v: number; // Velocity
  d: number; // Direction (1 or -1)
  goal: number; // Target X

  // New fields for trajectory calculation
  startY: number;   // Where the ball was hit from
  bounceY: number;  // Calculated landing spot (Depth)
  state: 'FLIGHT' | 'BOUNCE' | 'SERVE_FLIGHT';
  lastUpdate: number;
}

export type PhysicsResult = 'CONTINUE' | 'BOUNCE' | 'FLOOR_HIT';
