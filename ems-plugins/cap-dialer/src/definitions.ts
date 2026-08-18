export interface DialerCallState {
  active: boolean;
  number?: string;
  direction?: 'incoming' | 'outgoing';
  startedAt?: string;
}

export interface RecentCallsOptions {
  limit?: number;
}

export interface DialerPlugin {
  isDefaultDialer(): Promise<{ value: boolean }>;
  requestDefaultDialer(): Promise<{ requested: true }>;
  dial(options: { number: string }): Promise<{ started: boolean }>;
  getCallState(): Promise<DialerCallState>;
  getRecentCalls(options?: RecentCallsOptions): Promise<{ items: Record<string, unknown>[] }>;
  syncCalls(): Promise<{ queued: true }>;
}
