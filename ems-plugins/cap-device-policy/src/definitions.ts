export interface ManagementStatus {
  managed: boolean;
  deviceOwner: boolean;
  profileOwner: boolean;
  deviceAdminActive: boolean;
}

export interface PolicyCapabilities {
  canLockNow: boolean;
  canWipeData: boolean;
  canSetKeyguardDisabled: boolean;
}

export interface DevicePolicyPlugin {
  getManagementStatus(): Promise<ManagementStatus>;
  getCapabilities(): Promise<PolicyCapabilities>;
  isDeviceOwner(): Promise<{ value: boolean }>;
  isProfileOwner(): Promise<{ value: boolean }>;
}
