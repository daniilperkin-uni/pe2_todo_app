<script setup lang="ts">
import { defineProps } from 'vue';
import type { Assignee } from '@/types/assignee';
import { Button } from 'agnostic-vue';

const props = defineProps<{
  assignees: Assignee[];
}>();

const emit = defineEmits(['edit', 'delete', 'details']);

// Emittiert das 'edit'-Event mit der ID des Zuständigen.
function handleEdit(id: number) {
  emit('edit', id);
}

// Emittiert das 'delete'-Event mit der ID des Zuständigen.
function handleDelete(id: number) {
  emit('delete', id);
}

// Emittiert das 'details'-Event mit der ID des Zuständigen.
function handleDetails(id: number) {
  emit('details', id);
}
</script>

<template>
  <div class="assignee-list">
    <table class="card border-none">
      <thead>
        <tr>
          <th>ID</th>
          <th>Vorname</th>
          <th>Nachname</th>
          <th>E-Mail</th>
          <th>Aktionen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="assignee in props.assignees" :key="assignee.id">
          <td>{{ assignee.id }}</td>
          <td>{{ assignee.prename }}</td>
          <td>{{ assignee.name }}</td>
          <td>{{ assignee.email }}</td>
          <td>
            <Button small mode="primary" @click="handleEdit(assignee.id)">Bearbeiten</Button>
            <Button small mode="secondary" @click="handleDetails(assignee.id)">Details</Button>
            <Button small mode="danger" @click="handleDelete(assignee.id)">Löschen</Button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* Add any specific styles for AssigneeList here if needed */
.assignee-list {
  margin-top: 20px;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
th {
  background-color: #f2f2f2;
}
</style>
