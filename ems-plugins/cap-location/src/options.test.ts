import { describe, expect, it } from 'vitest';
import { normalizeTrackingOptions } from './options';

describe('location option normalization', () => {
  it('applies defaults', () => {
    expect(normalizeTrackingOptions()).toEqual({
      intervalMs: 60000,
      minDistanceMeters: 20,
      accuracy: 'high',
      batchSize: 100,
      foregroundTitle: 'FieldForce tracking active',
      foregroundBody: 'Location sync is running in the background',
    });
  });

  it('clamps low values', () => {
    expect(normalizeTrackingOptions({ intervalMs: 1, minDistanceMeters: -2, batchSize: 900 }).batchSize).toBe(500);
  });
});
