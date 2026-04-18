<template>
  <div class="tu-app">
    <!-- Header -->
    <div class="tu-header">
      <div class="tu-header-left">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
        </svg>
        <span class="tu-title">用量统计</span>
      </div>
      <div class="tu-header-right">
        <!-- Date Range -->
        <div class="tu-date-range">
          <input v-model="startDate" type="date" class="tu-date-input" title="开始日期" />
          <span class="tu-date-sep">至</span>
          <input v-model="endDate" type="date" class="tu-date-input" title="结束日期" />
          <button class="tu-date-btn" @click="applyFilter" title="应用筛选">筛选</button>
          <button v-if="startDate || endDate" class="tu-date-btn clear" @click="clearDateFilter" title="清除筛选">清除</button>
        </div>
        <!-- Export Button -->
        <button class="tu-export-btn" @click="handleExport" :disabled="exporting">
          <svg v-if="!exporting" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          <span v-if="exporting">导出中...</span>
          <span v-else>导出</span>
        </button>
      </div>
    </div>

    <!-- Summary Cards -->
    <div class="tu-summary">
      <div class="tu-summary-card" style="--kpi-color: #0A84FF">
        <div class="tu-kpi-bar"></div>
        <div class="tu-kpi-body">
          <div class="tu-kpi-value">{{ formatNum(summary.totalRequests) }}</div>
          <div class="tu-kpi-label">请求总数</div>
        </div>
      </div>
      <div class="tu-summary-card" style="--kpi-color: #30D158">
        <div class="tu-kpi-bar"></div>
        <div class="tu-kpi-body">
          <div class="tu-kpi-value">{{ formatNum(summary.totalPromptTokens) }}</div>
          <div class="tu-kpi-label">输入 Tokens</div>
        </div>
      </div>
      <div class="tu-summary-card" style="--kpi-color: #BF5AF2">
        <div class="tu-kpi-bar"></div>
        <div class="tu-kpi-body">
          <div class="tu-kpi-value">{{ formatNum(summary.totalCompletionTokens) }}</div>
          <div class="tu-kpi-label">输出 Tokens</div>
        </div>
      </div>
      <div class="tu-summary-card" style="--kpi-color: #FF9F0A">
        <div class="tu-kpi-bar"></div>
        <div class="tu-kpi-body">
          <div class="tu-kpi-value">{{ formatNum(summary.totalTokens) }}</div>
          <div class="tu-kpi-label">总 Tokens</div>
        </div>
      </div>
    </div>

    <!-- Main: Dept Tree + Users + Records -->
    <div class="tu-main">
      <!-- Left: Department Tree -->
      <div class="tu-dept-panel">
        <div class="tu-panel-header">组织架构</div>
        <div class="tu-dept-tree">
          <div
            class="tree-item tree-root"
            :class="{ active: selectedDeptId === null }"
            @click="selectedDeptId = null; loadUsers(1)"
          >
            <span class="dot dot-root"></span>
            <span class="tree-label">全部用户</span>
          </div>
          <template v-for="dept in deptTree" :key="dept.id">
            <DeptNode
              :dept="dept"
              :selected-id="selectedDeptId"
              :depth="0"
              @select="onDeptSelect"
            />
          </template>
        </div>
      </div>

      <!-- Left-Middle: User List -->
      <div class="tu-users-panel">
        <div class="tu-panel-header">
          用户 ({{ userTotal }})
          <div class="tu-user-search">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="userKeyword" placeholder="搜索用户..." class="tu-search-input" @input="onUserSearch" />
          </div>
        </div>
        <div class="tu-user-list">
          <div v-if="usersLoading" class="tu-loading">加载中...</div>
          <template v-else>
            <div
              v-for="user in users"
              :key="user.userId"
              class="tu-user-card"
              :class="{ active: selectedUserId === user.userId }"
              @click="selectUser(user.userId)"
            >
              <div class="tu-user-avatar">{{ getInitials(user.nickname || user.username) }}</div>
              <div class="tu-user-info">
                <div class="tu-user-name" :title="user.nickname || user.username">{{ user.nickname || user.username }}</div>
                <div class="tu-user-meta">
                  <span class="tu-dept-name">{{ user.deptName || '未分配' }}</span>
                  <span class="tu-tokens-badge">{{ formatTokens(user.totalTokens) }} tokens</span>
                </div>
              </div>
            </div>
            <div v-if="users.length === 0 && !usersLoading" class="tu-empty">无结果</div>
          </template>
        </div>
        <!-- User Pagination -->
        <div class="tu-pagination">
          <button class="tu-page-btn" :disabled="userPage <= 1" @click="loadUsers(userPage - 1)">上一页</button>
          <span class="tu-page-info">{{ userPage }} / {{ userTotalPages }}</span>
          <button class="tu-page-btn" :disabled="userPage >= userTotalPages" @click="loadUsers(userPage + 1)">下一页</button>
        </div>
      </div>

      <!-- Right: Records Table -->
      <div class="tu-records-panel">
        <div class="tu-panel-header">
          <span>用量明细</span>
          <span class="tu-records-count">共 {{ recordTotal }} 条</span>
        </div>
        <div class="tu-table-wrap">
          <table class="tu-table">
            <thead>
              <tr>
                <th>日期</th>
                <th>用户</th>
                <th>部门</th>
                <th>模型</th>
                <th>请求数</th>
                <th>输入</th>
                <th>输出</th>
                <th>总计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="recordsLoading">
                <td colspan="8" class="tu-table-loading">加载中...</td>
              </tr>
              <tr v-else-if="records.length === 0">
                <td colspan="8" class="tu-table-empty">暂无数据</td>
              </tr>
              <template v-else>
                <tr v-for="record in records" :key="record.id">
                  <td>{{ record.usageDate }}</td>
                  <td>{{ record.nickname || record.username }}</td>
                  <td>{{ record.deptName || '-' }}</td>
                  <td>{{ record.modelName || '-' }}</td>
                  <td class="tu-num">{{ record.requestCount }}</td>
                  <td class="tu-num">{{ formatNum(record.promptTokens) }}</td>
                  <td class="tu-num">{{ formatNum(record.completionTokens) }}</td>
                  <td class="tu-num tu-total">{{ formatNum(record.totalTokens) }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
        <!-- Records Pagination -->
        <div class="tu-pagination">
          <button class="tu-page-btn" :disabled="recordPage <= 1" @click="loadRecords(recordPage - 1)">上一页</button>
          <span class="tu-page-info">{{ recordPage }} / {{ recordTotalPages }}</span>
          <button class="tu-page-btn" :disabled="recordPage >= recordTotalPages" @click="loadRecords(recordPage + 1)">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { adminTokenUsageApi, type TokenUsageUser, type TokenUsageRecord, type TokenUsageSummary } from '@/api/tokenUsage'
import { deptApi, type DeptInfo } from '@/api/dept'
import { useDesktopStore } from '@/stores/desktop'

const desktop = useDesktopStore()

// ==================== Search/Filter State ====================
const userKeyword = ref('')
const startDate = ref('')
const endDate = ref('')
const selectedDeptId = ref<number | null>(null)
const selectedUserId = ref<number | null>(null)
const exporting = ref(false)

// ==================== Data State ====================
const deptTree = ref<DeptInfo[]>([])
const users = ref<TokenUsageUser[]>([])
const records = ref<TokenUsageRecord[]>([])
const summary = ref<TokenUsageSummary>({ totalRequests: 0, totalPromptTokens: 0, totalCompletionTokens: 0, totalTokens: 0 })

const usersLoading = ref(false)
const recordsLoading = ref(false)

// ==================== Pagination State ====================
const userPage = ref(1)
const recordPage = ref(1)
const userTotal = ref(0)
const recordTotal = ref(0)
const userPageSize = 20
const recordPageSize = 15

const userTotalPages = computed(() => Math.max(1, Math.ceil(userTotal.value / userPageSize)))
const recordTotalPages = computed(() => Math.max(1, Math.ceil(recordTotal.value / recordPageSize)))

// ==================== Debounce Timers ====================
let userSearchTimer: ReturnType<typeof setTimeout> | null = null

// ==================== Load Functions ====================
async function loadDeptTree() {
  try {
    deptTree.value = await deptApi.tree()
  } catch (e: any) {
    desktop.addToast('加载部门树失败', 'error')
  }
}

async function loadUsers(page = 1) {
  usersLoading.value = true
  try {
    const res = await adminTokenUsageApi.getUsers(
      userKeyword.value || undefined,
      selectedDeptId.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined,
      page,
      userPageSize
    )
    users.value = res.records
    userTotal.value = res.total
    userPage.value = page
  } catch (e: any) {
    desktop.addToast('加载用户列表失败', 'error')
  } finally {
    usersLoading.value = false
  }
}

async function loadRecords(page = 1) {
  recordsLoading.value = true
  try {
    const res = await adminTokenUsageApi.getRecords(
      selectedUserId.value || undefined,
      undefined,
      userKeyword.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined,
      page,
      recordPageSize
    )
    records.value = res.records
    recordTotal.value = res.total
    recordPage.value = page
  } catch (e: any) {
    desktop.addToast('加载用量记录失败', 'error')
  } finally {
    recordsLoading.value = false
  }
}

async function loadSummary() {
  try {
    const res = await adminTokenUsageApi.getSummary(
      selectedUserId.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined
    )
    summary.value = res
  } catch (e: any) {
    console.error('Failed to load summary', e)
  }
}

// ==================== Event Handlers ====================
function onUserSearch() {
  if (userSearchTimer) clearTimeout(userSearchTimer)
  userSearchTimer = setTimeout(() => {
    userPage.value = 1
    loadUsers(1)
  }, 300)
}

function onDeptSelect(deptId: number) {
  selectedDeptId.value = deptId
  selectedUserId.value = null
  userPage.value = 1
  loadUsers(1)
}

function selectUser(userId: number) {
  selectedUserId.value = userId
  recordPage.value = 1
  loadRecords(1)
  loadSummary()
}

function applyFilter() {
  userPage.value = 1
  recordPage.value = 1
  loadUsers(1)
  loadRecords(1)
  loadSummary()
}

function clearDateFilter() {
  startDate.value = ''
  endDate.value = ''
  userPage.value = 1
  recordPage.value = 1
  loadUsers(1)
  loadRecords(1)
  loadSummary()
}

async function handleExport() {
  exporting.value = true
  try {
    const result = await adminTokenUsageApi.exportTokenUsage(
      selectedUserId.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined,
      'md'
    )
    const blob = new Blob([result.content], { type: 'text/markdown;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = result.filename || `token_usage_${new Date().toISOString().substring(0, 10)}.md`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    desktop.addToast('导出成功', 'success')
  } catch (e: any) {
    desktop.addToast('导出失败: ' + (e.message || '未知错误'), 'error')
  } finally {
    exporting.value = false
  }
}

// ==================== Utility Functions ====================
function getInitials(name: string): string {
  if (!name) return '?'
  return name.slice(0, 1).toUpperCase()
}

function formatNum(n: number | undefined): string {
  if (!n) return '0'
  return n.toLocaleString()
}

function formatTokens(n: number | undefined): string {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

// ==================== DeptNode Sub-Component ====================
const DeptNode = defineComponent({
  props: {
    dept: { type: Object as () => DeptInfo, required: true },
    selectedId: { type: Number as () => number | null, default: null },
    depth: { type: Number, default: 0 }
  },
  emits: ['select'],
  setup(props, { emit }) {
    const expanded = ref(true)
    const hasChildren = computed(() => (props.dept.children || []).length > 0)

    return () => {
      const children = props.dept.children || []
      const indent = props.depth * 16 + 10

      return h('div', { class: 'tree-branch' }, [
        h('div', {
          class: ['tree-item', { active: props.selectedId === props.dept.id }],
          style: { paddingLeft: indent + 'px' },
          onClick: () => emit('select', props.dept.id)
        }, [
          hasChildren.value
            ? h('span', {
                class: ['tree-toggle', { open: expanded.value }],
                onClick: (e: Event) => { e.stopPropagation(); expanded.value = !expanded.value }
              }, expanded.value ? '▾' : '▸')
            : h('span', { class: 'tree-toggle-spacer' }),
          h('span', { class: props.depth === 0 ? 'dot dot-solid' : 'dot dot-hollow' }),
          h('span', { class: 'tree-label' }, props.dept.deptName)
        ]),
        hasChildren.value && expanded.value
          ? h('div', { class: 'tree-children' },
              children.map((child: DeptInfo) =>
                h(DeptNode, {
                  key: child.id,
                  dept: child,
                  selectedId: props.selectedId,
                  depth: props.depth + 1,
                  onSelect: (id: number) => emit('select', id)
                })
              )
            )
          : null
      ])
    }
  }
})

// ==================== Init ====================
onMounted(async () => {
  // Set default date range to current month
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  startDate.value = firstDay.toISOString().split('T')[0]
  endDate.value = now.toISOString().split('T')[0]

  await Promise.all([loadDeptTree(), loadUsers(1), loadSummary()])
})
</script>

<style scoped>
.tu-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  color: var(--text-primary);
}

/* Header */
.tu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.tu-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #FF9F0A;
}

.tu-title {
  font-size: 14px;
  font-weight: 600;
}

.tu-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Date Range */
.tu-date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tu-date-input {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  color: var(--text-primary);
  outline: none;
  width: 110px;
  color-scheme: dark;
}

.tu-date-input:focus {
  border-color: rgba(255, 159, 10, 0.4);
}

.tu-date-sep {
  color: var(--text-disabled);
  font-size: 12px;
}

.tu-date-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.15s;
}

.tu-date-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.tu-date-btn.clear {
  color: #FF9F0A;
}

/* Export Button */
.tu-export-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #FF9F0A, #FF6B00);
  border: none;
  color: white;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.tu-export-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.tu-export-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Summary Cards */
.tu-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 12px 16px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border-subtle);
}

