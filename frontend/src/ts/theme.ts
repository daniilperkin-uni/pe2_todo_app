import { ref } from 'vue'

export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'pe2-theme'

/**
 * The theme currently applied to the document. The inline bootstrap script in
 * index.html sets the data-theme attribute before first paint (stored choice,
 * otherwise the system preference); this ref mirrors it for the UI.
 */
export const activeTheme = ref<Theme>(
  document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light'
)

/** Applies the given theme and remembers the choice across visits. */
export function applyTheme(next: Theme): void {
  activeTheme.value = next
  document.documentElement.setAttribute('data-theme', next)
  try {
    localStorage.setItem(STORAGE_KEY, next)
  } catch {
    // Storage can be unavailable (private mode); the theme still applies.
  }
}

/** Switches between the light and the dark theme. */
export function toggleTheme(): void {
  applyTheme(activeTheme.value === 'dark' ? 'light' : 'dark')
}
