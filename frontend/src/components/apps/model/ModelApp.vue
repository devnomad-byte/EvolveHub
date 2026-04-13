<template>
  <div class="model-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <Bot :size="20" class="header-icon" />
        <span class="header-title">模型管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ allModels.length }}</span>
          <span class="stat-label">总模型</span>
        </span>
        <span class="stat-badge">
          <span class="stat-num">{{ llmModels.length }}</span>
          <span class="stat-label">LLM</span>
        </span>
        <span class="stat-badge">
          <span class="stat-num">{{ embedModels.length }}</span>
          <span class="stat-label">Embedding</span>
        </span>
      </div>
      <button class="btn-add" @click="openCreateModal">
        <Plus :size="14" />
        添加模型
      </button>
    </div>

    <!-- Main Content -->
    <div class="model-content">
      <!-- LLM section -->
      <div class="model-section">
        <div class="section-header">
          <span class="section-title">对话模型 (LLM)</span>
        </div>
        <div v-if="llmModels.length > 0" class="model-grid">
          <div v-for="m in llmModels" :key="m.id" class="model-card" :class="{ 'is-preferred': m.isPreferred }">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag">LLM</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div v-if="m.isPreferred" class="model-preferred">偏好 ⭐</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无对话模型，点击右上角添加</div>
      </div>

      <!-- Embedding section -->
      <div class="model-section">
        <div class="section-header">
          <span class="section-title">向量模型 (Embedding)</span>
        </div>
        <div v-if="embedModels.length > 0" class="model-grid">
          <div v-for="m in embedModels" :key="m.id" class="model-card">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag embedding">EMBED</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无向量模型，点击右上角添加</div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑模型' : '添加模型' }}</span>
            <button class="modal-close" @click="showModal = false">
              <X :size="16" />
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>模型名称 <span class="required">*</span></label>
                <input v-model="form.name" placeholder="如：gpt-4o" />
              </div>
              <div class="form-field">
                <label>模型类型 <span class="required">*</span></label>
                <select v-model="form.modelType" class="form-select" :disabled="isEditing">
                  <option value="LLM">对话模型 (LLM)</option>
                  <option value="EMBEDDING">向量模型 (Embedding)</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>提供商 <span class="required">*</span></label>
                <input v-model="form.provider" placeholder="如：OpenAI / Anthropic / DeepSeek" />
              </div>
              <div class="form-field">
                <label>状态</label>
                <div class="toggle-wrapper">
                  <div class="toggle" :class="{ on: form.enabled === 1 }" @click="form.enabled = form.enabled === 1 ? 0 : 1">
                    <div class="toggle-dot"></div>
                  </div>
                  <span class="toggle-label">{{ form.enabled === 1 ? '启用' : '禁用' }}</span>
                </div>
              </div>
            </div>
            <div class="form-field">
              <label>API 密钥 <span class="required">*</span></label>
              <input v-model="form.apiKey" type="password" placeholder="sk-..." />
            </div>
            <div class="form-field">
              <label>Base URL</label>
              <input v-model="form.baseUrl" placeholder="https://api.openai.com/v1（可选）" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存修改' : '添加模型' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Bot, Plus, X } from 'lucide-vue-next'
import { adminModelConfigApi, type ModelConfigInfo, type CreateModelConfigRequest, type UpdateModelConfigRequest } from '../../../api/adminModelConfig'
import { useDesktopStore } from '../../../stores/desktop'

const desktop = useDesktopStore()

// ==================== Data ====================
const allModels = ref<ModelConfigInfo[]>([])
const isLoading = ref(true)

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  name: '',
  provider: '',
  apiKey: '',
  baseUrl: '',
  enabled: 1,
  modelType: 'LLM'
})

// ==================== Computed ====================
const llmModels = computed(() => allModels.value.filter(m => m.modelType === 'LLM'))
const embedModels = computed(() => allModels.value.filter(m => m.modelType === 'EMBEDDING'))

// ==================== Load Data ====================
async function loadModels() {
  try {
    const res = await adminModelConfigApi.list(1, 1000)
    allModels.value = res.records
  } catch (e: any) {
    desktop.addToast('加载模型列表失败', 'error')
  }
}

// ==================== Modal Actions ====================
function openCreateModal() {
  isEditing.value = false
  form.value = {
    id: 0,
    name: '',
    provider: '',
    apiKey: '',
    baseUrl: '',
    enabled: 1,
    modelType: 'LLM'
  }
  showModal.value = true
}

