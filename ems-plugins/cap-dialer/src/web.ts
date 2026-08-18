import { WebPlugin } from '@capacitor/core';
import type { DialerCallState, DialerPlugin, RecentCallsOptions } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class DialerWeb extends WebPlugin implements DialerPlugin {
  isDefaultDialer() { return unsupported(); }
  requestDefaultDialer() { return unsupported(); }
  dial(_options: { number: string }) { return unsupported(); }
  getCallState(): Promise<DialerCallState> { return unsupported(); }
  getRecentCalls(_options?: RecentCallsOptions) { return unsupported(); }
  syncCalls() { return unsupported(); }
}
