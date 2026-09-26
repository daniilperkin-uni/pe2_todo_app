import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import TodoForm from './TodoForm.vue';
import type { Todo } from '@/types/todo';

vi.mock('@/services/apiService', () => ({
  getAssignees: vi.fn().mockResolvedValue([]),
}));

// An already overdue todo: the API rejects new past due dates, but an overdue
// todo must stay editable and keep the due date it already has.
function overdueTodo(): Todo {
  return {
    id: 1,
    title: 'Pay overdue bill',
    description: 'Was due a while ago',
    finished: false,
    priority: 'MEDIUM',
    status: 'OPEN',
    assigneeList: [],
    createdDate: '2026-01-01',
    dueDate: '2026-01-05',
    finishedDate: '',
    category: 'finance',
    recurrenceRule: 'NONE',
  };
}

async function mountEditForm(todo: Todo) {
  const wrapper = mount(TodoForm, { props: { initialTodo: todo, isEdit: true } });
  await flushPromises();
  return wrapper;
}

describe('TodoForm due date validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('accepts the unchanged past due date of an overdue todo', async () => {
    const wrapper = await mountEditForm(overdueTodo());

    await wrapper.find('form').trigger('submit');

    expect(wrapper.emitted('submit')).toHaveLength(1);
    expect(wrapper.text()).not.toContain('Due date must be in the future.');
  });

  it('rejects a newly entered past due date', async () => {
    const wrapper = await mountEditForm(overdueTodo());

    await wrapper.find('input#dueDate').setValue('2026-01-04');
    await wrapper.find('form').trigger('submit');

    expect(wrapper.emitted('submit')).toBeUndefined();
    expect(wrapper.text()).toContain('Due date must be in the future.');
  });
});
