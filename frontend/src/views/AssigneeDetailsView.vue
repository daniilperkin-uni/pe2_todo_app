<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getAssignee, updateAssignee, deleteAssignee } from '@/services/apiService';
import AssigneeForm from '@/components/AssigneeForm.vue';
import type { Assignee, AssigneeCreateUpdate } from '@/types/assignee';
import { showToast, Toast } from '@/ts/toasts';
import { Button } from 'agnostic-vue';

const route = useRoute();
const router = useRouter();
const assignee = ref<Assignee | null>(null);
const isLoading = ref<boolean>(true);
const isEditing = ref<boolean>(false);

const assigneeId = ref<number>(Number(route.params.id));

// Ruft die Details des Zuständigen vom Backend ab.
async function fetchAssignee() {
  isLoading.value = true;
  try {
    assignee.value = await getAssignee(assigneeId.value);
  } catch (error) {
    console.error('Error fetching assignee:', error);
    showToast(new Toast('Error', 'Failed to load assignee. Displaying offline data if available.', 'error'));
    assignee.value = null; // Ensure it's null if not found or error
  } finally {
    isLoading.value = false;
  }
}

// Aktualisiert die Daten des Zuständigen im Backend.
async function handleUpdate(updatedAssignee: AssigneeCreateUpdate) {
  try {
    await updateAssignee(assigneeId.value, updatedAssignee);
    showToast(new Toast('Success', 'Assignee updated successfully!', 'success'));
    isEditing.value = false;
    await fetchAssignee(); // Refresh details
  } catch (error) {
    console.error('Error updating assignee:', error);
    showToast(new Toast('Error', 'Failed to update assignee.', 'error'));
  }
}

// Löscht den Zuständigen nach Bestätigung.
async function handleDelete() {
  if (confirm('Are you sure you want to delete this assignee?')) {
    try {
      await deleteAssignee(assigneeId.value);
      showToast(new Toast('Success', 'Assignee deleted successfully!', 'success'));
      router.push('/assignees'); // Go back to list
    } catch (error) {
      console.error('Error deleting assignee:', error);
      showToast(new Toast('Error', 'Failed to delete assignee.', 'error'));
    }
  }
}

// Aktiviert den Bearbeitungsmodus.
function startEditing() {
  isEditing.value = true;
}

// Deaktiviert den Bearbeitungsmodus.
function cancelEditing() {
  isEditing.value = false;
}

// Watch for changes in route params (e.g., id) to re-fetch data
watch(
  () => route.params.id,
  (newId) => {
    assigneeId.value = Number(newId);
    fetchAssignee();
  },
);

onMounted(fetchAssignee);
</script>

<template>
  <div class="assignee-details-view">
    <h1 class="heading">Assignee Details</h1>

    <div v-if="isLoading">Loading assignee details...</div>
    <div v-else-if="!assignee">Assignee not found.</div>
    <div v-else class="assignee-content">
      <div v-if="!isEditing" class="details-display card border-none">
        <p><strong>ID:</strong> {{ assignee.id }}</p>
        <p><strong>Prename:</strong> {{ assignee.prename }}</p>
        <p><strong>Name:</strong> {{ assignee.name }}</p>
        <p><strong>Email:</strong> {{ assignee.email }}</p>
        <div class="actions">
          <Button mode="primary" @click="startEditing">Edit Assignee</Button>
          <Button mode="danger" @click="handleDelete">Delete Assignee</Button>
          <Button mode="secondary" @click="router.push('/assignees')">Back to List</Button>
        </div>
      </div>
      <AssigneeForm
        v-else
        :initialAssignee="assignee"
        :isEdit="true"
        @submit="handleUpdate"
        @cancel="cancelEditing"
      />
    </div>
  </div>
</template>

<style scoped>
.assignee-details-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.heading {
  margin-bottom: 20px;
  text-align: center;
}
.assignee-content {
  margin-top: 20px;
}
.details-display {
  padding: 20px;
  border-radius: 8px;
}
.details-display p {
  margin-bottom: 10px;
}
.details-display strong {
  margin-right: 5px;
}
.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
