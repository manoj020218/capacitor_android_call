import { registerPlugin } from '@capacitor/core';
import type { CorePlugin } from './definitions';

const Core = registerPlugin<CorePlugin>('JenixCore', {
  web: () => import('./web').then((m) => new m.CoreWeb()),
});

export * from './config';
export * from './definitions';
export { Core };
