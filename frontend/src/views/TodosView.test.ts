import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'
import TodosView from './TodosView.vue'
import TodoItem from '@/components/TodoItem.vue'
import type { Todo } from '@/types/todo'

const getTodos = vi.fn()
const deleteTodo = vi.fn()

vi.mock('@/services/apiService', () => ({
  getTodos: (...args: unknown[]) => getTodos(...args),
  updateTodo: vi.fn(),
  deleteTodo: (...args: unknown[]) => deleteTodo(...args),
  downloadTodosCsv: vi.fn(),
  createPriorityCorrection: vi.fn()
}))

vi.mock('@/ts/toasts', () => ({
  showToast: vi.fn(),
  Toast: class {
    close() {}
  }
}))

vi.mock('file-saver', () => ({ saveAs: vi.fn() }))

vi.mock('vue-router', () => ({
  useRouter: () => ({ currentRoute: { value: { path: '/todos' } }, push: vi.fn() })
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
  const wrapper = mount(TodosView)
  await flushPromises()
  return wrapper
}

describe('TodosView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('splits the loaded todos into open and finished sections', async () => {
    getTodos.mockResolvedValue([
      todo({ id: 1, title: 'Alpha task' }),
      todo({ id: 2, title: 'Beta task' }),
      todo({ id: 3, title: 'Gamma task', finished: true, status: 'DONE' })
    ])

    const wrapper = await mountView()

    expect(wrapper.text()).toContain('Open Tasks (2)')
    expect(wrapper.text()).toContain('Finished Tasks (1)')
    expect(wrapper.text()).toContain('Alpha task')
    expect(wrapper.text()).toContain('Gamma task')
  })

  it('narrows the list through the title filter', async () => {
    getTodos.mockResolvedValue([
      todo({ id: 1, title: 'Alpha task' }),
      todo({ id: 2, title: 'Beta task' })
    ])

    const wrapper = await mountView()
    await wrapper.find('input[placeholder="Search todos..."]').setValue('alpha')

    expect(wrapper.text()).toContain('Alpha task')
    expect(wrapper.text()).not.toContain('Beta task')
  })

  it('asks for confirmation before deleting instead of using a native dialog', async () => {
    getTodos.mockResolvedValue([todo({ id: 7, title: 'Alpha task' })])

    const wrapper = await mountView()
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)

    wrapper.findAllComponents(TodoItem)[0]!.vm.$emit('delete', 7)
    await nextTick()

    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(deleteTodo).not.toHaveBeenCalled()
  })

  it('deletes the todo only after the confirmation is accepted', async () => {
    getTodos.mockResolvedValue([todo({ id: 7, title: 'Alpha task' })])
    deleteTodo.mockResolvedValue(undefined)

    const wrapper = await mountView()
    wrapper.findAllComponents(TodoItem)[0]!.vm.$emit('delete', 7)
    await nextTick()

    const confirmButton = wrapper
      .findAll('.modal-overlay button')
      .find((button) => button.text() === 'Delete')
    expect(confirmButton).toBeDefined()

    await confirmButton!.trigger('click')
    await flushPromises()

    expect(deleteTodo).toHaveBeenCalledWith(7)
  })

  it('reports a failed load and stops the loading state', async () => {
    getTodos.mockRejectedValue(new Error('boom'))

    const wrapper = await mountView()

    expect(wrapper.text()).not.toContain('Loading todos...')
    expect(wrapper.text()).toContain('Failed to load todos')
    expect(wrapper.text()).toContain('Retry')
  })
})
