<template>
  <div class="mcp-editor-panel">
    <div class="editor-header">
      <div class="editor-title">
        <Server :size="20" />
        <span>{{ mcpName }}</span>
      </div>
      <div class="editor-actions">
        <button class="btn btn-outline btn-sm" @click="loadMcp">
          <RefreshCw :size="14" /> 刷新
        </button>
        <button class="btn btn-primary btn-sm" @click="saveMcp" :disabled="saving">
          <Save :size="14" /> 保存
        </button>
      </div>
    </div>

    <div class="editor-content">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else class="form-content">
        <div class="form-section">
          <h3>基本信息</h3>
          <div class="form-group">
            <label>服务名称 *</label>
            <input v-model="form.name" type="text" placeholder="输入服务名称" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="2" placeholder="输入服务描述"></textarea>
          </div>
        </div>

        <div class="form-section">
          <h3>连接配置</h3>
          <div class="form-group">
            <label>传输方式 *</label>
            <div class="radio-group">
              <label class="radio-item" :class="{ active: form.transportType === 'REMOTE' }">
                <input type="radio" v-model="form.transportType" value="REMOTE" />
                <Globe :size="14" /> 远程 URL (SSE)
              </label>
              <label class="radio-item" :class="{ active: form.transportType === 'UPLOADED' }">
                <input type="radio" v-model="form.transportType" value="UPLOADED" />
                <Upload :size="14" /> 本地上传 (STDIO)
              </label>
            </div>
          </div>
          <div class="form-group" v-if="form.transportType === 'REMOTE'">
            <label>服务器地址 *</label>
            <input v-model="form.serverUrl" type="text" placeholder="例如: https://mcp.example.com/sse" />
          </div>
          <div class="form-group" v-if="form.transportType === 'REMOTE'">
            <label>传输协议</label>
            <div class="radio-group">
              <label class="radio-item" :class="{ active: form.protocol === 'SSE' }">
                <input type="radio" v-model="form.protocol" value="SSE" />
                SSE (旧版)
              </label>
              <label class="radio-item" :class="{ active: form.protocol === 'STREAMABLE_HTTP' }">
                <input type="radio" v-model="form.protocol" value="STREAMABLE_HTTP" />
                Streamable HTTP (新版)
              </label>
            </div>
            <small>不确定选哪个？SSE 服务器地址通常以 /sse 结尾</small>
          </div>
          <div class="form-group" v-if="form.transportType === 'UPLOADED'">
            <label>上传 MCP Server ZIP 包</label>
            <input type="file" @change="handleFileSelect" accept=".zip" />
            <div v-if="form.packagePath && !selectedFile" class="file-info-current">
              当前包: {{ form.packagePath }}
            </div>
            <div v-if="selectedFile" class="file-info">
              {{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})
            </div>
            <div v-if="uploadStatus.uploading" class="upload-progress">
              上传中... {{ uploadStatus.progress }}%
            </div>
            <div v-if="uploadStatus.success" class="upload-success">
              上传成功！{{ form.packagePath }}
            </div>
            <div v-if="uploadStatus.error" class="upload-error">
              {{ uploadStatus.error }}
            </div>
          </div>

          <div class="form-section" v-if="form.transportType === 'UPLOADED'">
            <h3>STDIO 配置</h3>
            <div class="form-group">
              <label>启动命令 *</label>
              <input v-model="form.command" type="text" placeholder="例如: node, python, npx" />
              <small>可执行文件或命令（留空则自动检测 package.json 入口）</small>
            </div>
            <div class="form-group">
              <label>命令参数</label>
              <input v-model="form.args" type="text" placeholder='例如: ["D:\\test"]' />
              <small>JSON 数组格式，MCP 业务参数（如允许访问的目录路径）</small>
            </div>
            <div class="form-group">
              <label>环境变量</label>
              <input v-model="form.env" type="text" placeholder='例如: {"NODE_ENV": "production"}' />
              <small>JSON 对象格式</small>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h3>权限配置</h3>
          <div class="form-group">
            <label>可见范围</label>
            <select v-model="form.scope">
              <option value="SYSTEM">系统级 - 所有用户可用</option>
              <option value="DEPT">部门级 - 指定部门可用</option>
              <option value="USER">个人级 - 指定用户可用</option>
            </select>
          </div>
          <div class="form-group" v-if="form.scope === 'DEPT'">
            <label>指定部门 *</label>
            <select v-model="form.deptId">
              <option :value="null" disabled>请选择部门</option>
              <option v-for="dept in deptList" :key="dept.id" :value="dept.id">
                {{ dept.deptName }}
              </option>
            </select>
          </div>
          <div class="form-group" v-if="form.scope === 'USER'">
            <label>授权用户 * <small>搜索并选择可使用此服务的用户</small></label>
            <input v-model="userSearch" type="text" class="user-search-input" placeholder="搜索用户名或昵称..." />
            <div class="user-pick-list">
              <div
                v-for="user in filteredUsers"
                :key="user.id"
                class="user-pick-item"
                :class="{ picked: selectedUserIds.has(user.id) }"
                @click="toggleUser(user.id)"
              >
                <div class="user-pick-avatar">{{ (user.nickname || user.username).charAt(0) }}</div>
                <div class="user-pick-info">
                  <span class="user-pick-name">{{ user.nickname || user.username }}</span>
                  <span class="user-pick-dept">{{ user.deptName }}</span>
                </div>
                <span v-if="selectedUserIds.has(user.id)" class="user-pick-check">✓</span>
              </div>
            </div>
            <div v-if="selectedUserIds.size > 0" class="user-picked-summary">
              已选 {{ selectedUserIds.size }} 人
            </div>
          </div>
          <div class="form-group">
            <label>启用状态</label>
            <label class="toggle-switch">
              <input type="checkbox" v-model="form.enabled" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label">{{ form.enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="editor-footer">
      <span class="status-text" :class="{ saved: lastSaved }">
        {{ lastSaved ? '已保存' : '未保存' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Server, RefreshCw, Save, Globe, Upload } from 'lucide-vue-next'
import { adminMcpConfigApi, type TransportType, type McpScope, type Protocol } from '@/api/adminMcpConfig'
import { adminDeptApi, type DeptInfo } from '@/api/adminDept'
import { adminUserApi, type UserInfo } from '@/api/adminUser'
import { resourceGrantApi } from '@/api/resourceGrant'
import { uploadMcpZip } from '@/api/upload'
import { useDesktopStore } from '@/stores/desktop'

const desktop = useDesktopStore()

const props = defineProps<{
  mcpId: number
  mcpName: string
}>()

const loading = ref(false)
const saving = ref(false)
const lastSaved = ref(false)
const selectedFile = ref<File | null>(null)
const uploadStatus = ref({
  uploading: false,
  progress: 0,
  success: false,
  error: ''
})
const deptList = ref<DeptInfo[]>([])
const allUsers = ref<UserInfo[]>([])
const userSearch = ref('')
const selectedUserIds = ref<Set<number>>(new Set())

const filteredUsers = computed(() => {
  const kw = userSearch.value.toLowerCase()
  return allUsers.value.filter(u => {
    if (!kw) return true
    return (u.nickname || '').toLowerCase().includes(kw)
      || (u.username || '').toLowerCase().includes(kw)
  })
})

function toggleUser(userId: number) {
  const s = new Set(selectedUserIds.value)
  if (s.has(userId)) s.delete(userId)
  else s.add(userId)
  selectedUserIds.value = s
}

const form = ref({
  name: '',
  description: '',
  transportType: 'REMOTE' as TransportType,
  serverUrl: '',
  packagePath: '',
  command: '',
  args: '',
  env: '',
  protocol: 'SSE' as Protocol,
  scope: 'SYSTEM' as McpScope,
  deptId: null as number | null,
  enabled: true
})

async function loadMcp() {
  loading.value = true
  try {
    const res = await adminMcpConfigApi.get(props.mcpId)
    form.value = {
      name: res.name || '',
      description: res.description || '',
      transportType: res.transportType || 'REMOTE',
      serverUrl: res.serverUrl || '',
      packagePath: res.packagePath || '',
      command: res.command || '',
      args: res.args || '',
      env: res.env || '',
      protocol: (res.protocol as Protocol) || 'SSE',
      scope: res.scope || 'SYSTEM',
      deptId: res.deptId || null,
      enabled: res.enabled === 1
    }
    selectedFile.value = null
    uploadStatus.value = { uploading: false, progress: 0, success: false, error: '' }
  } catch (e) {
    console.error('加载 MCP 失败', e)
    desktop.addToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

async function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return

  selectedFile.value = input.files[0]
  uploadStatus.value = { uploading: true, progress: 0, success: false, error: '' }

  try {
    const result = await uploadMcpZip(selectedFile.value, { overwrite: true }, (progress) => {
      uploadStatus.value = { uploading: true, progress, success: false, error: '' }
    })

    if (result.success) {
      form.value.packagePath = result.packagePath
      uploadStatus.value = { uploading: false, progress: 100, success: true, error: '' }
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (e) {
    uploadStatus.value = { uploading: false, progress: 0, success: false, error: (e as Error).message }
    desktop.addToast(`文件上传失败: ${(e as Error).message}`, 'error')
  }
}

async function saveMcp() {
  if (!form.value.name) {
    desktop.addToast('请填写服务名称', 'error')
    return
  }

  if (form.value.transportType === 'REMOTE' && !form.value.serverUrl) {
    desktop.addToast('请填写服务器地址', 'error')
    return
  }

  if (form.value.transportType === 'UPLOADED' && !form.value.packagePath) {
    desktop.addToast('请先上传 MCP Server ZIP 包', 'error')
    return
  }

  if (form.value.scope === 'DEPT' && !form.value.deptId) {
    desktop.addToast('请选择部门', 'error')
    return
  }

  if (form.value.scope === 'USER' && selectedUserIds.value.size === 0) {
    desktop.addToast('请选择至少一个授权用户', 'error')
    return
  }

  // 验证 JSON 格式
  if (form.value.args) {
    try {
      JSON.parse(form.value.args)
    } catch {
      desktop.addToast('命令参数格式错误，应为 JSON 数组', 'error')
      return
    }
  }

  if (form.value.env) {
    try {
      JSON.parse(form.value.env)
    } catch {
      desktop.addToast('环境变量格式错误，应为 JSON 对象', 'error')
      return
    }
  }

  saving.value = true
  try {
    await adminMcpConfigApi.update({
      id: props.mcpId,
      name: form.value.name,
      description: form.value.description,
      transportType: form.value.transportType,
      serverUrl: form.value.serverUrl,
      packagePath: form.value.packagePath,
      command: form.value.command,
      args: form.value.args,
      env: form.value.env,
      protocol: form.value.protocol,
      scope: form.value.scope,
      deptId: form.value.deptId || undefined,
      enabled: form.value.enabled ? 1 : 0
    })

    // USER scope: 同步授权用户
    if (form.value.scope === 'USER') {
      // 先获取现有授权
      const existingGrants = await resourceGrantApi.listByResource('MCP', props.mcpId)
      const existingIds = new Set(existingGrants.map(g => g.userId))
      // 新增授权
      const toAdd = Array.from(selectedUserIds.value).filter(id => !existingIds.has(id))
      // 移除授权
      const toRemove = existingGrants.filter(g => !selectedUserIds.value.has(g.userId))
      await Promise.all([
        ...toAdd.map(userId => resourceGrantApi.assign(userId, 'MCP', props.mcpId)),
        ...toRemove.map(g => resourceGrantApi.revoke(g.userId, 'MCP', props.mcpId))
      ])
    }

    lastSaved.value = true
    setTimeout(() => { lastSaved.value = false }, 2000)
    desktop.addToast('保存成功', 'success')
  } catch (e) {
    console.error('保存 MCP 失败', e)
    desktop.addToast('保存失败', 'error')
  } finally {
    saving.value = false
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function loadDeptList() {
  try {
    const res = await adminDeptApi.list(1, 100)
    deptList.value = res.records
  } catch (e) {
    console.error('加载部门列表失败', e)
  }
}

async function loadUsers() {
  try {
    allUsers.value = await adminUserApi.list()
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

async function loadGrantedUsers() {
  try {
    const grants = await resourceGrantApi.listByResource('MCP', props.mcpId)
    if (grants.length > 0) {
      selectedUserIds.value = new Set(grants.map(g => g.userId))
    }
  } catch (e) {
    console.error('加载授权用户失败', e)
  }
}

onMounted(() => {
  loadMcp()
  loadDeptList()
  loadUsers()
  loadGrantedUsers()
})
</script>

<style scoped>
.mcp-editor-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1C1C1E;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid var(--border-subtle);
}

.editor-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.editor-title svg {
  color: #0A84FF;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: var(--text-disabled);
}

.spinner {
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

.form-content {
  max-width: 600px;
}

.form-section {
  margin-bottom: 24px;
}

.form-section h3 {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-subtle);
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
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
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-group textarea {
  resize: vertical;
  min-height: 60px;
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

.file-info-current {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  font-size: 14px;
  color: var(--text-secondary);
}

.file-info {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  font-size: 14px;
  color: #30D158;
}

.upload-progress {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(255, 159, 10, 0.1);
  border: 1px solid #FF9F0A;
  border-radius: 4px;
  font-size: 14px;
  color: #FF9F0A;
  text-align: center;
}

.upload-success {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(48, 209, 88, 0.1);
  border: 1px solid #30D158;
  border-radius: 4px;
  font-size: 14px;
  color: #30D158;
}

.upload-error {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(255, 69, 58, 0.1);
  border: 1px solid #FF453A;
  border-radius: 4px;
  font-size: 14px;
  color: #FF453A;
}

.radio-group {
  display: flex;
  gap: 8px;
}

.radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}

.radio-item input {
  display: none;
}

.radio-item:hover {
  border-color: rgba(10, 132, 255, 0.5);
}

.radio-item.active {
  background: rgba(10, 132, 255, 0.1);
  border-color: #0A84FF;
  color: #0A84FF;
}

/* Toggle */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  vertical-align: middle;
  margin-right: 10px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  inset: 0;
  background: #3A3A3C;
  border-radius: 24px;
  cursor: pointer;
  transition: 0.2s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.2s;
}

.toggle-switch input:checked + .toggle-slider {
  background: #30D158;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
  vertical-align: middle;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid var(--border-subtle);
  font-size: 12px;
  color: var(--text-disabled);
}

.editor-footer .saved {
  color: #30D158;
}

small {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #8E8E93;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 11px;
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

.btn-outline {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}

/* User Picker */
.user-search-input {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
  margin-bottom: 8px;
}
.user-search-input:focus { outline: none; border-color: #0A84FF; }

.user-pick-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.15);
}
.user-pick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.15s;
}
.user-pick-item:hover { background: rgba(255, 255, 255, 0.05); }
.user-pick-item.picked { background: rgba(10, 132, 255, 0.1); }

.user-pick-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5AC8FA);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; color: white; flex-shrink: 0;
}
.user-pick-info { flex: 1; min-width: 0; }
.user-pick-name { display: block; font-size: 13px; color: var(--text-primary); }
.user-pick-dept { display: block; font-size: 11px; color: var(--text-disabled); }
.user-pick-check { color: #0A84FF; font-weight: 600; font-size: 14px; }
.user-picked-summary { margin-top: 8px; font-size: 12px; color: #0A84FF; }
</style>
