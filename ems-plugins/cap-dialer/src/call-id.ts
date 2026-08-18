export function buildExternalCallId(deviceId: string, phoneNumber: string, startedAt: string, direction: string) {
  return `${deviceId}:${direction}:${startedAt}:${phoneNumber}`.replace(/\s+/g, '');
}
