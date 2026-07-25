<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import type { AssigneeCreateUpdate } from '@/types/assignee';
import { Button } from 'agnostic-vue';

const props = defineProps<{
  initialAssignee?: AssigneeCreateUpdate; // For editing existing assignees
  isEdit?: boolean;
}>();

const emit = defineEmits(['submit', 'cancel']);

const assigneeForm = ref<AssigneeCreateUpdate>({
  prename: '',
  name: '',
  email: '',
});

const errors = ref<{ [key: string]: string }>({});

watchEffect(() => {
  if (props.initialAssignee) {
    assigneeForm.value = { ...props.initialAssignee };
  }
});

// Validiert die Formularfelder des Zuständigen.
function validateForm() {
  errors.value = {};
  if (!assigneeForm.value.prename) {
    errors.value.prename = 'Vorname ist erforderlich.';
  }
  if (!assigneeForm.value.name) {
    errors.value.name = 'Nachname ist erforderlich.';
  }
  if (!assigneeForm.value.email) {
    errors.value.email = 'E-Mail ist erforderlich.';
  }

  return Object.keys(errors.value).length === 0;
}

// Verarbeitet das Absenden des Formulars, trimmt die E-Mail und emittiert das 'submit'-Event.
function handleSubmit() {
  if (validateForm()) {
    const trimmedAssigneeForm: AssigneeCreateUpdate = {
      ...assigneeForm.value,
      email: assigneeForm.value.email.trim(),
    };
    emit('submit', trimmedAssigneeForm);
  }
}

// Emittiert das 'cancel'-Event.
function handleCancel() {
  emit('cancel');
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="assignee-form card border-none">
    <div class="form-group">
      <label for="prename">Vorname:</label>
      <input type="text" id="prename" v-model="assigneeForm.prename" @input="delete errors.prename" />
      <span v-if="errors.prename" class="error-message">{{ errors.prename }}</span>
    </div>

    <div class="form-group">
      <label for="name">Nachname:</label>
      <input type="text" id="name" v-model="assigneeForm.name" @input="delete errors.name" />
      <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
    </div>

    <div class="form-group">
      <label for="email">E-Mail:</label>
      <input type="email" id="email" v-model="assigneeForm.email" @input="delete errors.email" />
      <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
    </div>

    <div class="form-actions">
      <Button type="submit" mode="primary">{{ isEdit ? 'Update Assignee' : 'Create Assignee' }}</Button>
      <Button type="button" mode="secondary" @click="handleCancel">Abbrechen</Button>
    </div>
  </form>
</template>

<style scoped>
.assignee-form {
  max-width: 500px;
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
.form-group input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
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
