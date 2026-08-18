import { WebPlugin } from '@capacitor/core';
import type { PushPlugin, PushStatus } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class PushWeb extends WebPlugin implements PushPlugin {
  getToken() { return unsupported(); }
  refreshRegistration() { return unsupported(); }
  getPushStatus(): Promise<PushStatus> { return unsupported(); }
  dispatchPayload(_payload: Record<string, string>) { return unsupported(); }
}
