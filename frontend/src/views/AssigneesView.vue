<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { Assignee } from '@/types/assignee';
import { getAssignees, deleteAssignee } from '@/services/apiService';
import AssigneeList from '@/components/AssigneeList.vue';
import { showToast, Toast } from '@/ts/toasts';
import { Button } from 'agnostic-vue';

const router = useRouter();
const assignees = ref<Assignee[]>([]);
const isLoading = ref<boolean>(true);
const assigneeToDelete = ref<number | null>(null);

// Ruft alle Zuständigen vom Backend ab.
async function fetchAssignees() {
  isLoading.value = true;
  try {
    assignees.value = await getAssignees();
  } catch (error) {
    console.error('Error fetching assignees:', error);
    showToast(new Toast('Error', 'Failed to load assignees. Please try again later.', 'error'));
  } finally {
    isLoading.value = false;
  }
}

// Öffnet den In-App-Bestätigungsdialog zum Löschen.
function requestDelete(id: number) {
  assigneeToDelete.value = id;
}

// Bricht das Löschen ab und schließt den Bestätigungsdialog.
function cancelDelete() {
  assigneeToDelete.value = null;
}

// Bestätigt das Löschen eines Zuständigen und aktualisiert die Liste.
async function confirmDelete() {
  const id = assigneeToDelete.value;
  if (id === null) return;
  assigneeToDelete.value = null;
  try {
    await deleteAssignee(id);
    showToast(new Toast('Success', 'Assignee deleted successfully.', 'success'));
    await fetchAssignees(); // Refresh the list
  } catch (error: unknown) {
    console.error('Error deleting assignee:', error);
    const message = error instanceof Error ? error.message : String(error);
    showToast(new Toast('Error', `Failed to delete assignee: ${message}`, 'error'));
  }
}

// Navigiert zur Bearbeitungsseite des Zuständigen.
function handleEdit(id: number) {
  router.push(`/assignees/${id}/edit`);
}

// Navigiert zur Detailseite des Zuständigen.
function handleDetails(id: number) {
  router.push(`/assignees/${id}`);
}

// Navigiert zur Seite zum Erstellen eines neuen Zuständigen.
function createNewAssignee() {
  router.push('/create-assignee');
}

onMounted(fetchAssignees);
</script>

<template>
  <div class="assignees-view">
    <h1 class="heading">Assignees Overview</h1>

    <div class="controls card border-none">
      <Button mode="primary" @click="createNewAssignee">Create New Assignee</Button>
    </div>

    <div v-if="isLoading" class="loading-message card">Loading assignees...</div>
    <div v-else-if="assignees.length === 0" class="empty-state card">
      <p class="empty-state-title">No assignees found</p>
      <p class="empty-state-text">Create an assignee to get started.</p>
      <Button mode="primary" @click="createNewAssignee">Create New Assignee</Button>
    </div>
    <AssigneeList
      v-else
      :assignees="assignees"
      @edit="handleEdit"
      @delete="requestDelete"
      @details="handleDetails"
    />

    <!-- In-app delete confirmation dialog -->
    <div v-if="assigneeToDelete !== null" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal card" role="dialog" aria-modal="true" aria-labelledby="confirm-delete-assignee-title">
        <h3 id="confirm-delete-assignee-title" class="modal-title">Delete this assignee?</h3>
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
.assignees-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.heading {
  margin-bottom: 20px;
  text-align: center;
}
.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
}
.loading-message {
  text-align: center;
  padding: var(--space-xl, 30px);
  color: var(--color-text-light);
}
.empty-state {
  text-align: center;
  padding: var(--space-xl, 30px);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm, 10px);
}
.empty-state-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}
.empty-state-text {
  color: var(--color-text-light);
  margin-bottom: var(--space-sm, 10px);
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
