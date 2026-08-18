import { WebPlugin } from '@capacitor/core';
import type { DevicePolicyPlugin, ManagementStatus, PolicyCapabilities } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class DevicePolicyWeb extends WebPlugin implements DevicePolicyPlugin {
  getManagementStatus(): Promise<ManagementStatus> { return unsupported(); }
  getCapabilities(): Promise<PolicyCapabilities> { return unsupported(); }
  isDeviceOwner() { return unsupported(); }
  isProfileOwner() { return unsupported(); }
}
