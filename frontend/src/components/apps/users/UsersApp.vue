<template>
  <div class="users-app">
    <!-- Header -->
    <div class="users-header">
      <div class="header-left">
        <svg class="header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <span class="header-title">用户管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ users.length }}</span>
          <span class="stat-label">总用户</span>
        </span>
        <span class="stat-badge active">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">启用</span>
        </span>
        <span class="stat-badge disabled">
          <span class="stat-num">{{ users.length - activeCount }}</span>
          <span class="stat-label">禁用</span>
        </span>
      </div>
    </div>

    <!-- Main Content: Left Dept Tree + Right User List -->
    <div class="users-main">
      <!-- Left: Department Tree -->
      <div class="dept-panel">
        <div class="dept-panel-header">组织架构</div>
        <div class="dept-tree">
          <!-- Root: All Users -->
          <div
            class="tree-item tree-root"
            :class="{ active: selectedDeptId === null }"
            @click="selectedDeptId = null"
          >
            <span class="dot dot-root"></span>
            <span class="tree-label">全部用户</span>
          </div>

          <!-- Tree Branches -->
          <template v-for="dept in deptTree" :key="dept.id">
            <DeptNode
              :dept="dept"
              :selected-id="selectedDeptId"
              :depth="0"
              @select="selectedDeptId = $event"
            />
          </template>
        </div>
        <!-- Dept Stats Bar -->
        <div class="dept-stats">
          <div class="stats-dept-name">{{ selectedDeptName }}</div>
          <div class="stats-row">
            <span class="stats-total">{{ deptUserList.length }} 人</span>
            <span class="stats-divider">·</span>
            <span class="stats-active">正常 {{ deptActiveCount }}</span>
            <span class="stats-divider">·</span>
            <span class="stats-disabled">禁用 {{ deptUserList.length - deptActiveCount }}</span>
          </div>
        </div>
      </div>

      <!-- Right: User List -->
      <div class="user-panel">
        <!-- Toolbar -->
        <div class="user-toolbar">
          <div class="toolbar-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="searchQuery" placeholder="搜索用户名、昵称..." class="search-input" />
          </div>
          <button class="btn-create" @click="openCreateModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="20" y1="8" x2="20" y2="14"/>
              <line x1="23" y1="11" x2="17" y2="11"/>
            </svg>
            创建用户
          </button>
        </div>

        <!-- User Cards -->
        <div class="user-list">
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="filteredUsers.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
            </svg>
            <span>暂无用户数据</span>
          </div>
          <div
            v-for="u in filteredUsers"
            :key="u.id"
            class="user-card"
          >
            <div class="card-left">
              <div class="card-avatar" :class="avatarColor(u.roles?.[0]?.roleCode || 'USER')">
                <img v-if="u.avatar && u.avatar.startsWith('data:')" :src="u.avatar" alt="" />
                <span v-else>{{ (u.nickname || u.username).charAt(0).toUpperCase() }}</span>
              </div>
              <div class="card-info">
                <div class="card-name">
                  <span class="name-text">{{ u.nickname || u.username }}</span>
                  <template v-for="r in u.roles" :key="r.id">
                    <span class="role-badge" :class="roleColor(r.roleCode)">
                      {{ roleLabel(r.roleCode) }}
                    </span>
                  </template>
                </div>
                <div class="card-meta">
                  <span class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"/><path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94"/></svg>
                    {{ u.username }}
                  </span>
                  <span v-if="u.deptName" class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    {{ u.deptName }}
                  </span>
                  <span v-if="u.email" class="meta-item">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                    {{ u.email }}
                  </span>
                </div>
              </div>
            </div>
            <div class="card-right">
              <span class="status-tag" :class="u.status === 1 ? 'active' : 'disabled'">
                <span class="status-dot"></span>
                {{ u.status === 1 ? '正常' : '已禁用' }}
              </span>
              <div class="card-actions">
                <button class="action-btn edit" title="编辑" @click="openEditModal(u)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button class="action-btn reset" title="重置密码" @click="openResetModal(u)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                </button>
                <button v-if="u.id !== currentUserId" class="action-btn delete" title="删除" @click="handleDelete(u)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
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
            <span class="modal-title">{{ isEditing ? '编辑用户' : '创建用户' }}</span>
            <button class="modal-close" @click="showModal = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>用户名 <span class="required">*</span></label>
                <input v-model="form.username" :disabled="isEditing" placeholder="请输入用户名" />
              </div>
              <div class="form-field">
                <label>昵称</label>
                <input v-model="form.nickname" placeholder="请输入昵称" />
              </div>
            </div>
            <div v-if="!isEditing" class="form-row">
              <div class="form-field">
                <label>密码 <span class="required">*</span></label>
                <input v-model="form.password" type="password" placeholder="请输入密码（6-20位）" />
              </div>
              <div class="form-field">
                <label>确认密码 <span class="required">*</span></label>
                <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>邮箱</label>
                <input v-model="form.email" placeholder="user@example.com" />
              </div>
              <div class="form-field">
                <label>手机号</label>
                <input v-model="form.phone" placeholder="请输入手机号" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>部门</label>
                <select v-model="form.deptId" class="form-select">
                  <option :value="0">无部门</option>
                  <option v-for="d in flatDepts" :key="d.id" :value="d.id">
                    {{ '&nbsp;&nbsp;'.repeat(d._level || 0) }}{{ d.deptName }}
                  </option>
                </select>
              </div>
              <div class="form-field">
                <label>角色</label>
                <div class="role-checkbox-list">
                  <label v-for="r in roleOptions" :key="r.id" class="role-checkbox-item">
                    <input type="checkbox" :value="r.id" v-model="form.roleIds" />
                    <span class="role-checkbox-label">{{ r.roleName }}</span>
                  </label>
                </div>
              </div>
            </div>
            <div class="form-row" v-if="isEditing">
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
              <span v-else>{{ isEditing ? '保存修改' : '创建用户' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Reset Password Modal -->
    <teleport to="body">
      <div v-if="showResetModal" class="modal-overlay" @click.self="showResetModal = false">
        <div class="modal modal-sm">
          <div class="modal-header">
            <span class="modal-title">重置密码 - {{ resetTarget?.nickname || resetTarget?.username }}</span>
            <button class="modal-close" @click="showResetModal = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="reset-warning">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF9F0A" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <span>重置后该用户需要使用新密码登录</span>
            </div>
            <div class="form-field">
              <label>新密码 <span class="required">*</span></label>
              <input v-model="resetPassword" type="password" placeholder="请输入新密码（6-20位）" />
            </div>
            <div class="form-field">
              <label>确认密码 <span class="required">*</span></label>
              <input v-model="resetConfirmPassword" type="password" placeholder="请再次输入新密码" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showResetModal = false">取消</button>
            <button class="btn btn-warning" @click="handleResetPassword" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>确认重置</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { adminUserApi, type UserInfo as AdminUserInfo } from '../../../api/adminUser'
import { deptApi, type DeptInfo } from '../../../api/dept'
import { useDesktopStore } from '../../../stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

// 类型别名，保持组件内使用 UserInfo
type UserInfo = AdminUserInfo

const desktop = useDesktopStore()
const { confirm } = useConfirm()
const currentUserId = computed(() => desktop.currentUser?.id || 0)

// ==================== Data ====================
const users = ref<UserInfo[]>([])
const deptTree = ref<DeptInfo[]>([])
const isLoading = ref(true)
const searchQuery = ref('')
const selectedDeptId = ref<number | null>(null)

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  deptId: 0,
  roleIds: [5] as number[],
  status: 1
})

