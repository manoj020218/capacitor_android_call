import { registerPlugin } from '@capacitor/core';
import type { DeviceHealthPlugin } from './definitions';

const DeviceHealth = registerPlugin<DeviceHealthPlugin>('JenixDeviceHealth', {
  web: () => import('./web').then((m) => new m.DeviceHealthWeb()),
});

export * from './definitions';
export * from './options';
export { DeviceHealth };
