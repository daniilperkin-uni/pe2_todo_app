<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue';
import type { Todo } from '@/types/todo';
import { Button } from 'agnostic-vue';

const props = defineProps<{
  todo: Todo;
}>();

const emit = defineEmits(['toggle-finished', 'edit', 'delete']);

const priorityClass = computed(() => {
  switch (props.todo.priority) {
    case 'LOW':
      return 'priority-low';
    case 'MEDIUM':
      return 'priority-medium';
    case 'HIGH':
      return 'priority-high';
    default:
      return '';
  }
});

const assigneeNames = computed(() => {
  if (!props.todo.assigneeList || props.todo.assigneeList.length === 0) {
    return 'Not assigned';
  }
  return props.todo.assigneeList.map(a => `${a.prename} ${a.name}`).join(', ');
});

function handleToggleFinished() {
  emit('toggle-finished', props.todo.id, !props.todo.finished);
}

function handleEdit() {
  emit('edit', props.todo.id);
}

function handleDelete() {
  emit('delete', props.todo.id);
}
</script>

<template>
  <div :class="['todo-card', { 'is-finished': todo.finished }]">
    <div class="main-content">
      <input
        type="checkbox"
        :id="`todo-checkbox-${todo.id}`"
        :checked="todo.finished"
        class="todo-checkbox"
        @change="handleToggleFinished"
      />
      <div class="todo-info">
        <label :for="`todo-checkbox-${todo.id}`" class="todo-title">{{ todo.title }}</label>
        <p class="todo-description">{{ todo.description }}</p>
        <div class="todo-meta">
          <span v-if="todo.category" class="meta-tag category-tag">{{ todo.category }}</span>
          <span :class="['meta-tag', 'priority-tag', priorityClass]">{{ todo.priority }}</span>
          <span v-if="todo.dueDate" class="meta-tag due-date-tag">
            Due: {{ new Date(todo.dueDate).toLocaleDateString() }}
          </span>
        </div>
      </div>
    </div>
    <div class="todo-actions">
      <div class="assignee-info" :title="assigneeNames">
        {{ assigneeNames }}
      </div>
      <div class="action-buttons">
        <Button size="small" mode="secondary" @click="handleEdit">Edit</Button>
        <Button size="small" mode="danger" @click="handleDelete">Delete</Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.todo-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-md);
  box-shadow: var(--box-shadow-sm);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.todo-card:hover {
  box-shadow: var(--box-shadow-md);
  border-color: var(--color-border-hover);
}

.is-finished {
  background-color: #fafafa;
}

.is-finished .todo-title,
.is-finished .todo-description {
  text-decoration: line-through;
  color: var(--color-text-light);
}

.main-content {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.todo-checkbox {
  margin-top: 5px;
  width: 18px;
  height: 18px;
  accent-color: var(--color-primary);
}

.todo-info {
  flex-grow: 1;
}

.todo-title {
  font-size: 1.125rem;
  font-weight: 600;
  cursor: pointer;
  display: block;
  margin-bottom: var(--space-xs);
}

.todo-description {
  font-size: 0.9rem;
  color: var(--color-text-light);
  margin-bottom: var(--space-md);
}

.todo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  font-size: 0.8rem;
}

.meta-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  background-color: var(--color-secondary);
  color: var(--color-text-light);
  text-transform: capitalize;
}

.priority-tag.priority-low {
  background-color: #eaf7eb;
  color: #58a15c;
}
.priority-tag.priority-medium {
  background-color: #fdf3e6;
  color: #e49122;
}
.priority-tag.priority-high {
  background-color: #fbeae9;
  color: #d14343;
}

.todo-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-md);
  margin-left: calc(18px + var(--space-md)); /* Align with text */
}

.assignee-info {
  font-size: 0.85rem;
  color: var(--color-text-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.action-buttons {
  display: flex;
  gap: var(--space-sm);
}
</style>
