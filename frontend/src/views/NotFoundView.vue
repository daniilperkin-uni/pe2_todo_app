<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Button } from 'agnostic-vue';

const router = useRouter();
</script>

<template>
  <div class="not-found-view">
    <h1 class="heading">Page not found</h1>
    <p class="not-found-text">The page you requested does not exist.</p>
    <Button mode="primary" @click="router.push('/todos')">Back to todos</Button>
  </div>
</template>

<style scoped>
.not-found-view {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-xl);
  text-align: center;
}

.not-found-text {
  color: var(--color-text-light);
  margin-bottom: var(--space-lg);
}
</style>
