<template>
  <div class="s3-app">
    <!-- Header -->
    <div class="s3-header">
      <div class="s3-header-left">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="s3-title">S3 文件管理</span>
      </div>
      <div class="s3-header-right">
        <div v-if="currentBucket" class="s3-bucket-info">
          <span class="s3-info-item">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><polyline points="13 2 13 9 20 9"/></svg>
            {{ currentBucket.fileCount }} 文件
          </span>
          <span class="s3-info-item">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
            {{ formatSize(currentBucket.totalSize) }}
          </span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="s3-main">
      <!-- Left: Bucket List -->
      <div class="s3-bucket-panel">
        <div class="s3-panel-header">存储桶</div>
        <div class="s3-bucket-list">
          <div v-if="bucketsLoading" class="s3-loading">加载中...</div>
          <template v-else>
            <div
              v-for="bucket in buckets"
              :key="bucket.name"
              class="s3-bucket-item"
              :class="{ active: currentBucket?.name === bucket.name }"
              @click="selectBucket(bucket)"
            >
              <div class="s3-bucket-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                </svg>
              </div>
              <div class="s3-bucket-info">
                <div class="s3-bucket-name">{{ bucket.name }}</div>
                <div class="s3-bucket-meta">{{ bucket.fileCount }} 文件</div>
              </div>
            </div>
            <div v-if="!buckets?.length && !bucketsLoading" class="s3-empty">暂无存储桶</div>
          </template>
        </div>
      </div>

      <!-- Right: File Browser -->
      <div class="s3-file-panel">
        <template v-if="currentBucket">
          <!-- Toolbar -->
          <div class="s3-toolbar">
            <!-- Path Breadcrumb -->
            <div class="s3-breadcrumb">
              <span class="s3-breadcrumb-item" @click="navigateTo('')">根目录</span>
              <template v-for="(part, index) in pathParts" :key="index">
                <span class="s3-breadcrumb-sep">/</span>
                <span class="s3-breadcrumb-item" @click="navigateTo(getPathPrefix(index))">{{ part }}</span>
              </template>
            </div>
            <div class="s3-toolbar-actions">
              <!-- Upload -->
              <label class="s3-action-btn" title="上传文件">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
                <input type="file" class="s3-file-input" @change="handleUpload" multiple />
              </label>
              <!-- New Folder -->
              <button class="s3-action-btn" @click="showNewFolderDialog = true" title="新建文件夹">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/><line x1="12" y1="11" x2="12" y2="17"/><line x1="9" y1="14" x2="15" y2="14"/>
                </svg>
              </button>
              <!-- Refresh -->
              <button class="s3-action-btn" @click="loadFiles" title="刷新">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- File List -->
          <div class="s3-file-list">
            <div v-if="filesLoading" class="s3-loading">加载中...</div>
            <template v-else>
              <div
                v-for="file in files"
                :key="file.path"
                class="s3-file-item"
                :class="{ selected: selectedFile?.path === file.path }"
                @click="selectFile(file)"
                @dblclick="openFile(file)"
              >
                <div class="s3-file-icon" :class="file.type">
                  <svg v-if="file.type === 'folder'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                  </svg>
                  <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><polyline points="13 2 13 9 20 9"/>
                  </svg>
                </div>
                <div class="s3-file-details">
                  <div class="s3-file-name" :title="file.name">{{ file.name }}</div>
                  <div class="s3-file-meta">
                    <span v-if="file.type === 'file'">{{ formatSize(file.size) }}</span>
                    <span v-if="file.modifiedTime">{{ file.modifiedTime }}</span>
                  </div>
                </div>
                <div class="s3-file-actions">
                  <button v-if="selectedFile?.path === file.path" class="s3-file-action-btn" @click.stop="downloadFile(file)" title="下载">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                  </button>
                  <button v-if="selectedFile?.path === file.path" class="s3-file-action-btn" @click.stop="startRename(file)" title="重命名">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/>
                    </svg>
                  </button>
                  <button v-if="selectedFile?.path === file.path" class="s3-file-action-btn delete" @click.stop="confirmDelete(file)" title="删除">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                  </button>
                </div>
              </div>
              <div v-if="!files?.length && !filesLoading" class="s3-empty">此目录为空</div>
            </template>
          </div>
        </template>
        <template v-else>
          <div class="s3-empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            </svg>
            <p>请选择一个存储桶</p>
          </div>
        </template>
      </div>
    </div>

    <!-- New Folder Dialog -->
    <div v-if="showNewFolderDialog" class="s3-dialog-overlay" @click.self="showNewFolderDialog = false">
      <div class="s3-dialog">
        <div class="s3-dialog-header">新建文件夹</div>
        <div class="s3-dialog-body">
          <input v-model="newFolderName" type="text" class="s3-dialog-input" placeholder="文件夹名称" @keyup.enter="createFolder" />
        </div>
        <div class="s3-dialog-footer">
          <button class="s3-dialog-btn" @click="showNewFolderDialog = false">取消</button>
          <button class="s3-dialog-btn primary" @click="createFolder">创建</button>
        </div>
      </div>
    </div>

    <!-- Rename Dialog -->
    <div v-if="showRenameDialog" class="s3-dialog-overlay" @click.self="showRenameDialog = false">
      <div class="s3-dialog">
        <div class="s3-dialog-header">重命名</div>
        <div class="s3-dialog-body">
          <input v-model="renameTargetName" type="text" class="s3-dialog-input" placeholder="新名称" @keyup.enter="executeRename" />
        </div>
        <div class="s3-dialog-footer">
          <button class="s3-dialog-btn" @click="showRenameDialog = false">取消</button>
          <button class="s3-dialog-btn primary" @click="executeRename">确定</button>
        </div>
      </div>
    </div>

    <!-- Delete Confirm Dialog -->
    <div v-if="showDeleteDialog" class="s3-dialog-overlay" @click.self="showDeleteDialog = false">
      <div class="s3-dialog">
        <div class="s3-dialog-header">确认删除</div>
        <div class="s3-dialog-body">
          <p class="s3-dialog-message">确定要删除 {{ deleteTarget?.type === 'folder' ? '文件夹' : '文件' }} "{{ deleteTarget?.name }}" 吗？</p>
          <p v-if="deleteTarget?.type === 'folder'" class="s3-dialog-warning">文件夹内的所有内容也将被删除</p>
        </div>
        <div class="s3-dialog-footer">
          <button class="s3-dialog-btn" @click="showDeleteDialog = false">取消</button>
          <button class="s3-dialog-btn danger" @click="executeDelete">删除</button>
        </div>
      </div>
    </div>

    <!-- Upload Progress -->
    <div v-if="uploadingCount > 0" class="s3-upload-indicator">
      <svg class="s3-spin" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"/><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"/><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"/>
      </svg>
      <span>上传中... {{ uploadingCount }} 个文件</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { s3BrowserApi, type S3Bucket, type S3File } from '@/api/s3Browser'

