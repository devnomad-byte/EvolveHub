<template>
  <div class="dept-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <svg class="header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="4" y="4" width="16" height="16" rx="2"/>
          <rect x="9" y="9" width="6" height="6"/>
          <line x1="9" y1="2" x2="9" y2="4"/>
          <line x1="15" y1="2" x2="15" y2="4"/>
          <line x1="9" y1="20" x2="9" y2="22"/>
          <line x1="15" y1="20" x2="15" y2="22"/>
          <line x1="20" y1="9" x2="22" y2="9"/>
          <line x1="20" y1="15" x2="22" y2="15"/>
          <line x1="2" y1="9" x2="4" y2="9"/>
          <line x1="2" y1="15" x2="4" y2="15"/>
        </svg>
        <span class="header-title">部门管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ deptList.length }}</span>
          <span class="stat-label">总部门</span>
        </span>
        <span class="stat-badge active">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">启用</span>
        </span>
        <span class="stat-badge disabled">
          <span class="stat-num">{{ deptList.length - activeCount }}</span>
          <span class="stat-label">禁用</span>
        </span>
      </div>
    </div>

    <!-- Main: Left Tree + Right Table -->
    <div class="dept-main">
      <!-- Left: Department Tree -->
      <div class="dept-panel">
        <div class="dept-panel-header">组织架构</div>
        <div class="dept-tree">
          <div
            class="tree-item tree-root"
            :class="{ active: selectedDeptId === null }"
            @click="selectedDeptId = null"
          >
            <span class="dot dot-root"></span>
            <span class="tree-label">全部部门</span>
          </div>
          <DeptTreeNode
            v-for="dept in deptTree"
            :key="dept.id"
            :dept="dept"
            :selected-id="selectedDeptId"
            :depth="0"
            @select="onDeptSelect"
          />
        </div>
      </div>

      <!-- Right: Department Table -->
      <div class="table-panel">
        <!-- Toolbar -->
        <div class="table-toolbar">
          <div class="toolbar-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="searchQuery" placeholder="搜索部门名称..." class="search-input" />
          </div>
          <button class="btn-create" @click="openCreateModal(null)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            创建部门
          </button>
        </div>

        <!-- Table -->
        <div class="table-container">
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredDepts.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="4" y="4" width="16" height="16" rx="2"/>
              <rect x="9" y="9" width="6" height="6"/>
            </svg>
            <span>暂无部门数据</span>
          </div>
          <table v-else class="data-table">
            <thead>
              <tr>
                <th>部门名称</th>
                <th>排序</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="dept in filteredDepts" :key="dept.id">
                <td>
                  <div class="dept-name-cell">
                    <Building :size="16" class="dept-icon" />
                    {{ dept.deptName }}
                  </div>
                </td>
                <td>{{ dept.sort }}</td>
                <td>
                  <span class="status-tag" :class="dept.status === 1 ? 'active' : 'disabled'">
                    <span class="status-dot"></span>
                    {{ dept.status === 1 ? '正常' : '禁用' }}
                  </span>
                </td>
                <td>{{ formatTime(dept.createTime) }}</td>
                <td>
                  <div class="action-btns">
                    <button class="action-btn" title="添加子部门" @click="openCreateModal(dept)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="5" x2="12" y2="19"/>
                        <line x1="5" y1="12" x2="19" y2="12"/>
                      </svg>
                    </button>
                    <button class="action-btn edit" title="编辑" @click="openEditModal(dept)">
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                      </svg>
                    </button>
                    <button class="action-btn delete" title="删除" @click="handleDelete(dept)">
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
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑部门' : '创建部门' }}</span>
            <button class="modal-close" @click="showModal = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-field">
              <label>部门名称 <span class="required">*</span></label>
              <input v-model="form.deptName" placeholder="请输入部门名称" />
            </div>
            <div class="form-field">
              <label>上级部门</label>
              <select v-model="form.parentId" class="form-select">
                <option :value="0">无（顶级部门）</option>
                <option v-for="dept in allDeptsForSelect" :key="dept.id" :value="dept.id">
                  {{ '　'.repeat(getDepth(dept.id)) }}{{ dept.deptName }}
                </option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>排序</label>
                <input v-model.number="form.sort" type="number" placeholder="0" />
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
              <span v-else>{{ isEditing ? '保存修改' : '创建部门' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Building } from 'lucide-vue-next'
import { adminDeptApi, type DeptInfo } from '../../../api/adminDept'
import { deptApi, type DeptInfo as DeptTreeInfo } from '../../../api/dept'
import { useDesktopStore } from '../../../stores/desktop'
import DeptTreeNode from './DeptTreeNode.vue'

const desktop = useDesktopStore()

// ==================== Data ====================
const deptList = ref<DeptInfo[]>([])
const deptTree = ref<DeptTreeInfo[]>([])
const isLoading = ref(true)
const searchQuery = ref('')
const selectedDeptId = ref<number | null>(null)

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  parentId: 0,
  deptName: '',
  sort: 0,
  status: 1
})

