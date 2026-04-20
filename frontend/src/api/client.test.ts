import { describe, it, expect } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { toApiError } from './client';

/**
 * Helper that builds a realistic AxiosError with the given response payload.
 * Mirrors what the backend GlobalExceptionHandler sends back.
 */
function axiosErrorWith(responseData: unknown, status = 422): AxiosError {
  const headers = new AxiosHeaders();
  const err = new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    { headers, url: '/enrollments' } as never,
    null,
    {
      data: responseData,
      status,
      statusText: 'Unprocessable Entity',
      headers,
      config: { headers } as never,
    }
  );
  return err;
}

describe('toApiError', () => {
  it('passes through a typed ApiError from the backend response', () => {
    const err = axiosErrorWith({ type: 'prerequisite', message: 'Must pass ENG101' });
    const normalized = toApiError(err);
    expect(normalized).toEqual({ type: 'prerequisite', message: 'Must pass ENG101' });
  });

  it('falls back to generic "unknown" when axios response is missing type/message fields', () => {
    const err = axiosErrorWith({ somethingElse: 'nope' });
    const normalized = toApiError(err);
    expect(normalized.type).toBe('unknown');
    expect(normalized.message).toBe('Request failed');
  });

  it('uses the Error message for non-Axios errors', () => {
    const normalized = toApiError(new Error('network down'));
    expect(normalized).toEqual({ type: 'unknown', message: 'network down' });
  });

  it('uses a friendly default message for non-Error values (strings, null, etc.)', () => {
    expect(toApiError('oops').message).toBe('An unexpected error occurred');
    expect(toApiError(null).message).toBe('An unexpected error occurred');
    expect(toApiError(undefined).message).toBe('An unexpected error occurred');
  });
});