const showResetModal = ref(false)
const resetTarget = ref<UserInfo | null>(null)
const resetPassword = ref('')
const resetConfirmPassword = ref('')

// ==================== Role Options ====================
const roleOptions = ref([
  { id: 1, roleName: '超级管理员', roleCode: 'SUPER_ADMIN' },
  { id: 2, roleName: '高层领导', roleCode: 'LEADER' },
  { id: 3, roleName: '部门负责人', roleCode: 'DEPT_HEAD' },
  { id: 4, roleName: '普通管理员', roleCode: 'ADMIN' },
  { id: 5, roleName: '普通员工', roleCode: 'USER' }
])

// ==================== Computed ====================
const activeCount = computed(() => users.value.filter(u => u.status === 1).length)

// 当前选中部门的用户列表
const deptUserList = computed(() => {
  if (selectedDeptId.value === null) return users.value
  return users.value.filter(u => u.deptId === selectedDeptId.value)
})

const deptActiveCount = computed(() => deptUserList.value.filter(u => u.status === 1).length)

// 当前选中的部门名称
const selectedDeptName = computed(() => {
  if (selectedDeptId.value === null) return '全部用户'
  function findDeptName(list: DeptInfo[]): string {
    for (const d of list) {
      if (d.id === selectedDeptId.value) return d.deptName
      if (d.children?.length) {
        const found = findDeptName(d.children)
        if (found) return found
      }
    }
    return ''
  }
  return findDeptName(deptTree.value) || '全部用户'
})

