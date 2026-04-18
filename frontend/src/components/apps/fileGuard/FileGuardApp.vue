<template>
  <div class="file-guard-app">
    <!-- 头部 -->
    <div class="app-header">
      <div class="header-title">
        <FileSearch class="header-icon" />
        <span>File Guard</span>
        <span class="header-sub">敏感文件保护</span>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" @click="loadData" :disabled="loading">
          <RefreshCw :class="{ spinning: loading }" />
        </button>
      </div>
    </div>

    <!-- 配置栏 -->
    <div class="config-bar glass">
      <div class="config-item">
        <span class="config-label">全局开关</span>
        <button
          class="toggle-btn"
          :class="{ active: config.enabled === 1 }"
          @click="toggleConfig"
        >
          <span class="toggle-knob" />
        </button>
        <span class="config-status" :class="{ enabled: config.enabled === 1 }">
          {{ config.enabled === 1 ? '已启用' : '已禁用' }}
        </span>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tab-bar glass">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        <component :is="tab.icon" />
        {{ tab.name }}
      </button>
    </div>

    <!-- 规则列表 -->
    <div v-if="activeTab === 'rules'" class="content">
      <div class="content-header">
        <span class="content-title">敏感路径规则 ({{ rules.length }})</span>
        <button class="btn-primary" @click="showCreateModal = true">
          <Plus />
          新增规则
        </button>
      </div>

      <div class="rules-grid">
        <div v-for="rule in rules" :key="rule.id" class="rule-card glass">
          <div class="rule-header">
            <span class="rule-name">{{ rule.name }}</span>
            <span
              class="severity-badge"
              :class="rule.severity.toLowerCase()"
            >
              {{ rule.severity }}
            </span>
          </div>

          <div class="rule-path">
            <span class="path-type-badge">{{ rule.pathType }}</span>
            <code class="path-value">{{ rule.pathPattern }}</code>
          </div>

          <div v-if="rule.description" class="rule-desc">
            {{ rule.description }}
          </div>

          <div class="rule-footer">
            <span v-if="rule.isBuiltin === 1" class="builtin-tag">内置</span>
            <span v-if="rule.tools" class="tools-tag">
              <Shield />
              {{ formatTools(rule.tools) }}
            </span>
            <div class="rule-actions">
              <button
                class="icon-btn"
                :class="{ 'toggle-off': rule.enabled === 0 }"
                @click="toggleRule(rule)"
                :title="rule.enabled === 1 ? '禁用' : '启用'"
              >
                <Power :class="{ off: rule.enabled === 0 }" />
              </button>
              <button
                class="icon-btn"
                @click="editRule(rule)"
                title="编辑"
              >
                <Edit3 />
              </button>
              <button
                class="icon-btn danger"
                @click="deleteRule(rule)"
                title="删除"
              >
                <Trash2 />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑规则弹窗 -->
    <teleport to="body">
      <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click.self="closeModals">
        <div class="modal">
          <div class="modal-header">
            <span>{{ showEditModal ? '编辑规则' : '新增规则' }}</span>
            <button class="icon-btn" @click="closeModals">
              <X />
            </button>
          </div>

        <div class="modal-body">
          <div class="form-group">
            <label>规则ID <span class="required">*</span></label>
            <input
              v-model="form.ruleId"
              type="text"
              placeholder="如: FG_MY_RULE"
              :disabled="showEditModal"
            />
          </div>

          <div class="form-group">
            <label>规则名称 <span class="required">*</span></label>
            <input v-model="form.name" type="text" placeholder="规则显示名称" />
          </div>

          <div v-if="showCreateModal || form.isBuiltin !== 1" class="form-row">
            <div class="form-group">
              <label>路径模式 <span class="required">*</span></label>
              <input v-model="form.pathPattern" type="text" placeholder="如: ~/.ssh/ 或 *.pem" />
            </div>
            <div class="form-group">
              <label>路径类型 <span class="required">*</span></label>
              <select v-model="form.pathType">
                <option value="DIRECTORY">目录</option>
                <option value="FILE">文件</option>
                <option value="WILDCARD">通配符</option>
              </select>
            </div>
          </div>

          <div v-if="form.isBuiltin === 1" class="form-group">
            <label>路径信息</label>
            <div class="builtin-path-info">
              <span class="path-type-badge">{{ form.pathType }}</span>
              <code>{{ form.pathPattern }}</code>
              <span class="builtin-hint">（内置规则，路径不可修改）</span>
            </div>
          </div>

          <div class="form-group">
            <label>严重级别</label>
            <select v-model="form.severity">
              <option value="CRITICAL">CRITICAL</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
              <option value="INFO">INFO</option>
            </select>
          </div>

          <div class="form-group">
            <label>适用工具</label>
            <input
              v-model="form.tools"
              type="text"
              placeholder='JSON数组，如: ["read_file","write_file"]，留空表示全部'
            />
            <span class="form-hint">留空表示所有工具</span>
          </div>

          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="2" placeholder="规则描述" />
          </div>

          <div class="form-group">
            <label>修复建议</label>
            <textarea v-model="form.remediation" rows="2" placeholder="触发后的修复建议" />
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModals">取消</button>
          <button class="btn-primary" @click="submitForm" :disabled="submitting">
            {{ submitting ? '提交中...' : (showEditModal ? '保存' : '创建') }}
          </button>
        </div>
      </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  FileSearch, RefreshCw, Plus, Power, Edit3, Trash2, X, Shield
} from 'lucide-vue-next'
import { fileGuardApi, type FileGuardRule, type FileGuardConfig } from '@/api/fileGuard'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'
import { useAlert } from '@/composables/useAlert'

