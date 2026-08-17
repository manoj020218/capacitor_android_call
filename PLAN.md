# capacitor-native-call — Plan

A standalone, reusable Capacitor plugin that gives any Android app a real
VoIP-style incoming-call experience — full-screen wake-the-lock-screen UI,
a ringtone owned by a native service (not the OS notification tray), and a
clean JS/TS bridge so a host app never has to touch Kotlin.

**Status:** Tier 1 MVP built and device-verified (milestones 1–8, see §6) — on
a real Xiaomi HyperOS (Android 15) phone: locked-screen wake, ringtone,
vibration, Accept flow, and survival through forced deep Doze all confirmed
working. One real bug (`IncomingCallActivity` task-affinity collision with a
`singleTask` host `MainActivity`) was found and fixed during this pass — see
[README §Architecture](./README.md#architecture). Not yet dogfooded in
QRUnlock (milestone 9). See [`README.md`](./README.md) for install/usage docs,
current architecture, and OEM caveats (Xiaomi Autostart, FSI permission
resets).

---

## 1. Why this needs to exist

Built while fixing QRUnlock's incoming-call notifications. Every one of these is a
generic Capacitor/Android limitation, not a QRUnlock bug — any Capacitor app doing
VoIP-style calls (delivery apps, intercoms, telehealth, on-demand services) hits the
same wall:

1. **`@capacitor/push-notifications`' `Channel.sound` silently fails on the smallest
   mistake.** It must be a bare resource name with no extension
   (`'ringtone'`, not `'ringtone.wav'`). Get it wrong and Android doesn't error —
   it just falls back to silence. No plugin-level validation catches this.
2. **On Android 8+, a notification channel's sound and vibration are locked in at
   creation.** Per-message overrides sent from the server (FCM's
   `notification.sound` / `notification.vibrate_timings`) are **ignored** once the
   channel exists — a fact FCM's own docs bury and most tutorials don't mention.
   `Channel.vibration` in the Capacitor plugin is a bare boolean; there's no way to
   set a custom vibration *pattern* through the JS API at all.
3. **A notification sound plays once through, full stop.** There's no "ring until
   answered" without either `Notification.FLAG_INSISTENT` (unavailable via
   Capacitor) or a service that owns playback itself.
4. **Tapping a notification doesn't wake a locked screen.** A real incoming-call
   feel — screen turns on, Accept/Decline is immediately visible, no unlocking
   first — needs `setShowWhenLocked`/`setTurnScreenOn` on an Activity plus a
   `setFullScreenIntent()` notification. None of this is reachable from Capacitor's
   JS layer.
5. **Offering a choice of ringtones multiplies problem #2** — each choice needs its
   own pre-registered channel, decided per-message on the server, because a
   channel's sound can't be changed after creation.

None of this is achievable purely from the JS/Capacitor-plugin side. It needs a thin
layer of native Android code — but that code is generic. Nobody should have to
re-derive it per project.

---

## 2. What this plugin owns vs. what the host app owns

