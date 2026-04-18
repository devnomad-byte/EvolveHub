<template>
  <teleport to="body">
    <div v-if="alertVisible" class="alert-overlay" @click.self="close">
      <div class="alert-dialog">
        <div class="alert-title">{{ alertOptions.title }}</div>
        <div class="alert-message">{{ alertOptions.message }}</div>
        <div class="alert-actions">
          <button class="alert-btn" @click="close">
            {{ alertOptions.confirmText || '确定' }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { alertVisible, alertOptions, resolveAlert } from '@/composables/useAlert'

function close() {
  resolveAlert()
}
</script>

<style scoped>
.alert-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.alert-dialog {
  width: 360px;
  background: rgba(44, 44, 46, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 14px;
  border: 1px solid var(--border-subtle, rgba(255, 255, 255, 0.1));
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  padding: 24px;
}

.alert-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #fff);
  margin-bottom: 8px;
}

.alert-message {
  font-size: 13px;
  color: var(--text-secondary, #aaa);
  line-height: 1.5;
  margin-bottom: 20px;
}

.alert-actions {
  display: flex;
  justify-content: center;
}

.alert-btn {
  height: 34px;
  padding: 0 24px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: #0A84FF;
  color: #fff;
  transition: all 0.15s;
  box-shadow: 0 2px 8px rgba(10, 132, 255, 0.3);
}

.alert-btn:hover {
  background: #0070E0;
}
</style>