// ==================== Computed ====================
const activeCount = computed(() => deptList.value.filter(d => d.status === 1).length)

// All depts for select dropdown (flat, used in parent picker)
const allDeptsForSelect = computed(() => {
  return deptList.value.filter(d => d.id !== form.value.id)
})

const filteredDepts = computed(() => {
  let list = deptList.value
  if (selectedDeptId.value !== null) {
    list = list.filter(d => d.parentId === selectedDeptId.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(d => d.deptName.toLowerCase().includes(q))
  }
  return list
})

// ==================== Helpers ====================
function getDepth(deptId: number): number {
  // approximate depth by counting ancestors (simplified)
  return 0
}

function formatTime(time: string) {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

// ==================== Load Data ====================
async function loadDeptTree() {
  try {
    deptTree.value = await deptApi.tree()
  } catch (e: any) {
    desktop.addToast('加载部门树失败', 'error')
  }
}

async function loadDeptList() {
  try {
    const res = await adminDeptApi.list(1, 100)
    deptList.value = res.records
  } catch (e: any) {
    desktop.addToast('加载部门列表失败', 'error')
  }
}

// ==================== Tree Select ====================
function onDeptSelect(id: number) {
  selectedDeptId.value = id
}

// ==================== Modal Actions ====================
function openCreateModal(parentDept: DeptInfo | null) {
  isEditing.value = false
  form.value = {
    id: 0,
    parentId: parentDept ? parentDept.id : 0,
    deptName: '',
    sort: 0,
    status: 1
  }
  showModal.value = true
}

function openEditModal(dept: DeptInfo) {
  isEditing.value = true
  form.value = {
    id: dept.id,
    parentId: dept.parentId,
    deptName: dept.deptName,
    sort: dept.sort,
    status: dept.status
  }
  showModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return
  if (!form.value.deptName.trim()) {
    desktop.addToast('请输入部门名称', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      await adminDeptApi.update({
        id: form.value.id,
        parentId: form.value.parentId,
        deptName: form.value.deptName,
        sort: form.value.sort,
        status: form.value.status
      })
      desktop.addToast('部门更新成功', 'success')
    } else {
      await adminDeptApi.create({
        parentId: form.value.parentId,
        deptName: form.value.deptName,
        sort: form.value.sort
      })
      desktop.addToast('部门创建成功', 'success')
    }
    showModal.value = false
    await Promise.all([loadDeptTree(), loadDeptList()])
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(dept: DeptInfo) {
  if (!confirm(`确定要删除部门「${dept.deptName}」吗？`)) return
  try {
    await adminDeptApi.delete(dept.id)
    desktop.addToast('部门已删除', 'success')
    await Promise.all([loadDeptTree(), loadDeptList()])
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await Promise.all([loadDeptTree(), loadDeptList()])
  isLoading.value = false
})
</script>

<style scoped>
.dept-app {
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
  color: #64D2FF;
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
.dept-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ========== Left Tree Panel ========== */
.dept-panel {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dept-panel-header {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-subtle);
}

.dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.tree-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 6px;
  margin: 0 8px;
  font-size: 13px;
  color: var(--text-primary);
  transition: all 0.15s;
  user-select: none;
}

.tree-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.tree-item.active {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.tree-root {
  font-weight: 500;
  margin-bottom: 4px;
}

.toggle-icon {
  display: flex;
  align-items: center;
  color: var(--text-disabled);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-disabled);
  flex-shrink: 0;
}

.dot-root {
  background: #64D2FF;
}

.tree-icon {
  color: #64D2FF;
  flex-shrink: 0;
}

.tree-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== Right Table Panel ========== */
.table-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
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
  width: 240px;
  transition: all 0.2s;
}

.toolbar-search:focus-within {
  border-color: #64D2FF;
  box-shadow: 0 0 0 3px rgba(100, 210, 255, 0.1);
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
  background: linear-gradient(135deg, #64D2FF, #5AC8FA);
  color: white;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(100, 210, 255, 0.3);
}

.btn-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(100, 210, 255, 0.4);
}

/* ========== Table ========== */
.table-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
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
  border-top-color: #64D2FF;
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
  padding: 10px 12px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}

.data-table td {
  padding: 12px 12px;
  font-size: 13px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.data-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.dept-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.dept-icon {
  color: #64D2FF;
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

.action-btn:hover { background: rgba(100, 210, 255, 0.12); color: #64D2FF; }
.action-btn.edit:hover { background: rgba(10, 132, 255, 0.12); color: #0A84FF; }
.action-btn.delete:hover { background: rgba(255, 69, 58, 0.12); color: #FF453A; }

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
  width: 480px;
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
  border-color: #64D2FF;
  box-shadow: 0 0 0 3px rgba(100, 210, 255, 0.1);
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
  background: linear-gradient(135deg, #64D2FF, #5AC8FA);
  color: white;
  box-shadow: 0 2px 8px rgba(100, 210, 255, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(100, 210, 255, 0.4);
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
