<template>
  <div class="mcp-create-panel">
    <div class="editor-header">
      <div class="editor-title">
        <Server :size="20" />
        <span>创建 MCP</span>
      </div>
      <div class="editor-actions">
        <button class="btn btn-primary btn-sm" @click="saveMcp" :disabled="saving">
          <Save :size="14" /> 创建
        </button>
      </div>
    </div>

    <div class="editor-content">
      <div class="form-content">
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
            <label>上传 MCP Server ZIP 包 *</label>
            <input type="file" @change="handleFileSelect" accept=".zip" />
            <div v-if="selectedFile" class="file-info">
              {{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})
            </div>
            <div v-if="uploadStatus.uploading" class="upload-progress">
              上传中... {{ uploadStatus.progress }}%
            </div>
            <div v-if="uploadStatus.success" class="upload-success">
              ✅ 上传成功！文件路径: {{ form.packagePath }} ({{ formatSize(selectedFile.size) }})
            </div>
            <div v-if="uploadStatus.error" class="upload-error">
              ❌ {{ uploadStatus.error }}
            </div>
          </div>

          <div class="form-section" v-if="form.transportType === 'UPLOADED'">
            <h3>STDIO 配置</h3>
            <div class="form-group">
              <label>启动命令</label>
              <input v-model="form.command" type="text" placeholder="例如: node, python, npx" />
              <small>留空则自动从 package.json 检测入口文件</small>
            </div>
            <div class="form-group">
              <label>命令参数</label>
              <input v-model="form.args" type="text" placeholder='例如: ["src/index.js", "--port", "3000"]' />
              <small>JSON 数组格式，例如: ["arg1", "arg2"]</small>
            </div>
            <div class="form-group">
              <label>环境变量</label>
              <input v-model="form.env" type="text" placeholder='例如: {"NODE_ENV": "production", "PORT": "3000"}' />
              <small>JSON 对象格式，例如: {"KEY": "value"}</small>
            </div>
            <div class="form-group">
              <label>工作目录</label>
              <input v-model="form.workDir" type="text" placeholder="例如: /path/to/working/directory" />
              <small>可选，留空则使用默认目录</small>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Server, Save, Globe, Upload } from 'lucide-vue-next'
import { adminMcpConfigApi, type TransportType, type McpScope, type Protocol } from '@/api/adminMcpConfig'
import { adminDeptApi, type DeptInfo } from '@/api/adminDept'
import { adminUserApi, type UserInfo } from '@/api/adminUser'
import { resourceGrantApi } from '@/api/resourceGrant'
import { uploadMcpZip } from '@/api/upload'
import { useDesktopStore } from '@/stores/desktop'
import { useWindowStore } from '@/stores/window'

const desktop = useDesktopStore()
const winStore = useWindowStore()

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

onMounted(() => {
  loadDeptList()
  loadUsers()
})

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

const saving = ref(false)

const form = ref({
  name: '',
  description: '',
  transportType: 'REMOTE' as TransportType,
  serverUrl: '',
  packagePath: '',
  command: '',
  args: '',
  env: '',
  workDir: '',
  protocol: 'SSE' as Protocol,
  scope: 'SYSTEM' as McpScope,
  deptId: null as number | null,
  enabled: true
})

const selectedFile = ref<File | null>(null)
const uploadStatus = ref({
  uploading: false,
  progress: 0,
  success: false,
  error: ''
})

async function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return

  selectedFile.value = input.files[0]
  uploadStatus.value = { uploading: false, progress: 0, success: false, error: '' }

  // 自动上传文件
  await uploadFile(selectedFile.value)
}

