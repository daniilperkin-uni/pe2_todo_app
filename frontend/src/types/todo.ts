import type { Assignee } from './assignee' // Add this line

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

export type TodoStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE'

export type RecurrenceRule = 'NONE' | 'WEEKLY' | 'MONTHLY'

export interface Todo {
  id: number
  title: string
  // Nullable on the wire: the backend stores null for an absent description,
  // for a todo whose status is not DONE (dueDate cleared server-side on
  // create) and for finishedDate while the todo is unfinished.
  description: string | null
  finished: boolean
  priority: Priority
  status: TodoStatus
  assigneeList: Assignee[]
  createdDate: string // ISO-8601 date string (yyyy-MM-dd)
  dueDate: string | null // ISO-8601 date string (yyyy-MM-dd)
  finishedDate: string | null // ISO-8601 date string (yyyy-MM-dd)
  category?: string | null
  recurrenceRule: RecurrenceRule
  nextOccurrenceDate?: string | null
}

export interface TodoCreateUpdate {
  title: string
  description: string
  finished: boolean
  priority: Priority
  dueDate: string // ISO-8601 date string (yyyy-MM-dd)
  assigneeIdList: number[] // List of Assignee IDs
  recurrenceRule?: RecurrenceRule
}
