<template>
  <div class="tool-guard-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <Shield :size="20" class="header-icon" />
        <span class="header-title">Tool Guard</span>
      </div>
      <div class="header-actions">
        <button class="btn btn-outline btn-sm" @click="loadData">
          <RefreshCw :size="14" :class="{ 'spin': isLoading }" />
          刷新
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="app-content">
      <!-- Left Panel: Rules -->
      <div class="rules-panel">
        <!-- Config Section -->
        <div class="config-section">
          <div class="config-row">
            <span class="config-label">全局开关</span>
            <div class="toggle-wrapper">
              <div class="toggle" :class="{ on: config.enabled === 1 }" @click="toggleGlobalEnabled">
                <div class="toggle-dot"></div>
              </div>
              <span class="toggle-label">{{ config.enabled === 1 ? '启用' : '禁用' }}</span>
            </div>
          </div>
          <div class="config-row">
            <span class="config-label">受保护工具</span>
            <span class="config-value">{{ formatTools(config.guardedTools) }}</span>
          </div>
        </div>

        <!-- Rules Header -->
        <div class="panel-header">
          <span class="panel-title">安全规则</span>
          <button class="btn btn-primary btn-sm" @click="openCreateModal">
            <Plus :size="14" />
            新增规则
          </button>
        </div>

        <!-- Rules List -->
        <div class="rules-list">
          <div
            v-for="rule in rules"
            :key="rule.id"
            class="rule-item"
            :class="{ disabled: rule.enabled === 0 }"
          >
            <div class="rule-header">
              <span class="rule-severity" :class="rule.severity.toLowerCase()">
                {{ rule.severity }}
              </span>
              <span class="rule-name">{{ rule.name }}</span>
              <span v-if="rule.isBuiltin === 1" class="rule-builtin">内置</span>
            </div>
            <div class="rule-id">{{ rule.ruleId }}</div>
            <div class="rule-actions">
              <button
                class="btn btn-icon"
                :class="{ 'text-success': rule.enabled === 1, 'text-muted': rule.enabled === 0 }"
                @click="toggleRule(rule)"
                :title="rule.enabled === 1 ? '禁用' : '启用'"
              >
                <Power :size="14" />
              </button>
              <button class="btn btn-icon" @click="openEditModal(rule)" title="编辑">
                <Edit2 :size="14" />
              </button>
              <button
                v-if="rule.isBuiltin !== 1"
                class="btn btn-icon text-danger"
                @click="deleteRule(rule)"
                title="删除"
              >
                <Trash2 :size="14" />
              </button>
            </div>
          </div>
          <div v-if="rules.length === 0" class="empty-tip">暂无规则</div>
        </div>
      </div>

      <!-- Right Panel: History -->
      <div class="history-panel">
        <div class="panel-header">
          <span class="panel-title">阻断历史</span>
          <div class="history-actions">
            <button class="btn btn-outline btn-sm" @click="loadHistory">
              <RefreshCw :size="14" :class="{ 'spin': isLoadingHistory }" />
            </button>
            <button class="btn btn-outline btn-sm text-danger" @click="clearHistory">
              <Trash2 :size="14" />
              清除
            </button>
          </div>
        </div>

        <!-- Filters -->
        <div class="history-filters">
          <select v-model="historyFilter.severity" class="filter-select" @change="loadHistory">
            <option value="">全部级别</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="HIGH">HIGH</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="LOW">LOW</option>
          </select>
        </div>

        <!-- History List -->
        <div class="history-list">
          <div
            v-for="item in history"
            :key="item.id"
            class="history-item"
            :class="item.action.toLowerCase()"
          >
            <div class="history-header">
              <span class="history-action" :class="item.action.toLowerCase()">
                {{ item.action === 'BLOCKED' ? '阻断' : '警告' }}
              </span>
              <span class="history-severity" :class="item.severity.toLowerCase()">
                {{ item.severity }}
              </span>
              <span class="history-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="history-info">
              <span class="history-user">{{ item.userNickname || '未知用户' }}</span>
              <span class="history-tool">{{ item.toolName }}</span>
            </div>
            <div class="history-rule">规则: {{ item.matchedRuleId }}</div>
            <div class="history-value">{{ item.matchedValue }}</div>
          </div>
          <div v-if="history.length === 0" class="empty-tip">暂无历史记录</div>
        </div>

        <!-- Pagination -->
        <div v-if="historyTotal > 0" class="history-pagination">
          <button class="btn btn-outline btn-sm" :disabled="historyPage <= 1" @click="historyPage--; loadHistory()">
            上一页
          </button>
          <span class="pagination-info">{{ historyPage }} / {{ Math.ceil(historyTotal / historyPageSize) }}</span>
          <button class="btn btn-outline btn-sm" :disabled="historyPage * historyPageSize >= historyTotal" @click="historyPage++; loadHistory()">
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal rule-modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑规则' : '新增规则' }}</span>
            <button class="modal-close" @click="showModal = false">
              <X :size="16" />
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>规则ID <span class="required">*</span></label>
                <input v-model="form.ruleId" placeholder="如：TOOL_CMD_CUSTOM_001" :disabled="isEditing" />
              </div>
              <div class="form-field">
                <label>规则名称 <span class="required">*</span></label>
                <input v-model="form.name" placeholder="如：自定义危险命令" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>工具 <span class="required">*</span></label>
                <input v-model="form.tools" placeholder='JSON数组，如：["execute_shell_command"]' />
              </div>
              <div class="form-field">
                <label>参数 <span class="required">*</span></label>
                <input v-model="form.params" placeholder='JSON数组，如：["command"]' />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>严重级别 <span class="required">*</span></label>
                <select v-model="form.severity" class="form-select">
                  <option value="CRITICAL">CRITICAL</option>
                  <option value="HIGH">HIGH</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="LOW">LOW</option>
                  <option value="INFO">INFO</option>
                </select>
              </div>
              <div class="form-field">
                <label>威胁类别</label>
                <input v-model="form.category" placeholder="如：command_injection" />
              </div>
            </div>
            <div class="form-field">
              <label>正则表达式 <span class="required">*</span></label>
              <textarea v-model="form.patterns" placeholder='JSON数组，如：["\\\\brm\\\\b","\\\\bdel\\\\b"]' rows="3" class="form-textarea"></textarea>
            </div>
            <div class="form-field">
              <label>排除正则（可选）</label>
              <input v-model="form.excludePatterns" placeholder='JSON数组，如：["^\\\\s*#"]' />
            </div>
            <div class="form-field">
              <label>描述</label>
              <input v-model="form.description" placeholder="规则的详细描述" />
            </div>
            <div class="form-field">
              <label>处理建议</label>
              <input v-model="form.remediation" placeholder="触发规则后的处理建议" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存' : '创建' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Shield, Plus, X, Edit2, Trash2, Power, RefreshCw } from 'lucide-vue-next'
