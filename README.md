# capacitor-native-call

A standalone Capacitor plugin that gives an Android app a real VoIP-style
incoming-call experience: a full-screen intent that wakes a locked screen, a
foreground service that rings until answered, and a typed JS/TS bridge — no
Kotlin required in the host app for the common case.

Background and design rationale live in [`PLAN.md`](./PLAN.md). This README
is the how-to; PLAN.md is the why.

**Platform support:** Android only. iOS is a stub that rejects every call —
see [PLAN.md §7](./PLAN.md#7-explicit-non-goals-for-now).

---

## Install

```bash
npm install capacitor-native-call
npx cap sync android
```

### 1. Add your ringtone files

Drop each ringtone audio file into the host app's own resources — this
plugin never bundles ringtone assets (PLAN.md §7):

```
android/app/src/main/res/raw/ringtone_classic.mp3
android/app/src/main/res/raw/ringtone_chime.mp3
```

### 2. Wire up Firebase Cloud Messaging

This plugin's ringing is triggered by a **data-only** FCM message, so it
reaches native code even with the app fully killed. You need:

- A Firebase project with Android app registered, `google-services.json` in
  `android/app/`
- `com.google.gms.google-services` applied in `android/app/build.gradle` and
  the classpath added in the project-level `android/build.gradle`

The plugin's own `android/build.gradle` already depends on
`firebase-messaging-ktx` — you don't add that yourself.

**If this is the only FCM listener in your app** (no
`@capacitor/push-notifications`, no custom `FirebaseMessagingService`), you're
done — `CallMessagingService` is already declared in the plugin's manifest
and will receive messages automatically.

**If you already have another `FirebaseMessagingService`** (most commonly via
`@capacitor/push-notifications`): Android only delivers FCM messages to one
`FirebaseMessagingService` per app, so the two will conflict. Remove this
plugin's service from your merged manifest and call the handler yourself:

```xml
<!-- android/app/src/main/AndroidManifest.xml -->
<service
    android:name="com.nativecall.plugin.CallMessagingService"
    tools:node="remove" />
```

```kotlin
// In your own FirebaseMessagingService.onMessageReceived(message):
import com.nativecall.plugin.NativeCallMessageHandler

override fun onMessageReceived(message: RemoteMessage) {
    if (NativeCallMessageHandler.handleData(applicationContext, message.data)) {
        return // it was an incoming_call message, already handled
    }
    // ... your existing handling for other message types
}
```

### 3. Server-side payload contract

```json
{
  "data": {
    "type": "incoming_call",
    "callId": "unique-id-for-this-call",
    "title": "Front Gate",
    "body": "Visitor calling",
    "ringtone": "classic"
  }
}
```

`ringtone` must match a `key` from the `ringtones` array passed to
`initialize()`; omit it to use the app's current default.

**This must be a data-only message** (no top-level `notification` key) — a
`notification` block is delivered straight to the system tray by Google Play
services when the app is backgrounded, bypassing this plugin's code entirely.

---

## Usage

```ts
import { NativeCall } from 'capacitor-native-call';

// Once, at app startup, before any call can arrive:
await NativeCall.initialize({
  ringtones: [
    { key: 'classic', resourceName: 'ringtone_classic' },
    { key: 'chime', resourceName: 'ringtone_chime' },
  ],
  defaultRingtone: 'classic',
  ringDurationSeconds: 45,
});

// Android 14+ requires explicit user permission for full-screen intents —
// without it, calls degrade to a normal heads-up notification that won't
// wake a locked screen. Check once after initialize():
const { granted } = await NativeCall.checkFullScreenIntentPermission();
if (!granted) {
  // Show your own explanation UI, then:
  await NativeCall.requestFullScreenIntentPermission();
}

// Native code starts ringing automatically when an incoming_call FCM message
// arrives — nothing to call from JS for that part. Just listen for outcomes:
NativeCall.addListener('callAnswered', ({ callId }) => {
  // Navigate to your call screen and connect signaling (WebRTC, Socket.IO, ...).
});
NativeCall.addListener('callDeclined', ({ callId }) => {
  // Tell your server the callee declined.
});
NativeCall.addListener('callTimedOut', ({ callId }) => {
  // Mark the call missed.
});

// If the caller cancels before being answered:
await NativeCall.stopRinging({ callId });

// Change which ringtone future calls use:
await NativeCall.setRingtone({ key: 'chime' });
```

### Notification permission (Android 13+)

Posting any notification — including the full-screen one this plugin uses —
requires the runtime `POST_NOTIFICATIONS` permission on Android 13+. This
plugin declares the permission in its manifest but does **not** prompt for it
itself, so the request fits into whatever permission-priming flow the host
app already has (e.g. via `@capacitor/push-notifications`'
`requestPermissions()`, which shares the same OS permission). If it's never
granted, the foreground service still rings and vibrates, but no notification
— and therefore no lock-screen wake — appears.

### Known OEM caveats (Xiaomi/MIUI/HyperOS, and likely similar skins)

Confirmed on-device on a Xiaomi HyperOS (Android 15) phone — two OEM-specific
settings, beyond stock Android permissions, were required for a locked device
to actually wake to the call screen:

- **Autostart.** MIUI/HyperOS blocks a backgrounded app's foreground-service
  starts unless the app is allowed to "Autostart" (Settings → Apps → Manage
  apps → *your app* → Autostart, or Security app → Autostart). Without it, the
  ringtone/vibration/notification never fire at all when the app isn't
  already running. Real calling apps commonly prompt users to enable this on
  first launch on Xiaomi devices; this plugin doesn't do that prompting for
  you (it's OEM-specific UI with no stable public API), but expect to need it.
- **Full-screen-intent permission can reset.** On this device, reinstalling
  the APK (`adb install -r`, and plausibly a Play Store update) reset the
  "Full screen notifications" grant back to denied, even though it isn't a
  normal runtime permission. Don't assume a grant from
  `checkFullScreenIntentPermission()` persists forever — re-check it on every
  app launch, not just once at first install.

Doze itself was **not** a problem once the above were in place: with
Autostart and the full-screen-intent permission granted, the foreground
service, its notification, and the lock-screen Activity all worked correctly
with the device forced into deep Doze (`dumpsys deviceidle force-idle`).

### What "Accept" does today

`IncomingCallActivity` always shows a minimal native Accept/Decline screen —
sufficient to wake a locked device safely before any WebView has booted (see
[PLAN.md §4](./PLAN.md#4-proposed-jsts-api)). Tapping **Accept** brings the
host app's own launcher Activity to the foreground and fires `callAnswered`;
your `callAnswered` listener is what actually navigates to your call UI and
connects signaling. `incomingCallRoute` is accepted by `initialize()` and
persisted for forward compatibility, but nothing currently reads it — handing
off directly into a host app's own WebView call screen (reusing that route)
is tracked as a roadmap item, not yet implemented.

---

## Architecture

| Piece | Owns |
|---|---|
| `CallMessagingService` / `NativeCallMessageHandler` | Receiving the `incoming_call` FCM data message |
| `IncomingCallService` | Foreground service: loops the ringtone (`MediaPlayer`) and vibration (`Vibrator`) until answered/declined/timed out |
| `CallNotificationBuilder` | The full-screen-intent notification + its Accept/Decline actions |
| `RingtoneChannelManager` | One silent `NotificationChannel` per configured ringtone (see below) |
| `IncomingCallActivity` | The lock-screen-wake Accept/Decline screen |
| `CallActionReceiver` | Accept/Decline tapped from the notification tray directly |
| `NativeCallEvents` | Fan-out from native call outcomes to the JS bridge, with a durable mailbox for events that fire before the bridge exists |
| `NativeCallConfig` | Persisted `initialize()` config — survives process death |
| `NativeCallPlugin` | The `@CapacitorPlugin` bridge itself |

**Why notification channels are silent.** Android locks a channel's sound in
at creation and ignores later per-message overrides — the exact
`@capacitor/push-notifications` limitation this plugin exists to route
around (PLAN.md §1.2). Pre-registering one channel per ringtone `key` at
`initialize()` solves that, but a channel's sound only ever plays once
through — no "ring until answered" (PLAN.md §1.3). So channels here are
created with `setSound(null, null)`; `IncomingCallService`'s own `MediaPlayer`
+ `Vibrator` loop owns 100% of what you actually hear and feel.

**Event delivery across process death.** A call can be answered from the
lock screen while the app's JS bridge doesn't exist yet (process was fully
killed). `NativeCallEvents` persists the most recent outcome to
`SharedPreferences`; `NativeCallPlugin.load()` drains it once the bridge
comes up, in addition to Capacitor's own `retainUntilConsumed` delivery for
the narrower race where the bridge exists but `addListener()` hasn't
resolved yet.

**Why `IncomingCallActivity` sets its own `taskAffinity`.** Capacitor's
default `MainActivity` uses `launchMode="singleTask"`. Without a distinct
`taskAffinity`, `IncomingCallActivity` joins the host app's own task — and
bringing that task forward (which the full-screen intent does) re-triggers
`singleTask`'s clear-top behavior, immediately replacing the call screen with
`MainActivity` before the user ever sees it. Found on-device: the call screen
would flash and vanish, resuming `MainActivity` instead. Giving it its own
task affinity keeps it in a separate task so it isn't affected by the host
app's own launch mode.

---

## Two-tier roadmap

**Tier 1 (this plugin today):** foreground service + full-screen-intent
notification + Activity — the approach WhatsApp/Signal-class apps used for
years. No special Play Store review category needed for direct-APK
distribution.

**Tier 2 (future, not started):** real `ConnectionService`/Telecom API
integration — the call appears in Android's native Phone UI, respects Do Not
Disturb call exceptions, shows on Bluetooth/car displays. Significantly more
native code and OEM-specific edge cases (MIUI/OneUI handle Telecom
integration differently); don't start until Tier 1 is proven across a few
real projects. See [PLAN.md §3](./PLAN.md#3-two-tier-approach).

**Not implemented / explicit non-goals for now** — see
[PLAN.md §7](./PLAN.md#7-explicit-non-goals-for-now):
- iOS (CallKit — its own planning pass)
- Telecom/ConnectionService (Tier 2)
- Bundled ringtone assets
- Deep-linking `IncomingCallActivity` directly into the host app's WebView route

---

## Development

```bash
npm install
npm run build        # tsc + rollup -> dist/
npm run verify:android  # gradle build of the android/ library module
```

`example/` is a minimal Capacitor app wired to this plugin for manual,
on-device testing of the full ring → lock-screen wake → answer/decline loop —
see [`example/README.md`](./example/README.md).

## License

MIT
