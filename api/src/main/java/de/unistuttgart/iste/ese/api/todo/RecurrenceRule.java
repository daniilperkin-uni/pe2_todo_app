package de.unistuttgart.iste.ese.api.todo;

/**
 * Recurrence cadence of a {@link Todo}.
 *
 * <ul>
 *   <li>{@link #NONE} - one-off todo (default)</li>
 *   <li>{@link #WEEKLY} - repeats every week</li>
 *   <li>{@link #MONTHLY} - repeats every month; month-end dates roll over
 *       to the last valid day of the shorter month (Jan 31 -> Feb 28)</li>
 * </ul>
 *
 * Deliberately a simple enum rather than full RRULE strings - the two
 * cadences cover the actual usage and stay trivially validatable.
 */
public enum RecurrenceRule {
    NONE,
    WEEKLY,
    MONTHLY
}
