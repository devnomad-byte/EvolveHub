import { ref, h, createApp } from 'vue'

export interface AlertOptions {
  title: string
  message: string
  confirmText?: string
}

const visible = ref(false)
const options = ref<AlertOptions>({ title: '', message: '' })

export const alertVisible = visible
export const alertOptions = options
let _resolve: (() => void) | null = null

export function useAlert() {
  function showAlert(opts: AlertOptions): Promise<void> {
    options.value = { confirmText: '确定', ...opts }
    visible.value = true
    return new Promise<void>((resolve) => {
      _resolve = resolve
    })
  }

  function alert(title: string, message: string): Promise<void> {
    return showAlert({ title, message })
  }

  return { alertVisible: visible, alertOptions: options, showAlert, alert }
}

export function resolveAlert() {
  visible.value = false
  _resolve?.()
  _resolve = null
}
