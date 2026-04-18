<template>
  <div class="skill-editor-panel">
    <!-- 文件工具栏 -->
    <div class="file-toolbar">
      <button class="btn btn-sm" @click="loadFiles">
        <RefreshCw :size="14" :class="{ spinning: loading }" /> 刷新
      </button>
      <button class="btn btn-sm" @click="showUploadDialog = true">
        <Upload :size="14" /> 上传
      </button>
      <button class="btn btn-sm" @click="showNewFolderDialog = true">
        <FolderPlus :size="14" /> 新建文件夹
      </button>
      <span class="toolbar-divider"></span>
      <button class="btn btn-sm" :class="{ active: viewMode === 'edit' }" @click="viewMode = 'edit'">
        <FileText :size="14" /> 编辑
      </button>
      <button class="btn btn-sm" :class="{ active: viewMode === 'preview' }" @click="viewMode = 'preview'">
        <Eye :size="14" /> 预览
      </button>
    </div>

    <div class="editor-content">
      <!-- 文件树（左侧） -->
      <div class="file-sidebar">
        <div class="sidebar-header">文件</div>
        <div class="sidebar-tree">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="files.length === 0" class="empty">
            <Folder :size="24" />
            <span>暂无文件</span>
          </div>
          <FileTreeNode
            v-else
            v-for="node in files"
            :key="node.path"
            :node="node"
            :depth="0"
            @select="selectFile"
            @delete="handleDelete"
          />
        </div>
      </div>

      <!-- 编辑器/预览区（右侧） -->
      <div class="editor-main">
        <!-- 编辑视图 -->
        <div v-show="viewMode === 'edit'" class="edit-view" ref="editorContainer"></div>

        <!-- 预览视图 -->
        <div v-show="viewMode === 'preview'" class="preview-view">
          <div class="preview-content" v-html="renderedContent"></div>
        </div>
      </div>
    </div>

    <!-- 状态栏 -->
    <div class="editor-statusbar">
      <span>{{ currentFile || 'SKILL.md' }}</span>
      <span v-if="isDirty" class="dirty">● 未保存</span>
      <button class="btn btn-sm btn-primary" @click="() => saveContent()" :disabled="!isDirty">保存</button>
    </div>

    <!-- 上传对话框 -->
    <div v-if="showUploadDialog" class="modal-overlay" @click.self="showUploadDialog = false">
      <div class="modal">
        <div class="modal-header">
          <h3>上传文件</h3>
          <button class="modal-close" @click="showUploadDialog = false"><X :size="18" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>目标路径</label>
            <input v-model="uploadPath" type="text" placeholder="留空表示根目录" />
          </div>
          <div class="form-group">
            <label>选择文件</label>
            <input type="file" @change="handleFileSelect" />
          </div>
          <div v-if="selectedFile" class="file-info">{{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showUploadDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="!selectedFile || uploading" @click="uploadFile">
            {{ uploading ? '上传中...' : '上传' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 新建文件夹对话框 -->
    <div v-if="showNewFolderDialog" class="modal-overlay" @click.self="showNewFolderDialog = false">
      <div class="modal">
        <div class="modal-header">
          <h3>新建文件夹</h3>
          <button class="modal-close" @click="showNewFolderDialog = false"><X :size="18" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>文件夹名称</label>
            <input v-model="newFolderName" type="text" placeholder="例如: references" />
          </div>
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
      @force-proceed="handleForceProceed"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { RefreshCw, Upload, FolderPlus, Folder, X, FileText, Eye } from 'lucide-vue-next'
import { marked } from 'marked'
import * as monaco from 'monaco-editor'
import 'monaco-editor/esm/vs/editor/editor.all.js'
import '@/utils/monacoEnv'
import { getLanguageFromFilename } from '@/utils/monacoLanguages'
import { adminSkillFileApi, type FileNode } from '@/api/adminSkillFile'
import { adminSkillConfigApi } from '@/api/adminSkillConfig'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'
import ScanReportDialog from '@/components/common/ScanReportDialog.vue'
import type { ScanResult } from '@/components/common/ScanReportDialog.vue'
import FileTreeNode from './FileTreeNode.vue'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

const props = defineProps<{
  skillId: number
  skillName: string
}>()

const files = ref<FileNode[]>([])
const content = ref('')
const originalContent = ref('')
const isDirty = computed(() => content.value !== originalContent.value)
const currentFile = ref('')
const viewMode = ref<'edit' | 'preview'>('edit')
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

// Monaco editor refs
const editorContainer = ref<HTMLElement | null>(null)
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null

const renderedContent = computed(() => {
  if (!content.value) return ''
  return marked(content.value, { breaks: true })
})

// Define custom dark theme matching project colors
monaco.editor.defineTheme('evolvehub-dark', {
  base: 'vs-dark',
  inherit: true,
  rules: [],
  colors: {
    'editor.background': '#1C1C1E',
    'editor.lineHighlightBackground': '#2C2C2E',
    'editorLineNumber.foreground': '#636366',
    'editorLineNumber.activeForeground': '#A1A1A6',
    'editor.selectionBackground': '#264F78',
    'editorCursor.foreground': '#FFFFFF',
    'editorIndentGuide.background': '#2C2C2E',
    'editorIndentGuide.activeBackground': '#3A3A3C',
  }
})

function createEditor() {
  if (!editorContainer.value) return

  editorInstance = monaco.editor.create(editorContainer.value, {
    value: content.value,
    language: getLanguageFromFilename(currentFile.value || 'SKILL.md'),
    theme: 'evolvehub-dark',
    fontSize: 14,
    fontFamily: "'Monaco', 'Menlo', 'Courier New', monospace",
    lineNumbers: 'on',
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    automaticLayout: true,
    padding: { top: 16 },
    renderLineHighlight: 'all',
    smoothScrolling: true,
    cursorBlinking: 'smooth',
    tabSize: 2,
  })

  // Sync Monaco content -> content ref
  editorInstance.onDidChangeModelContent(() => {
    content.value = editorInstance?.getValue() || ''
  })

  // Ctrl+S / Cmd+S shortcut
  editorInstance.addAction({
    id: 'save-content',
    label: 'Save Content',
    keybindings: [
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS
    ],
    run: () => {
      saveContent()
    }
  })
}

function updateEditorContent(newContent: string, filename: string) {
  if (!editorInstance) return
  const model = editorInstance.getModel()
  if (model) {
    // Preserve undo stack by using executeEdits instead of setValue when possible
    model.setValue(newContent)
    monaco.editor.setModelLanguage(model, getLanguageFromFilename(filename))
  }
}

onMounted(async () => {
  await loadFiles()
  await loadContent()
  await nextTick()
  createEditor()
})

onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.dispose()
    editorInstance = null
  }
})

// Re-layout Monaco when switching back to edit view
watch(viewMode, async (mode) => {
  if (mode === 'edit' && editorInstance) {
    await nextTick()
    editorInstance.layout()
  }
})

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

async function loadContent() {
  try {
    const blob = await adminSkillFileApi.downloadFile(props.skillId, 'SKILL.md')
    const arrayBuffer = await blob.arrayBuffer()
    const text = new TextDecoder().decode(arrayBuffer)
    content.value = text
    originalContent.value = text
    currentFile.value = 'SKILL.md'
  } catch (e) {
    // 文件不存在，使用空内容
    content.value = '# ' + props.skillName + '\n\n'
    originalContent.value = content.value
    currentFile.value = 'SKILL.md'
  }

  // Update editor if already created
  updateEditorContent(content.value, currentFile.value)
}

async function selectFile(node: FileNode) {
  if (node.type === 'folder') return
  if (isDirty.value) {
    await saveContent()
  }
  currentFile.value = node.name
  try {
    const blob = await adminSkillFileApi.downloadFile(props.skillId, node.path)
    const arrayBuffer = await blob.arrayBuffer()
    const text = new TextDecoder().decode(arrayBuffer)
    content.value = text
    originalContent.value = text
  } catch (e) {
    content.value = ''
    originalContent.value = ''
  }

  updateEditorContent(content.value, node.name)
}

async function saveContent(skipScan = false) {
  try {
    await adminSkillConfigApi.updateContent(props.skillId, content.value, skipScan)
    originalContent.value = content.value
    desktop.addToast('保存成功', 'success')
  } catch (e: any) {
    if (e.code === 4001 && e.data) {
      scanResult.value = e.data
      showScanReport.value = true
    } else {
      console.error('保存失败', e)
      desktop.addToast('保存失败', 'error')
    }
  }
}

async function handleForceProceed() {
  showScanReport.value = false
  await saveContent(true)
}

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

async function uploadFile() {
  if (!selectedFile.value) return
  uploading.value = true
  try {
    await adminSkillFileApi.uploadFile(props.skillId, uploadPath.value, selectedFile.value)
    showUploadDialog.value = false
    uploadPath.value = ''
    selectedFile.value = null
    await loadFiles()
  } catch (e) {
    console.error('上传失败', e)
    desktop.addToast('上传失败', 'error')
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
</script>

<style scoped>
.skill-editor-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.file-toolbar {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-subtle);
  background: #2C2C2E;
}

.file-toolbar .btn.active {
  background: rgba(10, 132, 255, 0.2);
  color: #0A84FF;
}

.toolbar-divider {
  width: 1px;
  background: var(--border-subtle);
  margin: 0 4px;
}

.editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.file-sidebar {
  width: 200px;
  border-right: 1px solid var(--border-subtle);
  background: #252528;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 8px 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-disabled);
  border-bottom: 1px solid var(--border-subtle);
}

.sidebar-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.edit-view, .preview-view {
  flex: 1;
  overflow: hidden;
}

.preview-view {
  overflow-y: auto;
  padding: 16px;
  background: #1C1C1E;
}

.preview-content {
  max-width: 800px;
  margin: 0 auto;
  color: var(--text-primary);
  line-height: 1.6;
}

.preview-content :deep(h1) { font-size: 1.8em; margin: 0.5em 0; }
.preview-content :deep(h2) { font-size: 1.4em; margin: 0.5em 0; }
.preview-content :deep(h3) { font-size: 1.2em; margin: 0.5em 0; }
.preview-content :deep(code) { background: rgba(255,255,255,0.1); padding: 2px 6px; border-radius: 4px; }
.preview-content :deep(pre) { background: rgba(0,0,0,0.3); padding: 12px; border-radius: 8px; overflow-x: auto; }
.preview-content :deep(blockquote) { border-left: 3px solid #0A84FF; padding-left: 12px; margin-left: 0; color: var(--text-secondary); }

.editor-statusbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  background: #252528;
  border-top: 1px solid var(--border-subtle);
  font-size: 12px;
  color: var(--text-disabled);
}

.editor-statusbar .dirty {
  color: #FF9F0A;
}

.editor-statusbar .btn {
  margin-left: auto;
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--text-disabled);
  gap: 8px;
  font-size: 12px;
}

.file-info {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Modal dialogs - non-transparent */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1E1E22;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-group input[type="text"],
.form-group select {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input[type="text"]:focus,
.form-group select:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-group input[type="file"] {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px dashed var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
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
  padding: 8px 14px;
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
  background: #3A3A3C;
  color: #636366;
  cursor: not-allowed;
}

.btn-outline {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}
</style>
