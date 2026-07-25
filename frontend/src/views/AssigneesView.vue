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

// Ruft alle Zuständigen vom Backend ab oder lädt Offline-Daten bei Fehler.
async function fetchAssignees() {
  isLoading.value = true;
  try {
    assignees.value = await getAssignees();
  } catch (error) {
    console.error('Error fetching assignees:', error);
    showToast(new Toast('Error', 'Failed to load assignees. Displaying offline data.', 'error'));
  } finally {
    isLoading.value = false;
  }
}

// Löscht einen Zuständigen und aktualisiert die Liste.
async function handleDelete(id: number) {
  if (confirm('Are you sure you want to delete this assignee?')) {
    try {
      await deleteAssignee(id);
      showToast(new Toast('Success', 'Assignee deleted successfully.', 'success'));
      await fetchAssignees(); // Refresh the list
    } catch (error) {
      console.error('Error deleting assignee:', error);
      showToast(new Toast('Error', 'Failed to delete assignee.', 'error'));
    }
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

    <div v-if="isLoading">Loading assignees...</div>
    <div v-else-if="assignees.length === 0">No assignees found.</div>
    <AssigneeList
      v-else
      :assignees="assignees"
      @edit="handleEdit"
      @delete="handleDelete"
      @details="handleDetails"
    />
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
</style>
