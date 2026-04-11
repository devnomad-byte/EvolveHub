<template>
  <div class="notification-panel">
    <div class="panel-header">
      <span class="panel-title">通知中心</span>
      <span class="panel-close" @click="desktop.toggleNotifications">✕</span>
    </div>
    <div class="panel-list">
      <div v-for="n in desktop.notifications" :key="n.id" class="notification-item">
        <span class="notif-icon">{{ iconForType(n.type) }}</span>
        <div class="notif-content">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-message">{{ n.message }}</div>
          <div class="notif-time">{{ n.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDesktopStore } from '../../stores/desktop'

const desktop = useDesktopStore()

function iconForType(type: string) {
  const map: Record<string, string> = { success: '🟢', info: '📄', warning: '⚠️', error: '🔴' }
  return map[type] || '📄'
}
</script>

<style scoped>
.notification-panel {
  position: fixed;
  top: var(--menubar-height);
  right: 0;
  width: 320px;
  height: calc(100% - var(--menubar-height));
  background: rgba(30, 30, 30, 0.92);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border-left: 1px solid var(--border-subtle);
  z-index: 9500;
  animation: slideIn 250ms ease-out;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
}

.panel-close {
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
}

.panel-list {
  padding: 4px 0;
}

.notification-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.notif-icon { font-size: 14px; margin-top: 2px; }

.notif-title {
  font-size: 13px;
  font-weight: 500;
}

.notif-message {
  font-size: 12px;
  color: var(--text-secondary);
}

.notif-time {
  font-size: 11px;
  color: var(--text-disabled);
  margin-top: 2px;
}
</style>
