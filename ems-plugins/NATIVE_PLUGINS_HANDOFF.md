# Native Plugins Handoff

Last updated: 2026-08-18

## cap-core

- status: implemented
- public API: `configure`, `setUserSession`, `registerDevice`, `getDeviceId`, `getNativeStatus`, `clearSession`
- native pieces: secure prefs, device ID, HTTP client, queue store, network status
- permissions: none beyond internet/network access
- backend endpoints: `POST /api/v1/devices/register`
- tests: base URL normalization and header redaction
- manual tests: configure, register, clear session, verify network event
- known limitations: Android source reuse is local-workspace based for now

## cap-device-health

- status: implemented
- public API: `getStatus`, `startHeartbeat`, `stopHeartbeat`, `sendHeartbeatNow`
- native pieces: heartbeat worker, status monitor, scheduler, uploader
- permissions: network access only
- backend endpoints: `POST /api/v1/device-health/heartbeat`
- tests: interval normalization
- manual tests: background heartbeat with app foreground, background, and killed
- known limitations: battery-change events depend on app process being alive

## cap-location

- status: implemented
- public API: `startTracking`, `stopTracking`, `getCurrentLocation`, `getTrackingStatus`, `getPendingCount`, `syncNow`, `checkPermissions`, `requestPermissions`
- native pieces: foreground service, queue store, sync worker, recovery receiver
- permissions: coarse, fine, background location, notifications, foreground service
- backend endpoints: `POST /api/v1/locations/batch`
- tests: tracking option normalization
- manual tests: locked screen + killed app, screen on + killed app, app visible
- known limitations: duplicate protection relies on timestamp/device dedupe

## cap-lifecycle

- status: implemented
- public API: `getLastBootState`, `getRecoveryStatus`
- native pieces: boot receiver, package-replaced receiver, recovery worker
- permissions: `RECEIVE_BOOT_COMPLETED`
- backend endpoints: none directly
- tests: recovery state mapping
- manual tests: reboot recovery and app update recovery
- known limitations: recovery uses supported broadcasts/work scheduling only

## cap-push

- status: implemented
- public API: `getToken`, `refreshRegistration`, `getPushStatus`, `dispatchPayload`
- native pieces: command router, token uploader, notification helper, event mailbox
- permissions: notifications, internet, network access
- backend endpoints: `PATCH /api/v1/devices/:deviceId/fcm-token`
- tests: command parsing
- manual tests: `SYNC_NOW`, `CONFIG_UPDATED`, `ADMIN_MESSAGE`, `DEVICE_STATUS_REQUEST`, `VIDEO_CALL`
- known limitations: host app must provide the single `FirebaseMessagingService`

## cap-dialer

- status: implemented
- public API: `isDefaultDialer`, `requestDefaultDialer`, `dial`, `getCallState`, `getRecentCalls`, `syncCalls`
- native pieces: dialer role helper, in-call service, call log reader, sync worker
- permissions: call phone, read call log, read phone state, bind in-call service
- backend endpoints: `POST /api/v1/calls/ingest`
- tests: external call ID formatting
- manual tests: incoming, outgoing, missed, rejected, failed, dual-SIM inspection
- known limitations: backend contract has no dual-SIM field, so it stays local

## cap-device-policy

- status: implemented
- public API: `getManagementStatus`, `getCapabilities`, `isDeviceOwner`, `isProfileOwner`
- native pieces: device admin receiver, capability reporter
- permissions: none beyond device admin configuration by host app
- backend endpoints: none directly
- tests: capability derivation
- manual tests: unmanaged, profile owner, device owner
- known limitations: read-only capability bridge, no privilege escalation

## Next Exact Step

1. Install workspace dependencies in `EMS/Plugins`.
2. Run `npm run build`, `npm run test`, and `npm run check:lines`.
3. Wire the packages into the actual Capacitor Android host app.
4. Validate the physical-device scenarios listed above.
