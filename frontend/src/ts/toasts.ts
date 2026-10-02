import { type Ref, ref } from 'vue'

type ToastType = 'info' | 'success' | 'error' | 'warning' | 'dark'

export class Toast {
  title: string
  message: string
  type: ToastType
  expired = false
  key = Math.random()

  /**
   * Creates a toast that closes itself after the given timeout.
   *
   * @param title - headline shown in the toast
   * @param message - the notification text
   * @param type - visual style, defaults to 'info'
   * @param timeout - seconds until the toast closes itself
   */
  constructor(title: string, message: string, type: ToastType | null = null, timeout: number = 5) {
    this.title = title
    this.message = message
    this.type = type || 'info'

    setTimeout(() => this.close(), timeout * 1_000)
  }

  /** Marks the toast as expired and removes it from the active list. */
  close() {
    this.expired = true
    activeToasts.value = activeToasts.value.filter((toast) => !toast.expired)
  }
}

export const activeToasts: Ref<Toast[]> = ref([])

/**
 * Shows the given toast and keeps it in the global active list until it
 * expires.
 *
 * @param toast - the toast to display
 */
export function showToast(toast: Toast) {
  activeToasts.value.push(toast)
}
