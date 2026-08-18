import { WebPlugin } from '@capacitor/core';
import type { CorePlugin, CorePluginConfig, DeviceRegistrationInput, NativeStatus, UserSessionInput } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class CoreWeb extends WebPlugin implements CorePlugin {
  configure(_config: CorePluginConfig) { return unsupported(); }
  setUserSession(_session: UserSessionInput) { return unsupported(); }
  registerDevice(_input?: DeviceRegistrationInput) { return unsupported(); }
  getDeviceId() { return unsupported(); }
  getNativeStatus(): Promise<NativeStatus> { return unsupported(); }
  clearSession() { return unsupported(); }
}
