import { describe, it, expect } from 'vitest';
import { formatDays, formatTimeRange } from './format';

describe('formatDays', () => {
  it('returns em-dash for empty list', () => {
    expect(formatDays([])).toBe('—');
  });

  it('formats Mon/Wed/Fri as the shorthand "MWF"', () => {
    expect(formatDays(['MON', 'WED', 'FRI'])).toBe('MWF');
  });

  it('is order-independent for MWF', () => {
    expect(formatDays(['FRI', 'MON', 'WED'])).toBe('MWF');
  });

  it('formats Tue/Thu as "TTh"', () => {
    expect(formatDays(['TUE', 'THU'])).toBe('TTh');
  });

  it('falls back to space-joined abbreviations for other combos', () => {
    expect(formatDays(['MON', 'TUE'])).toBe('MON TUE');
  });
});

describe('formatTimeRange', () => {
  it('formats whole-hour times without minutes', () => {
    expect(formatTimeRange('09:00', '10:00')).toBe('9am–10am');
  });

  it('formats half-hour times with minutes', () => {
    expect(formatTimeRange('09:30', '10:45')).toBe('9:30am–10:45am');
  });

  it('handles afternoon times with pm suffix', () => {
    expect(formatTimeRange('13:00', '14:30')).toBe('1pm–2:30pm');
  });

  it('handles noon as 12pm', () => {
    expect(formatTimeRange('12:00', '13:00')).toBe('12pm–1pm');
  });

  it('handles midnight as 12am', () => {
    expect(formatTimeRange('00:00', '01:00')).toBe('12am–1am');
  });
});
