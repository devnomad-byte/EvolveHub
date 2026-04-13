<template>
  <div class="role-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <svg class="header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
        <span class="header-title">角色管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ roles.length }}</span>
          <span class="stat-label">总角色</span>
        </span>
        <span class="stat-badge active">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">启用</span>
        </span>
        <span class="stat-badge disabled">
          <span class="stat-num">{{ roles.length - activeCount }}</span>
          <span class="stat-label">禁用</span>
        </span>
      </div>
    </div>

    <!-- Main: Left Role List + Right Detail Panel -->
    <div class="role-main">
      <!-- Left: Role Table -->
      <div class="role-table-panel">
        <div class="table-toolbar">
          <div class="toolbar-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="searchQuery" placeholder="搜索角色名称..." class="search-input" />
          </div>
          <button class="btn-create" @click="openCreateModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            创建角色
          </button>
        </div>

        <div class="table-container">
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredRoles.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <span>暂无角色数据</span>
          </div>
          <table v-else class="data-table">
            <thead>
              <tr>
                <th>角色名称</th>
                <th>编码</th>
                <th>数据范围</th>
                <th>排序</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="role in filteredRoles"
                :key="role.id"
                :class="{ active: selectedRole?.id === role.id }"
                @click="selectRole(role)"
              >
                <td>
                  <div class="role-name-cell">
                    <Shield :size="16" class="role-icon" />
                    {{ role.roleName }}
                  </div>
                </td>
                <td><code class="code-tag">{{ role.roleCode }}</code></td>
                <td><span class="data-scope-tag">{{ dataScopeLabel(role.dataScope) }}</span></td>
                <td>{{ role.sort }}</td>
                <td>
                  <span class="status-tag" :class="role.status === 1 ? 'active' : 'disabled'">
                    <span class="status-dot"></span>
                    {{ role.status === 1 ? '正常' : '禁用' }}
                  </span>
                </td>
                <td @click.stop>
                  <div class="action-btns">
                    <button class="action-btn edit" title="编辑" @click="openEditModal(role)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                    </button>
                    <button class="action-btn delete" title="删除" @click="handleDelete(role)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Right: Role Detail Panel -->
      <div class="role-detail-panel">
        <div v-if="!selectedRole" class="detail-empty">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          <span>点击左侧角色查看详情</span>
        </div>
        <div v-else class="detail-content">
          <div class="detail-header">
            <Shield :size="20" class="detail-icon" />
            <div>
              <div class="detail-name">{{ selectedRole.roleName }}</div>
              <div class="detail-code">{{ selectedRole.roleCode }}</div>
            </div>
            <span class="status-tag" :class="selectedRole.status === 1 ? 'active' : 'disabled'">
              <span class="status-dot"></span>
              {{ selectedRole.status === 1 ? '正常' : '禁用' }}
            </span>
          </div>

          <div class="detail-fields">
            <div class="detail-field">
              <span class="field-label">数据范围</span>
              <span class="field-value">{{ dataScopeLabel(selectedRole.dataScope) }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">排序</span>
              <span class="field-value">{{ selectedRole.sort }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">备注</span>
              <span class="field-value">{{ selectedRole.remark || '-' }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">创建时间</span>
              <span class="field-value">{{ formatTime(selectedRole.createTime) }}</span>
            </div>
          </div>

          <!-- Permission Assignment -->
          <div class="perm-section">
            <div class="perm-section-header">
              <span class="perm-title">权限分配</span>
              <span class="perm-count">{{ assignedPermIds.size }} / {{ allPermissions.length }}</span>
            </div>
            <div v-if="permLoading" class="perm-loading">
              <div class="spinner-sm"></div>
              <span>加载权限中...</span>
            </div>
            <div v-else class="perm-list">
              <label
                v-for="perm in allPermissions"
                :key="perm.id"
                class="perm-item"
                :class="{ assigned: assignedPermIds.has(perm.id) }"
              >
                <input
                  type="checkbox"
                  :checked="assignedPermIds.has(perm.id)"
                  @change="onPermChange(perm.id, ($event.target as HTMLInputElement).checked)"
                />
                <span class="perm-name">{{ perm.permName }}</span>
                <span class="perm-code">{{ perm.permCode }}</span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑角色' : '创建角色' }}</span>
            <button class="modal-close" @click="showModal = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>角色名称 <span class="required">*</span></label>
                <input v-model="form.roleName" placeholder="如：超级管理员" />
              </div>
              <div class="form-field">
                <label>角色编码 <span class="required">*</span></label>
                <input v-model="form.roleCode" :disabled="isEditing" placeholder="如：SUPER_ADMIN" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>数据范围</label>
                <select v-model="form.dataScope" class="form-select">
                  <option :value="1">全部数据</option>
                  <option :value="2">本部门及子部门</option>
                  <option :value="3">本部门</option>
                  <option :value="4">仅本人</option>
                  <option :value="5">自定义部门</option>
                </select>
              </div>
              <div class="form-field">
                <label>排序</label>
                <input v-model.number="form.sort" type="number" placeholder="0" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>状态</label>
                <div class="toggle-wrapper">
                  <div class="toggle" :class="{ on: form.status === 1 }" @click="form.status = form.status === 1 ? 0 : 1">
                    <div class="toggle-dot"></div>
                  </div>
                  <span class="toggle-label">{{ form.status === 1 ? '正常' : '禁用' }}</span>
                </div>
              </div>
            </div>
            <div class="form-field">
              <label>备注</label>
              <input v-model="form.remark" placeholder="备注信息（可选）" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存修改' : '创建角色' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Shield } from 'lucide-vue-next'
import { adminRoleApi, type RoleInfo } from '../../../api/adminRole'
import { adminPermissionApi, type PermissionInfo } from '../../../api/adminPermission'
import { useDesktopStore } from '../../../stores/desktop'

const desktop = useDesktopStore()

// ==================== Data ====================
const roles = ref<RoleInfo[]>([])
const allPermissions = ref<PermissionInfo[]>([])
const isLoading = ref(true)
const permLoading = ref(false)
const searchQuery = ref('')
const selectedRole = ref<RoleInfo | null>(null)
const assignedPermIds = ref<Set<number>>(new Set())

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  roleName: '',
  roleCode: '',
  dataScope: 4,
  sort: 0,
  status: 1,
  remark: ''
})

// ==================== Computed ====================
const activeCount = computed(() => roles.value.filter(r => r.status === 1).length)

const filteredRoles = computed(() => {
  if (!searchQuery.value.trim()) return roles.value
  const q = searchQuery.value.toLowerCase()
  return roles.value.filter(r => r.roleName.toLowerCase().includes(q) || r.roleCode.toLowerCase().includes(q))
})

// ==================== Helpers ====================
const dataScopeLabel = (scope: number) => {
  const map: Record<number, string> = {
    1: '全部数据', 2: '本部门及子部门', 3: '本部门', 4: '仅本人', 5: '自定义部门'
  }
  return map[scope] || '未知'
}

function formatTime(time: string) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

// ==================== Load Data ====================
async function loadRoles() {
  try {
    const res = await adminRoleApi.list(1, 100)
    roles.value = res.records
  } catch (e: any) {
    desktop.addToast('加载角色列表失败', 'error')
  }
}

// ==================== Select Role ====================
async function selectRole(role: RoleInfo) {
  selectedRole.value = role
  await loadRolePermissions(role.id)
}

// ==================== Permission Assignment ====================
async function loadAllPermissions() {
  try {
    const res = await adminPermissionApi.list(1, 1000)
    allPermissions.value = res.records
  } catch (e: any) {
    desktop.addToast('加载权限列表失败', 'error')
  }
}

async function loadRolePermissions(roleId: number) {
  permLoading.value = true
  try {
    const ids = await adminRoleApi.getPermissions(roleId)
    assignedPermIds.value = new Set(ids)
  } catch (e: any) {
    desktop.addToast('加载角色权限失败', 'error')
    assignedPermIds.value = new Set()
  } finally {
    permLoading.value = false
  }
}

async function onPermChange(permId: number, checked: boolean) {
  if (!selectedRole.value) return
  try {
    if (checked) {
      await adminRoleApi.assignPermission({
        roleId: selectedRole.value.id,
        permissionId: permId
      })
      assignedPermIds.value.add(permId)
    } else {
      await adminRoleApi.removePermission({
        roleId: selectedRole.value.id,
        permissionId: permId
      })
      assignedPermIds.value.delete(permId)
    }
    // 触发响应式更新
    assignedPermIds.value = new Set(assignedPermIds.value)
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
    // 回滚 checkbox 状态
    await loadRolePermissions(selectedRole.value.id)
  }
}

// ==================== Modal Actions ====================
function openCreateModal() {
  isEditing.value = false
  form.value = {
    id: 0,
    roleName: '',
    roleCode: '',
    dataScope: 4,
    sort: 0,
    status: 1,
    remark: ''
  }
  showModal.value = true
}

function openEditModal(role: RoleInfo) {
  isEditing.value = true
  form.value = {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    dataScope: role.dataScope,
    sort: role.sort,
    status: role.status,
    remark: role.remark || ''
  }
  showModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return
  if (!form.value.roleName.trim()) {
    desktop.addToast('请输入角色名称', 'error')
    return
  }
  if (!isEditing.value && !form.value.roleCode.trim()) {
    desktop.addToast('请输入角色编码', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      await adminRoleApi.update({
        id: form.value.id,
        roleName: form.value.roleName,
        roleCode: form.value.roleCode,
        dataScope: form.value.dataScope,
        sort: form.value.sort,
        status: form.value.status,
        remark: form.value.remark
      })
      desktop.addToast('角色更新成功', 'success')
      selectedRole.value = null
    } else {
      await adminRoleApi.create({
        roleName: form.value.roleName,
        roleCode: form.value.roleCode,
        dataScope: form.value.dataScope,
        sort: form.value.sort,
        remark: form.value.remark
      })
      desktop.addToast('角色创建成功', 'success')
    }
    showModal.value = false
    await loadRoles()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(role: RoleInfo) {
  if (!confirm(`确定要删除角色「${role.roleName}」吗？`)) return
  try {
    await adminRoleApi.delete(role.id)
    desktop.addToast('角色已删除', 'success')
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
      assignedPermIds.value = new Set()
    }
    await loadRoles()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await Promise.all([loadRoles(), loadAllPermissions()])
  isLoading.value = false
})
</script>

<style scoped>
.role-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ========== Header ========== */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  color: #BF5AF2;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-stats {
  display: flex;
  gap: 8px;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 12px;
}

.stat-num {
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  color: var(--text-secondary);
}

.stat-badge.active .stat-num { color: #30D158; }
.stat-badge.disabled .stat-num { color: #FF453A; }

/* ========== Main Layout ========== */
.role-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ========== Left Table Panel ========== */
.role-table-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--border-subtle);
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.toolbar-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  width: 200px;
  transition: all 0.2s;
}

.toolbar-search:focus-within {
  border-color: #BF5AF2;
  box-shadow: 0 0 0 3px rgba(191, 90, 242, 0.1);
}

.toolbar-search svg {
  color: var(--text-disabled);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  color: white;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(191, 90, 242, 0.3);
}

.btn-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(191, 90, 242, 0.4);
}

