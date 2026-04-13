<template>
  <div class="perm-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <svg class="header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
        </svg>
        <span class="header-title">权限管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ allPerms.length }}</span>
          <span class="stat-label">总权限</span>
        </span>
      </div>
    </div>

    <!-- Main: Left Tree + Right Detail -->
    <div class="perm-main">
      <!-- Left: Permission Tree -->
      <div class="perm-tree-panel">
        <div class="perm-tree-toolbar">
          <input v-model="searchQuery" placeholder="搜索权限名称..." class="tree-search" />
          <button class="btn-create" @click="openCreateModal(null)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </button>
        </div>
        <div class="perm-tree">
          <div
            class="perm-root-item"
            :class="{ active: selectedPerm === null }"
            @click="selectedPerm = null"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
            <span>全部权限</span>
          </div>
          <PermissionTreeNode
            v-for="perm in filteredRootPerms"
            :key="perm.id"
            :perm="perm"
            :selected-id="selectedPerm?.id ?? null"
            @select="onPermSelect"
          />
        </div>
      </div>

      <!-- Right: Detail Panel -->
      <div class="perm-detail-panel">
        <div v-if="!selectedPerm" class="detail-empty">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
          </svg>
          <span>点击左侧权限查看详情</span>
          <button class="btn-create-root" @click="openCreateModal(null)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            新建顶级权限
          </button>
        </div>

        <div v-else class="detail-content">
          <div class="detail-header">
            <component :is="getTypeIcon(selectedPerm.permType)" :size="20" class="detail-icon" />
            <div class="detail-info">
              <div class="detail-name">{{ selectedPerm.permName }}</div>
              <code class="detail-code">{{ selectedPerm.permCode }}</code>
            </div>
            <span class="type-badge" :class="selectedPerm.permType.toLowerCase()">{{ selectedPerm.permType }}</span>
          </div>

          <div class="detail-fields">
            <div class="detail-field">
              <span class="field-label">权限ID</span>
              <span class="field-value">{{ selectedPerm.id }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">父权限</span>
              <span class="field-value">{{ getParentName(selectedPerm.parentId) }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">路径</span>
              <span class="field-value">{{ selectedPerm.path || '-' }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">图标</span>
              <span class="field-value">{{ selectedPerm.icon || '-' }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">排序</span>
              <span class="field-value">{{ selectedPerm.sort }}</span>
            </div>
            <div class="detail-field">
              <span class="field-label">状态</span>
              <span class="status-tag" :class="selectedPerm.status === 1 ? 'active' : 'disabled'">
                <span class="status-dot"></span>
                {{ selectedPerm.status === 1 ? '正常' : '禁用' }}
              </span>
            </div>
            <div class="detail-field">
              <span class="field-label">创建时间</span>
              <span class="field-value">{{ formatTime(selectedPerm.createTime) }}</span>
            </div>
          </div>

          <div class="detail-actions">
            <button class="btn-action add-child" @click="openCreateModal(selectedPerm)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              添加子权限
            </button>
            <button class="btn-action edit" @click="openEditModal">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              编辑
            </button>
            <button class="btn-action delete" @click="handleDelete">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑权限' : '创建权限' }}</span>
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
                <label>权限名称 <span class="required">*</span></label>
                <input v-model="form.permName" placeholder="如：用户列表" />
              </div>
              <div class="form-field">
                <label>权限编码 <span class="required">*</span></label>
                <input v-model="form.permCode" :disabled="isEditing" placeholder="如：user:list" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>权限类型 <span class="required">*</span></label>
                <select v-model="form.permType" class="form-select" :disabled="isEditing">
                  <option value="MENU">菜单 (MENU)</option>
                  <option value="BUTTON">按钮 (BUTTON)</option>
                  <option value="API">API接口 (API)</option>
                </select>
              </div>
              <div class="form-field">
                <label>父权限</label>
                <select v-model="form.parentId" class="form-select">
                  <option :value="0">无（顶级权限）</option>
                  <option v-for="perm in allPermsForSelect" :key="perm.id" :value="perm.id">
                    {{ '　'.repeat(getDepth(perm.id)) }}{{ perm.permName }}
                  </option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>路径</label>
                <input v-model="form.path" placeholder="/user/list 或 api路径" />
              </div>
              <div class="form-field">
                <label>排序</label>
                <input v-model.number="form.sort" type="number" placeholder="0" />
              </div>
            </div>
            <div class="form-row" v-if="form.permType === 'MENU'">
              <div class="form-field">
                <label>图标名称</label>
                <input v-model="form.icon" placeholder="如：Users（lucide图标名）" />
              </div>
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
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存修改' : '创建权限' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Menu, MousePointerClick, Globe } from 'lucide-vue-next'
import { adminPermissionApi, type PermissionInfo, type CreatePermissionRequest, type UpdatePermissionRequest } from '../../../api/adminPermission'
import { useDesktopStore } from '../../../stores/desktop'
import PermissionTreeNode from './PermissionTreeNode.vue'

const desktop = useDesktopStore()

// ==================== Data ====================
const allPerms = ref<PermissionInfo[]>([])
const isLoading = ref(true)
const searchQuery = ref('')
const selectedPerm = ref<PermissionInfo | null>(null)

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  parentId: 0,
  permName: '',
  permCode: '',
  permType: 'MENU',
  path: '',
  icon: '',
  sort: 0,
  status: 1
})

// ==================== Computed ====================
// Build permission tree from flat list
const permTree = computed(() => {
  const map = new Map<number, PermissionInfo & { children: PermissionInfo[] }>()
  const roots: (PermissionInfo & { children: PermissionInfo[] })[] = []

  // First pass: create node copies with children array
  for (const p of allPerms.value) {
    map.set(p.id, { ...p, children: [] })
  }

  // Second pass: build tree
  for (const p of allPerms.value) {
    const node = map.get(p.id)!
    if (p.parentId === 0) {
      roots.push(node)
    } else {
      const parent = map.get(p.parentId)
      if (parent) {
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    }
  }

  return roots.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
})

const filteredRootPerms = computed(() => {
  if (!searchQuery.value.trim()) return permTree.value
  const q = searchQuery.value.toLowerCase()
  const result: (PermissionInfo & { children: PermissionInfo[] })[] = []
  function filterTree(nodes: (PermissionInfo & { children: PermissionInfo[] })[], parentMatch = false) {
    for (const node of nodes) {
      const matches = node.permName.toLowerCase().includes(q)
      const childMatch = filterTree(node.children, matches || parentMatch)
      if (matches || childMatch.length > 0) {
        result.push({ ...node, children: childMatch })
      }
    }
    return result
  }
  return filterTree(permTree.value)
})

const allPermsForSelect = computed(() => {
  return allPerms.value.filter(p => p.id !== form.value.id)
})

// ==================== Helpers ====================
function getDepth(_permId: number): number {
  return 0 // simplified, depth not needed for select options
}

function getParentName(parentId: number): string {
  if (parentId === 0) return '无（顶级）'
  const p = allPerms.value.find(p => p.id === parentId)
  return p ? p.permName : '未知'
}

function getTypeIcon(type: string) {
  switch (type) {
    case 'MENU': return Menu
    case 'BUTTON': return MousePointerClick
    case 'API': return Globe
    default: return Menu
  }
}

function formatTime(time: string) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

// ==================== Load Data ====================
async function loadPermissions() {
  try {
    const res = await adminPermissionApi.list(1, 1000)
    allPerms.value = res.records
  } catch (e: any) {
    desktop.addToast('加载权限列表失败', 'error')
  }
}

// ==================== Select ====================
function onPermSelect(perm: PermissionInfo) {
  selectedPerm.value = perm
}

// ==================== Modal Actions ====================
function openCreateModal(parent: PermissionInfo | null) {
  isEditing.value = false
  form.value = {
    id: 0,
    parentId: parent ? parent.id : 0,
    permName: '',
    permCode: '',
    permType: 'MENU',
    path: '',
    icon: '',
    sort: 0,
    status: 1
  }
  showModal.value = true
}

function openEditModal() {
  if (!selectedPerm.value) return
  isEditing.value = true
  form.value = {
    id: selectedPerm.value.id,
    parentId: selectedPerm.value.parentId,
    permName: selectedPerm.value.permName,
    permCode: selectedPerm.value.permCode,
    permType: selectedPerm.value.permType,
    path: selectedPerm.value.path || '',
    icon: selectedPerm.value.icon || '',
    sort: selectedPerm.value.sort,
    status: selectedPerm.value.status
  }
  showModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return
  if (!form.value.permName.trim()) {
    desktop.addToast('请输入权限名称', 'error')
    return
  }
  if (!isEditing.value && !form.value.permCode.trim()) {
    desktop.addToast('请输入权限编码', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      const req: UpdatePermissionRequest = {
        id: form.value.id,
        parentId: form.value.parentId,
        permName: form.value.permName,
        permType: form.value.permType,
        path: form.value.path || undefined,
        icon: form.value.icon || undefined,
        sort: form.value.sort,
        status: form.value.status
      }
      await adminPermissionApi.update(req)
      desktop.addToast('权限更新成功', 'success')
      selectedPerm.value = null
    } else {
      const req: CreatePermissionRequest = {
        parentId: form.value.parentId,
        permName: form.value.permName,
        permCode: form.value.permCode,
        permType: form.value.permType,
        path: form.value.path || undefined,
        icon: form.value.icon || undefined,
        sort: form.value.sort
      }
      await adminPermissionApi.create(req)
      desktop.addToast('权限创建成功', 'success')
    }
    showModal.value = false
    await loadPermissions()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete() {
  if (!selectedPerm.value) return
  if (!confirm(`确定要删除权限「${selectedPerm.value.permName}」吗？`)) return
  try {
    await adminPermissionApi.delete(selectedPerm.value.id)
    desktop.addToast('权限已删除', 'success')
    selectedPerm.value = null
    await loadPermissions()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await loadPermissions()
  isLoading.value = false
})
</script>

<style scoped>
.perm-app {
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
  color: #FFD60A;
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

/* ========== Main Layout ========== */
.perm-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ========== Left Tree Panel ========== */
.perm-tree-panel {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.perm-tree-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-subtle);
}

.tree-search {
  flex: 1;
  height: 32px;
  padding: 0 10px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-primary);
  outline: none;
}

.tree-search:focus {
  border-color: #FFD60A;
}

.btn-create {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #FFD60A, #FF9F0A);
  color: #000;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-create:hover {
  transform: translateY(-1px);
}

.perm-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.perm-root-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 8px;
  border-radius: 6px;
  transition: all 0.15s;
}

.perm-root-item:hover, .perm-root-item.active {
  background: rgba(255, 214, 10, 0.08);
  color: #FFD60A;
}

/* ========== Right Detail Panel ========== */
.perm-detail-panel {
  flex: 1;
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

.btn-create-root {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  border-radius: 6px;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  background: linear-gradient(135deg, #FFD60A, #FF9F0A);
  color: #000;
  transition: all 0.2s;
  margin-top: 8px;
}

.detail-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-icon {
  color: #FFD60A;
  flex-shrink: 0;
}

.detail-info {
  flex: 1;
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

.type-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  flex-shrink: 0;
}

.type-badge.menu { background: rgba(48, 209, 88, 0.15); color: #30D158; }
.type-badge.button { background: rgba(10, 132, 255, 0.15); color: #0A84FF; }
.type-badge.api { background: rgba(191, 90, 242, 0.15); color: #BF5AF2; }

.detail-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 14px;
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

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-tag.active { color: #30D158; background: rgba(48, 209, 88, 0.1); }
.status-tag.disabled { color: #FF453A; background: rgba(255, 69, 58, 0.1); }

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action.add-child {
  background: rgba(255, 214, 10, 0.15);
  color: #FFD60A;
}

.btn-action.add-child:hover {
  background: rgba(255, 214, 10, 0.25);
}

.btn-action.edit {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.btn-action.edit:hover {
  background: rgba(10, 132, 255, 0.25);
}

.btn-action.delete {
  background: rgba(255, 69, 58, 0.15);
  color: #FF453A;
}

.btn-action.delete:hover {
  background: rgba(255, 69, 58, 0.25);
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
  width: 520px;
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
  border-color: #FFD60A;
  box-shadow: 0 0 0 3px rgba(255, 214, 10, 0.1);
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
  background: linear-gradient(135deg, #FFD60A, #FF9F0A);
  color: #000;
  box-shadow: 0 2px 8px rgba(255, 214, 10, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 214, 10, 0.4);
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
  border: 2px solid rgba(0, 0, 0, 0.3);
  border-top-color: #000;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
