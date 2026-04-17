<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal hub-dialog">
      <div class="modal-header">
        <h3>从 Hub 安装技能</h3>
        <button class="modal-close" @click="$emit('close')"><X :size="18" /></button>
      </div>

      <div class="modal-body">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索技能..."
            @keyup.enter="doSearch"
          />
          <button class="btn btn-primary" @click="doSearch">搜索</button>
        </div>

        <!-- Hub 选择 -->
        <div class="hub-filter">
          <label :class="{ active: !selectedHub }" @click="selectHub('')">
            <input type="radio" v-model="selectedHub" value="" /> 全部
          </label>
          <label v-for="hub in hubs" :key="hub" :class="{ active: selectedHub === hub }" @click="selectHub(hub)">
            <input type="radio" v-model="selectedHub" :value="hub" /> {{ hub }}
          </label>
        </div>

        <!-- URL 输入模式 (ModelScope 等不支持列表的 Hub) -->
        <div v-if="selectedHub === 'ModelScope'" class="url-input-mode">
          <div class="url-hint">
            <Link :size="16" />
            <span>请输入 ModelScope 技能 URL，例如：</span>
          </div>
          <div class="url-example">https://modelscope.cn/skills/@owner/skill-name</div>
          <input
            v-model="directUrl"
            type="text"
            class="url-input"
            placeholder="粘贴 ModelScope 技能 URL..."
          />
          <button class="btn btn-primary btn-block" @click="installFromUrl" :disabled="!directUrl">
            安装
          </button>
        </div>

        <!-- 结果区域 -->
        <div v-else class="results-area">
          <!-- 热门推荐 -->
          <div v-if="!keyword && results.length > 0" class="section-header">
            <Sparkles :size="14" /> 热门推荐
          </div>

          <!-- 搜索结果 -->
          <div class="search-results">
            <div v-if="loading" class="loading">
              <div class="spinner"></div>
              <span>加载中...</span>
            </div>
            <div v-else-if="results.length === 0 && keyword" class="empty">
              <Search :size="32" />
              <span>搜索 "{{ keyword }}" 没有找到结果</span>
              <button class="btn btn-sm" @click="loadRecommendations">查看推荐</button>
            </div>
            <div v-else-if="results.length === 0 && !keyword" class="empty">
              <Sparkles :size="32" />
              <span>暂无推荐内容</span>
              <button class="btn btn-sm" @click="loadRecommendations">刷新推荐</button>
            </div>
            <div v-else class="result-list">
              <div v-for="item in results" :key="item.bundleUrl" class="result-item">
                <div class="result-icon" :style="{ background: getHubGradient(item.hubName) }">
                  <Sparkles :size="20" />
                </div>
                <div class="result-info">
                  <div class="result-name">{{ item.name }}</div>
                  <div class="result-meta">
                    <span class="hub-badge" :style="{ background: getHubColor(item.hubName) }">{{ item.hubName }}</span>
                    <span v-if="item.author">👤 {{ item.author }}</span>
                    <span v-if="item.downloads">⬇️ {{ formatDownloads(item.downloads) }}</span>
                    <span v-if="item.version">v{{ item.version }}</span>
                  </div>
                  <div v-if="item.description" class="result-desc">{{ item.description }}</div>
                  <div v-if="item.tags?.length" class="result-tags">
                    <span v-for="tag in item.tags.slice(0, 5)" :key="tag" class="tag">{{ tag }}</span>
                  </div>
                </div>
                <div class="result-actions">
                  <button class="btn btn-sm btn-primary" @click="install(item)">安装</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 安全扫描报告 -->
    <ScanReportDialog
      :visible="showScanReport"
      :result="scanResult"
      @close="showScanReport = false"
      @force-proceed="handleForceProceedInstall"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { X, Sparkles, Search, Link } from 'lucide-vue-next'
import { adminSkillHubApi, type HubSearchResult } from '@/api/adminSkillHub'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'
import ScanReportDialog from '@/components/common/ScanReportDialog.vue'
import type { ScanResult } from '@/components/common/ScanReportDialog.vue'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

const emit = defineEmits<{
  close: []
  installed: [skillId: number]
}>()

const keyword = ref('')
const selectedHub = ref('')
const hubs = ref<string[]>([])
const results = ref<HubSearchResult[]>([])
const loading = ref(false)
const directUrl = ref('')

// Scan report state
const showScanReport = ref(false)
const scanResult = ref<ScanResult | null>(null)

// Pending install params for forceProceed retry
let pendingHubName = ''
let pendingBundleUrl = ''

const hubStyles: Record<string, { gradient: string; color: string }> = {
  ClawHub: { gradient: 'linear-gradient(135deg, #667eea, #764ba2)', color: 'rgba(102, 126, 234, 0.3)' },
  ModelScope: { gradient: 'linear-gradient(135deg, #11998e, #38ef7d)', color: 'rgba(56, 239, 125, 0.3)' },
  LobeHub: { gradient: 'linear-gradient(135deg, #ff6b6b, #ffd93d)', color: 'rgba(255, 217, 61, 0.3)' },
  GitHub: { gradient: 'linear-gradient(135deg, #333, #666)', color: 'rgba(102, 102, 102, 0.3)' }
}