const flatDepts = computed(() => {
  const result: (DeptInfo & { _level?: number })[] = []
  function flatten(list: DeptInfo[], level: number) {
    for (const d of list) {
      result.push({ ...d, _level: level })
      if (d.children?.length) flatten(d.children, level + 1)
    }
  }
  flatten(deptTree.value, 0)
  return result
})

const filteredUsers = computed(() => {
  let list = users.value
  if (selectedDeptId.value !== null) {
    list = list.filter(u => u.deptId === selectedDeptId.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(u =>
      u.username.toLowerCase().includes(q) ||
      (u.nickname || '').toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q)
    )
  }
  return list
})

// ==================== Methods ====================
function roleLabel(code: string) {
  const map: Record<string, string> = {
    SUPER_ADMIN: '超管', LEADER: '高层', DEPT_HEAD: '部门负责人',
    ADMIN: '管理员', USER: '普通员工'
  }
  return map[code] || code
}

function roleColor(code: string) {
  const map: Record<string, string> = {
    SUPER_ADMIN: 'red', LEADER: 'orange', DEPT_HEAD: 'purple',
    ADMIN: 'blue', USER: 'green'
  }
  return map[code] || 'green'
}

function avatarColor(code: string) {
  const map: Record<string, string> = {
    SUPER_ADMIN: 'grad-red', LEADER: 'grad-orange', DEPT_HEAD: 'grad-purple',
    ADMIN: 'grad-blue', USER: 'grad-green'
  }
  return map[code] || 'grad-blue'
}

// getDeptUserCount was here but never used

// ==================== Load Data ====================
async function loadUsers() {
  try {
    users.value = await adminUserApi.list()
  } catch (e: any) {
    desktop.addToast(e.message || '加载用户列表失败', 'error')
  }
}

async function loadDepts() {
  try {
    deptTree.value = await deptApi.tree()
  } catch (e: any) {
    desktop.addToast(e.message || '加载部门树失败', 'error')
  }
}

// ==================== Modal Actions ====================
function openCreateModal() {
  isEditing.value = false
  form.value = {
    id: 0, username: '', password: '', confirmPassword: '',
    nickname: '', email: '', phone: '', deptId: 0, roleIds: [5], status: 1
  }
  showModal.value = true
}

function openEditModal(u: UserInfo) {
  isEditing.value = true
  form.value = {
    id: u.id,
    username: u.username,
    password: '', confirmPassword: '',
    nickname: u.nickname || '',
    email: u.email || '',
    phone: u.phone || '',
    deptId: u.deptId || 0,
    roleIds: u.roles?.map(r => r.id) || [5],
    status: u.status
  }
  showModal.value = true
}