.table-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-disabled);
  font-size: 14px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #BF5AF2;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}

.data-table td {
  padding: 10px 10px;
  font-size: 13px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.data-table tr {
  cursor: pointer;
  transition: all 0.15s;
}

.data-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.data-table tr.active td {
  background: rgba(191, 90, 242, 0.08);
}

.role-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.role-icon {
  color: #BF5AF2;
  flex-shrink: 0;
}

.code-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 11px;
  font-family: monospace;
  color: var(--text-secondary);
}

.data-scope-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(191, 90, 242, 0.1);
  color: #BF5AF2;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-tag.active {
  color: #30D158;
  background: rgba(48, 209, 88, 0.1);
}

.status-tag.disabled {
  color: #FF453A;
  background: rgba(255, 69, 58, 0.1);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-tag.active .status-dot { background: #30D158; }
.status-tag.disabled .status-dot { background: #FF453A; }

.action-btns {
  display: flex;
  gap: 4px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.action-btn.edit:hover { background: rgba(10, 132, 255, 0.12); color: #0A84FF; }
.action-btn.delete:hover { background: rgba(255, 69, 58, 0.12); color: #FF453A; }

/* ========== Right Detail Panel ========== */
.role-detail-panel {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-disabled);
  font-size: 13px;
  padding: 40px 20px;
}

.detail-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-icon {
  color: #BF5AF2;
  flex-shrink: 0;
}

.detail-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.detail-code {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: monospace;
}

.detail-fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.field-label {
  color: var(--text-secondary);
}

.field-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* ========== Permission Section ========== */
.perm-section {
  border-top: 1px solid var(--border-subtle);
  padding-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-height: 0;
}

.perm-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.perm-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.perm-count {
  font-size: 11px;
  color: var(--text-secondary);
}

.perm-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-disabled);
  padding: 8px 0;
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-top-color: #BF5AF2;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.perm-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.perm-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 12px;
}

.perm-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.perm-item.assigned {
  background: rgba(191, 90, 242, 0.08);
}

.perm-item input[type="checkbox"] {
  width: 14px;
  height: 14px;
  accent-color: #BF5AF2;
  cursor: pointer;
  flex-shrink: 0;
}

.perm-name {
  flex: 1;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.perm-item.assigned .perm-name {
  color: #BF5AF2;
  font-weight: 500;
}

.perm-code {
  font-size: 10px;
  color: var(--text-disabled);
  font-family: monospace;
  flex-shrink: 0;
}

/* ========== Modal ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.modal {
  width: 500px;
  max-height: 90vh;
  background: rgba(30, 30, 30, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.modal-close:hover {
  background: rgba(255, 69, 58, 0.1);
  color: #FF453A;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-subtle);
}

/* ========== Form ========== */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-field label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

.required {
  color: #FF453A;
}

.form-field input, .form-select {
  height: 38px;
  padding: 0 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s;
  width: 100%;
}

.form-field input:focus, .form-select:focus {
  border-color: #BF5AF2;
  box-shadow: 0 0 0 3px rgba(191, 90, 242, 0.1);
}

.form-select {
  appearance: none;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

/* ========== Toggle ========== */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
}

.toggle {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.toggle.on {
  background: #30D158;
}

.toggle-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: all 0.2s;
}

.toggle.on .toggle-dot {
  left: 23px;
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ========== Buttons ========== */
.btn {
  height: 38px;
  padding: 0 20px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #BF5AF2, #9B59B6);
  color: white;
  box-shadow: 0 2px 8px rgba(191, 90, 242, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(191, 90, 242, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
</style>
