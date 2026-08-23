import { describe, it, expect } from 'vitest'
import {
  applyDueFilter,
  endOfWeek,
  isOverdue,
  matchesDueFilter,
  parseIsoDate,
} from './dueDateFilters'
import type { Todo } from '@/types/todo'

/** Fixed reference date: Wednesday, 2024-06-12. */
const TODAY = new Date(2024, 5, 12)

function makeTodo(overrides: Partial<Todo> = {}): Todo {
  return {
    id: 1,
    title: 'Test Todo',
    description: '',
    finished: false,
    priority: 'MEDIUM',
    status: 'OPEN',
    assigneeList: [],
    createdDate: '2024-06-01',
    dueDate: '2024-06-12',
    finishedDate: '',
    recurrenceRule: 'NONE',
    ...overrides,
  }
}

describe('parseIsoDate', () => {
  it('parses a valid ISO date string', () => {
    expect(parseIsoDate('2024-06-12')).toEqual(new Date(2024, 5, 12))
  })

  it('returns null for empty or malformed values', () => {
    expect(parseIsoDate(null)).toBeNull()
    expect(parseIsoDate(undefined)).toBeNull()
    expect(parseIsoDate('')).toBeNull()
    expect(parseIsoDate('not-a-date')).toBeNull()
    expect(parseIsoDate('2024-13-40')).toBeNull()
  })
})

describe('endOfWeek', () => {
  it('returns the Sunday of the week (week starts Monday)', () => {
    // Wednesday 2024-06-12 -> Sunday 2024-06-16
    expect(endOfWeek(new Date(2024, 5, 12))).toEqual(new Date(2024, 5, 16))
    // Monday 2024-06-10 -> Sunday 2024-06-16
    expect(endOfWeek(new Date(2024, 5, 10))).toEqual(new Date(2024, 5, 16))
    // Sunday maps to itself
    expect(endOfWeek(new Date(2024, 5, 16))).toEqual(new Date(2024, 5, 16))
  })
})

describe('isOverdue', () => {
  it('marks an unfinished todo with a past due date as overdue', () => {
    const todo = makeTodo({ dueDate: '2024-06-11' })
    expect(isOverdue(todo, TODAY)).toBe(true)
  })

  it('does not mark finished todos as overdue even when past due', () => {
    const todo = makeTodo({ dueDate: '2024-06-01', finished: true })
    expect(isOverdue(todo, TODAY)).toBe(false)
  })

  it('does not mark todos due today or later as overdue', () => {
    expect(isOverdue(makeTodo({ dueDate: '2024-06-12' }), TODAY)).toBe(false)
    expect(isOverdue(makeTodo({ dueDate: '2024-07-01' }), TODAY)).toBe(false)
  })

  it('treats todos without a due date as not overdue', () => {
    expect(isOverdue(makeTodo({ dueDate: '' }), TODAY)).toBe(false)
  })
})

describe('matchesDueFilter', () => {
  it('shows everything for the "all" filter', () => {
    expect(matchesDueFilter(makeTodo({ dueDate: '' }), 'all', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-12' }), 'all', TODAY)).toBe(true)
  })

  it('matches only todos without due date for "noDueDate"', () => {
    expect(matchesDueFilter(makeTodo({ dueDate: '' }), 'noDueDate', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-12' }), 'noDueDate', TODAY)).toBe(false)
  })

  it('matches only todos due today for "dueToday"', () => {
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-12' }), 'dueToday', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-13' }), 'dueToday', TODAY)).toBe(false)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-11' }), 'dueToday', TODAY)).toBe(false)
    expect(matchesDueFilter(makeTodo({ dueDate: '' }), 'dueToday', TODAY)).toBe(false)
  })

  it('matches today through Sunday for "dueThisWeek"', () => {
    // Wednesday 2024-06-12, week ends Sunday 2024-06-16
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-12' }), 'dueThisWeek', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-16' }), 'dueThisWeek', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-17' }), 'dueThisWeek', TODAY)).toBe(false)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-11' }), 'dueThisWeek', TODAY)).toBe(false)
  })

  it('matches unfinished past-due todos for "overdue"', () => {
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-11' }), 'overdue', TODAY)).toBe(true)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-11', finished: true }), 'overdue', TODAY)).toBe(false)
    expect(matchesDueFilter(makeTodo({ dueDate: '2024-06-12' }), 'overdue', TODAY)).toBe(false)
  })
})

describe('applyDueFilter', () => {
  it('filters a mixed list and preserves order', () => {
    const todos = [
      makeTodo({ id: 1, dueDate: '2024-06-11' }),
      makeTodo({ id: 2, dueDate: '2024-06-12' }),
      makeTodo({ id: 3, dueDate: '2024-06-15' }),
      makeTodo({ id: 4, dueDate: '', title: 'No date' }),
    ]
    const overdue = applyDueFilter(todos, 'overdue', TODAY)
    expect(overdue.map((t) => t.id)).toEqual([1])

    const thisWeek = applyDueFilter(todos, 'dueThisWeek', TODAY)
    expect(thisWeek.map((t) => t.id)).toEqual([2, 3])

    const none = applyDueFilter(todos, 'noDueDate', TODAY)
    expect(none.map((t) => t.id)).toEqual([4])
  })
})
