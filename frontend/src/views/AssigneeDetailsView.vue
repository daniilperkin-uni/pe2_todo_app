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
const showDeleteConfirm = ref<boolean>(false);

const assigneeId = ref<number>(Number(route.params.id));

// Ruft die Details des Zuständigen vom Backend ab.
async function fetchAssignee() {
  isLoading.value = true;
  try {
    assignee.value = await getAssignee(assigneeId.value);
  } catch (error) {
    console.error('Error fetching assignee:', error);
    showToast(new Toast('Error', 'Failed to load assignee details. Please try again later.', 'error'));
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

// Öffnet den In-App-Bestätigungsdialog zum Löschen.
function requestDelete() {
  showDeleteConfirm.value = true;
}

// Bricht das Löschen ab und schließt den Bestätigungsdialog.
function cancelDelete() {
  showDeleteConfirm.value = false;
}

// Löscht den Zuständigen nach Bestätigung.
async function confirmDelete() {
  showDeleteConfirm.value = false;
  try {
    await deleteAssignee(assigneeId.value);
    showToast(new Toast('Success', 'Assignee deleted successfully!', 'success'));
    router.push('/assignees'); // Go back to list
  } catch (error: unknown) {
    console.error('Error deleting assignee:', error);
    const message = error instanceof Error ? error.message : String(error);
    showToast(new Toast('Error', `Failed to delete assignee: ${message}`, 'error'));
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
          <Button mode="danger" @click="requestDelete">Delete Assignee</Button>
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

    <!-- In-app delete confirmation dialog -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal card" role="dialog" aria-modal="true" aria-labelledby="confirm-delete-detail-title">
        <h3 id="confirm-delete-detail-title" class="modal-title">Delete this assignee?</h3>
        <p class="modal-text">This will remove the assignee from all associated todos. This action cannot be undone.</p>
        <div class="modal-actions">
          <Button mode="secondary" @click="cancelDelete">Cancel</Button>
          <Button mode="danger" @click="confirmDelete">Delete</Button>
        </div>
      </div>
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
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  max-width: 400px;
  width: calc(100% - 32px);
  padding: var(--space-lg, 20px);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm, 10px);
}
.modal-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text);
}
.modal-text {
  color: var(--color-text-light);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm, 10px);
  margin-top: var(--space-sm, 10px);
}
</style>
