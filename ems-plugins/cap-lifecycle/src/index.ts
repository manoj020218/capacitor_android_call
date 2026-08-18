import { registerPlugin } from '@capacitor/core';
import type { LifecyclePlugin } from './definitions';

const Lifecycle = registerPlugin<LifecyclePlugin>('JenixLifecycle', {
  web: () => import('./web').then((m) => new m.LifecycleWeb()),
});

export * from './definitions';
export * from './state';
export { Lifecycle };
