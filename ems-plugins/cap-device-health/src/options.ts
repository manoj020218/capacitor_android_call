export function normalizeHeartbeatInterval(input?: number) {
  if (!input || Number.isNaN(input)) return 15;
  return Math.max(15, Math.round(input));
}
