import { WebPlugin } from '@capacitor/core';
import type { BootState, LifecyclePlugin, RecoveryStatus } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class LifecycleWeb extends WebPlugin implements LifecyclePlugin {
  getLastBootState(): Promise<BootState> { return unsupported(); }
  getRecoveryStatus(): Promise<RecoveryStatus> { return unsupported(); }
}
