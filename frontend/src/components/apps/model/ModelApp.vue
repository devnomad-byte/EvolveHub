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
      <div class="header-actions">
        <!-- SUPER_ADMIN 视图切换 -->
        <div v-if="isSuperAdmin" class="view-toggle">
          <button class="toggle-btn" :class="{ active: viewMode === 'type' }" @click="viewMode = 'type'">按类型</button>
          <button class="toggle-btn" :class="{ active: viewMode === 'user' }" @click="switchToUserView">按用户</button>
        </div>
        <button class="btn-add" @click="openCreateModal">
          <Plus :size="14" />
          添加模型
        </button>
        <button v-if="isSuperAdmin && viewMode === 'type'" class="btn-batch-test" @click="handleBatchTest">
          <Activity :size="14" />
          批量测试
        </button>
      </div>
    </div>

    <!-- Tab Bar -->
    <div v-if="viewMode === 'type'" class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        <component :is="tab.icon" :size="14" />
        {{ tab.label }}
        <span class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- 按用户视图 -->
    <div v-if="viewMode === 'user'" class="user-view">
      <!-- 左侧用户列表 -->
      <div class="user-panel">
        <div class="user-panel-header">按用户筛选</div>
        <div class="user-list">
          <!-- 系统级 -->
          <div
            class="user-item"
            :class="{ active: selectedOwnerId === null }"
            @click="selectedOwnerId = null"
          >
            <div class="user-avatar system">系</div>
            <div class="user-info">
              <div class="user-name">系统级模型</div>
              <div class="user-count">{{ systemModels.length }} 个模型</div>
            </div>
          </div>
          <!-- 各用户 -->
          <div
            v-for="user in userGroups"
            :key="user.ownerId"
            class="user-item"
            :class="{ active: selectedOwnerId === user.ownerId }"
            @click="selectedOwnerId = user.ownerId"
          >
            <div class="user-avatar">{{ user.nickname?.charAt(0) || '?' }}</div>
            <div class="user-info">
              <div class="user-name">{{ user.nickname || user.username }}</div>
              <div class="user-count">{{ user.count }} 个模型</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧模型列表 -->
      <div class="model-content">
        <div class="model-section">
          <div class="section-header">
            <span class="section-title">{{ selectedOwnerId === null ? '系统级模型' : (selectedUserNickname || '该用户') + ' 的模型' }}</span>
            <span class="section-desc">{{ selectedOwnerModels.length }} 个模型</span>
          </div>
          <div v-if="selectedOwnerModels.length > 0" class="model-grid">
            <div v-for="m in selectedOwnerModels" :key="m.id" class="model-card">
              <div class="model-card-header">
                <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
                <span class="model-type-tag" :class="m.modelType === 'EMBEDDING' ? 'embedding' : 'llm'">
                  {{ m.modelType === 'LLM' ? 'LLM' : 'EMBED' }}
                </span>
                <span v-if="m.scope === 'SYSTEM'" class="scope-badge system">系统级</span>
                <span v-else-if="m.scope === 'DEPT'" class="scope-badge dept">部门级</span>
                <span v-else class="scope-badge user">个人</span>
              </div>
              <div class="model-name">{{ m.name }}</div>
              <div class="model-provider">{{ m.provider }}</div>
              <div class="model-api-key">Key: {{ m.apiKey }}</div>
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
          <div v-else class="empty-tip">暂无模型</div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div v-if="viewMode === 'type'" class="model-content">
      <!-- Public Models Tab -->
      <div v-if="activeTab === 'public'" class="model-section">
        <div class="section-header">
          <span class="section-title">系统级模型</span>
          <span class="section-desc">所有用户均可使用的公共模型</span>
        </div>
        <div v-if="publicModels.length > 0" class="model-grid">
          <div v-for="m in publicModels" :key="m.id" class="model-card">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag" :class="m.modelType === 'EMBEDDING' ? 'embedding' : 'llm'">
                {{ m.modelType === 'LLM' ? 'LLM' : 'EMBED' }}
              </span>
              <span class="scope-badge system">系统级</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="testConnection(m)" title="测试连接">
                <Wifi :size="12" />
                测试
              </button>
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无系统级模型，点击右上角添加</div>
      </div>

      <!-- Dept Models Tab -->
      <div v-if="activeTab === 'dept'" class="model-section">
        <div class="section-header">
          <span class="section-title">部门模型</span>
          <span class="section-desc">按部门分配的模型配置</span>
        </div>
        <div v-if="deptModels.length > 0" class="model-grid">
          <div v-for="m in deptModels" :key="m.id" class="model-card dept">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag" :class="m.modelType === 'EMBEDDING' ? 'embedding' : 'llm'">
                {{ m.modelType === 'LLM' ? 'LLM' : 'EMBED' }}
              </span>
              <span class="scope-badge dept">部门级</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="testConnection(m)" title="测试连接">
                <Wifi :size="12" />
                测试
              </button>
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无部门级模型</div>
      </div>

      <!-- Personal Models Tab -->
      <div v-if="activeTab === 'personal'" class="model-section">
        <div class="section-header">
          <span class="section-title">个人模型</span>
          <span class="section-desc">个人专属的 LLM 模型配置</span>
        </div>
        <div v-if="personalModels.length > 0" class="model-grid">
          <div v-for="m in personalModels" :key="m.id" class="model-card personal">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag">LLM</span>
              <span class="scope-badge user">个人</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="testConnection(m)" title="测试连接">
                <Wifi :size="12" />
                测试
              </button>
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无个人模型配置</div>
      </div>

      <!-- Embedding Tab -->
      <div v-if="activeTab === 'embedding'" class="model-section">
        <div class="section-header">
          <span class="section-title">向量模型</span>
          <span class="section-desc">用于知识库向量化的 Embedding 模型</span>
        </div>
        <div v-if="embedModels.length > 0" class="model-grid">
          <div v-for="m in embedModels" :key="m.id" class="model-card">
            <div class="model-card-header">
              <div class="model-status-dot" :class="{ on: m.enabled === 1 }"></div>
              <span class="model-type-tag embedding">EMBED</span>
              <span v-if="m.scope === 'SYSTEM'" class="scope-badge system">系统级</span>
              <span v-else class="scope-badge user">个人</span>
            </div>
            <div class="model-name">{{ m.name }}</div>
            <div class="model-provider">{{ m.provider }}</div>
            <div v-if="m.baseUrl" class="model-url">{{ m.baseUrl }}</div>
            <div class="model-actions">
              <button class="btn btn-outline btn-sm" @click="testConnection(m)" title="测试连接">
                <Wifi :size="12" />
                测试
              </button>
              <button class="btn btn-outline btn-sm" @click="toggleEnabled(m)">
                {{ m.enabled === 1 ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal(m)">编辑</button>
              <button class="btn btn-outline btn-sm btn-danger" @click="handleDelete(m)">删除</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无向量模型配置</div>
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
                <select v-model="form.provider" class="form-select" @change="onProviderChange">
                  <option value="">请选择提供商</option>
                  <option v-for="p in providers" :key="p.id" :value="p.name">{{ p.name }}</option>
                </select>
              </div>
              <div class="form-field">
                <label>资源范围</label>
                <select v-model="form.scope" class="form-select" :disabled="isEditing">
                  <option value="SYSTEM">系统级（所有用户可用）</option>
                  <option value="DEPT">部门级（指定部门可用）</option>
                  <option value="USER">用户级（仅自己可用）</option>
                </select>
              </div>
            </div>
            <div v-if="form.scope === 'DEPT'" class="form-field">
              <label>所属部门 <span class="required">*</span></label>
              <select v-model="form.deptId" class="form-select">
                <option :value="null">请选择部门</option>
                <option v-for="d in flatDepts" :key="d.id" :value="d.id">
                  {{ '&nbsp;&nbsp;'.repeat(d._level || 0) }}{{ d.deptName }}
                </option>
              </select>
            </div>
            <div v-if="form.scope === 'USER'" class="form-field">
              <label>授权用户 <span class="required">*</span></label>
              <div class="user-picker">
                <input
                  v-model="userSearchQuery"
                  class="user-search"
                  placeholder="搜索用户名或昵称..."
                />
                <div v-if="selectedUserIds.size > 0" class="selected-users">
                  <span class="selected-count">已选 {{ selectedUserIds.size }} 人</span>
                </div>
                <div class="user-picker-list">
                  <div
                    v-for="u in filteredUsers"
                    :key="u.id"
                    class="user-picker-item"
                    :class="{ selected: selectedUserIds.has(u.id) }"
                    @click="toggleUserSelection(u.id)"
                  >
                    <div class="user-check">{{ selectedUserIds.has(u.id) ? '✓' : '' }}</div>
                    <div class="user-avatar-sm">{{ (u.nickname || u.username || '?').charAt(0) }}</div>
                    <div class="user-detail">
                      <span class="user-nick">{{ u.nickname || u.username }}</span>
                      <span v-if="u.deptName" class="user-dept-sm">{{ u.deptName }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="form-field">
              <label>API 密钥 <span class="required">*</span></label>
              <input v-model="form.apiKey" type="password" placeholder="sk-..." />
            </div>
            <div class="form-field">
              <label>Base URL</label>
              <div class="input-with-hint">
                <input v-model="form.baseUrl" placeholder="选择提供商后将自动填充（可手动修改）" />
                <span v-if="form.baseUrl && isBaseUrlAutoFilled" class="input-hint">已自动填充</span>
              </div>
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
          <div class="modal-footer">
            <button class="btn btn-outline" @click="testConnectionFromForm" :disabled="isTesting">
              <span v-if="isTesting" class="btn-spinner"></span>
              <span v-else><Wifi :size="14" /> 测试连接</span>
            </button>
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存修改' : '添加模型' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Batch Test Dialog -->
    <teleport to="body">
      <div v-if="showBatchTestDialog" class="modal-overlay" @click.self="closeBatchTestDialog">
        <div class="modal batch-test-modal">
          <div class="modal-header">
            <span class="modal-title">批量测试公共模型</span>
            <button class="modal-close" @click="closeBatchTestDialog">
              <X :size="16" />
            </button>
          </div>
          <div class="modal-body">
            <!-- Progress -->
            <div v-if="isBatchTesting" class="batch-test-progress">
              <div class="progress-header">
                <span class="progress-text">测试进度: {{ batchTestResults.length }} / {{ batchTestTotal }}</span>
                <span class="progress-percent">{{ Math.round((batchTestResults.length / batchTestTotal) * 100) }}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (batchTestResults.length / batchTestTotal * 100) + '%' }"></div>
              </div>
            </div>

            <!-- Summary -->
            <div v-if="!isBatchTesting && batchTestResults.length > 0" class="batch-test-summary">
              <div class="summary-item passed">
                <span class="summary-icon">✓</span>
                <span class="summary-count">{{ batchTestPassed }}</span>
                <span class="summary-label">通过</span>
              </div>
              <div class="summary-item failed">
                <span class="summary-icon">✗</span>
                <span class="summary-count">{{ batchTestFailed }}</span>
                <span class="summary-label">失败</span>
              </div>
              <div class="summary-item skipped">
                <span class="summary-icon">⊘</span>
                <span class="summary-count">{{ batchTestSkipped }}</span>
                <span class="summary-label">跳过</span>
              </div>
            </div>

            <!-- Results List -->
            <div class="batch-test-results">
              <div
                v-for="result in batchTestResults"
                :key="result.id"
                class="batch-test-row"
                :class="result.status"
              >
                <div class="batch-test-row-left">
                  <span class="batch-test-status-icon">
                    <template v-if="result.status === 'passed'">✓</template>
                    <template v-else-if="result.status === 'failed'">✗</template>
                    <template v-else>⊘</template>
                  </span>
                  <div class="batch-test-info">
                    <span class="batch-test-name">{{ result.name }}</span>
                    <span class="batch-test-provider">{{ result.provider }}</span>
                  </div>
                </div>
                <div class="batch-test-row-right">
                  <span class="batch-test-status-label">{{ result.status === 'passed' ? '通过' : result.status === 'failed' ? '失败' : '跳过' }}</span>
                  <span class="batch-test-message">{{ result.message }}</span>
                </div>
              </div>
              <div v-if="batchTestResults.length === 0 && !isBatchTesting" class="batch-test-empty">
                点击上方按钮开始测试
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeBatchTestDialog">
              关闭
            </button>
            <button v-if="!isBatchTesting && batchTestResults.length > 0" class="btn btn-outline" @click="copyBatchTestResults">
              复制结果
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { Bot, Plus, X, Wifi, Activity } from 'lucide-vue-next'
import { adminModelConfigApi, type ModelConfigInfo, type ModelConfigWithOwner, type CreateModelConfigRequest, type UpdateModelConfigRequest, type ModelScope, type ModelType } from '../../../api/adminModelConfig'
import { adminModelProviderApi, type ModelProviderInfo } from '../../../api/adminModelProvider'
import { deptApi, type DeptInfo } from '../../../api/dept'
import { adminUserApi, type UserInfo } from '../../../api/adminUser'
import { resourceGrantApi } from '../../../api/resourceGrant'
import { useDesktopStore } from '../../../stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

// ==================== Data ====================
const allModels = ref<ModelConfigInfo[]>([])
const providers = ref<ModelProviderInfo[]>([])
const deptTree = ref<DeptInfo[]>([])
const isLoading = ref(true)
const isBaseUrlAutoFilled = ref(false)

// ==================== User View State ====================
const viewMode = ref<'type' | 'user'>('type') // 'type' = 按类型, 'user' = 按用户
const allModelsWithOwner = ref<ModelConfigWithOwner[]>([])
const selectedOwnerId = ref<number | null>(null)

const isSuperAdmin = computed(() => desktop.isSuperAdmin)

const systemModels = computed(() => allModelsWithOwner.value.filter(m => m.scope === 'SYSTEM'))

const userGroups = computed(() => {
  const map = new Map<number, { ownerId: number; nickname: string; username: string; count: number }>()
  allModelsWithOwner.value.forEach(m => {
    if (m.ownerId != null) {
      if (!map.has(m.ownerId)) {
        map.set(m.ownerId, {
          ownerId: m.ownerId,
          nickname: m.ownerNickname || '',
          username: m.ownerUsername || '',
          count: 0
        })
      }
      map.get(m.ownerId)!.count++
    }
  })
  return Array.from(map.values())
})

const selectedOwnerModels = computed(() => {
  if (selectedOwnerId.value === null) {
    return systemModels.value
  }
  return allModelsWithOwner.value.filter(m => m.ownerId === selectedOwnerId.value)
})

const selectedUserNickname = computed(() => {
  if (selectedOwnerId.value === null) return null
  const group = userGroups.value.find(g => g.ownerId === selectedOwnerId.value)
  return group?.nickname || group?.username || null
})

const flatDepts = computed(() => {
  const result: (DeptInfo & { _level?: number })[] = []
  function flatten(list: DeptInfo[], level: number) {
    for (const d of list) {
      result.push({ ...d, _level: level })
      if (d.children?.length) flatten(d.children, level + 1)
    }
  }
  flatten(deptTree.value, 0)
  return result
})

async function switchToUserView() {
  viewMode.value = 'user'
  selectedOwnerId.value = null
  try {
    const res = await adminModelConfigApi.adminAll(1, 1000)
    allModelsWithOwner.value = res.records
  } catch (e: any) {
    desktop.addToast('加载用户模型失败', 'error')
  }
}

// ==================== Tab State ====================
const activeTab = ref('public')

const publicModels = computed(() => allModels.value.filter(m => m.scope === 'SYSTEM' && m.modelType === 'LLM'))
const deptModels = computed(() => allModels.value.filter(m => m.scope === 'DEPT' && m.modelType === 'LLM'))
const personalModels = computed(() => allModels.value.filter(m => m.scope === 'USER' && m.modelType === 'LLM'))
const embedModels = computed(() => allModels.value.filter(m => m.modelType === 'EMBEDDING'))
const llmModels = computed(() => allModels.value.filter(m => m.modelType === 'LLM'))

const tabs = computed(() => [
  { id: 'public', label: '公共模型', icon: GlobeIcon, count: publicModels.value.length },
  { id: 'dept', label: '部门模型', icon: BuildingIcon, count: deptModels.value.length },
  { id: 'personal', label: '个人模型', icon: UserIcon, count: personalModels.value.length },
  { id: 'embedding', label: '向量模型', icon: DatabaseIcon, count: embedModels.value.length }
])

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const isTesting = ref(false)

interface ModelFormState {
  id: number
  name: string
  provider: string
  apiKey: string
  baseUrl: string
  enabled: number
  modelType: ModelType
  scope: ModelScope
  deptId: number | null
}

const form = ref<ModelFormState>({
  id: 0,
  name: '',
  provider: '',
  apiKey: '',
  baseUrl: '',
  enabled: 1,
  modelType: 'LLM',
  scope: 'SYSTEM',
  deptId: null as number | null
})

// ==================== User Picker State (USER scope) ====================
const allUsers = ref<UserInfo[]>([])
const selectedUserIds = ref<Set<number>>(new Set())
const userSearchQuery = ref('')
const loadingGrants = ref(false)

const filteredUsers = computed(() => {
  const q = userSearchQuery.value.trim().toLowerCase()
  if (!q) return allUsers.value
  return allUsers.value.filter(u =>
    (u.nickname || '').toLowerCase().includes(q) ||
    (u.username || '').toLowerCase().includes(q)
  )
})

async function loadUsers() {
  try {
    allUsers.value = await adminUserApi.list()
  } catch (e: any) {
    console.error('[ModelApp] 加载用户列表失败', e)
  }
}

async function loadExistingGrants(resourceId: number) {
  loadingGrants.value = true
  try {
    const grants = await resourceGrantApi.listByResource('MODEL', resourceId)
    selectedUserIds.value = new Set(grants.map(g => g.userId))
  } catch (e: any) {
    selectedUserIds.value = new Set()
  } finally {
    loadingGrants.value = false
  }
}

function toggleUserSelection(userId: number) {
  if (selectedUserIds.value.has(userId)) {
    selectedUserIds.value.delete(userId)
  } else {
    selectedUserIds.value.add(userId)
  }
}

// ==================== Icon Components ====================
const GlobeIcon = () => h('svg', {
  width: '14', height: '14', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2'
}, [
  h('circle', { cx: '12', cy: '12', r: '10' }),
  h('line', { x1: '2', y1: '12', x2: '22', y2: '12' }),
  h('path', { d: 'M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z' })
])

const UserIcon = () => h('svg', {
  width: '14', height: '14', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2'
}, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const BuildingIcon = () => h('svg', {
  width: '14', height: '14', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2'
}, [
  h('rect', { x: '4', y: '2', width: '16', height: '20', rx: '2', ry: '2' }),
  h('path', { d: 'M9 22v-4h6v4' }),
  h('path', { d: 'M8 6h.01' }),
  h('path', { d: 'M16 6h.01' }),
  h('path', { d: 'M12 6h.01' }),
  h('path', { d: 'M12 10h.01' }),
  h('path', { d: 'M12 14h.01' }),
  h('path', { d: 'M16 10h.01' }),
  h('path', { d: 'M16 14h.01' }),
  h('path', { d: 'M8 10h.01' }),
  h('path', { d: 'M8 14h.01' })
])

const DatabaseIcon = () => h('svg', {
  width: '14', height: '14', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2'
}, [
  h('ellipse', { cx: '12', cy: '5', rx: '9', ry: '3' }),
  h('path', { d: 'M21 12c0 1.66-4 3-9 3s-9-1.34-9-3' }),
  h('path', { d: 'M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5' })
])

// ==================== Load Data ====================
async function loadModels() {
  try {
    isLoading.value = true
    const res = await adminModelConfigApi.list(1, 1000)
    allModels.value = res.records
  } catch (e: any) {
    desktop.addToast('加载模型列表失败', 'error')
  } finally {
    isLoading.value = false
  }
}

async function loadProviders() {
  try {
    const res = await adminModelProviderApi.list()
    providers.value = res
  } catch (e: any) {
    console.error('[ModelApp] 加载提供商列表失败', e)
  }
}

async function loadDepts() {
  try {
    deptTree.value = await deptApi.tree()
  } catch (e: any) {
    console.error('[ModelApp] 加载部门树失败', e)
  }
}

function onProviderChange() {
  if (form.value.provider) {
    const provider = providers.value.find(p => p.name === form.value.provider)
    if (provider?.defaultBaseUrl) {
      form.value.baseUrl = provider.defaultBaseUrl
      isBaseUrlAutoFilled.value = true
    }
  }
}

// ==================== Modal Actions ====================
function openCreateModal() {
  isEditing.value = false
  isBaseUrlAutoFilled.value = false
  selectedUserIds.value = new Set()
  userSearchQuery.value = ''
  form.value = {
    id: 0,
    name: '',
    provider: '',
    apiKey: '',
    baseUrl: '',
    enabled: 1,
    modelType: 'LLM',
    scope: 'SYSTEM',
    deptId: null
  }
  showModal.value = true
}

function openEditModal(model: ModelConfigInfo | ModelConfigWithOwner) {
  isEditing.value = true
  isBaseUrlAutoFilled.value = false
  selectedUserIds.value = new Set()
  userSearchQuery.value = ''
  form.value = {
    id: model.id,
    name: model.name,
    provider: model.provider,
    apiKey: model.apiKey,
    baseUrl: model.baseUrl || '',
    enabled: model.enabled,
    modelType: model.modelType,
    scope: (model.scope as ModelScope) || 'SYSTEM',
    deptId: (model as ModelConfigInfo).deptId || null
  }
  if (model.scope === 'USER' && model.id) {
    loadExistingGrants(model.id)
  }
  showModal.value = true
}

async function testConnection(model?: ModelConfigInfo) {
  const provider = model?.provider || form.value.provider
  const apiKey = model?.apiKey || form.value.apiKey
  const baseUrl = model?.baseUrl || form.value.baseUrl
  const modelType = model?.modelType || form.value.modelType

  if (!provider || !apiKey) {
    desktop.addToast('请填写提供商和 API 密钥', 'error')
    return
  }

  isTesting.value = true
  try {
    const res = await adminModelConfigApi.testConnection({ provider, apiKey, baseUrl, modelType })
    if (res.success) {
      desktop.addToast('连接测试成功！', 'success')
    } else {
      desktop.addToast(res.message || '连接测试失败', 'error')
    }
  } catch (e: any) {
    desktop.addToast(e.message || '连接测试失败', 'error')
  } finally {
    isTesting.value = false
  }
}

async function testConnectionFromForm() {
  await testConnection()
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
  if (form.value.scope === 'DEPT' && !form.value.deptId) {
    desktop.addToast('请选择所属部门', 'error')
    return
  }
  if (form.value.scope === 'USER' && selectedUserIds.value.size === 0) {
    desktop.addToast('请选择至少一个授权用户', 'error')
    return
  }

  isSubmitting.value = true
  try {
    let modelId: number = form.value.id

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
      // Sync grants for USER scope
      if (form.value.scope === 'USER') {
        await syncGrants(modelId)
      }
      desktop.addToast('模型更新成功', 'success')
    } else {
      const req: CreateModelConfigRequest = {
        name: form.value.name,
        provider: form.value.provider,
        apiKey: form.value.apiKey,
        baseUrl: form.value.baseUrl || undefined,
        enabled: form.value.enabled,
        modelType: form.value.modelType,
        scope: form.value.scope,
        deptId: form.value.scope === 'DEPT' ? form.value.deptId : undefined
      }
      const result = await adminModelConfigApi.create(req)
      modelId = result.id
      // Assign grants for USER scope
      if (form.value.scope === 'USER' && modelId) {
        for (const userId of selectedUserIds.value) {
          await resourceGrantApi.assign(userId, 'MODEL', modelId)
        }
      }
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

async function syncGrants(modelId: number) {
  const grants = await resourceGrantApi.listByResource('MODEL', modelId)
  const existingIds = new Set(grants.map(g => g.userId))
  // Add new grants
  for (const userId of selectedUserIds.value) {
    if (!existingIds.has(userId)) {
      await resourceGrantApi.assign(userId, 'MODEL', modelId)
    }
  }
  // Remove old grants
  for (const grant of grants) {
    if (!selectedUserIds.value.has(grant.userId)) {
      await resourceGrantApi.revoke(grant.userId, 'MODEL', modelId)
    }
  }
}

async function handleDelete(model: ModelConfigInfo | ModelConfigWithOwner) {
  if (!await confirm('删除模型', `确定要删除模型「${model.name}」吗？此操作不可恢复。`)) return
  try {
    await adminModelConfigApi.delete(model.id)
    desktop.addToast('模型已删除', 'success')
    await loadModels()
    if (viewMode.value === 'user') {
      await switchToUserView()
    }
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function toggleEnabled(model: ModelConfigInfo | ModelConfigWithOwner) {
  try {
    await adminModelConfigApi.update({
      id: model.id,
      enabled: model.enabled === 1 ? 0 : 1
    })
    await loadModels()
    if (viewMode.value === 'user') {
      await switchToUserView()
    }
    desktop.addToast(model.enabled === 1 ? '已禁用' : '已启用', 'success')
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  }
}

// ==================== Batch Test ====================
const showBatchTestDialog = ref(false)
const isBatchTesting = ref(false)
const batchTestResults = ref<Array<{ id: number; name: string; provider: string; enabled: number; status: string; message: string }>>([])
const batchTestTotal = ref(0)

const batchTestPassed = computed(() => batchTestResults.value.filter(r => r.status === 'passed').length)
const batchTestFailed = computed(() => batchTestResults.value.filter(r => r.status === 'failed').length)
const batchTestSkipped = computed(() => batchTestResults.value.filter(r => r.status === 'skipped').length)

async function handleBatchTest() {
  showBatchTestDialog.value = true
  batchTestResults.value = []
  isBatchTesting.value = true

  try {
    const res = await adminModelConfigApi.batchTest()
    batchTestTotal.value = res.total
    batchTestResults.value = res.results
  } catch (e: any) {
    desktop.addToast(e.message || '批量测试失败', 'error')
  } finally {
    isBatchTesting.value = false
  }
}

function closeBatchTestDialog() {
  showBatchTestDialog.value = false
  isBatchTesting.value = false
  batchTestResults.value = []
  // Refresh model list to reflect enabled/disabled changes
  loadModels()
}

function copyBatchTestResults() {
  const lines = [
    `批量测试结果：`,
    `通过 ${batchTestPassed.value} / 失败 ${batchTestFailed.value} / 跳过 ${batchTestSkipped.value}`,
    '',
    ...batchTestResults.value.map(r => {
      const statusText = r.status === 'passed' ? '✓ 通过' : r.status === 'failed' ? '✗ 失败' : '⊘ 跳过'
      return `${statusText} - ${r.name} (${r.provider}): ${r.message}`
    })
  ]
  navigator.clipboard.writeText(lines.join('\n')).then(() => {
    desktop.addToast('结果已复制到剪贴板', 'success')
  }).catch(() => {
    desktop.addToast('复制失败', 'error')
  })
}

// ==================== Init ====================
onMounted(async () => {
  await Promise.all([loadModels(), loadProviders(), loadDepts(), loadUsers()])
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

/* ========== View Toggle ========== */
.view-toggle {
  display: flex;
  gap: 2px;
  padding: 2px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-right: 8px;
}

.toggle-btn {
  height: 28px;
  padding: 0 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: rgba(191, 90, 242, 0.2);
  color: #BF5AF2;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ========== User View Layout ========== */
.user-view {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.user-panel {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.15);
}

.user-panel-header {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-subtle);
}

.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.15s;
  margin: 0 8px;
  border-radius: 8px;
}

.user-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.user-item.active {
  background: rgba(191, 90, 242, 0.15);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.user-avatar.system {
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  font-size: 11px;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-count {
  font-size: 11px;
  color: var(--text-secondary);
}

/* ========== Tab Bar ========== */
.tab-bar {
  display: flex;
  gap: 4px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.tab-btn.active {
  background: linear-gradient(135deg, rgba(191, 90, 242, 0.2), rgba(155, 89, 182, 0.2));
  color: #BF5AF2;
  border: 1px solid rgba(191, 90, 242, 0.3);
}

.tab-count {
  padding: 1px 6px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 11px;
}

.tab-btn.active .tab-count {
  background: rgba(191, 90, 242, 0.3);
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
  gap: 12px;
  margin-bottom: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-desc {
  font-size: 12px;
  color: var(--text-secondary);
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

.model-card.personal {
  border-color: rgba(48, 209, 88, 0.2);
}

.model-card.personal:hover {
  border-color: rgba(48, 209, 88, 0.4);
  box-shadow: 0 0 16px rgba(48, 209, 88, 0.1);
}

.model-card.dept {
  border-color: rgba(255, 159, 10, 0.2);
}

.model-card.dept:hover {
  border-color: rgba(255, 159, 10, 0.4);
  box-shadow: 0 0 16px rgba(255, 159, 10, 0.1);
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

.scope-badge {
  font-size: 9px;
  padding: 1px 5px;
  border-radius: 3px;
  font-weight: 600;
}

.scope-badge.system {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.scope-badge.user {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.scope-badge.dept {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
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

.model-api-key {
  font-size: 10px;
  color: var(--text-secondary);
  font-family: monospace;
  margin-bottom: 2px;
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

.input-with-hint {
  position: relative;
}

.input-hint {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 10px;
  color: #30D158;
  pointer-events: none;
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

/* ========== User Picker ========== */
.user-picker {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  overflow: hidden;
}

.user-search {
  width: 100%;
  height: 34px;
  padding: 0 10px;
  background: transparent;
  border: none;
  border-bottom: 1px solid var(--border-subtle);
  font-size: 12px;
  color: var(--text-primary);
  outline: none;
}

.user-search::placeholder {
  color: var(--text-disabled);
}

.selected-users {
  padding: 6px 10px;
  border-bottom: 1px solid var(--border-subtle);
}

.selected-count {
  font-size: 11px;
  color: #BF5AF2;
  font-weight: 500;
}

.user-picker-list {
  max-height: 180px;
  overflow-y: auto;
}

.user-picker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  cursor: pointer;
  transition: background 0.15s;
}

.user-picker-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.user-picker-item.selected {
  background: rgba(191, 90, 242, 0.1);
}

.user-check {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: transparent;
  flex-shrink: 0;
  transition: all 0.15s;
}

.user-picker-item.selected .user-check {
  background: #BF5AF2;
  border-color: #BF5AF2;
  color: #fff;
}

.user-avatar-sm {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-nick {
  font-size: 12px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dept-sm {
  font-size: 10px;
  color: var(--text-disabled);
  flex-shrink: 0;
}
</style>

/* ========== Batch Test Button ========== */
.btn-batch-test {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid rgba(48, 209, 88, 0.3);
  border-radius: 6px;
  background: rgba(48, 209, 88, 0.1);
  color: #30D158;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-batch-test:hover {
  background: rgba(48, 209, 88, 0.2);
  border-color: rgba(48, 209, 88, 0.5);
  transform: translateY(-1px);
}

/* ========== Batch Test Modal ========== */
.batch-test-modal {
  width: 650px;
  max-height: 80vh;
}

.batch-test-progress {
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.progress-percent {
  font-size: 12px;
  font-weight: 600;
  color: #30D158;
}

.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #30D158, #34C759);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.batch-test-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.summary-item.passed .summary-icon {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.summary-item.failed .summary-icon {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.summary-item.skipped .summary-icon {
  background: rgba(142, 142, 147, 0.2);
  color: #8E8E93;
}

.summary-count {
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
}

.summary-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.batch-test-results {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.batch-test-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  gap: 12px;
}

.batch-test-row.passed {
  border-color: rgba(48, 209, 88, 0.2);
}

.batch-test-row.failed {
  border-color: rgba(255, 69, 58, 0.2);
}

.batch-test-row.skipped {
  border-color: rgba(142, 142, 147, 0.15);
  opacity: 0.7;
}

.batch-test-row-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.batch-test-status-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.batch-test-row.passed .batch-test-status-icon {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.batch-test-row.failed .batch-test-status-icon {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.batch-test-row.skipped .batch-test-status-icon {
  background: rgba(142, 142, 147, 0.15);
  color: #8E8E93;
}

.batch-test-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.batch-test-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.batch-test-provider {
  font-size: 11px;
  color: var(--text-secondary);
}

.batch-test-row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
  min-width: 0;
  max-width: 280px;
}

.batch-test-status-label {
  font-size: 11px;
  font-weight: 600;
}

.batch-test-row.passed .batch-test-status-label {
  color: #30D158;
}

.batch-test-row.failed .batch-test-status-label {
  color: #FF453A;
}

.batch-test-row.failed .batch-test-message {
  color: #FF6B6B;
}

.batch-test-row.skipped .batch-test-status-label {
  color: #8E8E93;
}

.batch-test-message {
  font-size: 11px;
  color: var(--text-secondary);
  word-break: break-all;
  text-align: right;
}

.batch-test-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--text-disabled);
  font-size: 13px;
}
</style>
>>>>>>> Stashed changes
