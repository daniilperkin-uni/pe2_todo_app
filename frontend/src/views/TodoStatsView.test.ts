import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TodoStatsView from './TodoStatsView.vue'
import type { TodoStats } from '@/types/todoStats'

const getTodoStats = vi.fn()
const getPriorityCorrectionStats = vi.fn()

vi.mock('@/services/apiService', () => ({
  getTodoStats: (...args: unknown[]) => getTodoStats(...args),
  getPriorityCorrectionStats: (...args: unknown[]) => getPriorityCorrectionStats(...args)
}))

vi.mock('@/ts/toasts', () => ({
  showToast: vi.fn(),
  Toast: class {
    close() {}
  }
}))

function stats(overrides: Partial<TodoStats> = {}): TodoStats {
  return {
    totalTodos: 10,
    finishedTodos: 4,
    completionRate: 40,
    averageDaysToFinish: 3.25,
    todosPerPriority: { HIGH: 3, MEDIUM: 5, LOW: 2 },
    todosPerCategory: { general: 6, finance: 4 },
    todosPerAssignee: [{ assigneeId: 1, prename: 'Anna', name: 'Schmidt', count: 7 }],
    ...overrides
  }
}

async function mountView() {
  const wrapper = mount(TodoStatsView)
  await flushPromises()
  return wrapper
}

describe('TodoStatsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getPriorityCorrectionStats.mockResolvedValue({
      totalCorrections: 0,
      correctedToLow: 0,
      correctedToMedium: 0,
      correctedToHigh: 0
    })
  })

  it('renders the headline figures from the stats endpoint', async () => {
    getTodoStats.mockResolvedValue(stats())

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Statistics')
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('4')
    expect(wrapper.text()).toContain('40%')
    expect(wrapper.text()).toContain('3.3')
  })

  it('renders a dash when nothing has been finished yet', async () => {
    getTodoStats.mockResolvedValue(
      stats({ totalTodos: 2, finishedTodos: 0, completionRate: 0, averageDaysToFinish: null })
    )

    const wrapper = await mountView()

    // The average is meaningless with zero finished todos, so the view must not
    // print a number (and definitely not "null" or "NaN").
    expect(wrapper.text()).toContain('—')
    expect(wrapper.text()).not.toContain('NaN')
    expect(wrapper.text()).not.toContain('null')
  })

  it('lists the per-assignee counts', async () => {
    getTodoStats.mockResolvedValue(stats())

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Anna Schmidt')
    expect(wrapper.text()).toContain('Todos per Assignee')
  })

  it('reports a failed load instead of rendering empty charts', async () => {
    getTodoStats.mockRejectedValue(new Error('boom'))

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Failed to load the statistics.')
    expect(wrapper.text()).not.toContain('Todos per Assignee')
  })
})
