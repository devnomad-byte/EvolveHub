<template>
  <div class="security-scanner-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <Scan :size="20" class="header-icon" />
        <span class="header-title">Security Scanner</span>
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
          <span class="config-label">扫描模式</span>
          <div class="mode-buttons">
            <button
              v-for="m in modes"
              :key="m.value"
              class="mode-btn"
              :class="{ active: config.mode === m.value }"
              @click="setMode(m.value)"
            >
              {{ m.label }}
            </button>
          </div>
        </div>
        <div class="config-row">
          <span class="config-label">扫描超时</span>
          <div class="timeout-control">
            <input
              type="number"
              v-model.number="config.timeout"
              min="1"
              max="300"
              class="timeout-input"
              @change="updateTimeout"
            />
            <span class="timeout-unit">秒</span>
          </div>
        </div>
      </div>

      <!-- Main Panels -->
      <div class="main-panels">
        <!-- Left Panel: Whitelist -->
        <div class="whitelist-panel">
          <div class="panel-header">
            <span class="panel-title">白名单</span>
            <button class="btn btn-primary btn-sm" @click="openAddWhitelistModal">
              <Plus :size="14" />
              添加
            </button>
          </div>

          <div class="whitelist-list">
            <div v-for="item in whitelist" :key="item.id" class="whitelist-item">
              <div class="whitelist-info">
                <span class="whitelist-name">{{ item.skillName }}</span>
                <span class="whitelist-hash">{{ formatHash(item.contentHash) }}</span>
              </div>
              <button class="btn btn-icon text-danger" @click="deleteWhitelist(item)" title="删除">
                <Trash2 :size="14" />
              </button>
            </div>
            <div v-if="whitelist.length === 0" class="empty-tip">暂无白名单记录</div>
          </div>
        </div>

        <!-- Right Panel: Blocked History -->
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
            <select v-model="historyFilter.action" class="filter-select" @change="loadHistory">
              <option value="">全部类型</option>
              <option value="BLOCKED">阻断</option>
              <option value="WARNED">警告</option>
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
                <span class="history-severity" :class="item.maxSeverity?.toLowerCase()">
                  {{ item.maxSeverity }}
                </span>
                <span class="history-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="history-info">
                <span class="history-skill">{{ item.skillName }}</span>
                <span class="history-user">{{ item.userNickname || '未知用户' }}</span>
              </div>
              <div v-if="parseFindings(item.findings).length > 0" class="history-findings">
                <div v-for="(finding, idx) in parseFindings(item.findings).slice(0, 3)" :key="idx" class="finding-item">
                  <span class="finding-severity" :class="finding.severity?.toLowerCase()">{{ finding.severity }}</span>
                  <span class="finding-title">{{ finding.title }}</span>
                </div>
                <div v-if="parseFindings(item.findings).length > 3" class="finding-more">
                  还有 {{ parseFindings(item.findings).length - 3 }} 项问题...
                </div>
              </div>
              <div class="history-hash">Hash: {{ formatHash(item.contentHash) }}</div>
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
    </div>

    <!-- Add Whitelist Modal -->
    <teleport to="body">
      <div v-if="showWhitelistModal" class="modal-overlay" @click.self="showWhitelistModal = false">
        <div class="modal whitelist-modal">
          <div class="modal-header">
            <span class="modal-title">添加到白名单</span>
            <button class="modal-close" @click="showWhitelistModal = false">
              <X :size="16" />
            </button>
          </div>
          <div class="modal-body">
            <div v-if="whitelistFormError" class="form-error">
              {{ whitelistFormError }}
            </div>
            <div class="form-field">
              <label>技能名称 <span class="required">*</span></label>
              <input v-model="whitelistForm.skillName" placeholder="输入技能名称" />
            </div>
            <div class="form-field">
              <label>内容哈希 (SHA-256) <span class="required">*</span></label>
              <input v-model="whitelistForm.contentHash" placeholder="输入文件内容的 SHA-256 哈希值" class="hash-input" />
            </div>
            <div class="form-hint">
              白名单基于 skill_name + content_hash 生效。文件变更后 hash 变化，白名单自动失效。
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showWhitelistModal = false">取消</button>
            <button class="btn btn-primary" @click="handleAddWhitelist" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>添加</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Scan, Plus, X, Trash2, RefreshCw } from 'lucide-vue-next'
import { securityScannerApi, type SecurityScannerConfig, type SecurityScannerWhitelist, type SecurityScannerBlockedHistory, type SecurityScannerFinding } from '@/api/securityScanner'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

