# @jenix/cap-core

Shared native runtime for the EMS plugin set.

Public API:

- `configure`
- `setUserSession`
- `registerDevice`
- `getDeviceId`
- `getNativeStatus`
- `clearSession`

Responsibilities:

- backend base URL and header configuration
- secure session storage
- stable Android device identity
- common HTTP access to EMS
- queued-event persistence
- network availability state

Backend contract used:

- `POST /api/v1/devices/register`

This package is the only place that owns shared config, auth token storage,
queue storage, and device identity behavior for the plugin suite.