async function uploadFile(file: File) {
  if (!file) return

  uploadStatus.value = { uploading: true, progress: 0, success: false, error: '' }

  try {
    // 调用上传 API，传入进度回调
    const result = await uploadMcpZip(file, {
      overwrite: false
    }, (progress) => {
      uploadStatus.value = { uploading: true, progress, success: false, error: '' }
    })

    if (result.success) {
      form.value.packagePath = result.packagePath
      uploadStatus.value = { uploading: false, progress: 100, success: true, error: '' }
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (e) {
    console.error('文件上传失败:', e)
    uploadStatus.value = {
      uploading: false,
      progress: 0,
      success: false,
      error: (e as Error).message
    }
    console.error('文件上传失败:', e)
    desktop.addToast(`文件上传失败: ${(e as Error).message}`, 'error')
  }
}

async function saveMcp() {
  // 验证必填项
  if (!form.value.name) {
    desktop.addToast('请填写服务名称', 'error')
    return
  }

  // REMOTE 模式验证
  if (form.value.transportType === 'REMOTE' && !form.value.serverUrl) {
    desktop.addToast('请填写服务器地址', 'error')
    return
  }

  // UPLOADED 模式验证
  if (form.value.transportType === 'UPLOADED') {
    if (!form.value.packagePath) {
      desktop.addToast('请先上传 MCP Server ZIP 包', 'error')
      return
    }
  }

  // DEPT 模式验证
  if (form.value.scope === 'DEPT' && !form.value.deptId) {
    desktop.addToast('请选择部门', 'error')
    return
  }

  // USER 模式验证
  if (form.value.scope === 'USER' && selectedUserIds.value.size === 0) {
    desktop.addToast('请选择至少一个授权用户', 'error')
    return
  }

  saving.value = true
  try {
    // UPLOADED 模式验证（保存时二次检查）
    if (form.value.transportType === 'UPLOADED') {
      if (!form.value.packagePath) {
        desktop.addToast('请先上传 MCP Server ZIP 包', 'error')
        return
      }
    }

    // 解析 JSON 字段
    let argsParsed = undefined
    let envParsed = undefined

    if (form.value.args) {
      try {
        argsParsed = JSON.parse(form.value.args)
      } catch {
        desktop.addToast('命令参数格式错误，应为 JSON 数组', 'error')
        return
      }
    }

    if (form.value.env) {
      try {
        envParsed = JSON.parse(form.value.env)
      } catch {
        desktop.addToast('环境变量格式错误，应为 JSON 对象', 'error')
        return
      }
    }

    const createRequest = {
      name: form.value.name,
      description: form.value.description,
      transportType: form.value.transportType,
      serverUrl: form.value.serverUrl,
      packagePath: form.value.packagePath,
      command: form.value.command,
      args: form.value.args ? JSON.stringify(argsParsed) : undefined,
      env: form.value.env ? JSON.stringify(envParsed) : undefined,
      workDir: form.value.workDir || undefined,
      protocol: form.value.protocol,
      scope: form.value.scope,
      deptId: form.value.deptId || undefined,
      enabled: form.value.enabled ? 1 : 0
    }

    const result = await adminMcpConfigApi.create(createRequest)

    // USER scope: 授权给选中的用户
    if (form.value.scope === 'USER' && result.id) {
      await Promise.all(
        Array.from(selectedUserIds.value).map(userId =>
          resourceGrantApi.assign(userId, 'MCP', result.id)
        )
      )
    }

    desktop.addToast('MCP 配置创建成功', 'success')
    window.dispatchEvent(new CustomEvent('mcp-list-changed'))
    winStore.closeByAppId('mcp-create')
  } catch (e) {
    console.error('创建 MCP 失败', e)
    desktop.addToast(`创建失败: ${(e as Error).message}`, 'error')
  } finally {
    saving.value = false
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.mcp-create-panel {
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

.file-info {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
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

small {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #8E8E93;
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

.user-search-input:focus {
  outline: none;
  border-color: #0A84FF;
}

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

.user-pick-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.user-pick-item.picked {
  background: rgba(10, 132, 255, 0.1);
}

.user-pick-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5AC8FA);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.user-pick-info {
  flex: 1;
  min-width: 0;
}

.user-pick-name {
  display: block;
  font-size: 13px;
  color: var(--text-primary);
}

.user-pick-dept {
  display: block;
  font-size: 11px;
  color: var(--text-disabled);
}

.user-pick-check {
  color: #0A84FF;
  font-weight: 600;
  font-size: 14px;
}

.user-picked-summary {
  margin-top: 8px;
  font-size: 12px;
  color: #0A84FF;
}
</style>
