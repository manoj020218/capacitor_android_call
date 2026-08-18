export function mapRecoveryState(queued: boolean, lastRunAt?: string) {
  return { queued, lastRunAt };
}
