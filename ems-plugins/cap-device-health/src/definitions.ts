export interface HeartbeatOptions {
  intervalMinutes?: number;
}

export interface DeviceHealthStatus {
  timestamp: string;
  batteryPercent?: number;
  charging?: boolean;
  gpsEnabled: boolean;
  networkType: string;
  internetAvailable: boolean;
  appVersion?: string;
  androidVersion: string;
  manufacturer: string;
  model: string;
  trackingServiceRunning: boolean;
  lastLocationAt?: string;
}

export interface DeviceHealthPlugin {
  getStatus(): Promise<DeviceHealthStatus>;
  startHeartbeat(options?: HeartbeatOptions): Promise<{ running: true; intervalMinutes: number }>;
  stopHeartbeat(): Promise<{ running: false }>;
  sendHeartbeatNow(): Promise<{ queued: true }>;
}
