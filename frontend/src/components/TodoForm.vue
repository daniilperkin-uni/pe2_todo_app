<script setup lang="ts">
import { ref, watchEffect, onMounted } from 'vue';
import type { Todo, TodoCreateUpdate, Priority } from '@/types/todo';
import type { Assignee } from '@/types/assignee';
import { Button } from 'agnostic-vue';
import { getAssignees } from '@/services/apiService';

const props = defineProps<{
  initialTodo?: Todo; // For editing existing todos
  isEdit?: boolean;
}>();

const emit = defineEmits(['submit', 'cancel']);

const todoForm = ref<TodoCreateUpdate>({
  title: '',
  description: '',
  finished: false,
  priority: 'LOW', // Default priority
  dueDate: '',
  assigneeIdList: [],
});

const availableAssignees = ref<Assignee[]>([]);
const isLoadingAssignees = ref<boolean>(true);
const errors = ref<{ [key: string]: string }>({});

const priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH'];

// Fetches the list of available assignees from the backend.
async function fetchAssignees() {
  isLoadingAssignees.value = true;
  try {
    availableAssignees.value = await getAssignees();
  } catch (error) {
    console.error('Error fetching assignees for form:', error);
    // On error, availableAssignees stays empty so the form shows the
    // "No assignees available." placeholder instead of fabricated data.
  } finally {
    isLoadingAssignees.value = false;
  }
}

watchEffect(() => {
  if (props.initialTodo) {
    todoForm.value = {
      title: props.initialTodo.title,
      description: props.initialTodo.description,
      finished: props.initialTodo.finished,
      priority: props.initialTodo.priority,
      dueDate: props.initialTodo.dueDate,
      assigneeIdList: props.initialTodo.assigneeList?.map((a) => a.id) || [],
    };
  }
});

// Validates the form fields. The due date, when provided, must lie strictly
// in the future to match the backend rule in TodoService.validateDueDate.
function validateForm() {
  errors.value = {};
  if (!todoForm.value.title) {
    errors.value.title = 'Title is required.';
  }
  if (todoForm.value.dueDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(todoForm.value.dueDate + 'T00:00:00');
    if (isNaN(due.getTime())) {
      errors.value.dueDate = 'Due date is not a valid date.';
    } else if (due.getTime() <= today.getTime()) {
      errors.value.dueDate = 'Due date must be in the future.';
    }
  }
  return Object.keys(errors.value).length === 0;
}

// Handles form submission and emits the 'submit' event when validation passes.
function handleSubmit() {
  if (validateForm()) {
    emit('submit', todoForm.value);
  }
}

// Emits the 'cancel' event.
function handleCancel() {
  emit('cancel');
}

onMounted(fetchAssignees);
</script>

<template>
  <form @submit.prevent="handleSubmit" class="todo-form card border-none">
    <div class="form-group">
      <label for="title">Title:</label>
      <input type="text" id="title" v-model="todoForm.title" @input="delete errors.title" />
      <span v-if="errors.title" class="error-message">{{ errors.title }}</span>
    </div>

    <div class="form-group">
      <label for="description">Description:</label>
      <textarea id="description" v-model="todoForm.description"></textarea>
    </div>

    <div class="form-group">
      <label for="dueDate">Due Date:</label>
      <input
        type="date"
        id="dueDate"
        v-model="todoForm.dueDate"
        @change="delete errors.dueDate"
      />
      <span v-if="errors.dueDate" class="error-message">{{ errors.dueDate }}</span>
    </div>

    <div class="form-group">
      <label for="priority">Priority:</label>
      <select id="priority" v-model="todoForm.priority">
        <option v-for="p in priorities" :key="p" :value="p">{{ p }}</option>
      </select>
    </div>

    <div class="form-group">
      <label for="assignees">Assignees:</label>
      <div v-if="isLoadingAssignees">Loading assignees...</div>
      <div v-else-if="availableAssignees.length === 0">No assignees available.</div>
      <select v-else id="assignees" v-model="todoForm.assigneeIdList" multiple>
        <option v-for="assignee in availableAssignees" :key="assignee.id" :value="assignee.id">
          {{ assignee.prename }} {{ assignee.name }}
        </option>
      </select>
      <small class="form-hint">Hold Ctrl/Cmd to select multiple assignees.</small>
    </div>

    <div class="form-group checkbox-group">
      <input type="checkbox" id="finished" v-model="todoForm.finished" />
      <label for="finished">Finished</label>
    </div>

    <div class="form-actions">
      <Button type="submit" mode="primary">{{ isEdit ? 'Update Todo' : 'Create Todo' }}</Button>
      <Button type="button" mode="secondary" @click="handleCancel">Cancel</Button>
    </div>
  </form>
</template>

<style scoped>
.todo-form {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input[type='text'],
.form-group input[type='date'],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
}
.form-group select[multiple] {
  min-height: 100px;
}
.form-hint {
  font-size: 0.8em;
  color: var(--color-text-light);
  margin-top: 5px;
  display: block;
}
.checkbox-group {
  display: flex;
  align-items: center;
  margin-top: 20px;
}
.checkbox-group input {
  margin-right: 10px;
}
.error-message {
  color: var(--color-error);
  font-size: 0.9em;
  margin-top: 5px;
  display: block;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
