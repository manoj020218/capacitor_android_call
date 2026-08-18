# @jenix/cap-push

Routes EMS command payloads from the host app's single `FirebaseMessagingService`.

Public API:

- `getToken`
- `refreshRegistration`
- `getPushStatus`
- `dispatchPayload`

Important:

- This package does not register its own `FirebaseMessagingService`.
- Reuse the already-tested `capacitor-native-call` plugin for actual incoming
  call wake/ring behavior.
