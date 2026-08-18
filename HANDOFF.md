# capacitor-native-call — Handoff Notes

> Snapshot date: 2026-08-18. Point-in-time status for whoever picks this up
> next (human or Claude session). Complements `README.md` (install/usage/
> architecture) and `PLAN.md` (design rationale) — doesn't replace either.
> `PLAN.md`'s own status line is now stale (says "Not yet dogfooded in
> QRUnlock (milestone 9)") — that milestone is done, see below.

---

## 1. Current state

- **Published:** https://github.com/manoj020218/capacitor_android_call,
  `main` branch, clean, fully pushed. 7 commits.
- **Dogfooded and device-verified** in QRunlock (a separate project,
  `D:/IOT Device/QRunlock/qrunlock/` — see its own `HANDOFF.md` for the
  integration side of this story). All of Tier 1 confirmed working
  end-to-end on a real Xiaomi HyperOS (Android 15) device this session:
  killed app + lock-screen Accept, killed app + notification Accept, app
  already open + notification Accept, and Decline from every one of those
  states (including with the app fully killed, via the new
  `declineWebhookUrl`).
- QRunlock consumes this via a **git dependency**
  (`github:manoj020218/capacitor_android_call`), not a local path — every
  fix here requires a real commit+push before any consuming app can see it.
  There is no `dist/` in the repo (gitignored); a `prepare` npm script
  builds it automatically on install, which is what makes the git-dependency
  install path work at all — don't remove that script.

## 2. Bugs found and fixed while dogfooding (all pushed)

Chronological, each is its own commit — read the commit messages for full
detail, this is a summary:

1. **`Constants` was `internal object`** — inaccessible from a host app's own
   `FirebaseMessagingService` even though `IncomingCallService.sendAction()`
   is meant to be called cross-module (e.g. for message types this plugin
   doesn't own, like a call-cancel). Widened to `object` (no behavior
   change, compile-time visibility only).
2. **Missing `prepare` script** — installing straight from git (no npm
   publish) shipped an empty package since `dist/` is gitignored and
   nothing built it. Fixed with `"prepare": "npm run build"`.
3. **Notification's inline Accept action never foregrounded the host app** —
   `CallActionReceiver` stopped the ring and marked the call answered but
   never called anything like `launchHostApp()`, unlike
   `IncomingCallActivity.onAccept()` which does. First fix attempt (adding a
   plain `context.startActivity()` call to the receiver) hit a second,
   deeper bug:
4. **Android 12+ blocks a `BroadcastReceiver` from launching an Activity
   directly** — "notification trampoline" restriction, confirmed via
   `Background activity launch blocked!` / `Indirect notification activity
   start (trampoline) ... blocked` in logcat, no way around it regardless of
   permissions. Real fix: the Accept action's `PendingIntent` is now
   `PendingIntent.getActivity()` targeting `IncomingCallActivity` directly
   (system-exempt, same as the full-screen intent already was), with a new
   `EXTRA_AUTO_ANSWER` flag so it still answers in one tap. `Constants.
   EXTRA_AUTO_ANSWER` and `IncomingCallActivity.bindFrom()` both changed;
   `CallActionReceiver` now only handles Decline (Accept never routes
   through it anymore — a receiver-based broadcast was never the right tool
   for something that needs to open an Activity).
5. **`declineWebhookUrl` added** — declining never opens the app (by
   design), so there was previously no way to tell a host app's backend
   "declined" if the app was fully killed at the time; the JS `callDeclined`
   listener only fires once the bridge is next up, which could be a long
   time. `initialize({ declineWebhookUrl })` lets a host app opt into a
   native, fire-and-forget `POST` straight from `IncomingCallService` the
   instant Decline is tapped, from any entry point. `{callId}` is templated
   into the URL; no auth header is sent (the URL itself must carry whatever
   the host's backend needs, e.g. a capability token as a path segment) —
   this plugin still has zero opinion on the host's backend beyond that one
   templated POST.
6. **Restyled `IncomingCallActivity` + notification accent** — was generic
   iOS-style green/red circles unrelated to any consuming app. Now a
   themeable dark call screen (glass panel + ambient blobs, gradient Accept
   pill, solid Decline pill) via `colors.xml`/new drawables — a host app
   overrides `colors.xml` to reskin it. Also added `.setColor()` +
   `.setColorized(true)` to the tray notification for whatever accent
   theming Android/the OEM actually honors — **confirmed on at least one
   real OEM (Xiaomi HyperOS) that this is silently ignored**; the tray
   notification's true background/action-button colors are OS-rendered and
   not fully controllable by any app. Don't expect `setColorized()` to
   reliably show a themed notification — treat `IncomingCallActivity` as
   the one surface that's actually yours to brand.

## 3. What's still true from PLAN.md (unchanged this session)

- iOS is still a stub — `ios/` throws "not implemented." Not touched.
- Tier 2 (`ConnectionService`/Telecom API integration) not started, per
  `PLAN.md`'s own explicit non-goals — still the right call, Tier 1 just
  proved itself across a real integration.
- `incomingCallRoute` (deep-linking `IncomingCallActivity` directly into a
  host app's own WebView route) is still accepted/persisted by
  `initialize()` but nothing reads it — QRunlock doesn't use it either; its
  `callAnswered` JS listener does the navigating instead, which works fine
  and is arguably simpler than the deep-link approach would be.

## 4. Suggested next steps (not started, just observations from dogfooding)

- **`onNewToken` forwarding is host-app responsibility, not this plugin's.**
  QRunlock's own `QRunlockMessagingService.java` (in the *consuming* app,
  not here) has to remember to forward token refresh to whatever push
  plugin issued the original token — this plugin doesn't own FCM
  registration at all, only message *receipt*. Worth a README callout for
  the next integrator, since it's an easy thing to miss (a host app that
  gets this wrong won't notice until a token silently rotates).
- **Ring-timeout event has no equivalent to `declineWebhookUrl`.** If a call
  rings out unanswered with the app killed, `callTimedOut` has the exact
  same "no JS bridge to tell the backend" problem `declineWebhookUrl` was
  built to solve for Decline — QRunlock doesn't currently need this (the
  server already has its own independent ring timeout that fires
  regardless), but a plugin consumer that *does* need server-side awareness
  of a native timeout would hit the same gap. Same fix shape would apply:
  a `timeoutWebhookUrl` option, or generalize `declineWebhookUrl` into
  something like `outcomeWebhookUrl` that fires for both.

---

*Generated as a work-session handoff — not a permanent doc.*
