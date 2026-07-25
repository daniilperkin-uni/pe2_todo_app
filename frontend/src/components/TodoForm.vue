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

// Ruft die Liste der verfügbaren Zuständigen vom Backend ab.
async function fetchAssignees() {
  isLoadingAssignees.value = true;
  try {
    availableAssignees.value = await getAssignees();
  } catch (error) {
    console.error('Error fetching assignees for form:', error);
    // In case of error, availableAssignees will remain empty or with offline data if getAssignees provided it
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
      assigneeIdList: props.initialTodo.assigneeList?.map(a => a.id) || [],
    };
  }
});

// Validiert die Formularfelder des ToDos.
function validateForm() {
  errors.value = {};
  if (!todoForm.value.title) {
    errors.value.title = 'Titel ist erforderlich.';
  }
  // Optional: Add more validation for dueDate, etc.

  return Object.keys(errors.value).length === 0;
}

// Verarbeitet das Absenden des Formulars und emittiert das 'submit'-Event.
function handleSubmit() {
  if (validateForm()) {
    emit('submit', todoForm.value);
  }
}

// Emittiert das 'cancel'-Event.
function handleCancel() {
  emit('cancel');
}

onMounted(fetchAssignees);
</script>

<template>
  <form @submit.prevent="handleSubmit" class="todo-form card border-none">
    <div class="form-group">
      <label for="title">Titel:</label>
      <input type="text" id="title" v-model="todoForm.title" @input="delete errors.title" />
      <span v-if="errors.title" class="error-message">{{ errors.title }}</span>
    </div>

    <div class="form-group">
      <label for="description">Beschreibung:</label>
      <textarea id="description" v-model="todoForm.description"></textarea>
    </div>

    <div class="form-group">
      <label for="dueDate">Fälligkeitsdatum:</label>
      <input type="date" id="dueDate" v-model="todoForm.dueDate" />
    </div>

    <div class="form-group">
      <label for="priority">Priorität:</label>
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
      <label for="finished">Erledigt</label>
    </div>

    <div class="form-actions">
      <Button type="submit" mode="primary">{{ isEdit ? 'Update Todo' : 'Create Todo' }}</Button>
      <Button type="button" mode="secondary" @click="handleCancel">Abbrechen</Button>
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
.form-group input[type="text"],
.form-group input[type="date"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.form-group select[multiple] {
  min-height: 100px;
}
.form-hint {
  font-size: 0.8em;
  color: #666;
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
  color: red;
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
