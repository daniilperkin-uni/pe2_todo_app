<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { Todo } from '@/types/todo';
import { getTodos, updateTodo, deleteTodo, downloadTodosCsv } from '@/services/apiService';
import TodoList from '@/components/TodoList.vue';
import { showToast, Toast } from '@/ts/toasts';
import { Button } from 'agnostic-vue';
import { saveAs } from 'file-saver';

const router = useRouter();
const todos = ref<Todo[]>([]);
const isLoading = ref<boolean>(true);
const isDownloadingCsv = ref<boolean>(false);
// Identifier of the todo pending deletion confirmation, or null when the
// confirmation dialog is closed. Replaces the blocking native confirm() call.
const todoToDelete = ref<number | null>(null);

// Filter and Sort states
const filterTitle = ref<string>('');
const sortBy = ref<string>('createdDate');

const filteredAndSortedTodos = computed(() => {
  let result = [...todos.value];

  // Filter by title
  if (filterTitle.value) {
    const search = filterTitle.value.toLowerCase();
    result = result.filter((t) => t.title.toLowerCase().includes(search));
  }

  // Sort
  result.sort((a, b) => {
    if (sortBy.value === 'title') {
      return a.title.localeCompare(b.title);
    } else if (sortBy.value === 'dueDate') {
      if (!a.dueDate) return 1;
      if (!b.dueDate) return -1;
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
    } else if (sortBy.value === 'priority') {
      const priorityMap: Record<string, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };
      return (priorityMap[a.priority] ?? 3) - (priorityMap[b.priority] ?? 3);
    } else if (sortBy.value === 'createdDate') {
      if (!a.createdDate) return 1;
      if (!b.createdDate) return -1;
      return new Date(a.createdDate).getTime() - new Date(b.createdDate).getTime();
    }
    return 0;
  });

  return result;
});

const openTodos = computed(() => filteredAndSortedTodos.value.filter((t) => !t.finished));
const finishedTodos = computed(() => filteredAndSortedTodos.value.filter((t) => t.finished));

const sortOptions = [
  { value: 'createdDate', label: 'Date Created' },
  { value: 'title', label: 'Title' },
  { value: 'dueDate', label: 'Due Date' },
  { value: 'priority', label: 'Priority' },
];

// Fetches all todos from the backend and clears the loading state.
async function fetchTodos() {
  isLoading.value = true;
  try {
    todos.value = await getTodos();
  } catch (error) {
    console.error('Error fetching todos:', error);
    showToast(new Toast('Error', 'Failed to load todos. Please try again later.', 'error'));
  } finally {
    isLoading.value = false;
  }
}

// Toggles the finished state of a todo, optimistically updating the local list
// and rolling back on failure.
async function handleToggleFinished(id: number, finished: boolean) {
  const todoToUpdate = todos.value.find((todo) => todo.id === id);
  if (todoToUpdate) {
    const originalFinishedState = todoToUpdate.finished;
    todoToUpdate.finished = finished;
    try {
      const updatePayload = {
        title: todoToUpdate.title,
        description: todoToUpdate.description,
        finished: finished,
        priority: todoToUpdate.priority,
        dueDate: todoToUpdate.dueDate,
        assigneeIdList: todoToUpdate.assigneeList?.map((a) => a.id) || [],
      };
      await updateTodo(id, updatePayload);
      showToast(new Toast('Success', `Todo marked as ${finished ? 'finished' : 'not finished'}.`, 'success'));
      await fetchTodos();
    } catch (error) {
      console.error('Error toggling todo finished status:', error);
      showToast(new Toast('Error', 'Failed to update todo status.', 'error'));
      todoToUpdate.finished = originalFinishedState;
    }
  }
}

// Opens the in-app confirmation dialog for deleting the given todo.
function requestDelete(id: number) {
  todoToDelete.value = id;
}

// Cancels the pending deletion and closes the confirmation dialog.
function cancelDelete() {
  todoToDelete.value = null;
}

// Confirms the pending deletion, removing the todo and refreshing the list.
async function confirmDelete() {
  const id = todoToDelete.value;
  if (id === null) return;
  todoToDelete.value = null;
  try {
    await deleteTodo(id);
    showToast(new Toast('Success', 'Todo deleted successfully!', 'success'));
    await fetchTodos();
  } catch (error: unknown) {
    console.error('Error deleting todo:', error);
    const message = error instanceof Error ? error.message : String(error);
    showToast(new Toast('Error', `Failed to delete todo: ${message}`, 'error'));
  }
}

