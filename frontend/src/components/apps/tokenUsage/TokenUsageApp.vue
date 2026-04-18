<template>
  <div class="token-usage-app">
    <!-- Header -->
    <div class="tu-header">
      <div class="tu-header-left">
        <svg class="tu-header-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
        </svg>
        <span class="tu-header-title">用量统计</span>
      </div>
      <div class="tu-header-actions">
        <div class="tu-date-range">
          <input type="date" v-model="startDate" class="tu-date-input" />
          <span class="tu-date-sep">至</span>
          <input type="date" v-model="endDate" class="tu-date-input" />
        </div>
        <button v-if="isAdmin" class="tu-view-toggle" @click="toggleView">
          {{ isAdminView ? '管理视角' : '我的用量' }}
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

    <!-- Records Table -->
    <div class="tu-records">
      <div class="tu-records-header">
        <span class="tu-records-title">用量明细</span>
        <span class="tu-records-count">共 {{ totalRecords }} 条记录</span>
      </div>

      <div class="tu-table-wrap">
        <table class="tu-table">
          <thead>
            <tr>
              <th>日期</th>
              <th>用户 ID</th>
              <th>模型 ID</th>
              <th>请求数</th>
              <th>输入 Tokens</th>
              <th>输出 Tokens</th>
              <th>总 Tokens</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="recordsLoading">
              <td colspan="7" class="tu-table-loading">加载中...</td>
            </tr>
            <tr v-else-if="records.length === 0">
              <td colspan="7" class="tu-table-empty">暂无数据</td>
            </tr>
            <template v-else>
              <tr v-for="record in records" :key="record.id">
                <td>{{ record.usageDate }}</td>
                <td>{{ record.userId }}</td>
                <td>{{ record.modelConfigId }}</td>
                <td class="tu-num">{{ record.requestCount }}</td>
                <td class="tu-num">{{ formatNum(record.promptTokens) }}</td>
                <td class="tu-num">{{ formatNum(record.completionTokens) }}</td>
                <td class="tu-num tu-total">{{ formatNum(record.totalTokens) }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="tu-pagination">
        <button class="tu-page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadRecords()">上一页</button>
        <span class="tu-page-info">{{ pageNum }} / {{ totalPages }}</span>
        <button class="tu-page-btn" :disabled="pageNum >= totalPages" @click="pageNum++; loadRecords()">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { tokenUsageApi, adminTokenUsageApi, type TokenUsageRecord, type TokenUsageSummary } from '@/api/tokenUsage'

const startDate = ref('')
const endDate = ref('')
const isAdminView = ref(true)
const records = ref<TokenUsageRecord[]>([])
const summary = ref<TokenUsageSummary>({ totalRequests: 0, totalPromptTokens: 0, totalCompletionTokens: 0, totalTokens: 0 })
const recordsLoading = ref(false)
const pageNum = ref(1)
const totalRecords = ref(0)
const pageSize = 15

// Check if user is admin
const isAdmin = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const roles = userInfo.roles || []
    return roles.some((r: any) => ['SUPER_ADMIN', 'ADMIN'].includes(r.roleCode))
  } catch {
    return false
  }
})

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize)))

async function loadSummary() {
  try {
    const res = await tokenUsageApi.getUserSummary(startDate.value || undefined, endDate.value || undefined)
    summary.value = res
  } catch (e) {
    console.error('Failed to load summary', e)
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    if (isAdminView.value && isAdmin.value) {
      const res = await adminTokenUsageApi.getRecords(pageNum.value, pageSize)
      records.value = res.records
      totalRecords.value = res.total
    } else {
      const res = await tokenUsageApi.getUserRecords(startDate.value || undefined, endDate.value || undefined)
      records.value = res
      totalRecords.value = res.length
    }
  } catch (e) {
    console.error('Failed to load records', e)
  } finally {
    recordsLoading.value = false
  }
}

function toggleView() {
  isAdminView.value = !isAdminView.value
  pageNum.value = 1
  loadRecords()
}

function formatNum(n: number | undefined): string {
  if (!n) return '0'
  return n.toLocaleString()
}

// Watch date range changes
watch([startDate, endDate], () => {
  pageNum.value = 1
  loadSummary()
  loadRecords()
})

onMounted(() => {
  // Set default date range to current month
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  startDate.value = firstDay.toISOString().split('T')[0]
  endDate.value = now.toISOString().split('T')[0]

  loadSummary()
  loadRecords()
})
</script>

<style scoped>
.token-usage-app {
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
}

.tu-header-icon {
  color: #FF9F0A;
}

.tu-header-title {
  font-size: 14px;
  font-weight: 600;
}

.tu-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tu-date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tu-date-input {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 12px;
  padding: 4px 8px;
  outline: none;
  width: 130px;
}

.tu-date-input:focus {
  border-color: rgba(255, 159, 10, 0.3);
  box-shadow: 0 0 0 3px rgba(255, 159, 10, 0.1);
}

.tu-date-input::-webkit-calendar-picker-indicator {
  filter: invert(1);
  opacity: 0.4;
}

.tu-date-sep {
  font-size: 12px;
  color: var(--text-secondary);
}

.tu-view-toggle {
  background: rgba(255, 159, 10, 0.1);
  border: 1px solid rgba(255, 159, 10, 0.2);
  border-radius: 8px;
  color: #FF9F0A;
  font-size: 12px;
  padding: 5px 12px;
  cursor: pointer;
  transition: background 0.15s;
}

.tu-view-toggle:hover {
  background: rgba(255, 159, 10, 0.2);
}

/* Summary Cards */
.tu-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  flex-shrink: 0;
}

.tu-summary-card {
  position: relative;
  display: flex;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  padding: 14px 16px;
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
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.tu-kpi-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* Records */
.tu-records {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 16px 16px;
}

.tu-records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.tu-records-title {
  font-size: 13px;
  font-weight: 600;
}

.tu-records-count {
  font-size: 11px;
  color: var(--text-secondary);
}

.tu-table-wrap {
  flex: 1;
  overflow: auto;
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
  padding-top: 12px;
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

.tu-page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.tu-page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.tu-page-info {
  font-size: 11px;
  color: var(--text-secondary);
}
</style>
