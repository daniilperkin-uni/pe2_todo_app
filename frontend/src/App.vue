<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
import { Close, Toast, Toasts } from 'agnostic-vue'
import { activeToasts } from '@/ts/toasts'
import { activeTheme, toggleTheme } from '@/ts/theme'

import 'agnostic-vue/dist/common.min.css'
import 'agnostic-vue/dist/index.css'
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
          <RouterLink to="/stats">Stats</RouterLink>
          <button
            type="button"
            class="theme-toggle"
            :title="
              activeTheme === 'dark' ? 'Switch to the light theme' : 'Switch to the dark theme'
            "
            :aria-label="
              activeTheme === 'dark' ? 'Switch to the light theme' : 'Switch to the dark theme'
            "
            @click="toggleTheme"
          >
            {{ activeTheme === 'dark' ? '☀️' : '🌙' }}
          </button>
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
  /* The global h1 margin-bottom (1.5rem) is meant for page headings: flexbox
     centres the margin box, so it pushed the wordmark 12px above the bar's
     centre. Zeroing it puts the wordmark and its underline on the axis. */
  margin: 0;
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
  transition:
    color 0.2s,
    border-color 0.2s;
}

.main-nav a:hover {
  color: var(--color-primary);
}

.main-nav a.router-link-exact-active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.theme-toggle {
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
  padding: var(--space-xs) var(--space-sm);
  font-size: 0.95rem;
  line-height: 1;
  cursor: pointer;
}

.theme-toggle:hover {
  border-color: var(--color-border-hover);
  background-color: var(--color-secondary);
}

.main-content {
  flex-grow: 1;
  padding: var(--space-xl);
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}
</style>
