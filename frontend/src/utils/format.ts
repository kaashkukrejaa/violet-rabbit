/**
 * Formatting utilities for display of course schedules.
 */

const DAY_ORDER = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];

/**
 * Formats day abbreviations with common patterns.
 * Examples: ["MON", "WED", "FRI"] → "MWF", ["TUE", "THU"] → "TTh"
 */
export function formatDays(days: string[]): string {
  const sorted = [...days].sort(
    (a, b) => DAY_ORDER.indexOf(a) - DAY_ORDER.indexOf(b)
  );
  if (sorted.length === 0) return '—';
  if (arraysEqual(sorted, ['MON', 'WED', 'FRI'])) return 'MWF';
  if (arraysEqual(sorted, ['TUE', 'THU'])) return 'TTh';
  return sorted.map((d) => d.slice(0, 3)).join(' ');
}

/**
 * Formats time range for display.
 * Example: "09:00", "10:30" → "9am–10:30am"
 */
export function formatTimeRange(start: string, end: string): string {
  return `${formatTime(start)}–${formatTime(end)}`;
}

/**
 * Converts 24-hour time to 12-hour format.
 * Examples: "09:00" → "9am", "14:30" → "2:30pm"
 */
function formatTime(hhmm: string): string {
  const [h, m] = hhmm.split(':').map(Number);
  const period = h >= 12 ? 'pm' : 'am';
  const hour = ((h + 11) % 12) + 1;
  return m === 0 ? `${hour}${period}` : `${hour}:${String(m).padStart(2, '0')}${period}`;
}

function arraysEqual(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false;
  return a.every((x, i) => x === b[i]);
}
