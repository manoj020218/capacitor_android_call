import type { ManagementStatus, PolicyCapabilities } from './definitions';

export function deriveCapabilities(status: ManagementStatus): PolicyCapabilities {
  return {
    canLockNow: status.deviceAdminActive,
    canWipeData: status.deviceOwner || status.profileOwner,
    canSetKeyguardDisabled: status.deviceOwner || status.profileOwner,
  };
}
