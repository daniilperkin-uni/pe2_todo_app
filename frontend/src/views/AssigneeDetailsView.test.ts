import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import AssigneeDetailsView from './AssigneeDetailsView.vue'
import type { Assignee } from '@/types/assignee'

const getAssignee = vi.fn()
const updateAssignee = vi.fn()
const deleteAssignee = vi.fn()

vi.mock('@/services/apiService', () => {
  // The view distinguishes a 404 from other failures with `instanceof
  // ApiError`, so the mock has to provide the same class shape.
  class ApiError extends Error {
    readonly status: number

    constructor(message: string, status: number) {
      super(message)
      this.name = 'ApiError'
      this.status = status
    }
  }

  return {
    ApiError,
    getAssignee: (...args: unknown[]) => getAssignee(...args),
    updateAssignee: (...args: unknown[]) => updateAssignee(...args),
    deleteAssignee: (...args: unknown[]) => deleteAssignee(...args)
  }
})

vi.mock('@/ts/toasts', () => ({
  showToast: vi.fn(),
  Toast: class {
    close() {}
  }
}))

const push = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '42' } }),
  useRouter: () => ({ push })
}))

function assignee(overrides: Partial<Assignee> = {}): Assignee {
  return { id: 42, prename: 'Anna', name: 'Schmidt', email: 'anna@uni-stuttgart.de', ...overrides }
}

async function mountView() {
  const wrapper = mount(AssigneeDetailsView)
  await flushPromises()
  return wrapper
}

describe('AssigneeDetailsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads the assignee identified by the route parameter', async () => {
    getAssignee.mockResolvedValue(assignee())

    const wrapper = await mountView()

    expect(getAssignee).toHaveBeenCalledWith(42)
    expect(wrapper.text()).toContain('Assignee Details')
    expect(wrapper.text()).toContain('Anna')
    expect(wrapper.text()).toContain('Schmidt')
    expect(wrapper.text()).toContain('anna@uni-stuttgart.de')
  })

  it('asks for confirmation before deleting and does not delete on cancel', async () => {
    getAssignee.mockResolvedValue(assignee())

    const wrapper = await mountView()

    const deleteButton = wrapper
      .findAll('button')
      .find((button) => button.text() === 'Delete Assignee')
    expect(deleteButton).toBeDefined()

    await deleteButton!.trigger('click')

    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(deleteAssignee).not.toHaveBeenCalled()
  })

  it('deletes and navigates back once the confirmation is accepted', async () => {
    getAssignee.mockResolvedValue(assignee())
    deleteAssignee.mockResolvedValue(undefined)

    const wrapper = await mountView()

    await wrapper
      .findAll('button')
      .find((button) => button.text() === 'Delete Assignee')!
      .trigger('click')

    const confirmButton = wrapper
      .findAll('.modal-overlay button')
      .find((button) => button.text() === 'Delete')
    expect(confirmButton).toBeDefined()

    await confirmButton!.trigger('click')
    await flushPromises()

    expect(deleteAssignee).toHaveBeenCalledWith(42)
    expect(push).toHaveBeenCalledWith('/assignees')
  })

  // The ApiError class lives in the mocked module, so build failures through
  // the same import the view uses.
  async function apiError(status: number): Promise<Error> {
    const { ApiError } = await import('@/services/apiService')
    return new ApiError(`request failed with status ${status}`, status)
  }

  it('shows a retryable error state instead of "not found" when the load fails', async () => {
    getAssignee.mockRejectedValue(await apiError(500))

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Failed to load assignee details')
    expect(wrapper.text()).not.toContain('Assignee not found')
    expect(wrapper.findAll('button').some((button) => button.text() === 'Retry')).toBe(true)
  })

  it('still reports a genuinely missing assignee for a 404', async () => {
    getAssignee.mockRejectedValue(await apiError(404))

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Assignee not found')
    expect(wrapper.text()).not.toContain('Failed to load assignee details')
  })

  it('retries the load from the error state', async () => {
    getAssignee.mockRejectedValueOnce(await apiError(503))
    getAssignee.mockResolvedValueOnce(assignee())

    const wrapper = await mountView()
    const retry = wrapper.findAll('button').find((button) => button.text() === 'Retry')
    expect(retry).toBeDefined()

    await retry!.trigger('click')
    await flushPromises()

    expect(getAssignee).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('Anna')
    expect(wrapper.text()).not.toContain('Failed to load assignee details')
  })
})