**Plugin owns (native, Kotlin):**
- Receiving the FCM data message for an incoming call (a dedicated
  `FirebaseMessagingService`, or a hook the host app's own service calls into)
- Starting a foreground `Service` that loops a ringtone (`MediaPlayer`) and vibrates
  (`Vibrator.vibrate(pattern, repeatIndex)`) until told to stop
- Posting a `setFullScreenIntent()` notification that wakes the lock screen
- An `IncomingCallActivity` with `setShowWhenLocked(true)` / `setTurnScreenOn(true)`
  that the full-screen intent launches
- Stopping ringtone/vibration/notification cleanly on answer, decline, or timeout

**Host app owns (JS/TS, via the plugin's bridge):**
- The actual Accept/Decline UI (see §4 — the plugin should not force its own
  design). Either the host app supplies a route the `IncomingCallActivity` deep-links
  into (reusing the app's existing WebView/React screen), or a fully-native minimal
  UI for the pre-WebView-boot instant.
- All call signaling (WebRTC, Socket.IO, whatever) — this plugin has zero opinion on
  how the call itself connects. It only owns "the phone is ringing."
- Ringtone asset(s) and the list of choices offered to the end user
- What "answer" and "decline" actually *do* in the app

---

## 3. Two-tier approach

**Tier 1 (MVP, build first):** Foreground service + full-screen-intent notification +
Activity, as described above. This is what WhatsApp/Signal-class apps used for years
before deeper OS integration. Achievable with a moderate amount of Kotlin, no special
Play Store review category needed for direct-APK distribution (relevant for apps like
QRUnlock that don't ship via Play Store), though `USE_FULL_SCREEN_INTENT` does need
user-grantable permission handling on Android 14+.

**Tier 2 (future, optional):** Real `ConnectionService`/Telecom API integration — the
call appears in Android's native Phone UI, respects system Do Not Disturb call
exceptions, shows on Bluetooth/car displays, etc. Significantly more native code and
device-specific edge cases (OEM skins like MIUI/OneUI handle Telecom integration
differently). Don't attempt until Tier 1 is proven across a few real projects.

This plan covers Tier 1 only.

---

## 4. Proposed JS/TS API

```ts
import { NativeCall } from 'capacitor-native-call';

// Once, at app startup (register channels/config)
await NativeCall.initialize({
  ringtones: [
    { key: 'classic', resourceName: 'ringtone_classic' },
    { key: 'chime', resourceName: 'ringtone_chime' },
  ],
  defaultRingtone: 'classic',
  ringDurationSeconds: 60,
  // Route the full-screen Activity opens into inside the host app's own WebView.
  // If omitted, the plugin shows a minimal native fallback screen instead.
  incomingCallRoute: '/call/incoming',
});

// When an FCM data message for a call arrives, native code auto-starts ringing —
// nothing to call from JS for that part. JS just listens for outcomes:
NativeCall.addListener('callAnswered', ({ callId }) => { /* navigate, connect */ });
NativeCall.addListener('callDeclined', ({ callId }) => { /* notify server */ });
NativeCall.addListener('callTimedOut', ({ callId }) => { /* mark missed */ });

// If the app itself decides to stop ringing (e.g. call cancelled by caller):
await NativeCall.stopRinging({ callId });

// Change the user's ringtone preference (re-registers the matching channel):
await NativeCall.setRingtone({ key: 'chime' });
```

The FCM payload contract (data-only message, so it always reaches native code even
app-killed):
```json
{
  "data": {
    "type": "incoming_call",
    "callId": "...",
    "title": "Front Gate",
    "body": "Visitor calling",
    "ringtone": "classic"
  }
}
```

---

## 5. Package layout (standard Capacitor plugin shape)

```
capacitor-native-call/
├── package.json              # capacitor.android.src config block
├── src/
│   ├── definitions.ts         # TS interface (NativeCallPlugin)
│   ├── index.ts                # registerPlugin() + web fallback (no-op/console.warn)
│   └── web.ts                  # NativeCallWeb — graceful no-op for browser/iOS-until-built
├── android/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml         # declares service + activity + FCM receiver
│       ├── java/.../NativeCallPlugin.kt        # @CapacitorPlugin bridge
│       ├── java/.../IncomingCallService.kt     # foreground service: ringtone + vibration
│       ├── java/.../IncomingCallActivity.kt    # lock-screen-wake Activity
│       └── java/.../CallMessagingService.kt    # FirebaseMessagingService (or hook)
├── ios/                        # stub only for now — throws "not implemented on iOS"
├── example/                    # a minimal demo Capacitor app for manual testing
└── README.md
```

Consumers install with `npm install capacitor-native-call && npx cap sync` — same as
any Capacitor plugin. No manual native file copying, unlike what QRUnlock had to do
by hand this round.

---

## 6. Milestones

1. **Scaffold** ✅ — package.json/tsconfig/rollup, `src/definitions.ts` +
   `index.ts` + `web.ts`, Kotlin plugin skeleton. Verified via a real
   `npx cap add android` + `npx cap sync` against `example/` — synced clean,
   plugin auto-detected.
2. **Foreground ringtone service** ✅ **device-verified** — `IncomingCallService`
   starts/stops a looping `MediaPlayer` + repeating `Vibrator` pattern, cleans
   up in `onDestroy`. Confirmed on a real Xiaomi HyperOS phone with the device
   forced into deep Doze (`dumpsys deviceidle force-idle`): service stayed
   `isForeground=true` and Doze stayed `IDLE` throughout — not broken by our
   own service.
3. **FCM wiring** ✅ (code path verified via direct service invocation, not a
   real Firebase project) — `CallMessagingService`/`NativeCallMessageHandler`
   start the service directly from a data-only message; documented dual path
   for apps with an existing `FirebaseMessagingService`. Still not verified
   against a real FCM round-trip with the app fully killed — that needs an
   actual Firebase project.
4. **Full-screen intent + lock-screen Activity** ✅ **device-verified** —
   `CallNotificationBuilder` + `IncomingCallActivity`
   (`setShowWhenLocked`/`setTurnScreenOn` with a pre-O_MR1 window-flags
   fallback). Confirmed on-device: screen wakes to the Accept/Decline screen
   from fully locked, Accept dismisses the keyguard and foregrounds the host
   app. Found and fixed a real bug in this pass: `IncomingCallActivity` needs
   its own `android:taskAffinity` or a host `MainActivity` using Capacitor's
   default `singleTask` launch mode reclaims focus and wipes the call screen
   the instant it appears (see README §Architecture for the mechanism).
5. **Deep-link into host app's WebView route** — **not implemented.**
   `IncomingCallActivity` always shows the native fallback UI; `Accept`
   foregrounds the host app and fires `callAnswered`, and the host app's own
   JS listener does the navigating. `incomingCallRoute` is accepted/persisted
   by `initialize()` for forward compatibility but nothing reads it yet.
6. **Multi-ringtone channel management** ✅ **device-verified** —
   `RingtoneChannelManager` registers one silent channel per `ringtones[]`
   entry at `initialize()`, confirmed via `dumpsys notification`
   (`mSound=null`, `mImportance=4`); `setRingtone()` switches the default
   without re-registering channels.
7. **Android 14 full-screen-intent permission handling** ✅ **device-verified**
   — `checkFullScreenIntentPermission()` / `requestFullScreenIntentPermission()`
   wrap `NotificationManager.canUseFullScreenIntent()` and
   `ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT`; confirmed the settings intent
   opens the correct system screen and the check reflects the grant
   afterward. Also discovered: this grant can reset on APK
   reinstall/update on at least one OEM (see README "Known OEM caveats") —
   don't assume it's permanent.
8. **Example app + README** ✅ — `example/` is a plain-JS Capacitor harness
   (not a generated native project — run `npx cap add android` locally),
   using an import map + vendored ESM output instead of a bundler, plus a
   manual end-to-end test plan; root `README.md` covers install/FCM
   wiring/API/architecture/OEM caveats. Not yet published to npm.
9. **Dogfood in QRUnlock** — swap QRUnlock's current JS-only ringing (built this
   session) over to this plugin once it's proven, retiring the workarounds.

---

## 7. Explicit non-goals (for now)

- iOS support (CallKit is a whole separate, equally deep integration — worth its own
  planning pass later, not bundled into this one)
- Telecom/ConnectionService (Tier 2, see §3)
- Bundling any specific ringtone assets — the host app always supplies its own
- A prebuilt Accept/Decline UI beyond a bare-minimum native fallback
