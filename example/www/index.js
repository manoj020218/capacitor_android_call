import { NativeCall } from 'capacitor-native-call';

const logEl = document.getElementById('log');
function log(...args) {
  logEl.textContent += args.map((a) => (typeof a === 'object' ? JSON.stringify(a) : a)).join(' ') + '\n';
  logEl.scrollTop = logEl.scrollHeight;
}

NativeCall.addListener('callAnswered', (e) => log('callAnswered', e));
NativeCall.addListener('callDeclined', (e) => log('callDeclined', e));
NativeCall.addListener('callTimedOut', (e) => log('callTimedOut', e));

document.getElementById('init').addEventListener('click', async () => {
  await NativeCall.initialize({
    ringtones: [
      { key: 'classic', resourceName: 'ringtone_classic' },
      { key: 'chime', resourceName: 'ringtone_chime' },
    ],
    defaultRingtone: 'classic',
    ringDurationSeconds: 30,
  });
  log('initialized');
});

document.getElementById('check-perm').addEventListener('click', async () => {
  const result = await NativeCall.checkFullScreenIntentPermission();
  log('checkFullScreenIntentPermission', result);
});

document.getElementById('request-perm').addEventListener('click', async () => {
  await NativeCall.requestFullScreenIntentPermission();
  log('opened full-screen-intent settings');
});

document.getElementById('set-ringtone').addEventListener('click', async () => {
  await NativeCall.setRingtone({ key: 'chime' });
  log('default ringtone set to chime');
});

document.getElementById('stop-ringing').addEventListener('click', async () => {
  const callId = window.prompt('callId to stop:');
  if (!callId) return;
  await NativeCall.stopRinging({ callId });
  log('stopRinging', callId);
});
