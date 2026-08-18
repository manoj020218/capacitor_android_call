export function normalizeBaseUrl(input: string) {
  return input.trim().replace(/\/+$/, '');
}

export function redactHeaders(headers: Record<string, string>) {
  const sensitive = /authorization|token|secret|password/i;
  return Object.fromEntries(
    Object.entries(headers).map(([key, value]) => [key, sensitive.test(key) ? '[redacted]' : value]),
  );
}