import { toolGuardApi, type ToolGuardRule, type ToolGuardConfig, type ToolGuardHistory } from '@/api/toolGuard'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

// ==================== State ====================
const isLoading = ref(false)
const isLoadingHistory = ref(false)
const rules = ref<ToolGuardRule[]>([])
const history = ref<ToolGuardHistory[]>([])
const config = reactive<ToolGuardConfig>({
  enabled: 1,
  guardedTools: '[]',
  deniedTools: '[]',
  updateTime: null
})

// History filter & pagination
const historyFilter = reactive({
  severity: ''
})
const historyPage = ref(1)
const historyPageSize = ref(20)
const historyTotal = ref(0)

// Modal state
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = reactive({
  id: 0,
  ruleId: '',
  name: '',
  tools: '["execute_shell_command"]',
  params: '["command"]',
  severity: 'HIGH',
  patterns: '',
  excludePatterns: '',
  category: 'command_injection',
  description: '',
  remediation: ''
})

// ==================== Load Data ====================
async function loadData() {
  isLoading.value = true
  try {
    const [rulesRes, configRes] = await Promise.all([
      toolGuardApi.listRules(),
      toolGuardApi.getConfig()
    ])
    rules.value = rulesRes
    Object.assign(config, configRes)
  } catch (e: any) {
    desktop.addToast('加载数据失败', 'error')
  } finally {
    isLoading.value = false
  }
}

