<template>
  <div class="model-app">
    <div class="model-content">
      <!-- LLM section -->
      <div class="model-section">
        <div class="section-header">
          <span class="section-title">对话模型 (LLM)</span>
          <button class="btn btn-primary btn-sm">+ 添加模型</button>
        </div>
        <div class="model-grid">
          <div v-for="m in llmModels" :key="m.id" class="model-card">
            <div class="model-status-dot" :class="{ on: m.enabled }"></div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.isPreferred" class="model-preferred">偏好 ⭐</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm">测试</button>
              <button class="btn btn-outline btn-sm">编辑</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Embedding section -->
      <div class="model-section">
        <div class="section-header">
          <span class="section-title">向量模型 (Embedding)</span>
        </div>
        <div class="model-grid">
          <div v-for="m in embedModels" :key="m.id" class="model-card">
            <div class="model-status-dot" :class="{ on: m.enabled }"></div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">1024 维</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm">测试</button>
              <button class="btn btn-outline btn-sm">编辑</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Preference -->
      <div class="model-preference">
        <span>我的偏好模型：qwen-max</span>
        <button class="btn btn-outline btn-sm">修改偏好</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ModelConfig } from '../../../types'

const llmModels = ref<ModelConfig[]>([
  { id: '1', name: '通义千问 qwen-max', provider: 'OpenAI兼容', modelType: 'LLM', enabled: true, isPreferred: true },
  { id: '2', name: 'GPT-4o', provider: 'OpenAI原生', modelType: 'LLM', enabled: true },
  { id: '3', name: 'Claude 3.5 Sonnet', provider: 'Anthropic', modelType: 'LLM', enabled: false }
])

const embedModels = ref<ModelConfig[]>([
  { id: '4', name: 'Qwen3-Embedding', provider: 'OpenAI兼容', modelType: 'EMBEDDING', enabled: true },
  { id: '5', name: 'bge-large-zh', provider: 'OpenAI兼容', modelType: 'EMBEDDING', enabled: true }
])
</script>

<style scoped>
.model-app {
  height: 100%;
  overflow-y: auto;
}

.model-content {
  padding: 16px;
}

.model-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.model-card {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border-subtle);
  transition: box-shadow 150ms;
}

.model-card:hover {
  box-shadow: 0 0 16px rgba(10, 132, 255, 0.15);
}

.model-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FF453A;
  margin-bottom: 8px;
}

.model-status-dot.on {
  background: #30D158;
}

.model-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.model-provider {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.model-preferred {
  font-size: 11px;
  color: #FFD60A;
  margin-bottom: 8px;
}

.model-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.model-preference {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid var(--border-subtle);
  font-size: 14px;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
}
</style>