// State
const buckets = ref<S3Bucket[]>([])
const currentBucket = ref<S3Bucket | null>(null)
const currentPath = ref('')
const files = ref<S3File[]>([])
const selectedFile = ref<S3File | null>(null)

const bucketsLoading = ref(false)
const filesLoading = ref(false)
const uploadingCount = ref(0)

// Dialogs
const showNewFolderDialog = ref(false)
const newFolderName = ref('')
const showRenameDialog = ref(false)
const renameTargetName = ref('')
const renameTarget = ref<S3File | null>(null)
const showDeleteDialog = ref(false)
const deleteTarget = ref<S3File | null>(null)

// Computed
const pathParts = computed(() => {
  if (!currentPath.value) return []
  return currentPath.value.split('/').filter(p => p)
})

// Methods
function formatSize(bytes: number | string): string {
  const num = typeof bytes === 'string' ? parseInt(bytes, 10) : bytes
  if (isNaN(num) || num === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(num) / Math.log(k))
  return parseFloat((num / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

async function loadBuckets() {
  bucketsLoading.value = true
  try {
    buckets.value = await s3BrowserApi.getBuckets()
  } catch (e) {
    console.error('加载桶列表失败', e)
  } finally {
    bucketsLoading.value = false
  }
}

function selectBucket(bucket: S3Bucket) {
  currentBucket.value = bucket
  currentPath.value = ''
  selectedFile.value = null
  loadFiles()
}

function navigateTo(path: string) {
  currentPath.value = path
  selectedFile.value = null
  loadFiles()
}

function getPathPrefix(index: number): string {
  return pathParts.value.slice(0, index + 1).join('/')
}

async function loadFiles() {
  if (!currentBucket.value) return
  filesLoading.value = true
  try {
    files.value = await s3BrowserApi.getFiles(currentBucket.value.name, currentPath.value)
  } catch (e) {
    console.error('加载文件列表失败', e)
  } finally {
    filesLoading.value = false
  }
}

function selectFile(file: S3File) {
  selectedFile.value = file
}

function openFile(file: S3File) {
  if (file.type === 'folder') {
    currentPath.value = file.path
    selectedFile.value = null
    loadFiles()
  }
}

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length || !currentBucket.value) return

  uploadingCount.value = input.files.length

  for (const file of input.files) {
    try {
      await s3BrowserApi.uploadFile(currentBucket.value.name, currentPath.value, file)
    } catch (e) {
      console.error('上传失败', file.name, e)
    }
  }

  uploadingCount.value = 0
  input.value = ''
  loadFiles()
}

async function createFolder() {
  if (!newFolderName.value.trim() || !currentBucket.value) return
  try {
    await s3BrowserApi.createFolder(currentBucket.value.name, currentPath.value, newFolderName.value.trim())
    showNewFolderDialog.value = false
    newFolderName.value = ''
    loadFiles()
  } catch (e) {
    console.error('创建文件夹失败', e)
  }
}

function startRename(file: S3File) {
  renameTarget.value = file
  // 对于文件夹，移除末尾的 /
  renameTargetName.value = file.type === 'folder' ? file.name.replace(/\/$/, '') : file.name
  showRenameDialog.value = true
}

async function executeRename() {
  if (!renameTargetName.value.trim() || !currentBucket.value || !renameTarget.value) return
  try {
    await s3BrowserApi.rename(currentBucket.value.name, renameTarget.value.path, renameTargetName.value.trim())
    showRenameDialog.value = false
    renameTarget.value = null
    renameTargetName.value = ''
    loadFiles()
  } catch (e) {
    console.error('重命名失败', e)
  }
}

function confirmDelete(file: S3File) {
  deleteTarget.value = file
  showDeleteDialog.value = true
}

async function executeDelete() {
  if (!deleteTarget.value || !currentBucket.value) return
  try {
    await s3BrowserApi.deleteFile(currentBucket.value.name, deleteTarget.value.path)
    showDeleteDialog.value = false
    deleteTarget.value = null
    selectedFile.value = null
    loadFiles()
  } catch (e) {
    console.error('删除失败', e)
  }
}

async function downloadFile(file: S3File) {
  if (!currentBucket.value || file.type === 'folder') return
  try {
    const blob = await s3BrowserApi.downloadFile(currentBucket.value.name, file.path)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = file.name
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error('下载失败', e)
  }
}

// Lifecycle
onMounted(() => {
  loadBuckets()
})
</script>

<style scoped>
.s3-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

/* Header */
.s3-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.s3-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.s3-title {
  font-size: 14px;
  font-weight: 600;
}

.s3-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.s3-bucket-info {
  display: flex;
  gap: 12px;
}

.s3-info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-secondary);
}

