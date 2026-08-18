import { WebPlugin } from '@capacitor/core';
import type { DeviceHealthPlugin, DeviceHealthStatus, HeartbeatOptions } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class DeviceHealthWeb extends WebPlugin implements DeviceHealthPlugin {
  getStatus(): Promise<DeviceHealthStatus> { return unsupported(); }
  startHeartbeat(_options?: HeartbeatOptions) { return unsupported(); }
  stopHeartbeat() { return unsupported(); }
  sendHeartbeatNow() { return unsupported(); }
}
