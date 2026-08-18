import { registerPlugin } from '@capacitor/core';
import type { DialerPlugin } from './definitions';

const Dialer = registerPlugin<DialerPlugin>('JenixDialer', {
  web: () => import('./web').then((m) => new m.DialerWeb()),
});

export * from './call-id';
export * from './definitions';
export { Dialer };