/* Main */
.s3-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* Bucket Panel */
.s3-bucket-panel {
  width: 200px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.s3-panel-header {
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.s3-bucket-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.s3-bucket-list::-webkit-scrollbar { width: 3px; }
.s3-bucket-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.s3-bucket-item {
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

.s3-bucket-item:hover { background: rgba(255, 255, 255, 0.04); }
.s3-bucket-item.active { background: rgba(10, 132, 255, 0.1); border-color: rgba(10, 132, 255, 0.25); }

.s3-bucket-icon {
  color: #FF9F0A;
  flex-shrink: 0;
}

.s3-bucket-name {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.s3-bucket-meta {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* File Panel */
.s3-file-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Toolbar */
.s3-toolbar {
  padding: 10px 16px;
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.s3-breadcrumb {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.s3-breadcrumb-item {
  color: var(--text-secondary);
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 4px;
}

.s3-breadcrumb-item:hover { color: #0A84FF; background: rgba(10, 132, 255, 0.1); }
.s3-breadcrumb-sep { color: var(--text-disabled); }

.s3-toolbar-actions {
  display: flex;
  gap: 4px;
}

.s3-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  transition: all 0.15s;
}

.s3-action-btn:hover { color: var(--text-primary); background: rgba(255, 255, 255, 0.06); }

.s3-file-input {
  display: none;
}

/* File List */
.s3-file-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  align-content: start;
}

.s3-file-list::-webkit-scrollbar { width: 3px; }
.s3-file-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.s3-file-item {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.s3-file-item:hover { background: rgba(255, 255, 255, 0.04); }
.s3-file-item.selected { background: rgba(10, 132, 255, 0.1); border-color: rgba(10, 132, 255, 0.25); }

.s3-file-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
}

.s3-file-icon.folder { color: #FF9F0A; background: rgba(255, 159, 10, 0.1); }
.s3-file-icon.file { color: #64D2FF; }

.s3-file-details {
  width: 100%;
  text-align: center;
}

.s3-file-name {
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.s3-file-meta {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 2px;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.s3-file-actions {
  display: none;
  gap: 4px;
}

.s3-file-item.selected .s3-file-actions {
  display: flex;
}

.s3-file-action-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.06);
  border: none;
  transition: all 0.15s;
}

.s3-file-action-btn:hover { color: var(--text-primary); background: rgba(255, 255, 255, 0.1); }
.s3-file-action-btn.delete:hover { color: #FF453A; }

/* Empty States */
.s3-empty, .s3-loading {
  padding: 20px;
  text-align: center;
  color: var(--text-disabled);
  font-size: 12px;
}

.s3-empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-disabled);
}

.s3-empty-state svg { opacity: 0.3; }
.s3-empty-state p { font-size: 13px; }

/* Dialog */
.s3-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.s3-dialog {
  background: var(--bg-glass);
  backdrop-filter: blur(40px);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  width: 320px;
  overflow: hidden;
}

.s3-dialog-header {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-subtle);
}

.s3-dialog-body {
  padding: 16px;
}

.s3-dialog-input {
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
  box-sizing: border-box;
}

.s3-dialog-input:focus { border-color: rgba(10, 132, 255, 0.3); }

.s3-dialog-message {
  font-size: 13px;
  margin: 0;
}

.s3-dialog-warning {
  font-size: 11px;
  color: #FF9F0A;
  margin-top: 6px;
}

.s3-dialog-footer {
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid var(--border-subtle);
}

.s3-dialog-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-secondary);
  transition: all 0.15s;
}

.s3-dialog-btn:hover { background: rgba(255, 255, 255, 0.1); color: var(--text-primary); }
.s3-dialog-btn.primary { background: #0A84FF; color: #fff; }
.s3-dialog-btn.primary:hover { background: #0070E0; }
.s3-dialog-btn.danger { background: rgba(255, 69, 58, 0.15); color: #FF453A; }
.s3-dialog-btn.danger:hover { background: rgba(255, 69, 58, 0.25); }

/* Upload Indicator */
.s3-upload-indicator {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 16px;
  background: var(--bg-glass);
  backdrop-filter: blur(40px);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  z-index: 100;
}

.s3-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
