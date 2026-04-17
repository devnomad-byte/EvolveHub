<template>
  <div class="skill-file-panel">
    <div class="file-toolbar">
      <button class="btn btn-sm" @click="refreshFiles"><RefreshCw :size="14" /> 刷新</button>
      <button class="btn btn-sm" @click="showUploadDialog = true"><Upload :size="14" /> 上传</button>
      <button class="btn btn-sm" @click="showNewFolderDialog = true"><FolderPlus :size="14" /> 新建文件夹</button>
    </div>

    <div class="file-tree">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="files.length === 0" class="empty"><Folder :size="32" /><span>暂无文件</span></div>
      <FileTreeNode v-else v-for="node in files" :key="node.path" :node="node" :depth="0" @select="selectFile" @delete="handleDelete" />
    </div>

    <!-- 上传对话框 -->
    <div v-if="showUploadDialog" class="modal-overlay" @click.self="showUploadDialog = false">
      <div class="modal">
        <div class="modal-header"><h3>上传文件</h3><button class="modal-close" @click="showUploadDialog = false"><X :size="18" /></button></div>
        <div class="modal-body">
          <div class="form-group"><label>目标路径</label><input v-model="uploadPath" type="text" placeholder="留空表示根目录" /></div>
          <div class="form-group"><label>选择文件</label><input type="file" @change="handleFileSelect" /></div>
          <div v-if="selectedFile" class="file-info">{{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showUploadDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="!selectedFile || uploading" @click="uploadFile">{{ uploading ? '上传中...' : '上传' }}</button>
        </div>
      </div>
    </div>

    <!-- 新建文件夹对话框 -->
    <div v-if="showNewFolderDialog" class="modal-overlay" @click.self="showNewFolderDialog = false">
      <div class="modal">
        <div class="modal-header"><h3>新建文件夹</h3><button class="modal-close" @click="showNewFolderDialog = false"><X :size="18" /></button></div>
        <div class="modal-body">
          <div class="form-group"><label>文件夹名称</label><input v-model="newFolderName" type="text" placeholder="例如: references" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showNewFolderDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="!newFolderName" @click="createFolder">创建</button>
        </div>
      </div>
    </div>

    <!-- 安全扫描报告 -->
    <ScanReportDialog
      :visible="showScanReport"
      :result="scanResult"
      @close="showScanReport = false"
      @force-proceed="handleForceProceedUpload"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RefreshCw, Upload, FolderPlus, Folder, X } from 'lucide-vue-next'
import { adminSkillFileApi, type FileNode } from '@/api/adminSkillFile'
import ScanReportDialog from '@/components/common/ScanReportDialog.vue'
import type { ScanResult } from '@/components/common/ScanReportDialog.vue'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

const props = defineProps<{ skillId: number }>()

const files = ref<FileNode[]>([])
const loading = ref(false)
const showUploadDialog = ref(false)
const showNewFolderDialog = ref(false)
const uploadPath = ref('')
const newFolderName = ref('')
const selectedFile = ref<File | null>(null)
const uploading = ref(false)

// Scan report state
const showScanReport = ref(false)
const scanResult = ref<ScanResult | null>(null)

async function loadFiles() {
  loading.value = true
  try {
    files.value = await adminSkillFileApi.listFiles(props.skillId)
  } catch (e) {
    console.error('加载文件列表失败', e)
  } finally {
    loading.value = false
  }
}

async function refreshFiles() { await loadFiles() }
function selectFile(_node: FileNode) {}

async function handleDelete(node: FileNode) {
  if (!await confirm('删除文件', `确定删除 "${node.name}"？此操作不可恢复。`)) return
  try {
    await adminSkillFileApi.deleteFile(props.skillId, node.path)
    await loadFiles()
  } catch (e) {
    console.error('删除失败', e)
    desktop.addToast('删除失败', 'error')
  }
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.length) selectedFile.value = input.files[0]
}

// 记录上传参数，用于 forceProceed 时重试
let pendingUploadFile: File | null = null
let pendingUploadPath = ''

async function uploadFile() {
  if (!selectedFile.value) return
  uploading.value = true
  pendingUploadFile = selectedFile.value
  pendingUploadPath = uploadPath.value
  try {
    await adminSkillFileApi.uploadFile(props.skillId, uploadPath.value, selectedFile.value)
    showUploadDialog.value = false
    uploadPath.value = ''
    selectedFile.value = null
    await loadFiles()
  } catch (e: any) {
    if (e.code === 4001 && e.data) {
      showUploadDialog.value = false
      scanResult.value = e.data
      showScanReport.value = true
    } else {
      console.error('上传失败', e)
      desktop.addToast('上传失败', 'error')
    }
  } finally {
    uploading.value = false
  }
}

async function createFolder() {
  if (!newFolderName.value) return
  try {
    await adminSkillFileApi.createFolder(props.skillId, newFolderName.value + '/')
    showNewFolderDialog.value = false
    newFolderName.value = ''
    await loadFiles()
  } catch (e) {
    console.error('创建文件夹失败', e)
    desktop.addToast('创建文件夹失败', 'error')
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function handleForceProceedUpload() {
  showScanReport.value = false
  if (!pendingUploadFile) return
  uploading.value = true
  try {
    await adminSkillFileApi.uploadFile(props.skillId, pendingUploadPath, pendingUploadFile, true)
    showUploadDialog.value = false
    uploadPath.value = ''
    selectedFile.value = null
    await loadFiles()
  } catch (e: any) {
    console.error('上传失败', e)
    desktop.addToast('上传失败', 'error')
  } finally {
    uploading.value = false
    pendingUploadFile = null
    pendingUploadPath = ''
  }
}

onMounted(() => { loadFiles() })
</script>

<style scoped>
.skill-file-panel { display: flex; flex-direction: column; height: 100%; }
.file-toolbar { display: flex; gap: 8px; padding: 12px; border-bottom: 1px solid var(--border-subtle); }
.file-tree { flex: 1; overflow-y: auto; padding: 8px; }
.loading, .empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; color: var(--text-disabled); gap: 12px; }
.file-info { font-size: 12px; color: var(--text-secondary); margin-top: 8px; }
</style>
