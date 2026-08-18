# EMS Native Plugins

This folder contains the reusable Android-first Capacitor plugin layer for the
EMS backend in `../`.

Packages:

- `cap-core`: shared config, session, queue, device identity, HTTP, status
- `cap-device-health`: heartbeat scheduling and device health upload
- `cap-location`: foreground/background location collection and batch sync
- `cap-lifecycle`: boot and package-replaced recovery coordination
- `cap-push`: command routing and FCM token integration
- `cap-dialer`: dialer role bridge and call metadata sync
- `cap-device-policy`: device-owner/profile-owner capability detection

Notes:

- The already-tested incoming-call flow remains external in
  `D:\IOT Device\QRunlock\capacitor-native-call`.
- `cap-push` is designed to forward `VIDEO_CALL` commands into that plugin's
  native handler when it is present.
- EMS backend contracts come from `../BACKEND_HANDOFF.md` and the modules under
  `../src/modules/`.

Workspace commands:

```bash
npm install
npm run build
npm run test
npm run check:lines
```