const desktop = useDesktopStore()
const { confirm } = useConfirm()
const { alert } = useAlert()

const loading = ref(false)
const submitting = ref(false)
const rules = ref<FileGuardRule[]>([])
const config = ref<FileGuardConfig>({ enabled: 1, updateTime: null })
const activeTab = ref('rules')
const tabs = [
  { id: 'rules', name: '规则列表', icon: FileSearch }
]

const showCreateModal = ref(false)
const showEditModal = ref(false)
const form = ref({
  id: null as number | null,
  ruleId: '',
  name: '',
  pathPattern: '',
  pathType: 'DIRECTORY',
  tools: '',
  description: '',
  remediation: '',
  severity: 'HIGH',
  isBuiltin: 0
})

async function loadData() {
  loading.value = true
  try {
    const [rulesRes, configRes] = await Promise.all([
      fileGuardApi.listRules(),
      fileGuardApi.getConfig()
    ])
    rules.value = rulesRes
    config.value = configRes
  } catch (e: any) {
    desktop.addToast('加载数据失败', 'error')
  } finally {
    loading.value = false
  }
}

async function toggleConfig() {
  const newEnabled = config.value.enabled === 1 ? 0 : 1
  try {
    await fileGuardApi.updateConfig({ enabled: newEnabled })
    config.value.enabled = newEnabled
    desktop.addToast(config.value.enabled === 1 ? '已启用' : '已禁用', 'success')
  } catch (e: any) {
    desktop.addToast('更新失败', 'error')
  }
}

async function toggleRule(rule: FileGuardRule) {
  try {
    await fileGuardApi.toggleRule(rule.id)
    rule.enabled = rule.enabled === 1 ? 0 : 1
    desktop.addToast(rule.enabled === 1 ? '规则已启用' : '规则已禁用', 'success')
  } catch (e: any) {
    desktop.addToast('操作失败', 'error')
  }
}

function editRule(rule: FileGuardRule) {
  form.value = {
    id: rule.id,
    ruleId: rule.ruleId,
    name: rule.name,
    pathPattern: rule.pathPattern,
    pathType: rule.pathType,
    tools: rule.tools || '',
    description: rule.description || '',
    remediation: rule.remediation || '',
    severity: rule.severity,
    isBuiltin: rule.isBuiltin
  }
  showEditModal.value = true
}

