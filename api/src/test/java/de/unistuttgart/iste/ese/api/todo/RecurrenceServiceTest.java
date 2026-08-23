package de.unistuttgart.iste.ese.api.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the recurrence math in {@link TodoService}.
 *
 * Pure JUnit, no Spring context and no mocks - only the static
 * {@code computeNextOccurrence} is under test.
 */
class RecurrenceServiceTest {

    @Test
    @DisplayName("WEEKLY adds exactly seven days")
    void weeklyAddsSevenDays() {
        assertEquals(LocalDate.of(2026, 8, 30),
            TodoService.computeNextOccurrence(RecurrenceRule.WEEKLY, LocalDate.of(2026, 8, 23)));
    }

    @Test
    @DisplayName("MONTHLY adds one month for ordinary dates")
    void monthlyAddsOneMonth() {
        assertEquals(LocalDate.of(2026, 9, 15),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2026, 8, 15)));
    }

    @Test
    @DisplayName("MONTHLY rolls over month-end: Jan 31 -> Feb 28 in a common year")
    void monthlyRolloverJan31ToFeb28() {
        assertEquals(LocalDate.of(2026, 2, 28),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2026, 1, 31)));
    }

    @Test
    @DisplayName("MONTHLY rolls over month-end to Feb 29 in a leap year")
    void monthlyRolloverJan31ToFeb29LeapYear() {
        // 2028 is a leap year (divisible by 4, not a century year)
        assertEquals(LocalDate.of(2028, 2, 29),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2028, 1, 31)));
    }

    @Test
    @DisplayName("MONTHLY from Feb 28 lands on Mar 28 - clamping only shortens, never lengthens")
    void monthlyRolloverFebToMar() {
        // plusMonths clamps when the target month is SHORTER; going Feb->Mar
        // the day stays 28 (the rollover back to month-end would need
        // day-of-month memory, which simple enum recurrence deliberately
        // does not have).
        assertEquals(LocalDate.of(2026, 3, 28),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2026, 2, 28)));
    }

    @Test
    @DisplayName("MONTHLY handles 30-day months: Apr 30 -> May 31 unchanged")
    void monthlyThirtyDayMonth() {
        assertEquals(LocalDate.of(2026, 5, 30),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2026, 4, 30)));
    }

    @Test
    @DisplayName("NONE yields no next occurrence")
    void noneYieldsNull() {
        assertNull(TodoService.computeNextOccurrence(RecurrenceRule.NONE, LocalDate.of(2026, 1, 1)));
    }

    @Test
    @DisplayName("year boundary: Dec 15 -> Jan 15 of the next year")
    void monthlyAcrossYearBoundary() {
        assertEquals(LocalDate.of(2027, 1, 15),
            TodoService.computeNextOccurrence(RecurrenceRule.MONTHLY, LocalDate.of(2026, 12, 15)));
    }
}