// ==================== State ====================
const isLoading = ref(false)
const isLoadingHistory = ref(false)
const config = reactive<SecurityScannerConfig>({
  id: 1,
  enabled: 1,
  mode: 'block',
  timeout: 30,
  updateTime: null
})
const whitelist = ref<SecurityScannerWhitelist[]>([])
const history = ref<SecurityScannerBlockedHistory[]>([])

const modes = [
  { value: 'block', label: '阻断' },
  { value: 'warn', label: '警告' },
  { value: 'off', label: '关闭' }
]

// History filter & pagination
const historyFilter = reactive({
  action: ''
})
const historyPage = ref(1)
const historyPageSize = ref(20)
const historyTotal = ref(0)

// Whitelist modal
const showWhitelistModal = ref(false)
const isSubmitting = ref(false)
const whitelistFormError = ref('')
const whitelistForm = reactive({
  skillName: '',
  contentHash: ''
})

// ==================== Load Data ====================
async function loadData() {
  isLoading.value = true
  try {
    const [configRes, whitelistRes] = await Promise.all([
      securityScannerApi.getConfig(),
      securityScannerApi.listWhitelist()
    ])
    Object.assign(config, configRes)
    whitelist.value = whitelistRes
  } catch (e: any) {
    desktop.addToast('加载数据失败', 'error')
  } finally {
    isLoading.value = false
  }
}