async function loadHistory() {
  isLoadingHistory.value = true
  try {
    const res = await toolGuardApi.listHistory({
      pageNum: historyPage.value,
      pageSize: historyPageSize.value,
      severity: historyFilter.severity || undefined
    })
    history.value = res.records
    historyTotal.value = res.total
  } catch (e: any) {
    desktop.addToast('加载历史失败', 'error')
  } finally {
    isLoadingHistory.value = false
  }
}

// ==================== Actions ====================
async function toggleGlobalEnabled() {
  try {
    await toolGuardApi.updateConfig({
      enabled: config.enabled === 1 ? 0 : 1,
      guardedTools: config.guardedTools,
      deniedTools: config.deniedTools
    })
    config.enabled = config.enabled === 1 ? 0 : 1
    desktop.addToast(config.enabled === 1 ? '已启用' : '已禁用', 'success')
  } catch (e: any) {
    desktop.addToast('更新失败', 'error')
  }
}

async function toggleRule(rule: ToolGuardRule) {
  try {
    await toolGuardApi.toggleRule(rule.id)
    rule.enabled = rule.enabled === 1 ? 0 : 1
    desktop.addToast(rule.enabled === 1 ? '已启用' : '已禁用', 'success')
  } catch (e: any) {
    desktop.addToast('操作失败', 'error')
  }
}

function openCreateModal() {
  isEditing.value = false
  Object.assign(form, {
    id: 0,
    ruleId: '',
    name: '',
    tools: '["execute_shell_command"]',
    params: '["command"]',
    severity: 'HIGH',
    patterns: '[""]',
    excludePatterns: '',
    category: 'command_injection',
    description: '',
    remediation: ''
  })
  showModal.value = true
}

function openEditModal(rule: ToolGuardRule) {
  isEditing.value = true
  Object.assign(form, {
    id: rule.id,
    ruleId: rule.ruleId,
    name: rule.name,
    tools: rule.tools,
    params: rule.params,
    severity: rule.severity,
    patterns: rule.patterns,
    excludePatterns: rule.excludePatterns || '',
    category: rule.category,
    description: rule.description || '',
    remediation: rule.remediation || ''
  })
  showModal.value = true
}

