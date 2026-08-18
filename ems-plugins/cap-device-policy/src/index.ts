import { registerPlugin } from '@capacitor/core';
import type { DevicePolicyPlugin } from './definitions';

const DevicePolicy = registerPlugin<DevicePolicyPlugin>('JenixDevicePolicy', {
  web: () => import('./web').then((m) => new m.DevicePolicyWeb()),
});

export * from './capabilities';
export * from './definitions';
export { DevicePolicy };
