import type { PluginListenerHandle } from '@capacitor/core';

/** One ringtone the host app offers as a choice for incoming calls. */
export interface RingtoneOption {
  /**
   * Stable identifier used everywhere else in the API and in the FCM payload's
   * `ringtone` field (e.g. `'classic'`). Never changes once shipped — it's
   * what the server targets.
   */
  key: string;
  /**
   * The bare Android raw-resource name backing this ringtone, with **no
   * extension** (`'ringtone_classic'`, not `'ringtone_classic.wav'` or
   * `'ringtone_classic.mp3'`). The file itself must live at
   * `android/app/src/main/res/raw/<resourceName>.<ext>` in the host app.
   * Getting the extension wrong here is silent on the OS level; this plugin
   * validates it at `initialize()` and rejects the call instead of failing
   * quietly at ring-time.
   */
  resourceName: string;
}

export interface InitializeOptions {
  /** Every ringtone the host app wants selectable. Must include at least one entry. */
  ringtones: RingtoneOption[];
  /** `key` of the ringtone used until the user (or app) calls {@link NativeCallPlugin.setRingtone}. */
  defaultRingtone: string;
  /**
   * How long the phone rings before the plugin gives up, stops the service,
   * and emits `callTimedOut`. Defaults to 45 seconds.
   */
  ringDurationSeconds?: number;
  /**
   * A route in the host app's own web content (e.g. `/call/incoming`) that
   * the plugin should hand off to once the WebView has booted, so the call
   * screen is the app's real UI rather than the plugin's native fallback.
   * Omit to always use the minimal native fallback screen.
   */
  incomingCallRoute?: string;
  /**
   * Declining a call never opens the host app (by design — there's no reason
   * to interrupt the user just to say no), so there's normally no way to tell
   * your backend "declined" without the JS bridge, which may not exist at all
   * if the app was fully killed. If set, native code fires a bare, fire-and-
   * forget `POST` directly to this URL the moment Decline is tapped — from
   * any entry point (notification, lock screen) or a ring timeout. `{callId}`
   * is replaced with the FCM payload's `callId` before the request is sent;
   * the request has no body and no auth header, so the URL itself must carry
   * whatever your server needs to authorize the action (e.g. a per-call
   * capability token as a path segment). Omit to skip this entirely — the
   * host app's own `callDeclined` JS listener still fires normally whenever
   * the bridge is next up.
   */
  declineWebhookUrl?: string;
}

export interface StopRingingOptions {
  /** The `callId` from the FCM payload that started the current ring. */
  callId: string;
}

export interface SetRingtoneOptions {
  /** `key` of a ringtone previously registered via {@link InitializeOptions.ringtones}. */
  key: string;
}

export interface FullScreenIntentPermissionStatus {
  /**
   * Whether the app is currently allowed to post full-screen-intent
   * notifications. Always `true` below Android 14 (API 34), where the
   * permission does not exist and is implicitly granted.
   */
  granted: boolean;
}

export interface CallAnsweredEvent {
  callId: string;
}

export interface CallDeclinedEvent {
  callId: string;
}

export interface CallTimedOutEvent {
  callId: string;
}

export interface NativeCallPlugin {
  /**
   * Registers one notification channel per ringtone and persists the
   * configuration natively so it survives process death — call this once,
   * as early as possible during app startup, before any call can arrive.
   * Safe to call again later (e.g. after a config change); channel
   * registration is idempotent.
   */
  initialize(options: InitializeOptions): Promise<void>;

  /**
   * Stops the ringtone/vibration/notification for a specific call without
   * emitting `callDeclined` or `callAnswered` — use this when the *caller*
   * cancels (e.g. hangs up before being answered), not when the callee acts.
   */
  stopRinging(options: StopRingingOptions): Promise<void>;

  /**
   * Changes which registered ringtone future incoming calls use by default.
   * Does not affect a call that is currently ringing.
   */
  setRingtone(options: SetRingtoneOptions): Promise<void>;

  /**
   * Android 14+ requires the user to explicitly grant permission to post
   * full-screen-intent notifications (Settings → Apps → your app → Full
   * screen notifications). Check this after `initialize()` and prompt the
   * user via {@link requestFullScreenIntentPermission} if not granted —
   * otherwise incoming calls degrade to a normal heads-up notification that
   * won't wake a locked screen.
   */
  checkFullScreenIntentPermission(): Promise<FullScreenIntentPermissionStatus>;

  /**
   * Opens the system settings screen where the user grants full-screen-intent
   * permission. There is no programmatic grant — this only navigates there.
   * Resolves immediately after the settings screen is launched, not after the
   * user makes a choice; re-check with {@link checkFullScreenIntentPermission}
   * when the app resumes.
   */
  requestFullScreenIntentPermission(): Promise<void>;

  addListener(
    eventName: 'callAnswered',
    listenerFunc: (event: CallAnsweredEvent) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'callDeclined',
    listenerFunc: (event: CallDeclinedEvent) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'callTimedOut',
    listenerFunc: (event: CallTimedOutEvent) => void,
  ): Promise<PluginListenerHandle>;

  removeAllListeners(): Promise<void>;
}
