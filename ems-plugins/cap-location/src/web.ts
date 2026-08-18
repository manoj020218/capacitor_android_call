import { WebPlugin } from '@capacitor/core';
import type { LocationPlugin, PermissionStatus, TrackingOptions, TrackingStatus } from './definitions';

const unsupported = () => Promise.reject(new Error('Android only plugin'));

export class LocationWeb extends WebPlugin implements LocationPlugin {
  startTracking(_options?: TrackingOptions): Promise<TrackingStatus> { return unsupported(); }
  stopTracking() { return unsupported(); }
  getCurrentLocation() { return unsupported(); }
  getTrackingStatus(): Promise<TrackingStatus> { return unsupported(); }
  getPendingCount() { return unsupported(); }
  syncNow() { return unsupported(); }
  checkPermissions(): Promise<PermissionStatus> { return unsupported(); }
  requestPermissions(): Promise<PermissionStatus> { return unsupported(); }
}
