<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getTodo, createTodo, updateTodo } from '@/services/apiService';
import TodoForm from '@/components/TodoForm.vue';
import type { Todo, TodoCreateUpdate } from '@/types/todo';
import { showToast, Toast } from '@/ts/toasts';

const route = useRoute();
const router = useRouter();
const todo = ref<Todo | null>(null);
const isLoading = ref<boolean>(true);
const isEditMode = ref<boolean>(false);

const todoId = ref<number | null>(null);

onMounted(() => {
  if (route.params.id) {
    isEditMode.value = true;
    todoId.value = Number(route.params.id);
    fetchTodo();
  } else {
    isEditMode.value = false;
    isLoading.value = false; // No need to load for creation
  }
});

// Ruft die Details eines ToDos vom Backend ab.
async function fetchTodo() {
  isLoading.value = true;
  try {
    if (todoId.value) {
      todo.value = await getTodo(todoId.value);
    }
  } catch (error) {
    console.error('Error fetching todo:', error);
    showToast(new Toast('Error', 'Failed to load todo details. Displaying offline data if available.', 'error'));
    todo.value = null;
  } finally {
    isLoading.value = false;
  }
}

// Verarbeitet das Absenden des Formulars zum Erstellen oder Aktualisieren eines ToDos.
async function handleSubmit(todoPayload: TodoCreateUpdate) {
  try {
    if (isEditMode.value && todoId.value) {
      await updateTodo(todoId.value, todoPayload);
      showToast(new Toast('Success', 'Todo updated successfully!', 'success'));
    } else {
      await createTodo(todoPayload);
      showToast(new Toast('Success', 'Todo created successfully!', 'success'));
    }
    router.push('/todos'); // Go back to todo list after submission
  } catch (error) {
    console.error('Error submitting todo:', error);
    showToast(new Toast('Error', `Failed to ${isEditMode.value ? 'update' : 'create'} todo.`, 'error'));
  }
}

// Leitet den Benutzer zur ToDo-Übersichtsseite um.
function handleCancel() {
  router.push('/todos');
}
</script>

<template>
  <div class="create-update-todo-view">
    <h1 class="heading">{{ isEditMode ? 'Edit Todo' : 'Create New Todo' }}</h1>

    <div v-if="isLoading">Loading todo details...</div>
    <div v-else-if="isEditMode && !todo">Todo not found.</div>
    <TodoForm
      v-else
      :initialTodo="todo || undefined"
      :isEdit="isEditMode"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>

<style scoped>
.create-update-todo-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.heading {
  margin-bottom: 20px;
  text-align: center;
}
</style>
