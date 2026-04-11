<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div v-for="t in desktop.toasts" :key="t.id" class="toast-item" :class="t.type">
        <span class="toast-icon">{{ iconForType(t.type) }}</span>
        <span class="toast-msg">{{ t.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useDesktopStore } from '../../stores/desktop'

const desktop = useDesktopStore()

function iconForType(type: string) {
  const map: Record<string, string> = { success: '✅', error: '❌', info: 'ℹ️' }
  return map[type] || 'ℹ️'
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 40px;
  right: 20px;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  min-width: 200px;
  background: rgba(40, 40, 40, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 10px;
  font-size: 13px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.toast-enter-active {
  animation: toastIn 200ms ease;
}

.toast-leave-active {
  animation: toastOut 200ms ease;
}

@keyframes toastIn {
  from { opacity: 0; transform: translateX(40px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes toastOut {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(40px); }
}
</style>
