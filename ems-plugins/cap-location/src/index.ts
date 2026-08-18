import { registerPlugin } from '@capacitor/core';
import type { LocationPlugin } from './definitions';

const Location = registerPlugin<LocationPlugin>('JenixLocation', {
  web: () => import('./web').then((m) => new m.LocationWeb()),
});

export * from './definitions';
export * from './options';
export { Location };
