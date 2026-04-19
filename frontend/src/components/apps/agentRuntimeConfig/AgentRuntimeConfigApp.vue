<template>
  <div class="agent-runtime-config-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <Settings2 :size="20" class="header-icon" />
        <span class="header-title">Agent 运行时配置</span>
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
      <!-- Left Panel: Config Categories -->
      <div class="config-categories-panel">
        <div class="panel-header">
          <span class="panel-title">配置项</span>
        </div>
        <div class="categories-list">
          <div
            v-for="cat in configCategories"
            :key="cat.key"
            class="category-item"
            :class="{ active: selectedCategory === cat.key }"
            @click="selectCategory(cat.key)"
          >
            <component :is="cat.icon" :size="16" />
            <span class="category-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>

      <!-- Right Panel: Config Form -->
      <div class="config-form-panel">
        <div class="panel-header">
          <span class="panel-title">{{ currentConfigInfo?.name || '配置详情' }}</span>
          <button class="btn btn-primary btn-sm" @click="saveConfig" :disabled="isSaving">
            <Save :size="14" />
            保存
          </button>
        </div>

        <div v-if="currentFormData" class="form-content">
          <div class="form-description">{{ currentConfigInfo?.description }}</div>

          <!-- Dynamic form fields -->
          <div class="form-table">
            <div
              v-for="field in currentFields"
              :key="field.key"
              class="form-row"
            >
              <div class="form-label-col">
                <span class="form-label">{{ field.label }}</span>
                <span class="form-key">{{ field.key }}</span>
              </div>
              <div class="form-input-col">
                <!-- Boolean: Switch -->
                <label v-if="field.type === 'boolean'" class="switch-wrap">
                  <input type="checkbox" v-model="currentFormData[field.key]" />
                  <span class="switch-slider"></span>
                </label>

                <!-- Number: Input with step -->
                <input
                  v-else-if="field.type === 'number'"
                  type="number"
                  class="form-input"
                  v-model.number="currentFormData[field.key]"
                  :min="field.min"
                  :max="field.max"
                  :step="field.step || 1"
                />

                <!-- String: Text or Textarea -->
                <textarea
                  v-else-if="field.type === 'string' && field.multiline"
                  class="form-textarea"
                  v-model="currentFormData[field.key]"
                  :placeholder="field.hint || ''"
                  rows="3"
                ></textarea>
                <input
                  v-else
                  type="text"
                  class="form-input"
                  v-model="currentFormData[field.key]"
                  :placeholder="field.hint || ''"
                />
              </div>
              <div class="form-hint-col">
                <span v-if="field.hint && field.type !== 'string'" class="form-hint">{{ field.hint }}</span>
              </div>
            </div>
          </div>

          <!-- Change reason -->
          <div class="change-reason-row">
            <label class="form-label-inline">变更原因</label>
            <input
              v-model="changeReason"
              type="text"
              class="form-input"
              placeholder="请输入变更原因（可选）"
              style="flex: 1"
            />
          </div>
        </div>

        <!-- History Section -->
        <div class="history-section">
          <div class="panel-header">
            <span class="panel-title">变更历史</span>
            <button class="btn btn-outline btn-sm" @click="loadHistory">
              <RefreshCw :size="14" :class="{ 'spin': isLoadingHistory }" />
            </button>
          </div>

          <div class="history-table">
            <table>
              <thead>
                <tr>
                  <th>操作人</th>
                  <th>变更原因</th>
                  <th>时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in history" :key="item.id">
                  <td>{{ item.operatorName }}</td>
                  <td>{{ item.changeReason || '-' }}</td>
                  <td>{{ formatTime(item.createTime) }}</td>
                  <td>
                    <button class="btn btn-outline btn-xs" @click="viewDetail(item)">详情</button>
                  </td>
                </tr>
                <tr v-if="history.length === 0">
                  <td colspan="4" class="empty-cell">暂无历史记录</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div v-if="historyTotal > 0" class="history-pagination">
            <button class="btn btn-outline btn-sm" :disabled="historyPage <= 1" @click="historyPage--; loadHistory()">
              上一页
            </button>
            <span class="pagination-info">{{ historyPage }} / {{ Math.ceil(Number(historyTotal) / historyPageSize) }}</span>
            <button class="btn btn-outline btn-sm" :disabled="historyPage * historyPageSize >= Number(historyTotal)" @click="historyPage++; loadHistory()">
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <teleport to="body">
      <div v-if="detailModal.visible" class="modal-overlay" @click.self="detailModal.visible = false">
        <div class="modal">
          <div class="modal-header">
            <span class="modal-title">变更详情</span>
            <button class="btn btn-icon" @click="detailModal.visible = false">
              <X :size="18" />
            </button>
          </div>
          <div class="modal-body">
            <div class="detail-meta">
              <span>操作人：{{ detailModal.data?.operatorName }}</span>
              <span>时间：{{ formatTime(detailModal.data?.createTime) }}</span>
              <span>变更原因：{{ detailModal.data?.changeReason || '-' }}</span>
            </div>
            <div class="diff-view">
              <div class="diff-section">
                <div class="diff-label">旧值</div>
                <pre>{{ formatJson(detailModal.data?.oldValue) }}</pre>
              </div>
              <div class="diff-section">
                <div class="diff-label">新值</div>
                <pre>{{ formatJson(detailModal.data?.newValue) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Settings2, RefreshCw, Save, X,
  Cpu, Zap, Database, Brain, Globe, FileText
} from 'lucide-vue-next'
import { agentRuntimeConfigApi } from '@/api/agentRuntimeConfig'
import { useDesktopStore } from '@/stores/desktop'

const desktop = useDesktopStore()

// ============ Config Category Definitions ============

const configCategories = [
  { key: 'llm_retry', name: 'LLM 重试与限流', icon: Zap },
  { key: 'context_compact', name: '上下文压缩', icon: Cpu },
  { key: 'tool_result_compact', name: '工具结果压缩', icon: Database },
  { key: 'memory_summary', name: '记忆摘要', icon: Brain },
  { key: 'embedding', name: 'Embedding 配置', icon: Globe },
  { key: 'runtime_basic', name: '基础运行时配置', icon: FileText }
]

const configInfoMap: Record<string, { name: string; description: string }> = {
  llm_retry: { name: 'LLM 重试与限流', description: '配置 LLM 调用的重试策略、限流参数和并发控制' },
  context_compact: { name: '上下文压缩', description: '配置上下文压缩的触发阈值和保留比例' },
  tool_result_compact: { name: '工具结果压缩', description: '配置工具结果的压缩阈值和保留策略' },
  memory_summary: { name: '记忆摘要', description: '配置记忆摘要的生成策略和搜索参数' },
  embedding: { name: 'Embedding 配置', description: '配置 Embedding 模型的后端和参数' },
  runtime_basic: { name: '基础运行时配置', description: '配置 Agent 的基础运行时参数' }
}

// ============ Field Definitions ============

interface FormField {
  key: string
  label: string
  type: 'boolean' | 'number' | 'string'
  hint?: string
  min?: number
  max?: number
  step?: number
  multiline?: boolean
}

const fieldDefinitions: Record<string, FormField[]> = {
  llm_retry: [
    { key: 'llm_retry_enabled', label: '启用重试', type: 'boolean', hint: 'LLM API 调用失败时自动重试' },
    { key: 'llm_max_retries', label: '最大重试次数', type: 'number', min: 1, max: 10, hint: '最大重试次数' },
    { key: 'llm_backoff_base', label: '退避基础值(秒)', type: 'number', min: 0.1, max: 60, step: 0.5, hint: '指数退避初始延迟' },
    { key: 'llm_backoff_cap', label: '退避上限(秒)', type: 'number', min: 0.5, max: 120, step: 1, hint: '重试延迟上限' },
    { key: 'llm_max_concurrent', label: '最大并发数', type: 'number', min: 1, max: 100, hint: '同时进行的最大 LLM 调用数' },
    { key: 'llm_max_qpm', label: '最大QPM', type: 'number', min: 0, max: 10000, hint: '每分钟最大请求数，0=不限' },
    { key: 'llm_rate_limit_pause', label: '限流暂停(秒)', type: 'number', min: 1, max: 60, step: 0.5, hint: '触发 429 时全局暂停时间' },
    { key: 'llm_rate_limit_jitter', label: '限流抖动(秒)', type: 'number', min: 0, max: 10, step: 0.5, hint: '随机抖动范围' },
    { key: 'llm_acquire_timeout', label: '获取超时(秒)', type: 'number', min: 10, max: 600, step: 10, hint: '等待信号槽的最大时间' },
  ],
  context_compact: [
    { key: 'token_count_model', label: 'Token计数模型', type: 'string', hint: '用于计算 token 的模型名称' },
    { key: 'token_count_use_mirror', label: '使用HuggingFace镜像', type: 'boolean', hint: '是否使用 HF 镜像加速' },
    { key: 'token_count_estimate_divisor', label: 'Token估算除数', type: 'number', min: 2, max: 10, step: 0.5, hint: '字节长度/token 估算除数' },
    { key: 'context_compact_enabled', label: '启用上下文压缩', type: 'boolean', hint: '上下文达到阈值时自动压缩' },
    { key: 'memory_compact_ratio', label: '压缩触发比例', type: 'number', min: 0.3, max: 0.9, step: 0.05, hint: '触发压缩的上下文比例阈值' },
    { key: 'memory_reserve_ratio', label: '保留比例', type: 'number', min: 0.05, max: 0.3, step: 0.01, hint: '压缩后保留的上下文比例' },
    { key: 'compact_with_thinking_block', label: '包含思考块', type: 'boolean', hint: '压缩时是否保留思考块' },
  ],
  tool_result_compact: [
    { key: 'enabled', label: '启用压缩', type: 'boolean', hint: '是否压缩历史工具结果' },
    { key: 'recent_n', label: '最近N条保留完整', type: 'number', min: 1, max: 10, hint: '最近几条工具结果完整保留' },
    { key: 'old_max_bytes', label: '旧结果上限(字节)', type: 'number', min: 100, max: 50000, hint: '非最近工具结果的最大字节数' },
    { key: 'recent_max_bytes', label: '最近结果上限(字节)', type: 'number', min: 1000, max: 200000, hint: '最近工具结果的最大字节数' },
    { key: 'retention_days', label: '保留天数', type: 'number', min: 1, max: 30, hint: '工具结果文件保留天数' },
  ],
  memory_summary: [
    { key: 'memory_summary_enabled', label: '启用记忆摘要', type: 'boolean', hint: '压缩时生成记忆摘要' },
    { key: 'memory_prompt_enabled', label: '启用记忆提示', type: 'boolean', hint: '在系统提示中包含记忆引导' },
    { key: 'dream_cron', label: '梦的定时表达式', type: 'string', hint: 'Cron 表达式，如 "0 23 * * *"（每天23点）' },
    { key: 'force_memory_search', label: '强制记忆搜索', type: 'boolean', hint: '每轮对话强制搜索记忆' },
    { key: 'force_max_results', label: '强制搜索最大结果数', type: 'number', min: 1, max: 20, hint: '强制搜索返回的最大结果数' },
    { key: 'force_min_score', label: '强制搜索最低分数', type: 'number', min: 0, max: 1, step: 0.05, hint: '记忆搜索结果最低相关性分数' },
    { key: 'force_memory_search_timeout', label: '强制搜索超时(秒)', type: 'number', min: 1, max: 60, hint: '记忆搜索超时时间' },
    { key: 'rebuild_memory_index_on_start', label: '启动时重建索引', type: 'boolean', hint: 'Agent 启动时重建记忆搜索索引' },
    { key: 'recursive_file_watcher', label: '递归监视文件', type: 'boolean', hint: '递归监视 memory 目录下所有子目录' },
  ],
  embedding: [
    { key: 'backend', label: '后端', type: 'string', hint: 'Embedding 后端，如 "openai"' },
    { key: 'api_key', label: 'API Key', type: 'string', hint: 'Embedding 服务 API Key' },
    { key: 'base_url', label: 'Base URL', type: 'string', hint: 'API 基础地址' },
    { key: 'model_name', label: '模型名称', type: 'string', hint: 'Embedding 模型名称' },
    { key: 'dimensions', label: '向量维度', type: 'number', min: 64, max: 4096, hint: 'Embedding 向量维度' },
    { key: 'enable_cache', label: '启用缓存', type: 'boolean', hint: '是否启用 Embedding 缓存' },
    { key: 'use_dimensions', label: '使用自定义维度', type: 'boolean', hint: '是否使用自定义向量维度' },
    { key: 'max_cache_size', label: '最大缓存条数', type: 'number', min: 0, max: 100000, hint: 'Embedding 缓存最大条数' },
    { key: 'max_input_length', label: '最大输入长度', type: 'number', min: 1, max: 100000, hint: '单次最大输入 token 数' },
    { key: 'max_batch_size', label: '最大批次大小', type: 'number', min: 1, max: 100, hint: '每批最大 embedding 条数' },
  ],
  runtime_basic: [
    { key: 'max_iters', label: '最大迭代次数', type: 'number', min: 1, max: 1000, hint: 'ReAct Agent 最大推理-执行迭代次数' },
    { key: 'auto_continue_on_text_only', label: '纯文本自动继续', type: 'boolean', hint: '模型返回纯文本时自动继续执行' },
    { key: 'max_input_length', label: '最大输入长度', type: 'number', min: 1000, max: 1000000, hint: '模型上下文窗口最大 token 数' },
    { key: 'history_max_length', label: '历史最大长度', type: 'number', min: 1000, max: 100000, hint: '/history 命令输出的最大 token 数' },
  ],
}

// ============ State ============

const isLoading = ref(false)
const isLoadingHistory = ref(false)
const isSaving = ref(false)
const configs = ref<Record<string, any>>({})
const history = ref<any[]>([])
const historyTotal = ref(0)
const historyPage = ref(1)
const historyPageSize = 10
const selectedCategory = ref('llm_retry')
const changeReason = ref('')

const detailModal = ref({
  visible: false,
  data: null as any
})

// 当前选中的配置，解析后的对象
const currentFormData = computed(() => {
  return configs.value[selectedCategory.value] || null
})

const currentConfigInfo = computed(() => configInfoMap[selectedCategory.value])
const currentFields = computed(() => fieldDefinitions[selectedCategory.value] || [])

// ============ Methods ============

function selectCategory(key: string) {
  selectedCategory.value = key
  historyPage.value = 1
  loadHistory()
}

async function loadData() {
  isLoading.value = true
  try {
    const res = await agentRuntimeConfigApi.getAll()
    const list = Array.isArray(res) ? res : (res?.data || [])
    // 转为 { key -> parsedValue } 的 Map
    const map: Record<string, any> = {}
    for (const item of list) {
      map[item.configKey] = JSON.parse(item.configValue)
    }
    configs.value = map
  } catch (e: any) {
    desktop.addToast(e.message || '加载配置失败', 'error')
  } finally {
    isLoading.value = false
  }
}

async function loadHistory() {
  isLoadingHistory.value = true
  try {
    const res = await agentRuntimeConfigApi.getHistory({
      configKey: selectedCategory.value,
      pageNum: historyPage.value,
      pageSize: historyPageSize
    })
    history.value = res?.records || []
    historyTotal.value = res?.total || 0
  } catch (e: any) {
    desktop.addToast(e.message || '加载历史记录失败', 'error')
  } finally {
    isLoadingHistory.value = false
  }
}

async function saveConfig() {
  if (!currentFormData.value) return

  isSaving.value = true
  try {
    await agentRuntimeConfigApi.update(selectedCategory.value, {
      configValue: JSON.stringify(currentFormData.value),
      changeReason: changeReason.value || undefined
    })
    desktop.addToast('保存成功', 'success')
    changeReason.value = ''
    loadData()
    loadHistory()
  } catch (e: any) {
    desktop.addToast(e.message || '保存失败', 'error')
  } finally {
    isSaving.value = false
  }
}

function viewDetail(item: any) {
  detailModal.value = { visible: true, data: item }
}

function formatJson(jsonStr: string | null): string {
  if (!jsonStr) return '(空)'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

function formatTime(timeStr: string | null): string {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
  loadHistory()
})
</script>

<style scoped>
.agent-runtime-config-app {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon { color: var(--text-secondary); }
.header-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }

.app-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* Categories Panel */
.config-categories-panel {
  width: 220px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.01);
}

