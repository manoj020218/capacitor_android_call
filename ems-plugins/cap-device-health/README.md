# @jenix/cap-device-health

Uploads native device heartbeat payloads to EMS without requiring the WebView
to stay alive.

Public API:

- `getStatus`
- `startHeartbeat`
- `stopHeartbeat`
- `sendHeartbeatNow`

Backend contract used:

- `POST /api/v1/device-health/heartbeat`