function openResetModal(u: UserInfo) {
  resetTarget.value = u
  resetPassword.value = ''
  resetConfirmPassword.value = ''
  showResetModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return

  if (!isEditing.value) {
    if (!form.value.username.trim()) { desktop.addToast('请输入用户名', 'error'); return }
    if (!form.value.password || form.value.password.length < 6) { desktop.addToast('密码至少6位', 'error'); return }
    if (form.value.password !== form.value.confirmPassword) { desktop.addToast('两次密码不一致', 'error'); return }
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      await adminUserApi.update({
        id: form.value.id,
        nickname: form.value.nickname || undefined,
        email: form.value.email || undefined,
        phone: form.value.phone || undefined,
        deptId: form.value.deptId || undefined,
        roleIds: form.value.roleIds,
        status: form.value.status
      })
      desktop.addToast('用户更新成功', 'success')
    } else {
      await adminUserApi.create({
        username: form.value.username,
        password: form.value.password,
        nickname: form.value.nickname || undefined,
        email: form.value.email || undefined,
        phone: form.value.phone || undefined,
        deptId: form.value.deptId,
        roleIds: form.value.roleIds
      })
      desktop.addToast('用户创建成功', 'success')
    }
    showModal.value = false
    await loadUsers()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(u: UserInfo) {
  if (!await confirm('删除用户', `确定要删除用户「${u.nickname || u.username}」吗？此操作不可恢复。`)) return
  try {
    await adminUserApi.delete(u.id)
    desktop.addToast('用户已删除', 'success')
    await loadUsers()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function handleResetPassword() {
  if (!resetPassword.value || resetPassword.value.length < 6) {
    desktop.addToast('密码至少6位', 'error'); return
  }
  if (resetPassword.value !== resetConfirmPassword.value) {
    desktop.addToast('两次密码不一致', 'error'); return
  }
  if (!resetTarget.value) return
  isSubmitting.value = true
  try {
    await adminUserApi.resetPassword({
      userId: resetTarget.value.id,
      newPassword: resetPassword.value
    })
    desktop.addToast('密码重置成功', 'success')
    showResetModal.value = false
  } catch (e: any) {
    desktop.addToast(e.message || '重置失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await Promise.all([loadUsers(), loadDepts()])
  isLoading.value = false
})

// ==================== DeptNode Sub-Component ====================
const DeptNode = defineComponent({
  props: {
    dept: { type: Object as () => DeptInfo, required: true },
    selectedId: { type: Number as () => number | null, default: null },
    depth: { type: Number, default: 0 }
  },
  emits: ['select'],
  setup(props, { emit }) {
    const expanded = ref(true)
    const hasChildren = computed(() => (props.dept.children || []).length > 0)

    return () => {
      const children = props.dept.children || []
      const indent = props.depth * 16 + 10

      return h('div', { class: 'tree-branch' }, [
        h('div', {
          class: ['tree-item', { active: props.selectedId === props.dept.id }],
          style: { paddingLeft: indent + 'px' },
          onClick: () => emit('select', props.dept.id)
        }, [
          hasChildren.value
            ? h('span', {
                class: ['tree-toggle', { open: expanded.value }],
                onClick: (e: Event) => { e.stopPropagation(); expanded.value = !expanded.value }
              }, expanded.value ? '▾' : '▸')
            : h('span', { class: 'tree-toggle-spacer' }),
          h('span', { class: props.depth === 0 ? 'dot dot-solid' : 'dot dot-hollow' }),
          h('span', { class: 'tree-label' }, props.dept.deptName)
        ]),
        hasChildren.value && expanded.value
          ? h('div', { class: 'tree-children' },
              children.map((child: DeptInfo) =>
                h(DeptNode, {
                  key: child.id,
                  dept: child,
                  selectedId: props.selectedId,
                  depth: props.depth + 1,
                  onSelect: (id: number) => emit('select', id)
                })
              )
            )
          : null
      ])
    }
  }
})
</script>

<style scoped>
.users-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ========== Header ========== */
.users-header {
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
  color: #FF9F0A;
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
.users-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ========== Dept Panel ========== */
.dept-panel {
  width: 230px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.2);
  flex-shrink: 0;
}

.dept-panel-header {
  padding: 12px 14px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 1px;
  border-bottom: 1px solid var(--border-subtle);
}

.dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

/* --- Tree Item (single line, never wrap) --- */
.tree-item {
  height: 30px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding-right: 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.15s;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
}

.tree-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
}

.tree-item:hover .dot-hollow::after {
  border-color: rgba(255, 255, 255, 0.5);
}

.tree-item.active {
  background: rgba(255, 159, 10, 0.08);
  color: #FF9F0A;
}

.tree-root {
  padding-left: 14px !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  margin-bottom: 2px;
}

/* --- Triangle Toggle --- */
.tree-toggle {
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-disabled);
  cursor: pointer;
  border-radius: 3px;
  transition: all 0.15s;
  line-height: 1;
}

.tree-toggle:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
}

.tree-toggle-spacer {
  width: 16px;
  flex-shrink: 0;
}

/* --- Dot --- */
.dot {
  width: 14px;
  height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dot::after {
  content: '';
  border-radius: 50%;
  display: block;
}

.dot-root::after {
  width: 7px;
  height: 7px;
  background: #FF9F0A;
}

.dot-solid::after {
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.5);
}

.tree-item.active .dot-solid::after {
  background: #FF9F0A;
}

.dot-hollow::after {
  width: 6px;
  height: 6px;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  background: transparent;
  box-sizing: border-box;
}

.tree-item.active .dot-hollow::after {
  border-color: #FF9F0A;
}

/* --- Label --- */
.tree-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* --- Count Badge --- */
.tree-badge {
  font-size: 10px;
  color: var(--text-disabled);
  background: rgba(255, 255, 255, 0.05);
  padding: 0 5px;
  border-radius: 3px;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
  line-height: 16px;
}

.tree-item.active .tree-badge {
  background: rgba(255, 159, 10, 0.12);
  color: #FF9F0A;
}

.tree-children {
}

.tree-branch {
}

/* --- Dept Stats Bar --- */
.dept-stats {
  flex-shrink: 0;
  border-top: 1px solid var(--border-subtle);
  padding: 12px 14px;
  background: rgba(0, 0, 0, 0.15);
}

.stats-dept-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.stats-total {
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-divider {
  color: rgba(255, 255, 255, 0.15);
}

.stats-active {
  color: #30D158;
}

.stats-disabled {
  color: #FF453A;
}

/* ========== User Panel ========== */
.user-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== Toolbar ========== */
.user-toolbar {
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
  transition: all 0.2s;
  width: 260px;
}

.toolbar-search:focus-within {
  border-color: #FF9F0A;
  box-shadow: 0 0 0 3px rgba(255, 159, 10, 0.1);
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
  padding: 0;
}

.search-input::placeholder {
  color: var(--text-disabled);
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
  background: linear-gradient(135deg, #FF9F0A, #FF6B00);
  color: white;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(255, 159, 10, 0.3);
}

.btn-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 159, 10, 0.4);
}

/* ========== User List ========== */
.user-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  border-top-color: #FF9F0A;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== User Card ========== */
.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  transition: all 0.2s;
}

