<script setup lang="ts">
import type { Todo } from '@/types/todo';
import TodoItem from '@/components/TodoItem.vue';

defineProps<{
  todos: Todo[];
}>();

const emit = defineEmits(['toggle-finished', 'edit', 'delete', 'report-priority']);

function handleToggleFinished(id: number, finished: boolean) {
  emit('toggle-finished', id, finished);
}

function handleEdit(id: number) {
  emit('edit', id);
}

function handleDelete(id: number) {
  emit('delete', id);
}

function handleReportPriority(todo: Todo) {
  emit('report-priority', todo);
}
</script>

<template>
  <div class="todo-list-container">
    <div v-if="todos.length === 0" class="no-todos-message card">
      No tasks found.
    </div>
    <div v-else class="todo-list">
      <TodoItem
        v-for="todo in todos"
        :key="todo.id"
        :todo="todo"
        @toggle-finished="handleToggleFinished"
        @edit="handleEdit"
        @delete="handleDelete"
        @report-priority="handleReportPriority"
      />
    </div>
  </div>
</template>

<style scoped>
.todo-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md); /* Consistent spacing between cards */
}

.no-todos-message {
  text-align: center;
  padding: var(--space-xl);
  color: var(--color-text-light);
}
</style>
