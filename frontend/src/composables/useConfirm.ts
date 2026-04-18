import { ref } from 'vue'

export interface ConfirmOptions {
  title: string
  message: string
  confirmText?: string
  danger?: boolean
}

const visible = ref(false)
const options = ref<ConfirmOptions>({ title: '', message: '' })

// 导出供 ConfirmDialog 直接使用
export const confirmVisible = visible
export const confirmOptions = options
let _resolve: ((value: boolean) => void) | null = null

export function useConfirm() {
  function showConfirm(opts: ConfirmOptions): Promise<boolean> {
    options.value = { confirmText: '确认', danger: false, ...opts }
    visible.value = true
    return new Promise<boolean>((resolve) => {
      _resolve = resolve
    })
  }

  function confirm(title: string, message: string, opts?: Partial<ConfirmOptions>): Promise<boolean> {
    return showConfirm({ title, message, ...opts })
  }

  return { confirmVisible: visible, confirmOptions: options, showConfirm, confirm }
}

// 全局 resolve 方法，供 ConfirmDialog 组件调用
export function resolveConfirm(value: boolean) {
  visible.value = false
  _resolve?.(value)
  _resolve = null
}
