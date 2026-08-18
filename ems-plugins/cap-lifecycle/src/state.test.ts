import { describe, expect, it } from 'vitest';
import { mapRecoveryState } from './state';

describe('lifecycle recovery mapping', () => {
  it('returns queued status transparently', () => {
    expect(mapRecoveryState(true, '2026-08-18T00:00:00Z')).toEqual({
      queued: true,
      lastRunAt: '2026-08-18T00:00:00Z',
    });
  });
});