.user-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: var(--border-subtle);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.card-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-avatar.grad-red { background: linear-gradient(135deg, #FF453A, #FF2D55); }
.card-avatar.grad-orange { background: linear-gradient(135deg, #FF9F0A, #FF6B00); }
.card-avatar.grad-purple { background: linear-gradient(135deg, #BF5AF2, #9B59B6); }
.card-avatar.grad-blue { background: linear-gradient(135deg, #0A84FF, #5E5CE6); }
.card-avatar.grad-green { background: linear-gradient(135deg, #30D158, #34C759); }

.card-info {
  min-width: 0;
}

.card-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.meta-item svg {
  color: var(--text-disabled);
  flex-shrink: 0;
}

/* ========== Role Badge ========== */
.role-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.role-badge.red { background: rgba(255, 69, 58, 0.15); color: #FF453A; }
.role-badge.orange { background: rgba(255, 159, 10, 0.15); color: #FF9F0A; }
.role-badge.purple { background: rgba(191, 90, 242, 0.15); color: #BF5AF2; }
.role-badge.blue { background: rgba(10, 132, 255, 0.15); color: #0A84FF; }
.role-badge.green { background: rgba(48, 209, 88, 0.15); color: #30D158; }

/* ========== Card Right ========== */
.card-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.status-tag {
  display: flex;
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

.card-actions {
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
.action-btn.reset:hover { background: rgba(255, 159, 10, 0.12); color: #FF9F0A; }
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

.modal-sm {
  width: 400px;
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
  border-color: #FF9F0A;
  box-shadow: 0 0 0 3px rgba(255, 159, 10, 0.1);
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

/* ========== Role Checkbox List ========== */
.role-checkbox-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 6px 0;
}

.role-checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.15s;
  user-select: none;
}

.role-checkbox-item:hover {
  border-color: rgba(255, 159, 10, 0.3);
  background: rgba(255, 159, 10, 0.05);
}

.role-checkbox-item input[type="checkbox"] {
  display: none;
}

.role-checkbox-item:has(input:checked) {
  border-color: #FF9F0A;
  background: rgba(255, 159, 10, 0.1);
  color: #FF9F0A;
}

.role-checkbox-label {
  line-height: 1;
}
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

/* ========== Reset Warning ========== */
.reset-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: rgba(255, 159, 10, 0.08);
  border-left: 3px solid #FF9F0A;
  border-radius: 6px;
  font-size: 13px;
  color: #FF9F0A;
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
  background: linear-gradient(135deg, #FF9F0A, #FF6B00);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 159, 10, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 159, 10, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn-warning {
  background: linear-gradient(135deg, #FF9F0A, #FF6B00);
  color: white;
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
