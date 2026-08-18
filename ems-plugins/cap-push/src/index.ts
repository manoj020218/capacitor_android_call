import { registerPlugin } from '@capacitor/core';
import type { PushPlugin } from './definitions';

const Push = registerPlugin<PushPlugin>('JenixPush', {
  web: () => import('./web').then((m) => new m.PushWeb()),
});

export * from './commands';
export * from './definitions';
export { Push };
