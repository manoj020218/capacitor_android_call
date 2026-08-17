import { WebPlugin } from '@capacitor/core';

import type {
  FullScreenIntentPermissionStatus,
  InitializeOptions,
  NativeCallPlugin,
  SetRingtoneOptions,
  StopRingingOptions,
} from './definitions';

const UNSUPPORTED = '[NativeCall] is Android-only — this call is a no-op on web/iOS.';

export class NativeCallWeb extends WebPlugin implements NativeCallPlugin {
  async initialize(_options: InitializeOptions): Promise<void> {
    console.warn(UNSUPPORTED);
  }

  async stopRinging(_options: StopRingingOptions): Promise<void> {
    console.warn(UNSUPPORTED);
  }

  async setRingtone(_options: SetRingtoneOptions): Promise<void> {
    console.warn(UNSUPPORTED);
  }

  async checkFullScreenIntentPermission(): Promise<FullScreenIntentPermissionStatus> {
    console.warn(UNSUPPORTED);
    return { granted: false };
  }

  async requestFullScreenIntentPermission(): Promise<void> {
    console.warn(UNSUPPORTED);
  }
}
