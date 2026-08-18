import { describe, expect, it } from 'vitest';
import { readCommandType } from './commands';

describe('push command parser', () => {
  it('accepts known command types', () => {
    expect(readCommandType({ commandType: 'SYNC_NOW' })).toBe('SYNC_NOW');
  });

  it('rejects unknown command types', () => {
    expect(readCommandType({ commandType: 'NOPE' })).toBeUndefined();
  });
});
