# capacitor-native-call — example

A minimal Capacitor app whose only job is to exercise every method in the
plugin's JS API on a real device, for manual end-to-end testing. Not
published, not a template to build a real app from.

## Setup

Requires JDK 21+ (Capacitor 7's Android core module compiles with source/target
21) — set `org.gradle.java.home` in `android/gradle.properties` locally if
your default JDK is older, or export `JAVA_HOME` before running Gradle. Don't
commit a machine-specific path.

```bash
cd example
npm install
npx cap add android
```

Then, before the first run:

1. Drop two placeholder audio files into
   `android/app/src/main/res/raw/ringtone_classic.mp3` and
   `ringtone_chime.mp3` (any short `.mp3`/`.wav` works — the plugin only
   cares about the bare resource name).
2. Put a real `google-services.json` in `android/app/` and apply the
   `com.google.gms.google-services` plugin, if you intend to test the FCM
   path (step 4 below). Skip this if you only want to test `initialize()`,
   permission checks, and `stopRinging()`.

```bash
npx cap sync android
npx cap open android
```

Run on a physical device (foreground services + full-screen intents behave
inconsistently on some emulators).

**On Xiaomi/MIUI/HyperOS devices**, also enable Autostart for this app
(Settings → Apps → Manage apps → NativeCall Example → Autostart) before
testing steps 3+ below — without it, the OS silently blocks the foreground
service from starting while the app is backgrounded and nothing will happen
at all when a call comes in. See root README "Known OEM caveats" for details;
this was found and confirmed necessary during on-device testing.

## Manual test plan

1. **`initialize()`** — tap the button, confirm no error, check Logcat for
   `RingtoneChannelManager` creating two channels (`native_call_classic`,
   `native_call_chime`).
2. **Full-screen-intent permission (Android 14+ device only)** — tap
   `checkFullScreenIntentPermission()`; if `granted: false`, tap
   `requestFullScreenIntentPermission()`, grant it in Settings, re-check.
3. **End-to-end ring, screen locked** — send a data-only FCM message (see
   payload shape in the root [README](../README.md#3-server-side-payload-contract))
   to the device via the Firebase console's "Send test message" (data-only,
   not the notification composer) or `curl` against the FCM HTTP v1 API.
   Lock the device first. Expect: screen wakes directly to the Accept/Decline
   screen, ringtone loops, vibration repeats.
4. **Accept** — tap Accept; expect the app to foreground and `callAnswered`
   to log in the harness with the matching `callId`.
5. **Decline** — repeat step 3, tap Decline instead; expect ringing/
   vibration/notification to stop immediately and `callDeclined` to log next
   time the app is opened (it fired while backgrounded).
6. **Timeout** — repeat step 3, don't touch it; after `ringDurationSeconds`
   (30s in this harness), expect ringing to stop on its own and
   `callTimedOut` to log.
7. **`stopRinging()` mid-ring** — repeat step 3, before answering, call
   `stopRinging({ callId })` with the same `callId` from another device/tool;
   expect ringing to stop with no `callAnswered`/`callDeclined`/`callTimedOut`
   event (this is the "caller hung up" path).
8. **App fully killed** — force-stop the app from Android settings, then
   repeat step 3 — the call should still ring (proves the FCM path doesn't
   depend on the JS bridge being alive).
