import { type Ref, ref } from 'vue'

type ToastType = 'info' | 'success' | 'error' | 'warning' | 'dark'

export class Toast {
  title: string
  message: string
  type: ToastType
  expired = false
  key = Math.random()

  constructor(title: string, message: string, type: ToastType | null = null, timeout: number = 5) {
    this.title = title
    this.message = message
    this.type = type || 'info'

    setTimeout(() => this.close(), timeout * 1_000)
  }

  close() {
    this.expired = true
    activeToasts.value = activeToasts.value.filter((toast) => !toast.expired)
  }
}

export const activeToasts: Ref<Toast[]> = ref([])

export function showToast(toast: Toast) {
  activeToasts.value.push(toast)
}
