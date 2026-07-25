<script setup lang="ts">
import { useRouter } from 'vue-router';
import { createAssignee } from '@/services/apiService';
import AssigneeForm from '@/components/AssigneeForm.vue';
import type { AssigneeCreateUpdate } from '@/types/assignee';
import { showToast, Toast } from '@/ts/toasts';

const router = useRouter();

// Verarbeitet das Absenden des Formulars und erstellt einen Zuständigen.
async function handleSubmit(assignee: AssigneeCreateUpdate) {
  try {
    await createAssignee(assignee);
    showToast(new Toast('Success', 'Assignee created successfully!', 'success'));
    router.push('/assignees');
  } catch (error) {
    console.error('Error creating assignee:', error);
    showToast(new Toast('Error', 'Failed to create assignee.', 'error'));
  }
}

// Leitet den Benutzer zur Zuständigen-Übersichtsseite um.
function handleCancel() {
  router.push('/assignees');
}
</script>

<template>
  <div class="create-assignee-view">
    <h1 class="heading">Create New Assignee</h1>
    <AssigneeForm @submit="handleSubmit" @cancel="handleCancel" />
  </div>
</template>

<style scoped>
.create-assignee-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.heading {
  margin-bottom: 20px;
  text-align: center;
}
</style>
