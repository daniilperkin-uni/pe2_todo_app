<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router';
import { Close, Toast, Toasts } from 'agnostic-vue';
import { activeToasts } from '@/ts/toasts';

import 'agnostic-vue/dist/common.min.css';
import 'agnostic-vue/dist/index.css';
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <div class="header-content">
        <h1 class="logo">
          <RouterLink to="/todos">ToDo App</RouterLink>
        </h1>
        <nav class="main-nav">
          <RouterLink to="/todos">Todos</RouterLink>
          <RouterLink to="/assignees">Assignees</RouterLink>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <RouterView />
    </main>
  </div>

  <Toasts vertical-position="top" horizontal-position="end">
    <template v-for="toast of activeToasts" :key="toast.key">
      <Toast :type="toast.type" class="alert alert-border-left alert-info">
        <div class="flex-fill flex flex-column">
          <div class="flex">
            <h3 class="flex-fill">
              {{ toast.title }}
            </h3>
            <Close @click="toast.close()" />
          </div>
          <div class="flex">
            <div class="flex-fill">
              {{ toast.message }}
            </div>
          </div>
        </div>
      </Toast>
      <div class="mbe14" />
    </template>
  </Toasts>
</template>

<style>
/* Global styles to override AgnosticUI defaults and apply the new design */
:root {
  --agn-button-primary-background: var(--color-primary);
  --agn-button-primary-border-color: var(--color-primary-dark);
  --agn-button-primary-color: var(--vt-c-white);
  --agn-button-primary-hover-background: var(--color-primary-dark);

  --agn-input-border-radius: var(--border-radius-sm);
  --agn-input-border-color: var(--color-border);
  --agn-input-hover-border-color: var(--color-border-hover);
  --agn-input-focus-border-color: var(--color-primary);
}

.card {
  background-color: var(--color-surface);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--box-shadow-sm);
}

.heading {
  font-size: 2rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-lg);
}
</style>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-background);
}

.app-header {
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--box-shadow-sm);
  padding: 0 var(--space-xl);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 1.5rem;
  font-weight: 600;
}

.logo a {
  color: var(--color-text);
  text-decoration: none;
}

.main-nav {
  display: flex;
  gap: var(--space-lg);
}

.main-nav a {
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-text-light);
  text-decoration: none;
  padding: var(--space-sm) 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
}

.main-nav a:hover {
  color: var(--color-primary);
}

.main-nav a.router-link-exact-active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.main-content {
  flex-grow: 1;
  padding: var(--space-xl);
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}
</style>
