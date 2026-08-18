import { describe, expect, it } from 'vitest';
import { normalizeHeartbeatInterval } from './options';

describe('device health interval normalization', () => {
  it('defaults to 15 minutes', () => {
    expect(normalizeHeartbeatInterval()).toBe(15);
  });

  it('clamps values below 15', () => {
    expect(normalizeHeartbeatInterval(2)).toBe(15);
  });

  it('rounds valid intervals', () => {
    expect(normalizeHeartbeatInterval(21.2)).toBe(21);
  });
});
