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

// Filter and Sort states
const filterTitle = ref<string>('');
const sortBy = ref<string>('createdDate');

const filteredAndSortedTodos = computed(() => {
  let result = [...todos.value];

  // Filter by title
  if (filterTitle.value) {
    const search = filterTitle.value.toLowerCase();
    result = result.filter(t => t.title.toLowerCase().includes(search));
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
      const priorityMap: Record<string, number> = { 'HIGH': 0, 'MEDIUM': 1, 'LOW': 2 };
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

const openTodos = computed(() => filteredAndSortedTodos.value.filter(t => !t.finished));
const finishedTodos = computed(() => filteredAndSortedTodos.value.filter(t => t.finished));

const sortOptions = [
  { value: 'createdDate', label: 'Date Created' },
  { value: 'title', label: 'Title' },
  { value: 'dueDate', label: 'Due Date' },
  { value: 'priority', label: 'Priority' }
];

async function fetchTodos() {
  isLoading.value = true;
  try {
    todos.value = await getTodos();
  } catch (error) {
    console.error('Error fetching todos:', error);
    showToast(new Toast('Error', 'Failed to load todos. Displaying offline data.', 'error'));
  } finally {
    isLoading.value = false;
  }
}

async function handleToggleFinished(id: number, finished: boolean) {
  const todoToUpdate = todos.value.find(todo => todo.id === id);
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
        assigneeIdList: todoToUpdate.assigneeList?.map(a => a.id) || [],
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

async function handleDelete(id: number) {
  if (confirm('Are you sure you want to delete this todo?')) {
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
}

function handleEdit(id: number) {
  router.push(`/todos/${id}/edit`);
}

function createNewTodo() {
  router.push('/todos/create');
}

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
        <TodoList
          :todos="openTodos"
          @toggle-finished="handleToggleFinished"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </section>

      <section v-if="finishedTodos.length > 0" class="todos-section finished-section">
        <h2 class="section-title">Finished Tasks ({{ finishedTodos.length }})</h2>
        <TodoList
          :todos="finishedTodos"
          @toggle-finished="handleToggleFinished"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </section>
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

.filter-group, .sort-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.filter-group label, .sort-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-light);
}

.search-input, .sort-select {
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
  color: var(--color-text-dark);
}

.finished-section {
  opacity: 0.85;
}
</style>