import { beforeEach, describe, expect, it } from 'vitest'
import { activeTheme, applyTheme, toggleTheme } from '@/ts/theme'

describe('theme', () => {
  beforeEach(() => {
    document.documentElement.removeAttribute('data-theme')
    localStorage.clear()
  })

  it('applies the theme to the document and persists it', () => {
    applyTheme('dark')

    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
    expect(localStorage.getItem('pe2-theme')).toBe('dark')
    expect(activeTheme.value).toBe('dark')
  })

  it('toggles between dark and light', () => {
    applyTheme('light')
    toggleTheme()
    expect(activeTheme.value).toBe('dark')
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')

    toggleTheme()
    expect(activeTheme.value).toBe('light')
    expect(document.documentElement.getAttribute('data-theme')).toBe('light')
  })
})