function openEditModal(model: ModelConfigInfo) {
  isEditing.value = true
  form.value = {
    id: model.id,
    name: model.name,
    provider: model.provider,
    apiKey: model.apiKey,
    baseUrl: model.baseUrl || '',
    enabled: model.enabled,
    modelType: model.modelType
  }
  showModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return
  if (!form.value.name.trim()) {
    desktop.addToast('请输入模型名称', 'error')
    return
  }
  if (!form.value.provider.trim()) {
    desktop.addToast('请输入提供商', 'error')
    return
  }
  if (!form.value.apiKey.trim()) {
    desktop.addToast('请输入API密钥', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      const req: UpdateModelConfigRequest = {
        id: form.value.id,
        name: form.value.name,
        provider: form.value.provider,
        apiKey: form.value.apiKey || undefined,
        baseUrl: form.value.baseUrl || undefined,
        enabled: form.value.enabled,
        modelType: form.value.modelType
      }
      await adminModelConfigApi.update(req)
      desktop.addToast('模型更新成功', 'success')
    } else {
      const req: CreateModelConfigRequest = {
        name: form.value.name,
        provider: form.value.provider,
        apiKey: form.value.apiKey,
        baseUrl: form.value.baseUrl || undefined,
        enabled: form.value.enabled,
        modelType: form.value.modelType
      }
      await adminModelConfigApi.create(req)
      desktop.addToast('模型添加成功', 'success')
    }
    showModal.value = false
    await loadModels()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(model: ModelConfigInfo) {
  if (!confirm(`确定要删除模型「${model.name}」吗？`)) return
  try {
    await adminModelConfigApi.delete(model.id)
    desktop.addToast('模型已删除', 'success')
    await loadModels()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function toggleEnabled(model: ModelConfigInfo) {
  try {
    await adminModelConfigApi.update({
      id: model.id,
      enabled: model.enabled === 1 ? 0 : 1
    })
    await loadModels()
    desktop.addToast(model.enabled === 1 ? '已禁用' : '已启用', 'success')
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await loadModels()
  isLoading.value = false
})
</script>

<style scoped>
.model-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ========== Header ========== */
.app-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  color: #BF5AF2;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-stats {
  display: flex;
  gap: 8px;
  flex: 1;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 12px;
}

.stat-num {
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  color: var(--text-secondary);
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(191, 90, 242, 0.4);
}

/* ========== Content ========== */
.model-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.model-section {
  margin-bottom: 28px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.model-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border-subtle);
  transition: box-shadow 150ms, border-color 150ms;
}

.model-card:hover {
  border-color: rgba(191, 90, 242, 0.3);
  box-shadow: 0 0 16px rgba(191, 90, 242, 0.1);
}

.model-card.is-preferred {
  border-color: rgba(255, 214, 10, 0.4);
}

.model-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.model-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FF453A;
  flex-shrink: 0;
}

.model-status-dot.on {
  background: #30D158;
}

.model-type-tag {
  font-size: 9px;
  padding: 1px 5px;
  border-radius: 3px;
  font-weight: 600;
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
}

.model-type-tag.embedding {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.model-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-provider {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.model-url {
  font-size: 10px;
  color: var(--text-disabled);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  margin-top: 10px;
  flex-wrap: wrap;
}

.empty-tip {
  color: var(--text-disabled);
  font-size: 13px;
  padding: 20px 0;
}

/* ========== Buttons ========== */
.btn {
  height: 30px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-outline {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.btn-outline.btn-danger {
  color: #FF453A;
  border-color: rgba(255, 69, 58, 0.3);
}

.btn-outline.btn-danger:hover {
  background: rgba(255, 69, 58, 0.1);
}

/* ========== Modal ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.modal {
  width: 520px;
  max-height: 90vh;
  background: rgba(30, 30, 30, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.modal-close:hover {
  background: rgba(255, 69, 58, 0.1);
  color: #FF453A;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-subtle);
}

/* ========== Form ========== */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-field label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

.required {
  color: #FF453A;
}

.form-field input, .form-select {
  height: 38px;
  padding: 0 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s;
  width: 100%;
}

.form-field input:focus, .form-select:focus {
  border-color: #BF5AF2;
  box-shadow: 0 0 0 3px rgba(191, 90, 242, 0.1);
}

.form-select {
  appearance: none;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

/* ========== Toggle ========== */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
}

.toggle {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.toggle.on {
  background: #30D158;
}

.toggle-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: all 0.2s;
}

.toggle.on .toggle-dot {
  left: 23px;
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ========== Modal Buttons ========== */
.btn-primary {
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  color: #fff;
  box-shadow: 0 2px 8px rgba(191, 90, 242, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(191, 90, 242, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
