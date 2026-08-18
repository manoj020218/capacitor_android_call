import { describe, expect, it } from 'vitest';
import { buildExternalCallId } from './call-id';

describe('external call ids', () => {
  it('keeps device, direction, time, and number stable', () => {
    expect(buildExternalCallId('abc', '+9199', '2026-08-18T10:00:00Z', 'incoming'))
      .toBe('abc:incoming:2026-08-18T10:00:00Z:+9199');
  });
});
