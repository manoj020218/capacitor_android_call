export interface BootState {
  lastEvent: 'boot' | 'package_replaced' | 'manual' | 'unknown';
  lastEventAt?: string;
}

export interface RecoveryStatus {
  queued: boolean;
  lastRunAt?: string;
}

export interface LifecyclePlugin {
  getLastBootState(): Promise<BootState>;
  getRecoveryStatus(): Promise<RecoveryStatus>;
}
