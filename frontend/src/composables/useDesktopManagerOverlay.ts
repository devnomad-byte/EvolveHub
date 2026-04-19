import { ref } from 'vue'

const overlayVisible = ref(false)

export function useDesktopManagerOverlay() {
  function open() {
    overlayVisible.value = true
  }

  function close() {
    overlayVisible.value = false
  }

  function toggle() {
    overlayVisible.value = !overlayVisible.value
  }

  return {
    overlayVisible,
    open,
    close,
    toggle
  }
}
