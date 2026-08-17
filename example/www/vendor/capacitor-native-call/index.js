import { registerPlugin } from '@capacitor/core';
const NativeCall = registerPlugin('NativeCall', {
    web: () => import('./web.js').then((m) => new m.NativeCallWeb()),
});
export * from './definitions.js';
export { NativeCall };
