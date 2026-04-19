<template>
  <div class="cron-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <Clock :size="20" class="header-icon" />
        <span class="header-title">定时任务</span>
      </div>
      <div class="header-actions">
        <button class="btn btn-outline btn-sm" @click="loadJobs">
          <RefreshCw :size="14" :class="{ 'spin': isLoading }" />
          刷新
        </button>
        <button class="btn btn-primary btn-sm" @click="openCreateModal">
          <Plus :size="14" />
          新建任务
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="app-content">
      <!-- Left Panel: Jobs -->
      <div class="jobs-panel">
        <!-- Jobs Header -->
        <div class="panel-header">
          <span class="panel-title">任务列表</span>
          <div class="filter-group">
            <select v-model="filter.enabled" class="filter-select" @change="loadJobs">
              <option :value="undefined">全部状态</option>
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
        </div>

        <!-- Jobs List -->
        <div class="jobs-list">
          <div
            v-for="job in jobs"
            :key="job.id"
            class="job-item"
            :class="{ disabled: job.enabled === 0, selected: selectedJob?.id === job.id }"
            @click="selectJob(job)"
          >
            <div class="job-header">
              <span class="job-status" :class="job.enabled === 1 ? 'status-enabled' : 'status-disabled'">
                {{ job.enabled === 1 ? '启用' : '禁用' }}
              </span>
              <span class="job-name">{{ job.name }}</span>
            </div>
            <div class="job-cron">
              <Timer :size="12" />
              {{ job.cronExpression }}
            </div>
            <div class="job-meta">
              <span v-if="job.nextRunTime" class="job-next">
                下次: {{ formatTime(job.nextRunTime) }}
              </span>
              <span v-else class="job-next">未排期</span>
              <span class="job-status-text" :class="job.lastRunStatus?.toLowerCase()">
                {{ formatStatus(job.lastRunStatus) }}
              </span>
            </div>
          </div>
          <div v-if="jobs.length === 0" class="empty-tip">暂无任务</div>
        </div>
      </div>

      <!-- Right Panel: Detail/History -->
      <div class="detail-panel">
        <!-- No Selection -->
        <div v-if="!selectedJob" class="no-selection">
          <Clock :size="48" class="text-muted" />
          <span>选择左侧任务查看详情</span>
        </div>

        <!-- Job Detail -->
        <div v-else class="job-detail">
          <!-- Detail Header -->
          <div class="detail-header">
            <div class="detail-title">{{ selectedJob.name }}</div>
            <div class="detail-actions">
              <button
                class="btn btn-sm"
                :class="selectedJob.enabled === 1 ? 'btn-outline' : 'btn-primary'"
                @click="toggleJob"
              >
                <Power :size="14" />
                {{ selectedJob.enabled === 1 ? '暂停' : '启用' }}
              </button>
              <button class="btn btn-primary btn-sm" @click="runJob">
                <Play :size="14" />
                立即执行
              </button>
              <button class="btn btn-outline btn-sm" @click="openEditModal">
                <Edit2 :size="14" />
                编辑
              </button>
              <button class="btn btn-icon text-danger" @click="deleteCurrentJob">
                <Trash2 :size="14" />
              </button>
            </div>
          </div>

          <!-- Detail Info -->
          <div class="detail-info">
            <div class="info-row">
              <span class="info-label">cron表达式</span>
              <span class="info-value">{{ selectedJob.cronExpression }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">时区</span>
              <span class="info-value">{{ selectedJob.timezone }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">任务类型</span>
              <span class="info-value">{{ selectedJob.taskType }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">超时时间</span>
              <span class="info-value">{{ selectedJob.timeoutSeconds }}秒</span>
            </div>
            <div class="info-row">
              <span class="info-label">最大并发</span>
              <span class="info-value">{{ selectedJob.maxConcurrency }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">上次执行</span>
              <span class="info-value">{{ selectedJob.lastRunTime ? formatTime(selectedJob.lastRunTime) : '从未' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">下次执行</span>
              <span class="info-value">{{ selectedJob.nextRunTime ? formatTime(selectedJob.nextRunTime) : 'N/A' }}</span>
            </div>
            <div v-if="selectedJob.description" class="info-row">
              <span class="info-label">描述</span>
              <span class="info-value">{{ selectedJob.description }}</span>
            </div>
            <div v-if="selectedJob.lastRunError" class="info-row error-row">
              <span class="info-label">上次错误</span>
              <span class="info-value text-danger">{{ selectedJob.lastRunError }}</span>
            </div>
          </div>

          <!-- Prompt Template -->
          <div v-if="selectedJob.promptTemplate" class="detail-section">
            <div class="section-title">Prompt 模板</div>
            <div class="prompt-template">{{ selectedJob.promptTemplate }}</div>
          </div>

          <!-- History Section -->
          <div class="detail-section">
            <div class="section-header">
              <span class="section-title">执行历史</span>
              <button class="btn btn-outline btn-sm" @click="loadHistory">
                <RefreshCw :size="12" />
              </button>
            </div>
            <div class="history-list">
              <div v-for="item in history" :key="item.id" class="history-item">
                <div class="history-header">
                  <span class="history-status" :class="item.status.toLowerCase()">
                    {{ formatStatus(item.status) }}
                  </span>
                  <span class="history-trigger">{{ item.triggerType === 'SCHEDULED' ? '定时' : '手动' }}</span>
                  <span class="history-time">{{ formatTime(item.startTime) }}</span>
                </div>
                <div v-if="item.errorMessage" class="history-error">{{ item.errorMessage }}</div>
              </div>
              <div v-if="history.length === 0" class="empty-tip">暂无历史</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal cron-modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑任务' : '新建任务' }}</span>
            <button class="modal-close" @click="showModal = false">
              <X :size="16" />
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>任务名称 <span class="required">*</span></label>
                <input v-model="form.name" placeholder="如：每日提醒" />
              </div>
              <div class="form-field">
                <label>cron表达式 <span class="required">*</span></label>
                <input v-model="form.cronExpression" placeholder="如：0 9 * * *" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>时区</label>
                <select v-model="form.timezone" class="form-select">
                  <option value="Asia/Shanghai">Asia/Shanghai</option>
                  <option value="UTC">UTC</option>
                  <option value="America/New_York">America/New_York</option>
                </select>
              </div>
              <div class="form-field">
                <label>任务类型</label>
                <select v-model="form.taskType" class="form-select">
                  <option value="agent">AI对话</option>
                  <option value="text">发送文本</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>超时时间(秒)</label>
                <input v-model.number="form.timeoutSeconds" type="number" min="30" max="3600" />
              </div>
              <div class="form-field">
                <label>最大并发</label>
                <input v-model.number="form.maxConcurrency" type="number" min="1" max="10" />
              </div>
            </div>
            <div class="form-field">
              <label>描述</label>
              <input v-model="form.description" placeholder="任务描述(可选)" />
            </div>
            <div class="form-field">
              <label>Prompt 模板</label>
              <textarea v-model="form.promptTemplate" rows="4" placeholder="执行时的prompt，支持 {{current_date}}、{{current_time}}、{{job_name}} 变量" />
            </div>

            <!-- Cron Help -->
            <div class="cron-help">
              <div class="cron-help-title">cron 表达式说明</div>
              <div class="cron-help-grid">
                <div class="cron-help-item"><span>0 9 * * *</span> 每天9点</div>
                <div class="cron-help-item"><span>0 */2 * * *</span> 每2小时</div>
                <div class="cron-help-item"><span>0 9 * * 1-5</span> 工作日9点</div>
                <div class="cron-help-item"><span>*/10 * * * *</span> 每10分钟</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="submitForm">{{ isEditing ? '保存' : '创建' }}</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Clock, RefreshCw, Plus, Timer, Power, Play, Edit2, Trash2, X } from 'lucide-vue-next'
import { cronApi, type CronJob, type CronJobHistory } from '@/api/cron'
import { useDesktopStore } from '@/stores/desktop'
import { useAlert } from '@/composables/useAlert'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { alert } = useAlert()
const { confirm } = useConfirm()

const isLoading = ref(false)
const jobs = ref<CronJob[]>([])
const selectedJob = ref<CronJob | null>(null)
const history = ref<CronJobHistory[]>([])
const showModal = ref(false)
const isEditing = ref(false)
const filter = reactive({
  enabled: undefined as number | undefined
})

const form = reactive({
  id: null as number | null,
  name: '',
  cronExpression: '',
  timezone: 'Asia/Shanghai',
  taskType: 'agent',
  timeoutSeconds: 300,
  maxConcurrency: 1,
  description: '',
  promptTemplate: ''
})

async function loadJobs() {
  isLoading.value = true
  try {
    const res = await cronApi.listJobs({ enabled: filter.enabled })
    jobs.value = res.records
  } catch (e) {
    console.error('Failed to load jobs:', e)
  } finally {
    isLoading.value = false
  }
}

async function selectJob(job: CronJob) {
  selectedJob.value = job
  await loadHistory()
}

async function loadHistory() {
  if (!selectedJob.value) return
  try {
    const res = await cronApi.listJobHistory(selectedJob.value.id)
    history.value = res.records
  } catch (e) {
    console.error('Failed to load history:', e)
  }
}

function openCreateModal() {
  isEditing.value = false
  Object.assign(form, {
    name: '',
    cronExpression: '',
    timezone: 'Asia/Shanghai',
    taskType: 'agent',
    timeoutSeconds: 300,
    maxConcurrency: 1,
    description: '',
    promptTemplate: ''
  })
  showModal.value = true
}

function openEditModal() {
  if (!selectedJob.value) return
  isEditing.value = true
  Object.assign(form, {
    id: selectedJob.value.id,
    name: selectedJob.value.name,
    cronExpression: selectedJob.value.cronExpression,
    timezone: selectedJob.value.timezone,
    taskType: selectedJob.value.taskType,
    timeoutSeconds: selectedJob.value.timeoutSeconds,
    maxConcurrency: selectedJob.value.maxConcurrency,
    description: selectedJob.value.description || '',
    promptTemplate: selectedJob.value.promptTemplate || ''
  })
  showModal.value = true
}

async function submitForm() {
  if (!form.name || !form.cronExpression) {
    await alert('验证失败', '请填写任务名称和cron表达式')
    return
  }
  try {
    if (isEditing.value && selectedJob.value) {
      await cronApi.updateJob(selectedJob.value.id, form)
    } else {
      await cronApi.createJob(form)
    }
    showModal.value = false
    await loadJobs()
    desktop.addToast('保存成功', 'success')
  } catch (e: any) {
    console.error('Failed to save job:', e)
    desktop.addToast(e.message || '保存失败', 'error')
  }
}

async function toggleJob() {
  if (!selectedJob.value) return
  try {
    if (selectedJob.value.enabled === 1) {
      await cronApi.pauseJob(selectedJob.value.id)
    } else {
      await cronApi.resumeJob(selectedJob.value.id)
    }
    await loadJobs()
    const updated = jobs.value.find(j => j.id === selectedJob.value!.id)
    if (updated) selectedJob.value = updated
  } catch (e) {
    console.error('Failed to toggle job:', e)
  }
}

async function runJob() {
  if (!selectedJob.value) return
  try {
    await cronApi.runJob(selectedJob.value.id)
    desktop.addToast('任务已触发', 'success')
    await loadHistory()
  } catch (e: any) {
    console.error('Failed to run job:', e)
    desktop.addToast(e.message || '触发失败', 'error')
  }
}

async function deleteCurrentJob() {
  if (!selectedJob.value) return
  const ok = await confirm('删除确认', '确定要删除该定时任务吗？此操作不可恢复。', { danger: true })
  if (!ok) return
  try {
    await cronApi.deleteJob(selectedJob.value.id)
    selectedJob.value = null
    await loadJobs()
    desktop.addToast('删除成功', 'success')
  } catch (e: any) {
    console.error('Failed to delete job:', e)
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

function formatTime(time: string): string {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function formatStatus(status: string | undefined): string {
  if (!status) return '未知'
  const map: Record<string, string> = {
    SUCCESS: '成功',
    ERROR: '失败',
    RUNNING: '运行中',
    TIMEOUT: '超时',
    CANCELLED: '取消'
  }
  return map[status] || status
}

loadJobs()
</script>

<style scoped>
.cron-app {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: #30D158;
}

.header-title {
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.app-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.jobs-panel {
  width: 340px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.panel-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 13px;
}

.jobs-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.job-item {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  transition: all 0.15s;
}

.job-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.job-item.selected {
  background: rgba(48, 209, 88, 0.1);
  border-color: rgba(48, 209, 88, 0.3);
}

.job-item.disabled {
  opacity: 0.6;
}

.job-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.job-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.status-enabled {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.status-disabled {
  background: rgba(142, 142, 147, 0.2);
  color: #8E8E93;
}

.job-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 13px;
}

.job-cron {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  font-family: monospace;
}

.job-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-disabled);
}

.job-status-text {
  font-weight: 500;
}

.job-status-text.success {
  color: #30D158;
}

.job-status-text.error {
  color: #FF453A;
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-disabled);
}

.job-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.detail-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.detail-info {
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  border-bottom: 1px solid var(--border-subtle);
}

.info-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 11px;
  color: var(--text-disabled);
  text-transform: uppercase;
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);
}

.error-row .info-value {
  font-size: 12px;
}

.detail-section {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-bottom: 1px solid var(--border-subtle);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-primary);
}

.prompt-template {
  padding: 12px;
  background: #2c2c2e;
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  white-space: pre-wrap;
  font-family: monospace;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  padding: 10px;
  background: #2c2c2e;
  border-radius: 6px;
  margin-bottom: 6px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.history-status {
  font-weight: 500;
}

.history-status.success {
  color: #30D158;
}

.history-status.error {
  color: #FF453A;
}

.history-status.running {
  color: #0A84FF;
}

.history-trigger {
  color: var(--text-disabled);
}

.history-time {
  color: var(--text-disabled);
  margin-left: auto;
}

.history-error {
  margin-top: 6px;
  font-size: 11px;
  color: #FF453A;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1c1c1e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  width: 520px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-weight: 600;
  font-size: 15px;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.form-field:last-child {
  margin-bottom: 0;
}

.form-field label {
  font-size: 12px;
  color: var(--text-secondary);
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 8px 12px;
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  background: #2c2c2e;
  color: var(--text-primary);
  font-size: 13px;
}

.form-field textarea {
  resize: vertical;
  font-family: inherit;
}

.required {
  color: #FF453A;
}

.cron-help {
  margin-top: 16px;
  padding: 12px;
  background: #2c2c2e;
  border-radius: 8px;
}

.cron-help-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.cron-help-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.cron-help-item {
  font-size: 12px;
  color: var(--text-disabled);
}

.cron-help-item span {
  font-family: monospace;
  color: var(--text-primary);
  margin-right: 4px;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid var(--border-subtle);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
  background: transparent;
  color: var(--text-primary);
}

.btn:hover {
  background: rgba(255, 255, 255, 0.05);
}

.btn-primary {
  background: #0A84FF;
  border-color: #0A84FF;
  color: white;
}

.btn-primary:hover {
  background: #0070E0;
}

.btn-outline {
  background: transparent;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 11px;
}

.btn-icon {
  padding: 6px;
  border: none;
  background: transparent;
}

.text-danger {
  color: #FF453A;
}

.text-muted {
  color: var(--text-disabled);
}

.text-success {
  color: #30D158;
}

.empty-tip {
  padding: 24px;
  text-align: center;
  color: var(--text-disabled);
  font-size: 13px;
}

.filter-select {
  padding: 4px 8px;
  border: 1px solid var(--border-subtle);
  border-radius: 4px;
  background: #2c2c2e;
  color: var(--text-primary);
  font-size: 11px;
}

.filter-select option {
  background: #2c2c2e;
  color: var(--text-primary);
}

.filter-group {
  display: flex;
  gap: 8px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
