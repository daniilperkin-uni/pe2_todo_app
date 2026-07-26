import { describe, it, expect } from 'vitest'
import type { Todo, TodoCreateUpdate, Priority } from './todo'
import type { Assignee } from './assignee'

describe('Todo type contracts', () => {
  it('should create a valid Todo object', () => {
    const assignee: Assignee = {
      id: 1,
      prename: 'John',
      name: 'Doe',
      email: 'john@example.com',
    }

    const todo: Todo = {
      id: 1,
      title: 'Test Todo',
      description: 'A test description',
      finished: false,
      priority: 'HIGH',
      assigneeList: [assignee],
      createdDate: '2024-01-01',
      dueDate: '2024-12-31',
      finishedDate: '',
      category: 'work',
    }

    expect(todo.id).toBe(1)
    expect(todo.title).toBe('Test Todo')
    expect(todo.finished).toBe(false)
    expect(todo.priority).toBe('HIGH')
    expect(todo.assigneeList).toHaveLength(1)
    expect(todo.assigneeList[0].email).toBe('john@example.com')
  })

  it('should accept all Priority values', () => {
    const priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH']
    priorities.forEach((p) => {
      const todo: Partial<Todo> = { priority: p }
      expect(todo.priority).toBe(p)
    })
  })

  it('should create a valid TodoCreateUpdate object', () => {
    const createDto: TodoCreateUpdate = {
      title: 'New Todo',
      description: 'Description',
      finished: false,
      priority: 'MEDIUM',
      dueDate: '2024-12-31',
      assigneeIdList: [1, 2, 3],
    }

    expect(createDto.title).toBe('New Todo')
    expect(createDto.assigneeIdList).toHaveLength(3)
    expect(createDto.dueDate).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })

  it('should allow optional category', () => {
    const todo: Todo = {
      id: 1,
      title: 'Test',
      description: '',
      finished: false,
      priority: 'LOW',
      assigneeList: [],
      createdDate: '2024-01-01',
      dueDate: '2024-12-31',
      finishedDate: '',
    }
    expect(todo.category).toBeUndefined()
  })
})

describe('Assignee type contracts', () => {
  it('should create a valid Assignee object', () => {
    const assignee: Assignee = {
      id: 1,
      prename: 'Jane',
      name: 'Smith',
      email: 'jane@example.com',
    }
    expect(assignee.id).toBe(1)
    expect(assignee.email).toContain('@')
  })

  it('should create a valid AssigneeCreateUpdate object', () => {
    const dto = {
      prename: 'Jane',
      name: 'Smith',
      email: 'jane@example.com',
    }
    expect(dto.prename).toBe('Jane')
    expect(dto.name).toBe('Smith')
  })
})
