export interface TrackingOptions {
  intervalMs?: number;
  minDistanceMeters?: number;
  accuracy?: 'high' | 'balanced' | 'low';
  batchSize?: number;
  employeeId?: string;
  foregroundTitle?: string;
  foregroundBody?: string;
}

export interface PermissionStatus {
  location: 'granted' | 'denied' | 'prompt';
  background: 'granted' | 'denied' | 'prompt';
}

export interface TrackingStatus {
  running: boolean;
  pendingCount: number;
  lastLocationAt?: string;
  options: Required<Omit<TrackingOptions, 'employeeId'>>;
}

export interface LocationPlugin {
  startTracking(options?: TrackingOptions): Promise<TrackingStatus>;
  stopTracking(): Promise<{ running: false }>;
  getCurrentLocation(): Promise<Record<string, unknown>>;
  getTrackingStatus(): Promise<TrackingStatus>;
  getPendingCount(): Promise<{ count: number }>;
  syncNow(): Promise<{ queued: true }>;
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
}
