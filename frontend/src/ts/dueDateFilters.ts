import type { Todo } from '@/types/todo';

/**
 * Available due-date filter options for the todo list view.
 *
 * - `all`: every todo is shown
 * - `dueToday`: due date is today
 * - `dueThisWeek`: due date lies between today and the end of the current week
 * - `overdue`: due date is in the past and the todo is not finished
 * - `noDueDate`: the todo has no due date at all
 */
export type DueFilter = 'all' | 'dueToday' | 'dueThisWeek' | 'overdue' | 'noDueDate';

/** Minimal shape of a todo needed for due-date filtering. */
export type DueDateFilterable = Pick<Todo, 'dueDate' | 'finished'>;

/**
 * Converts a date into a comparable day number (YYYYMMDD).
 *
 * Using plain integers avoids timezone pitfalls that arise when comparing
 * ISO date strings via `Date.getTime()` across timezones.
 *
 * @param date - the date to convert
 * @returns an integer of the form YYYYMMDD
 */
function toDayNumber(date: Date): number {
  return date.getFullYear() * 10000 + (date.getMonth() + 1) * 100 + date.getDate();
}

/**
 * Parses an ISO date string (yyyy-MM-dd) into a local Date at midnight.
 *
 * @param value - the ISO date string to parse
 * @returns the parsed Date, or `null` when the value is empty or malformed
 */
export function parseIsoDate(value: string | null | undefined): Date | null {
  if (!value) return null;
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value);
  if (!match) return null;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(year, month - 1, day);
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return null;
  }
  return date;
}

/**
 * Returns the last day (Sunday) of the week containing the given date.
 *
 * The week starts on Monday, matching common European calendar conventions.
 *
 * @param date - a date within the week of interest
 * @returns the Sunday of that week at midnight
 */
export function endOfWeek(date: Date): Date {
  const result = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const daysSinceMonday = (result.getDay() + 6) % 7; // Monday = 0 ... Sunday = 6
  result.setDate(result.getDate() + (6 - daysSinceMonday));
  return result;
}

/**
 * Checks whether a todo is overdue: it has a due date before today and is
 * not finished yet.
 *
 * @param todo - the todo to inspect
 * @param today - the reference "today"; defaults to the current date
 * @returns `true` when the todo is overdue
 */
export function isOverdue(todo: DueDateFilterable, today: Date = new Date()): boolean {
  const due = parseIsoDate(todo.dueDate);
  if (!due || todo.finished) return false;
  return toDayNumber(due) < toDayNumber(today);
}

/**
 * Checks whether a todo matches the given due-date filter.
 *
 * @param todo - the todo to test
 * @param filter - the active due-date filter
 * @param today - the reference "today"; defaults to the current date
 * @returns `true` when the todo should be displayed for this filter
 */
export function matchesDueFilter(
  todo: DueDateFilterable,
  filter: DueFilter,
  today: Date = new Date(),
): boolean {
  const due = parseIsoDate(todo.dueDate);

  switch (filter) {
    case 'all':
      return true;
    case 'noDueDate':
      return due === null;
    case 'dueToday':
      return due !== null && toDayNumber(due) === toDayNumber(today);
    case 'dueThisWeek': {
      if (!due) return false;
      const day = toDayNumber(due);
      return day >= toDayNumber(today) && day <= toDayNumber(endOfWeek(today));
    }
    case 'overdue':
      return isOverdue(todo, today);
    default:
      return true;
  }
}

/**
 * Filters a list of todos by the given due-date filter, preserving order.
 *
 * @param todos - the todos to filter
 * @param filter - the active due-date filter
 * @param today - the reference "today"; defaults to the current date
 * @returns the filtered todos
 */
export function applyDueFilter<T extends DueDateFilterable>(
  todos: T[],
  filter: DueFilter,
  today: Date = new Date(),
): T[] {
  return todos.filter((todo) => matchesDueFilter(todo, filter, today));
}