async function loadHistory() {
  isLoadingHistory.value = true
  try {
    const res = await securityScannerApi.listHistory({
      pageNum: historyPage.value,
      pageSize: historyPageSize.value,
      action: historyFilter.action || undefined
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
    await securityScannerApi.updateConfig({
      enabled: config.enabled === 1 ? 0 : 1,
      mode: config.mode,
      timeout: config.timeout
    })
    config.enabled = config.enabled === 1 ? 0 : 1
    desktop.addToast(config.enabled === 1 ? '已启用' : '已禁用', 'success')
  } catch (e: any) {
    desktop.addToast('更新失败', 'error')
  }
}

async function setMode(mode: string) {
  try {
    await securityScannerApi.updateConfig({
      enabled: config.enabled,
      mode: mode,
      timeout: config.timeout
    })
    config.mode = mode as 'block' | 'warn' | 'off'
    desktop.addToast('扫描模式已更新', 'success')
  } catch (e: any) {
    desktop.addToast('更新失败', 'error')
  }
}

async function updateTimeout() {
  if (config.timeout < 1) config.timeout = 1
  if (config.timeout > 300) config.timeout = 300
  try {
    await securityScannerApi.updateConfig({
      enabled: config.enabled,
      mode: config.mode,
      timeout: config.timeout
    })
    desktop.addToast('超时时间已更新', 'success')
  } catch (e: any) {
    desktop.addToast('更新失败', 'error')
  }
}

function openAddWhitelistModal() {
  whitelistFormError.value = ''
  whitelistForm.skillName = ''
  whitelistForm.contentHash = ''
  showWhitelistModal.value = true
}

async function handleAddWhitelist() {
  whitelistFormError.value = ''
  if (!whitelistForm.skillName.trim()) {
    whitelistFormError.value = '请输入技能名称'
    return
  }
  if (!whitelistForm.contentHash.trim()) {
    whitelistFormError.value = '请输入内容哈希'
    return
  }
  if (!/^[a-fA-F0-9]{64}$/.test(whitelistForm.contentHash.trim())) {
    whitelistFormError.value = '哈希值格式不正确，应为 64 位十六进制字符'
    return
  }

  isSubmitting.value = true
  try {
    await securityScannerApi.addWhitelist({
      skillName: whitelistForm.skillName.trim(),
      contentHash: whitelistForm.contentHash.trim()
    })
    desktop.addToast('已添加到白名单', 'success')
    showWhitelistModal.value = false
    await loadData()
  } catch (e: any) {
    whitelistFormError.value = e.message || '添加失败'
  } finally {
    isSubmitting.value = false
  }
}

async function deleteWhitelist(item: SecurityScannerWhitelist) {
  if (!await confirm('删除白名单', `确定要将「${item.skillName}」从白名单中移除吗？`)) return
  try {
    await securityScannerApi.deleteWhitelist(item.id)
    desktop.addToast('已从白名单中移除', 'success')
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function clearHistory() {
  if (!await confirm('清除历史', '确定要清除所有历史记录吗？此操作不可恢复。')) return
  try {
    await securityScannerApi.clearHistory()
    desktop.addToast('历史已清除', 'success')
    await loadHistory()
  } catch (e: any) {
    desktop.addToast(e.message || '清除失败', 'error')
  }
}

// ==================== Helpers ====================
function formatHash(hash: string): string {
  if (!hash) return ''
  return hash.substring(0, 16) + '...' + hash.substring(hash.length - 8)
}

function formatTime(time: string): string {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

function parseFindings(findings: string): SecurityScannerFinding[] {
  if (!findings) return []
  try {
    return JSON.parse(findings)
  } catch {
    return []
  }
}

// ==================== Init ====================
onMounted(async () => {
  await Promise.all([loadData(), loadHistory()])
})
</script>

<style scoped>
.security-scanner-app {
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
  color: #BF5AF2;
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
  flex-direction: column;
  overflow: hidden;
}

/* ========== Config Section ========== */
.config-section {
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
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

.mode-buttons {
  display: flex;
  gap: 4px;
}

.mode-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.mode-btn:hover {
  border-color: rgba(191, 90, 242, 0.3);
}

.mode-btn.active {
  background: rgba(191, 90, 242, 0.2);
  border-color: #BF5AF2;
  color: #BF5AF2;
}

.timeout-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeout-input {
  width: 60px;
  height: 28px;
  padding: 0 8px;
  border-radius: 6px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font-size: 13px;
  text-align: center;
}

.timeout-input:focus {
  outline: none;
  border-color: #BF5AF2;
}

.timeout-unit {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== Main Panels ========== */
.main-panels {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ========== Whitelist Panel ========== */
.whitelist-panel {
  width: 35%;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.whitelist-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.whitelist-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  transition: all 0.15s;
}

.whitelist-item:hover {
  border-color: rgba(191, 90, 242, 0.3);
}

.whitelist-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.whitelist-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.whitelist-hash {
  font-size: 11px;
  color: var(--text-secondary);
  font-family: monospace;
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
  gap: 8px;
}

.history-filters {
  padding: 8px 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.filter-select {
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font-size: 12px;
}

.filter-select:focus {
  outline: none;
  border-color: #BF5AF2;
}

.filter-select option {
  background: #1c1c1e;
  color: var(--text-primary);
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.history-item {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  transition: all 0.15s;
}

.history-item:hover {
  border-color: rgba(191, 90, 242, 0.3);
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
  margin-bottom: 8px;
}

.history-action {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;
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

.history-severity.medium {
  background: rgba(255, 214, 10, 0.2);
  color: #FFD60A;
}

.history-severity.low {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.history-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-left: auto;
}

.history-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.history-skill {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.history-user {
  font-size: 12px;
  color: var(--text-secondary);
}

.history-findings {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 8px;
}

.finding-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.finding-severity {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 3px;
  text-transform: uppercase;
}

.finding-severity.critical { background: rgba(255, 69, 58, 0.3); color: #FF453A; }
.finding-severity.high { background: rgba(255, 159, 10, 0.3); color: #FF9F0A; }
.finding-severity.medium { background: rgba(255, 214, 10, 0.3); color: #FFD60A; }
.finding-severity.low { background: rgba(48, 209, 88, 0.3); color: #30D158; }

.finding-title {
  font-size: 12px;
  color: var(--text-primary);
}

.finding-more {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.history-hash {
  font-size: 10px;
  color: var(--text-disabled);
  font-family: monospace;
}

.history-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-subtle);
}

.pagination-info {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== Empty ========== */
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--text-disabled);
  font-size: 13px;
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
  width: 420px;
  background: rgba(40, 40, 44, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 14px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.form-field {
  margin-bottom: 16px;
}

.form-field:last-child {
  margin-bottom: 0;
}

.form-field label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-field input,
.form-field textarea {
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font-size: 13px;
  transition: all 0.15s;
}

.form-field input:focus,
.form-field textarea:focus {
  outline: none;
  border-color: #BF5AF2;
}

.form-field input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hash-input {
  font-family: monospace !important;
  font-size: 11px !important;
}

.form-error {
  background: rgba(255, 69, 58, 0.15);
  border: 1px solid rgba(255, 69, 58, 0.3);
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 12px;
  color: #FF453A;
  margin-bottom: 16px;
}

.form-hint {
  font-size: 11px;
  color: var(--text-disabled);
  line-height: 1.5;
  margin-top: 8px;
}

.required {
  color: #FF453A;
}

/* ========== Buttons ========== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 30px;
  padding: 0 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-sm {
  height: 26px;
  padding: 0 10px;
  font-size: 12px;
}

.btn-primary {
  background: #BF5AF2;
  color: white;
}

.btn-primary:hover {
  background: #A855C7;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.12);
}

.btn-outline {
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
}

.btn-icon {
  width: 28px;
  height: 28px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.btn-icon:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.text-danger {
  color: #FF453A !important;
}

.text-success {
  color: #30D158 !important;
}

.text-muted {
  color: var(--text-disabled) !important;
}

/* ========== Scrollbar ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.25);
}

.spin {
  animation: spin 1s linear infinite;
}
</style>
