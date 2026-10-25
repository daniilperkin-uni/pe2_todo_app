import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BoardView from './BoardView.vue'
import type { Todo } from '@/types/todo'

const getTodos = vi.fn()

vi.mock('@/services/apiService', () => ({
  getTodos: (...args: unknown[]) => getTodos(...args),
  transitionTodoStatus: vi.fn()
}))

vi.mock('@/ts/toasts', () => ({
  showToast: vi.fn(),
  Toast: class {
    close() {}
  }
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn(), currentRoute: { value: { path: '/board' } } })
}))

function todo(overrides: Partial<Todo> & { id: number; title: string }): Todo {
  return {
    description: '',
    finished: false,
    priority: 'MEDIUM',
    status: 'OPEN',
    assigneeList: [],
    createdDate: '2026-01-01',
    dueDate: '',
    finishedDate: '',
    category: 'general',
    recurrenceRule: 'NONE',
    ...overrides
  } as Todo
}

async function mountView() {
  const wrapper = mount(BoardView)
  await flushPromises()
  return wrapper
}

describe('BoardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders one column per status', async () => {
    getTodos.mockResolvedValue([])

    const wrapper = await mountView()

    const columns = wrapper.findAll('[data-status]')
    expect(columns).toHaveLength(3)
    expect(columns.map((column) => column.attributes('data-status'))).toEqual([
      'OPEN',
      'IN_PROGRESS',
      'DONE'
    ])
  })

  it('files each todo into the column that matches its status', async () => {
    getTodos.mockResolvedValue([
      todo({ id: 1, title: 'Open task', status: 'OPEN' }),
      todo({ id: 2, title: 'Running task', status: 'IN_PROGRESS' }),
      todo({ id: 3, title: 'Finished task', status: 'DONE', finished: true })
    ])

    const wrapper = await mountView()

    expect(wrapper.find('[data-status="OPEN"]').text()).toContain('Open task')
    expect(wrapper.find('[data-status="IN_PROGRESS"]').text()).toContain('Running task')
    expect(wrapper.find('[data-status="DONE"]').text()).toContain('Finished task')
    // A card must not leak into a neighbouring column.
    expect(wrapper.find('[data-status="OPEN"]').text()).not.toContain('Finished task')
  })

  it('shows the load failure instead of an empty board', async () => {
    getTodos.mockRejectedValue(new Error('boom'))

    const wrapper = await mountView()

    expect(wrapper.text()).not.toContain('Loading board...')
    expect(wrapper.text()).toContain('Failed')
    expect(wrapper.findAll('[data-status]')).toHaveLength(0)
  })
})
