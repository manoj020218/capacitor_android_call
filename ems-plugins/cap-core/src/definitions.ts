export interface CorePluginConfig {
  baseUrl: string;
  companyId?: string;
  defaultHeaders?: Record<string, string>;
}

export interface UserSessionInput {
  accessToken: string;
  refreshToken?: string;
  userId?: string;
  employeeId?: string;
  companyId?: string;
}

export interface DeviceRegistrationInput {
  employeeId?: string;
  platform?: string;
  manufacturer?: string;
  model?: string;
  androidVersion?: string;
  appVersion?: string;
  deviceName?: string;
  fcmToken?: string;
  managed?: boolean;
}

export interface NativeStatus {
  configured: boolean;
  hasUserAccessToken: boolean;
  hasDeviceToken: boolean;
  networkAvailable: boolean;
  baseUrl?: string;
  deviceId: string;
}

export interface CorePlugin {
  configure(config: CorePluginConfig): Promise<{ configured: true; baseUrl: string }>;
  setUserSession(session: UserSessionInput): Promise<void>;
  registerDevice(input?: DeviceRegistrationInput): Promise<{
    deviceId: string;
    deviceTokenStored: boolean;
  }>;
  getDeviceId(): Promise<{ deviceId: string }>;
  getNativeStatus(): Promise<NativeStatus>;
  clearSession(): Promise<void>;
}
