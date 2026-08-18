import { describe, expect, it } from 'vitest';
import { deriveCapabilities } from './capabilities';

describe('device policy capabilities', () => {
  it('maps profile owner to management actions', () => {
    expect(deriveCapabilities({
      managed: true,
      deviceOwner: false,
      profileOwner: true,
      deviceAdminActive: true,
    })).toEqual({
      canLockNow: true,
      canWipeData: true,
      canSetKeyguardDisabled: true,
    });
  });
});