.tu-summary-card {
  position: relative;
  display: flex;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  padding: 12px 14px;
  overflow: hidden;
}

.tu-kpi-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--kpi-color), transparent);
}

.tu-kpi-body {
  padding-left: 8px;
}

.tu-kpi-value {
  font-size: 20px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.tu-kpi-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 3px;
}

/* Main Layout */
.tu-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Dept Panel */
.tu-dept-panel {
  width: 200px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.tu-dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.tu-dept-tree::-webkit-scrollbar { width: 3px; }
.tu-dept-tree::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

/* Tree Items */
.tree-item {
  height: 30px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding-right: 10px;
  cursor: pointer;
  font-size: 12px;
  color: var(--text-secondary);
  transition: all 0.15s;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
}

.tree-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
}

.tree-item.active {
  background: rgba(255, 159, 10, 0.08);
  color: #FF9F0A;
}

.tree-root {
  padding-left: 14px !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  margin-bottom: 2px;
}

.tree-toggle {
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-disabled);
  cursor: pointer;
  border-radius: 3px;
  transition: all 0.15s;
  line-height: 1;
}

.tree-toggle:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
}

.tree-toggle-spacer {
  width: 16px;
  flex-shrink: 0;
}

.dot {
  width: 14px;
  height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dot::after {
  content: '';
  border-radius: 50%;
  display: block;
}

.dot-root::after {
  width: 7px;
  height: 7px;
  background: #FF9F0A;
}

.dot-solid::after {
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.5);
}

