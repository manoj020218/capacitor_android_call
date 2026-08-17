import { WebPlugin } from '@capacitor/core';
const UNSUPPORTED = '[NativeCall] is Android-only — this call is a no-op on web/iOS.';
export class NativeCallWeb extends WebPlugin {
    async initialize(_options) {
        console.warn(UNSUPPORTED);
    }
    async stopRinging(_options) {
        console.warn(UNSUPPORTED);
    }
    async setRingtone(_options) {
        console.warn(UNSUPPORTED);
    }
    async checkFullScreenIntentPermission() {
        console.warn(UNSUPPORTED);
        return { granted: false };
    }
    async requestFullScreenIntentPermission() {
        console.warn(UNSUPPORTED);
    }
}
