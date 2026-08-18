# @jenix/cap-location

Owns the EMS location foreground service, offline queue, and batch uploader.

Public API:

- `startTracking`
- `stopTracking`
- `getCurrentLocation`
- `getTrackingStatus`
- `getPendingCount`
- `syncNow`
- `checkPermissions`
- `requestPermissions`

Backend contract used:

- `POST /api/v1/locations/batch`
