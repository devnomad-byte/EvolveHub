<template>
  <teleport to="body">
    <div v-if="confirmVisible" class="confirm-overlay" @click.self="cancel">
      <div class="confirm-dialog">
        <div class="confirm-title">{{ confirmOptions.title }}</div>
        <div class="confirm-message">{{ confirmOptions.message }}</div>
        <div class="confirm-actions">
          <button class="confirm-btn confirm-btn-cancel" @click="cancel">取消</button>
          <button
            class="confirm-btn"
            :class="confirmOptions.danger !== false ? 'confirm-btn-danger' : 'confirm-btn-primary'"
            @click="ok"
          >
            {{ confirmOptions.confirmText || '确认' }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { confirmVisible, confirmOptions, resolveConfirm } from '@/composables/useConfirm'

function ok() { resolveConfirm(true) }
function cancel() { resolveConfirm(false) }
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.confirm-dialog {
  width: 360px;
  background: rgba(44, 44, 46, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 14px;
  border: 1px solid var(--border-subtle, rgba(255, 255, 255, 0.1));
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  padding: 24px;
}

.confirm-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #fff);
  margin-bottom: 8px;
}

.confirm-message {
  font-size: 13px;
  color: var(--text-secondary, #aaa);
  line-height: 1.5;
  margin-bottom: 20px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.confirm-btn {
  height: 34px;
  padding: 0 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}

.confirm-btn-cancel {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle, rgba(255, 255, 255, 0.1));
  color: var(--text-primary, #fff);
}

.confirm-btn-cancel:hover {
  background: rgba(255, 255, 255, 0.1);
}

.confirm-btn-danger {
  background: #FF453A;
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 69, 58, 0.3);
}

.confirm-btn-danger:hover {
  background: #E03E34;
}

.confirm-btn-primary {
  background: #0A84FF;
  color: #fff;
  box-shadow: 0 2px 8px rgba(10, 132, 255, 0.3);
}

.confirm-btn-primary:hover {
  background: #0070E0;
}
</style>
