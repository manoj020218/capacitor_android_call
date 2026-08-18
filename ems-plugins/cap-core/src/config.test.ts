import { describe, expect, it } from 'vitest';
import { normalizeBaseUrl, redactHeaders } from './config';

describe('cap-core config helpers', () => {
  it('normalizes trailing slashes', () => {
    expect(normalizeBaseUrl(' https://ems.example.com/api/ ')).toBe('https://ems.example.com/api');
  });

  it('redacts sensitive headers', () => {
    expect(redactHeaders({
      Authorization: 'Bearer abc',
      'X-App': 'ems',
      DeviceToken: 'secret',
    })).toEqual({
      Authorization: '[redacted]',
      'X-App': 'ems',
      DeviceToken: '[redacted]',
    });
  });
});