async function handleSubmit() {
  if (!form.ruleId.trim() || !form.name.trim() || !form.tools.trim() || !form.params.trim() || !form.patterns.trim()) {
    desktop.addToast('请填写必填字段', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      await toolGuardApi.updateRule({
        id: form.id,
        name: form.name,
        tools: form.tools,
        params: form.params,
        severity: form.severity,
        patterns: form.patterns,
        excludePatterns: form.excludePatterns || undefined,
        category: form.category || undefined,
        description: form.description || undefined,
        remediation: form.remediation || undefined
      })
      desktop.addToast('规则已更新', 'success')
    } else {
      await toolGuardApi.createRule({
        ruleId: form.ruleId,
        name: form.name,
        tools: form.tools,
        params: form.params,
        severity: form.severity,
        patterns: form.patterns,
        excludePatterns: form.excludePatterns || undefined,
        category: form.category || undefined,
        description: form.description || undefined,
        remediation: form.remediation || undefined
      })
      desktop.addToast('规则已创建', 'success')
    }
    showModal.value = false
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function deleteRule(rule: ToolGuardRule) {
  if (!await confirm('删除规则', `确定要删除规则「${rule.name}」吗？`)) return
  try {
    await toolGuardApi.deleteRule(rule.id)
    desktop.addToast('规则已删除', 'success')
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function clearHistory() {
  if (!await confirm('清除历史', '确定要清除所有历史记录吗？此操作不可恢复。')) return
  try {
    await toolGuardApi.clearHistory()
    desktop.addToast('历史已清除', 'success')
    await loadHistory()
  } catch (e: any) {
    desktop.addToast(e.message || '清除失败', 'error')
  }
}

// ==================== Helpers ====================
function formatTools(tools: string): string {
  try {
    const arr = JSON.parse(tools)
    return arr.length > 0 ? arr.join(', ') : '全部工具'
  } catch {
    return tools || '全部工具'
  }
}

function formatTime(time: string): string {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// ==================== Init ====================
onMounted(async () => {
  await Promise.all([loadData(), loadHistory()])
})
</script>

<style scoped>
.tool-guard-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ========== Header ========== */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  color: #FF453A;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ========== Content ========== */
.app-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ========== Rules Panel ========== */
.rules-panel {
  width: 45%;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-section {
  padding: 16px;
  background: rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid var(--border-subtle);
}

.config-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.config-row + .config-row {
  border-top: 1px solid var(--border-subtle);
}

.config-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.config-value {
  font-size: 12px;
  color: var(--text-primary);
  font-family: monospace;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle {
  width: 40px;
  height: 22px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.toggle.on {
  background: #30D158;
}

.toggle-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: all 0.2s;
}

.toggle.on .toggle-dot {
  left: 21px;
}

.toggle-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.rules-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.rule-item {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  transition: all 0.15s;
}

.rule-item:hover {
  border-color: rgba(191, 90, 242, 0.3);
}

.rule-item.disabled {
  opacity: 0.5;
}

.rule-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rule-severity {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;
}

.rule-severity.critical {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.rule-severity.high {
  background: rgba(255, 159, 10, 0.2);
  color: #FF9F0A;
}

.rule-severity.medium {
  background: rgba(255, 214, 10, 0.2);
  color: #FFD60A;
}

.rule-severity.low {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.rule-severity.info {
  background: rgba(142, 142, 147, 0.2);
  color: #8E8E93;
}

.rule-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.rule-builtin {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(142, 142, 147, 0.2);
  color: #8E8E93;
}

.rule-id {
  font-size: 11px;
  color: var(--text-disabled);
  font-family: monospace;
  margin-bottom: 8px;
}

.rule-actions {
  display: flex;
  gap: 4px;
}

/* ========== History Panel ========== */
.history-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-actions {
  display: flex;
  gap: 4px;
}

.history-filters {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  gap: 8px;
}

.filter-select {
  height: 30px;
  padding: 0 10px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-primary);
  cursor: pointer;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}

.history-item.blocked {
  border-left: 3px solid #FF453A;
}

.history-item.warned {
  border-left: 3px solid #FF9F0A;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.history-action {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
}

.history-action.blocked {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.history-action.warned {
  background: rgba(255, 159, 10, 0.2);
  color: #FF9F0A;
}

.history-severity {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;
}

.history-severity.critical {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.history-severity.high {
  background: rgba(255, 159, 10, 0.2);
  color: #FF9F0A;
}

.history-time {
  font-size: 11px;
  color: var(--text-disabled);
  margin-left: auto;
}

.history-info {
  display: flex;
  gap: 12px;
  margin-bottom: 4px;
}

.history-user {
  font-size: 12px;
  color: var(--text-primary);
  font-weight: 500;
}

.history-tool {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: monospace;
}

.history-rule {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.history-value {
  font-size: 11px;
  color: var(--text-disabled);
  font-family: monospace;
  word-break: break-all;
  background: rgba(0, 0, 0, 0.2);
  padding: 6px 8px;
  border-radius: 4px;
  max-height: 60px;
  overflow-y: auto;
}

.history-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px;
  border-top: 1px solid var(--border-subtle);
}

.pagination-info {
  font-size: 12px;
  color: var(--text-secondary);
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
  border: none;
}

.btn-sm {
  height: 28px;
  padding: 0 8px;
  font-size: 11px;
}

.btn-primary {
  background: linear-gradient(135deg, #FF453A, #BF5AF2);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 69, 58, 0.4);
}

.btn-outline {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-icon {
  width: 28px;
  height: 28px;
  padding: 0;
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
}

.btn-icon:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.btn.text-success {
  color: #30D158;
}

.btn.text-muted {
  color: var(--text-disabled);
}

.btn.text-danger {
  color: #FF453A;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 1s linear infinite;
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
  background: rgba(30, 30, 30, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
}

.rule-modal {
  width: 600px;
  max-height: 90vh;
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

.form-field input,
.form-select,
.form-textarea {
  height: 38px;
  padding: 0 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s;
}

.form-textarea {
  height: auto;
  padding: 10px 12px;
  resize: vertical;
  font-family: monospace;
}

.form-field input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: #BF5AF2;
  box-shadow: 0 0 0 3px rgba(191, 90, 242, 0.1);
}

.form-field input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-select {
  appearance: none;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

/* ========== Empty ========== */
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--text-disabled);
  font-size: 13px;
}
</style>
