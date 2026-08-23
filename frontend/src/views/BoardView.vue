<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { Todo, TodoStatus } from '@/types/todo';
import { getTodos, transitionTodoStatus } from '@/services/apiService';
import { showToast, Toast } from '@/ts/toasts';
import { isOverdue } from '@/ts/dueDateFilters';
import { Button } from 'agnostic-vue';

const router = useRouter();
const todos = ref<Todo[]>([]);
const isLoading = ref<boolean>(true);

// Id of the todo currently dragged; null while nothing is being dragged.
const draggedTodoId = ref<number | null>(null);
// Column that is the current drop target, used for the drop-zone highlight.
const dropTargetStatus = ref<TodoStatus | null>(null);

const columns: { status: TodoStatus; label: string }[] = [
  { status: 'OPEN', label: 'Open' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'DONE', label: 'Done' },
];

const todosByStatus = computed<Record<TodoStatus, Todo[]>>(() => {
  const grouped: Record<TodoStatus, Todo[]> = {
    OPEN: [],
    IN_PROGRESS: [],
    DONE: [],
  };
  for (const todo of todos.value) {
    grouped[todo.status]?.push(todo);
  }
  return grouped;
});

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

/**
 * Marks a todo as the one being dragged.
 *
 * @param event - the native dragstart event
 * @param todo - the todo attached to the dragged card
 */
function handleDragStart(event: DragEvent, todo: Todo) {
  draggedTodoId.value = todo.id;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    // Text payload as fallback for browsers that do not expose custom types
    // to the drop target during dragover.
    event.dataTransfer.setData('text/plain', String(todo.id));
  }
}

// Clears the drag state when the drag gesture ends without a drop.
function handleDragEnd() {
  draggedTodoId.value = null;
  dropTargetStatus.value = null;
}

/**
 * Highlights a column while an eligible card hovers over it.
 *
 * @param event - the native dragover event
 * @param status - the column the pointer is over
 */
function handleDragOver(event: DragEvent, status: TodoStatus) {
  if (draggedTodoId.value === null) return;
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move';
  }
  dropTargetStatus.value = status;
}

// Removes the column highlight when the card leaves the drop zone.
function handleDragLeave(status: TodoStatus) {
  if (dropTargetStatus.value === status) {
    dropTargetStatus.value = null;
  }
}

/**
 * Drops the dragged todo into the given column and persists the transition.
 *
 * The local list updates optimistically and rolls back on failure so the
 * board stays responsive even with a slow backend.
 *
 * @param event - the native drop event
 * @param status - the column the card was dropped into
 */
async function handleDrop(event: DragEvent, status: TodoStatus) {
  event.preventDefault();
  dropTargetStatus.value = null;

  let todoId = draggedTodoId.value;
  if (todoId === null && event.dataTransfer) {
    // Fallback for the text/plain payload set in handleDragStart.
    const raw = event.dataTransfer.getData('text/plain');
    todoId = raw ? Number(raw) : null;
  }
  draggedTodoId.value = null;
  if (todoId === null || Number.isNaN(todoId)) return;

  const todo = todos.value.find((t) => t.id === todoId);
  if (!todo || todo.status === status) return;

  const originalStatus = todo.status;
  todo.status = status;
  try {
    await transitionTodoStatus(todoId, status);
    showToast(new Toast('Success', `"${todo.title}" moved to ${status.replace('_', ' ').toLowerCase()}.`, 'success'));
    await fetchTodos();
  } catch (error) {
    console.error('Error transitioning todo status:', error);
    showToast(new Toast('Error', 'Failed to move the todo.', 'error'));
    todo.status = originalStatus;
  }
}

function handleEdit(id: number) {
  router.push(`/todos/${id}/edit`);
}

function createNewTodo() {
  router.push('/todos/create');
}

onMounted(fetchTodos);
</script>

<template>
  <div class="board-view">
    <div class="view-header">
      <h1 class="heading">Kanban Board</h1>
      <Button mode="primary" @click="createNewTodo">Create New Todo</Button>
    </div>

    <div v-if="isLoading" class="loading-message card">Loading board...</div>
    <div v-else class="board">
      <section
        v-for="column in columns"
        :key="column.status"
        :class="[
          'board-column',
          { 'drop-target': dropTargetStatus === column.status },
        ]"
        :data-status="column.status"
        @dragover="handleDragOver($event, column.status)"
        @dragleave="handleDragLeave(column.status)"
        @drop="handleDrop($event, column.status)"
      >
        <h2 class="column-title">
          {{ column.label }}
          <span class="column-count">{{ todosByStatus[column.status].length }}</span>
        </h2>

        <div class="column-cards">
          <article
            v-for="todo in todosByStatus[column.status]"
            :key="todo.id"
            :class="['board-card', { 'is-dragging': draggedTodoId === todo.id }]"
            draggable="true"
            @dragstart="handleDragStart($event, todo)"
            @dragend="handleDragEnd"
          >
            <p class="card-title">{{ todo.title }}</p>
            <p v-if="todo.description" class="card-description">{{ todo.description }}</p>
            <div class="card-meta">
              <span v-if="todo.category" class="meta-tag">{{ todo.category }}</span>
              <span class="meta-tag priority-tag">{{ todo.priority }}</span>
              <span v-if="isOverdue(todo)" class="meta-tag overdue-tag">Overdue</span>
            </div>
            <div class="card-actions">
              <button type="button" class="link-button" @click.stop="handleEdit(todo.id)">Edit</button>
            </div>
          </article>

          <p v-if="todosByStatus[column.status].length === 0" class="empty-column-hint">
            Drop tasks here
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.board-view {
  width: 100%;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.loading-message {
  text-align: center;
  padding: var(--space-xl);
  color: var(--color-text-light);
}

.board {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-md);
}

.board-column {
  background-color: var(--color-surface-alt);
  border: 2px dashed transparent;
  border-radius: var(--border-radius-md);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  min-height: 320px;
}

.board-column.drop-target {
  border-color: var(--color-primary);
  background-color: var(--color-surface);
}

.column-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.column-count {
  font-size: 0.8rem;
  font-weight: 600;
  background-color: var(--color-secondary);
  color: var(--color-text-light);
  border-radius: 999px;
  padding: 1px 8px;
}

.column-cards {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  flex-grow: 1;
}

.board-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
  box-shadow: var(--box-shadow-sm);
  padding: var(--space-md);
  cursor: grab;
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.board-card.is-dragging {
  opacity: 0.5;
}

.card-title {
  font-weight: 600;
  color: var(--color-text);
}

.card-description {
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
  font-size: 0.75rem;
}

.meta-tag {
  padding: 2px 8px;
  border-radius: 12px;
  background-color: var(--color-secondary);
  color: var(--color-text-light);
  text-transform: capitalize;
}

.priority-tag {
  font-weight: 600;
}

.overdue-tag {
  background-color: var(--color-priority-high-bg);
  color: var(--color-priority-high);
  font-weight: 700;
}

.card-actions {
  margin-top: var(--space-xs);
}

.link-button {
  background: none;
  border: none;
  padding: 0;
  font-size: 0.8rem;
  color: var(--color-primary);
  cursor: pointer;
  text-decoration: underline;
}

.empty-column-hint {
  text-align: center;
  color: var(--color-text-light);
  font-size: 0.85rem;
  padding: var(--space-lg);
  border: 1px dashed var(--color-border);
  border-radius: var(--border-radius-sm);
}
</style>