function getHubGradient(hubName: string) {
  return hubStyles[hubName]?.gradient || 'linear-gradient(135deg, #667eea, #764ba2)'
}

function getHubColor(hubName: string) {
  return hubStyles[hubName]?.color || 'rgba(102, 126, 234, 0.3)'
}

function formatDownloads(n: number) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n.toString()
}

onMounted(async () => {
  try {
    hubs.value = await adminSkillHubApi.getAdapters()
  } catch (e) {
    console.error('获取 Hub 列表失败', e)
  }
  // 默认加载推荐
  loadRecommendations()
})

function selectHub(hub: string) {
  selectedHub.value = hub
  keyword.value = ''
  results.value = []
  directUrl.value = ''
  if (hub !== 'ModelScope') {
    doSearch()
  }
}

async function doSearch() {
  loading.value = true
  try {
    results.value = await adminSkillHubApi.search(keyword.value || 'popular', selectedHub.value || undefined)
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

async function loadRecommendations() {
  keyword.value = ''
  await doSearch()
}

async function install(item: HubSearchResult) {
  if (!await confirm('安装技能', `确定安装 "${item.name}" 吗？`, { danger: false, confirmText: '安装' })) return
  loading.value = true
  pendingHubName = item.hubName
  pendingBundleUrl = item.bundleUrl
  try {
    const skillId = await adminSkillHubApi.install(item.hubName, item.bundleUrl)
    emit('installed', skillId)
    emit('close')
  } catch (e: any) {
    if (e.code === 4001 && e.data) {
      scanResult.value = e.data
      showScanReport.value = true
    } else {
      console.error('安装失败', e)
      desktop.addToast('安装失败', 'error')
    }
  } finally {
    loading.value = false
  }
}

async function installFromUrl() {
  if (!directUrl.value) return
  if (!await confirm('安装技能', '确定安装此技能吗？', { danger: false, confirmText: '安装' })) return
  loading.value = true
  pendingHubName = 'ModelScope'
  pendingBundleUrl = directUrl.value
  try {
    const skillId = await adminSkillHubApi.install('ModelScope', directUrl.value)
    emit('installed', skillId)
    emit('close')
  } catch (e: any) {
    if (e.code === 4001 && e.data) {
      scanResult.value = e.data
      showScanReport.value = true
    } else {
      console.error('安装失败', e)
      desktop.addToast('安装失败', 'error')
    }
  } finally {
    loading.value = false
  }
}

async function handleForceProceedInstall() {
  showScanReport.value = false
  loading.value = true
  try {
    const skillId = await adminSkillHubApi.install(pendingHubName, pendingBundleUrl, true)
    emit('installed', skillId)
    emit('close')
  } catch (e: any) {
    console.error('安装失败', e)
    desktop.addToast('安装失败', 'error')
  } finally {
    loading.value = false
    pendingHubName = ''
    pendingBundleUrl = ''
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1E1E22;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  width: 680px;
  max-width: 95%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-bar input {
  flex: 1;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  color: var(--text-primary);
  font-size: 14px;
}

.search-bar input:focus {
  outline: none;
  border-color: #0A84FF;
}

.hub-filter {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.hub-filter label {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.15s;
}

.hub-filter label input {
  display: none;
}

.hub-filter label:hover {
  background: rgba(255, 255, 255, 0.1);
}

.hub-filter label.active {
  background: rgba(10, 132, 255, 0.2);
  color: #0A84FF;
}

/* URL 输入模式 */
.url-input-mode {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
}

.url-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.url-example {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-disabled);
  background: rgba(0, 0, 0, 0.3);
  padding: 8px 12px;
  border-radius: 6px;
}

.url-input {
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  color: var(--text-primary);
  font-size: 14px;
}

.url-input:focus {
  outline: none;
  border-color: #0A84FF;
}

.btn-block {
  width: 100%;
  justify-content: center;
}

.results-area {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 12px;
  text-transform: uppercase;
}

.search-results {
  flex: 1;
  overflow-y: auto;
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: var(--text-disabled);
  gap: 12px;
}

.loading .spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-subtle);
  border-top-color: #0A84FF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  transition: all 0.15s;
}

.result-item:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(10, 132, 255, 0.3);
}

.result-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  color: white;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.result-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--text-disabled);
  margin-bottom: 4px;
}

.hub-badge {
  padding: 2px 6px;
  border-radius: 4px;
  color: #fff;
}

.result-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  color: var(--text-secondary);
}

.result-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background: #0A84FF;
  color: white;
}

.btn-primary:hover {
  background: #0070E0;
}

.btn-primary:disabled {
  background: rgba(10, 132, 255, 0.3);
  cursor: not-allowed;
}
</style>
