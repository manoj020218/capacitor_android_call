export type CommandType =
  | 'VIDEO_CALL'
  | 'SYNC_NOW'
  | 'CONFIG_UPDATED'
  | 'ADMIN_MESSAGE'
  | 'DEVICE_STATUS_REQUEST';

export interface PushStatus {
  token?: string;
  configured: boolean;
  nativeCallBridgeAvailable: boolean;
}

export interface PushPlugin {
  getToken(): Promise<{ token?: string }>;
  refreshRegistration(options?: { token?: string }): Promise<{ uploaded: boolean }>;
  getPushStatus(): Promise<PushStatus>;
  dispatchPayload(payload: Record<string, string>): Promise<{ handled: boolean; commandType?: CommandType }>;
}