async function deleteRule(rule: FileGuardRule) {
  if (!await confirm('删除规则', `确定要删除规则「${rule.name}」吗？`)) return
  try {
    await fileGuardApi.deleteRule(rule.id)
    rules.value = rules.value.filter(r => r.id !== rule.id)
    desktop.addToast('规则已删除', 'success')
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

function closeModals() {
  showCreateModal.value = false
  showEditModal.value = false
  form.value = {
    id: null,
    ruleId: '',
    name: '',
    pathPattern: '',
    pathType: 'DIRECTORY',
    tools: '',
    description: '',
    remediation: '',
    severity: 'HIGH',
    isBuiltin: 0
  }
}

async function submitForm() {
  if (!form.value.ruleId || !form.value.name) {
    await alert('提示', '请填写必填项')
    return
  }
  if (showCreateModal.value && !form.value.pathPattern) {
    await alert('提示', '请填写路径模式')
    return
  }

  submitting.value = true
  try {
    if (showEditModal.value) {
      const updateData: any = {
        id: form.value.id!,
        name: form.value.name,
        tools: form.value.tools || null,
        description: form.value.description || null,
        remediation: form.value.remediation || null,
        severity: form.value.severity
      }
      // 内置规则不允许修改路径
      if (form.value.isBuiltin !== 1) {
        updateData.pathPattern = form.value.pathPattern
        updateData.pathType = form.value.pathType
      }
      await fileGuardApi.updateRule(updateData)
      desktop.addToast('规则已更新', 'success')
    } else {
      await fileGuardApi.createRule({
        ruleId: form.value.ruleId,
        name: form.value.name,
        pathPattern: form.value.pathPattern,
        pathType: form.value.pathType,
        tools: form.value.tools || undefined,
        description: form.value.description || undefined,
        remediation: form.value.remediation || undefined,
        severity: form.value.severity
      })
      desktop.addToast('规则已创建', 'success')
    }
    closeModals()
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    submitting.value = false
  }
}

function formatTools(tools: string): string {
  try {
    const arr = JSON.parse(tools)
    return arr.length > 2 ? `${arr.slice(0, 2).join(', ')}...` : arr.join(', ')
  } catch {
    return tools
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-guard-app {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
  overflow: hidden;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-icon {
  color: #FF453A;
}

.header-sub {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.config-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 16px;
  border-radius: 8px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.config-status {
  font-size: 12px;
  color: var(--text-disabled);
}

.config-status.enabled {
  color: #30D158;
}

.toggle-btn {
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: var(--bg-tertiary);
  border: none;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}

.toggle-btn.active {
  background: #30D158;
}

.toggle-knob {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  transition: left 0.2s;
}

.toggle-btn.active .toggle-knob {
  left: 18px;
}

.tab-bar {
  display: flex;
  gap: 4px;
  padding: 4px 16px;
  border-radius: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
}

.tab-btn:hover {
  background: var(--bg-tertiary);
}

.tab-btn.active {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 16px 16px 16px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 12px 16px;
  background: rgba(28, 28, 30, 0.8);
  backdrop-filter: blur(20px);
}

.content-title {
  font-size: 13px;
  color: var(--text-secondary);
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.rule-card {
  padding: 14px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rule-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rule-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.severity-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
}

.severity-badge.critical {
  background: rgba(255, 69, 58, 0.15);
  color: #FF453A;
}

.severity-badge.high {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
}

.severity-badge.medium {
  background: rgba(255, 214, 10, 0.15);
  color: #FFD60A;
}

.severity-badge.low {
  background: rgba(100, 210, 255, 0.15);
  color: #64D2FF;
}

.severity-badge.info {
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
}

.rule-path {
  display: flex;
  align-items: center;
  gap: 8px;
}

.path-type-badge {
  font-size: 9px;
  font-weight: 600;
  padding: 2px 5px;
  border-radius: 3px;
  background: rgba(10, 132, 255, 0.25);
  color: #0A84FF;
  text-transform: uppercase;
  border: 1px solid rgba(10, 132, 255, 0.3);
}

.path-value {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-tertiary);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'SF Mono', monospace;
}

.rule-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.rule-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}

.builtin-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
}

.tools-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: var(--text-disabled);
}

.rule-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

.icon-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: var(--bg-tertiary);
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.15s;
}

.icon-btn:hover:not(:disabled) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.icon-btn.danger:hover:not(:disabled) {
  background: rgba(255, 69, 58, 0.15);
  color: #FF453A;
}

.icon-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.icon-btn .off {
  color: var(--text-disabled);
}

.toggle-off {
  opacity: 0.6;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #0A84FF;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-primary:hover:not(:disabled) {
  background: #0070E0;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-secondary:hover:not(:disabled) {
  background: var(--bg-secondary);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 弹窗 */
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
  width: 500px;
  max-height: 80vh;
  border-radius: 14px;
  background: rgba(44, 44, 46, 0.98);
  backdrop-filter: blur(40px);
  border: 1px solid var(--border-subtle);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

.form-group label {
  font-size: 12px;
  color: var(--text-secondary);
}

.required {
  color: #FF453A;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: border-color 0.15s;
}

.form-group select option {
  background: #1c1c1e;
  color: var(--text-primary);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #0A84FF;
}

.form-group input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-hint {
  font-size: 11px;
  color: var(--text-disabled);
}

.builtin-path-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-radius: 6px;
}

.builtin-path-info code {
  font-size: 12px;
  color: var(--text-secondary);
  background: transparent;
  padding: 0;
}

.builtin-hint {
  font-size: 11px;
  color: var(--text-disabled);
}
</style>