function handleEdit(id: number) {
  router.push(`/todos/${id}/edit`);
}

function createNewTodo() {
  router.push('/todos/create');
}

// Downloads all todos as a CSV file via the backend CSV endpoint.
async function handleDownloadCsv() {
  isDownloadingCsv.value = true;
  try {
    const csvBlob = await downloadTodosCsv();
    saveAs(csvBlob, 'todos.csv');
    showToast(new Toast('Success', 'Todos CSV downloaded successfully!', 'success'));
  } catch (error) {
    console.error('Error downloading CSV:', error);
    showToast(new Toast('Error', 'Failed to download todos CSV.', 'error'));
  } finally {
    isDownloadingCsv.value = false;
  }
}

onMounted(fetchTodos);
</script>

<template>
  <div class="todos-view">
    <div class="view-header">
      <h1 class="heading">Todos</h1>
      <div class="controls">
        <Button mode="secondary" @click="handleDownloadCsv" :disabled="isDownloadingCsv">
          {{ isDownloadingCsv ? 'Downloading...' : 'Download CSV' }}
        </Button>
        <Button mode="primary" @click="createNewTodo">Create New Todo</Button>
      </div>
    </div>

    <div class="filters-sort card">
      <div class="filter-group">
        <label for="filter-title">Filter by Title:</label>
        <input
          id="filter-title"
          v-model="filterTitle"
          type="text"
          placeholder="Search todos..."
          class="search-input"
        />
      </div>
      <div class="sort-group">
        <label for="sort-by">Sort by:</label>
        <select id="sort-by" v-model="sortBy" class="sort-select">
          <option v-for="option in sortOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="isLoading" class="loading-message card">Loading todos...</div>
    <div v-else class="todos-container">
      <section class="todos-section">
        <h2 class="section-title">Open Tasks ({{ openTodos.length }})</h2>
        <div v-if="openTodos.length === 0" class="empty-state card">
          <p class="empty-state-title">No open tasks yet</p>
          <p class="empty-state-text">Create a new todo to get started.</p>
          <Button mode="primary" @click="createNewTodo">Create New Todo</Button>
        </div>
        <TodoList
          v-else
          :todos="openTodos"
          @toggle-finished="handleToggleFinished"
          @edit="handleEdit"
          @delete="requestDelete"
        />
      </section>

      <section v-if="finishedTodos.length > 0" class="todos-section finished-section">
        <h2 class="section-title">Finished Tasks ({{ finishedTodos.length }})</h2>
        <TodoList
          :todos="finishedTodos"
          @toggle-finished="handleToggleFinished"
          @edit="handleEdit"
          @delete="requestDelete"
        />
      </section>
    </div>

    <!-- In-app delete confirmation dialog (replaces native confirm()) -->
    <div v-if="todoToDelete !== null" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal card" role="dialog" aria-modal="true" aria-labelledby="confirm-delete-title">
        <h3 id="confirm-delete-title" class="modal-title">Delete this todo?</h3>
        <p class="modal-text">This action cannot be undone.</p>
        <div class="modal-actions">
          <Button mode="secondary" @click="cancelDelete">Cancel</Button>
          <Button mode="danger" @click="confirmDelete">Delete</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.todos-view {
  width: 100%;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.controls {
  display: flex;
  gap: var(--space-md);
}

.filters-sort {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-lg);
  padding: var(--space-md);
  margin-bottom: var(--space-xl);
  align-items: flex-end;
}

.filter-group,
.sort-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.filter-group label,
.sort-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-light);
}

.search-input,
.sort-select {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
}

.search-input {
  min-width: 250px;
}

.sort-select {
  min-width: 150px;
  background-color: white;
}

.loading-message {
  text-align: center;
  padding: var(--space-xl);
  color: var(--color-text-light);
}

.todos-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.todos-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: var(--space-sm);
  color: var(--color-text);
}

.finished-section {
  opacity: 0.85;
}

.empty-state {
  text-align: center;
  padding: var(--space-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
}

.empty-state-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}

.empty-state-text {
  color: var(--color-text-light);
  margin-bottom: var(--space-sm);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  max-width: 400px;
  width: calc(100% - 32px);
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.modal-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text);
}

.modal-text {
  color: var(--color-text-light);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}
</style>