.categories-list { flex: 1; overflow-y: auto; padding: 8px; }

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  margin-bottom: 4px;
}

.category-item:hover { background: rgba(255, 255, 255, 0.05); color: var(--text-primary); }

.category-item.active {
  background: rgba(0, 245, 255, 0.1);
  color: #00f5ff;
  border: 1px solid rgba(0, 245, 255, 0.2);
}

.category-name { font-size: 13px; }

/* Form Panel */
.config-form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.panel-title { font-size: 14px; font-weight: 500; color: var(--text-primary); }

.form-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.form-description {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
}

.form-table { margin-bottom: 16px; }

.form-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  gap: 12px;
}

.form-label-col {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.form-label { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.form-key { font-size: 11px; color: var(--text-disabled); font-family: monospace; }

.form-input-col { flex: 1; min-width: 0; }

.form-hint-col { width: 160px; flex-shrink: 0; }
.form-hint { font-size: 11px; color: var(--text-disabled); }

.change-reason-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
}

.form-label-inline { font-size: 13px; color: var(--text-secondary); width: 80px; flex-shrink: 0; }

/* Inputs */
.form-input {
  width: 100%;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
  transition: border-color 0.2s;
}

.form-input:focus { outline: none; border-color: #00f5ff; }

.form-textarea {
  width: 100%;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
  resize: vertical;
  font-family: monospace;
}

.form-textarea:focus { outline: none; border-color: #00f5ff; }

/* Switch */
.switch-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.switch-wrap input { opacity: 0; width: 0; height: 0; position: absolute; }

.switch-slider {
  width: 40px;
  height: 22px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 11px;
  transition: 0.3s;
  position: relative;
}

.switch-slider::before {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #888;
  top: 3px;
  left: 3px;
  transition: 0.3s;
}

.switch-wrap input:checked + .switch-slider { background: rgba(0, 245, 255, 0.3); }
.switch-wrap input:checked + .switch-slider::before { background: #00f5ff; transform: translateX(18px); }

/* History Section */
.history-section {
  border-top: 1px solid var(--border-subtle);
  padding: 0;
  max-height: 280px;
  display: flex;
  flex-direction: column;
}

.history-section .panel-header { padding: 12px 20px; }

.history-table {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.history-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.history-table th {
  text-align: left;
  padding: 8px 10px;
  color: var(--text-disabled);
  border-bottom: 1px solid var(--border-subtle);
  font-weight: 500;
}

.history-table td {
  padding: 8px 10px;
  color: var(--text-secondary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.history-table tbody tr:hover td { background: rgba(255, 255, 255, 0.02); }

.empty-cell { text-align: center; color: var(--text-disabled); padding: 20px !important; }

.history-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  border-top: 1px solid var(--border-subtle);
}

.pagination-info { font-size: 12px; color: var(--text-secondary); }

/* Detail Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.modal {
  width: 700px;
  max-height: 80vh;
  background: rgba(20, 20, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title { font-size: 15px; font-weight: 500; color: var(--text-primary); }

.modal-body { flex: 1; padding: 20px; overflow-y: auto; }

.detail-meta {
  display: flex;
  gap: 24px;
  font-size: 12px;
  color: var(--text-disabled);
  margin-bottom: 16px;
}

.diff-view {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.diff-section { background: rgba(0, 0, 0, 0.3); border-radius: 8px; padding: 12px; }

.diff-label { font-size: 11px; color: var(--text-disabled); margin-bottom: 8px; }

.diff-section pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  color: #00f5ff;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn-primary {
  background: linear-gradient(135deg, #00f5ff, #00c7be);
  color: #000;
}

.btn-primary:hover { box-shadow: 0 0 20px rgba(0, 245, 255, 0.4); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-outline {
  background: transparent;
  border-color: var(--border-subtle);
  color: var(--text-secondary);
}

.btn-outline:hover { border-color: rgba(0, 245, 255, 0.5); color: #00f5ff; }

.btn-icon {
  padding: 6px;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-icon:hover { background: rgba(255, 255, 255, 0.1); color: var(--text-primary); }

.btn-sm { padding: 6px 12px; font-size: 12px; }
.btn-xs { padding: 3px 8px; font-size: 11px; }

/* Spin */
.spin { animation: spin 1s linear infinite; }

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
