import type { TrackingOptions } from './definitions';

export function normalizeTrackingOptions(options: TrackingOptions = {}) {
  return {
    intervalMs: Math.max(15_000, Math.round(options.intervalMs ?? 60_000)),
    minDistanceMeters: Math.max(0, Math.round(options.minDistanceMeters ?? 20)),
    accuracy: options.accuracy ?? 'high',
    batchSize: Math.min(500, Math.max(1, Math.round(options.batchSize ?? 100))),
    foregroundTitle: options.foregroundTitle ?? 'FieldForce tracking active',
    foregroundBody: options.foregroundBody ?? 'Location sync is running in the background',
  };
}