.tree-item.active .dot-solid::after {
  background: #FF9F0A;
}

.dot-hollow::after {
  width: 6px;
  height: 6px;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  background: transparent;
  box-sizing: border-box;
}

.tree-item.active .dot-hollow::after {
  border-color: #FF9F0A;
}

.tree-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Users Panel */
.tu-users-panel {
  width: 220px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.tu-panel-header {
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tu-user-search {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid transparent;
}

.tu-user-search:focus-within {
  border-color: rgba(255, 159, 10, 0.3);
}

.tu-user-search svg { color: var(--text-secondary); flex-shrink: 0; }

.tu-search-input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 11px;
  width: 100%;
}

.tu-search-input::placeholder { color: var(--text-disabled); }

/* User list */
.tu-user-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.tu-user-list::-webkit-scrollbar { width: 3px; }
.tu-user-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.tu-user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.tu-user-card:hover { background: rgba(255, 255, 255, 0.04); }
.tu-user-card.active { background: rgba(255, 159, 10, 0.1); border-color: rgba(255, 159, 10, 0.25); }

.tu-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF9F0A, #FF6B00);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.tu-user-info { flex: 1; min-width: 0; }

.tu-user-name {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tu-user-meta {
  display: flex;
  gap: 6px;
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-secondary);
}

.tu-tokens-badge {
  background: rgba(255, 159, 10, 0.12);
  color: #FF9F0A;
  padding: 1px 6px;
  border-radius: 8px;
  font-size: 10px;
}

/* Records Panel */
.tu-records-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tu-records-count {
  font-size: 11px;
  font-weight: 400;
  color: var(--text-disabled);
}

.tu-table-wrap {
  flex: 1;
  overflow: auto;
  margin: 8px;
  border-radius: 10px;
  border: 1px solid var(--border-subtle);
}

.tu-table-wrap::-webkit-scrollbar {
  width: 3px;
  height: 3px;
}

.tu-table-wrap::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.tu-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.tu-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.tu-table th {
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}

.tu-table td {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  white-space: nowrap;
}

.tu-table tbody tr:hover {
  background: rgba(255, 255, 255, 0.03);
}

.tu-num {
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.tu-total {
  font-weight: 600;
  color: #FF9F0A;
}

.tu-table-loading,
.tu-table-empty {
  text-align: center;
  padding: 40px 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

/* Pagination */
.tu-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.tu-page-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 11px;
  padding: 4px 10px;
  cursor: pointer;
  transition: background 0.15s;
}

.tu-page-btn:hover:not(:disabled) { background: rgba(255, 255, 255, 0.1); }
.tu-page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.tu-page-info { font-size: 11px; color: var(--text-secondary); }

/* States */
.tu-loading, .tu-empty {
  text-align: center;
  padding: 30px 16px;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
