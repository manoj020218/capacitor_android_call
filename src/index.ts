import { registerPlugin } from '@capacitor/core';

import type { NativeCallPlugin } from './definitions';

const NativeCall = registerPlugin<NativeCallPlugin>('NativeCall', {
  web: () => import('./web').then((m) => new m.NativeCallWeb()),
});

export * from './definitions';
export { NativeCall };
