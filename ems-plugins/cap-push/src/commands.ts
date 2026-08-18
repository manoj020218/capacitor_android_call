import type { CommandType } from './definitions';

const valid = new Set<CommandType>([
  'VIDEO_CALL',
  'SYNC_NOW',
  'CONFIG_UPDATED',
  'ADMIN_MESSAGE',
  'DEVICE_STATUS_REQUEST',
]);

export function readCommandType(payload: Record<string, string>) {
  const value = payload.commandType as CommandType | undefined;
  return value && valid.has(value) ? value : undefined;
}
