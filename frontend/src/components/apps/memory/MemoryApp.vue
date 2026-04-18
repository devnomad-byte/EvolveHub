<template>
  <div class="memory-app">
    <div v-if="canSelectUsers" class="mem-sidebar">
      <div class="mem-sidebar-header">
        <span class="mem-sidebar-title">用户列表</span>
        <span class="mem-sidebar-count">{{ filteredUsers.length }}</span>
      </div>
      <div class="mem-search">
        <input v-model="userFilter" class="mem-search-input" placeholder="搜索用户..." />
      </div>
      <div class="mem-user-list">
        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="mem-user-item"
          :class="{ active: selectedUserId === user.id }"
          @click="selectedUserId = user.id"
        >
          <div class="mem-user-avatar">{{ user.avatarLabel }}</div>
          <div class="mem-user-info">
            <div class="mem-user-name">{{ user.name }}</div>
            <div class="mem-user-subtitle">{{ user.dept || '未分配部门' }}</div>
          </div>
          <span class="mem-role-tag">{{ roleLabel(user.roleCode) }}</span>
        </div>
      </div>
    </div>

    <div class="mem-main">
      <template v-if="selectedUser">
        <div class="mem-detail-header">
          <div class="mem-detail-user">
            <span class="mem-detail-avatar">{{ selectedUser.avatarLabel }}</span>
            <div>
              <div class="mem-detail-name">{{ selectedUser.name }}</div>
              <div class="mem-detail-dept">
                {{ selectedUser.dept || '未分配部门' }}
                <span v-if="selectedUser.roleCode"> · {{ roleLabel(selectedUser.roleCode) }}</span>
              </div>
            </div>
          </div>
          <div class="mem-tabs">
            <button class="mem-tab" :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">画像 Markdown</button>
            <button class="mem-tab" :class="{ active: activeTab === 'long' }" @click="activeTab = 'long'">长期记忆</button>
          </div>
        </div>

        <div v-if="activeTab === 'profile'" class="mem-tab-content">
          <div class="mem-file-card">
            <div class="mem-file-header">
              <div class="mem-file-name">
                <span class="mem-file-icon">📄</span>
                MEMORY.md
              </div>
              <div class="mem-file-actions">
                <button v-if="!isEditingProfile" class="mem-btn mem-btn-ghost" :disabled="loadingDetail" @click="startEditProfile">编辑</button>
                <button v-if="isEditingProfile" class="mem-btn primary" :disabled="savingProfile" @click="saveProfile">
                  {{ savingProfile ? '保存中...' : '保存' }}
                </button>
                <button v-if="isEditingProfile" class="mem-btn mem-btn-ghost" :disabled="savingProfile" @click="cancelEditProfile">取消</button>
                <button class="mem-btn mem-btn-ghost" :disabled="loadingDetail" @click="refreshSelectedUser">刷新</button>
              </div>
            </div>
            <div class="mem-file-meta">
              <span>当前用户画像</span>
              <span>{{ profileDraft.length }} 字</span>
            </div>
            <div class="mem-file-body">
              <textarea
                v-if="isEditingProfile"
                v-model="profileDraft"
                class="mem-file-editor"
                spellcheck="false"
              ></textarea>
              <pre v-else>{{ profileMarkdown || '暂无画像内容' }}</pre>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'long'" class="mem-tab-content">
          <div class="mem-long-toolbar">
            <input v-model="memorySearch" class="mem-search-input mem-search-wide" placeholder="搜索长期记忆..." />
            <div class="mem-long-toolbar-right">
              <select v-model="memoryType" class="mem-select">
                <option value="all">全部类型</option>
                <option value="preference">偏好</option>
                <option value="fact">事实</option>
                <option value="tool_config">工具配置</option>
              </select>
              <button class="mem-btn mem-btn-ghost" :disabled="loadingDetail" @click="refreshSelectedUser">刷新</button>
            </div>
          </div>
          <div class="mem-long-list">
            <div v-if="loadingDetail" class="mem-empty">正在加载记忆...</div>
            <template v-else>
              <div v-for="memory in filteredMemories" :key="memory.id" class="mem-long-item">
                <div class="mem-long-left">
                  <span class="mem-type-badge" :class="'type-' + normalizeMemoryType(memory.memoryType)">
                    {{ typeLabel[normalizeMemoryType(memory.memoryType)] || '其他' }}
                  </span>
                  <div class="mem-long-content">{{ memory.content }}</div>
                </div>
                <div class="mem-long-right">
                  <div class="mem-importance">
                    <div class="mem-importance-bar">
                      <div
                        class="mem-importance-fill"
                        :class="'imp-' + importanceLevel(toImportance(memory.importance))"
                        :style="{ width: `${toImportance(memory.importance) * 100}%` }"
                      ></div>
                    </div>
                    <span class="mem-importance-val">{{ formatImportance(memory.importance) }}</span>
                  </div>
                  <span class="mem-long-date">{{ formatDate(memory.updatedAt) }}</span>
                  <div class="mem-long-actions">
                    <button class="mem-btn danger" @click="removeMemory(memory.id)">删除</button>
                  </div>
                </div>
              </div>
              <div v-if="!filteredMemories.length" class="mem-empty">暂无匹配的长期记忆</div>
            </template>
          </div>
        </div>
      </template>

      <div v-else class="mem-placeholder">
        <span class="mem-placeholder-icon">🧠</span>
        <div class="mem-placeholder-text">{{ loadingUsers ? '正在加载用户...' : '暂无可查看的记忆数据' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { adminUserApi } from '@/api/adminUser'
import { memoryApi, type MemoryManagedItemRecord } from '@/api/memory'
import { useDesktopStore } from '@/stores/desktop'

interface MemoryUserOption {
  id: number
  name: string
  dept: string
  roleCode: string
  avatarLabel: string
}

const desktop = useDesktopStore()
const loadingUsers = ref(false)
const loadingDetail = ref(false)
const savingProfile = ref(false)
const selectedUserId = ref<number | null>(null)
const activeTab = ref<'profile' | 'long'>('profile')
const userFilter = ref('')
const memorySearch = ref('')
const memoryType = ref('all')
const isEditingProfile = ref(false)
const profileMarkdown = ref('')
const profileDraft = ref('')
const users = ref<MemoryUserOption[]>([])
const memoryItems = ref<MemoryManagedItemRecord[]>([])

const typeLabel: Record<string, string> = {
  preference: '偏好',
  fact: '事实',
  tool_config: '工具配置'
}

const canSelectUsers = computed(() => {
  const roles = desktop.currentUser?.roles || []
  return roles.includes('SUPER_ADMIN') || roles.includes('ADMIN')
})

const filteredUsers = computed(() => {
  const keyword = userFilter.value.trim()
  if (!keyword) {
    return users.value
  }
  return users.value.filter(user => user.name.includes(keyword) || user.dept.includes(keyword))
})

const selectedUser = computed(() =>
  users.value.find(user => user.id === selectedUserId.value) || null
)

const filteredMemories = computed(() => {
  const keyword = memorySearch.value.trim()
  return memoryItems.value.filter(item => {
    const normalizedType = normalizeMemoryType(item.memoryType)
    if (memoryType.value !== 'all' && normalizedType !== memoryType.value) {
      return false
    }
    if (keyword && !item.content.includes(keyword)) {
      return false
    }
    return true
  })
})

watch(selectedUserId, () => {
  if (selectedUserId.value != null) {
    void loadSelectedUserData()
  }
})

onMounted(() => {
  void loadUsers()
})

async function loadUsers() {
  loadingUsers.value = true
  try {
    if (canSelectUsers.value) {
      const userList = await adminUserApi.list()
      users.value = userList.map(user => ({
        id: user.id,
        name: user.nickname || user.username,
        dept: user.deptName || '',
        roleCode: user.roles?.[0]?.roleCode || 'USER',
        avatarLabel: buildAvatarLabel(user.nickname || user.username)
      }))
    } else if (desktop.currentUser) {
      users.value = [{
        id: desktop.currentUser.id,
        name: desktop.currentUser.displayName || desktop.currentUser.username,
        dept: desktop.currentUser.deptName || '',
        roleCode: desktop.currentUser.roles?.[0] || 'USER',
        avatarLabel: buildAvatarLabel(desktop.currentUser.displayName || desktop.currentUser.username)
      }]
    } else {
      users.value = []
    }

    if (!users.value.length) {
      selectedUserId.value = null
      return
    }
    if (!users.value.some(user => user.id === selectedUserId.value)) {
      selectedUserId.value = users.value[0].id
    }
  } catch (error) {
    console.error('加载记忆用户列表失败', error)
    desktop.addToast(error instanceof Error ? error.message : '加载用户列表失败', 'error')
  } finally {
    loadingUsers.value = false
  }
}

async function loadSelectedUserData() {
  if (selectedUserId.value == null) {
    return
  }
  loadingDetail.value = true
  isEditingProfile.value = false
  try {
    const [profile, items] = await Promise.all([
      memoryApi.getProfile(selectedUserId.value),
      memoryApi.listItems(selectedUserId.value)
    ])
    profileMarkdown.value = profile.markdownContent || ''
    profileDraft.value = profile.markdownContent || ''
    memoryItems.value = Array.isArray(items) ? items : []
  } catch (error) {
    console.error('加载记忆详情失败', error)
    desktop.addToast(error instanceof Error ? error.message : '加载记忆详情失败', 'error')
    profileMarkdown.value = ''
    profileDraft.value = ''
    memoryItems.value = []
  } finally {
    loadingDetail.value = false
  }
}

function startEditProfile() {
  profileDraft.value = profileMarkdown.value
  isEditingProfile.value = true
}

function cancelEditProfile() {
  profileDraft.value = profileMarkdown.value
  isEditingProfile.value = false
}

async function saveProfile() {
  if (selectedUserId.value == null) {
    return
  }
  savingProfile.value = true
  try {
    const profile = await memoryApi.saveProfile({
      targetUserId: selectedUserId.value,
      markdownContent: profileDraft.value
    })
    profileMarkdown.value = profile.markdownContent || ''
    profileDraft.value = profile.markdownContent || ''
    isEditingProfile.value = false
    desktop.addToast('画像已保存', 'success')
  } catch (error) {
    console.error('保存画像失败', error)
    desktop.addToast(error instanceof Error ? error.message : '保存画像失败', 'error')
  } finally {
    savingProfile.value = false
  }
}

async function refreshSelectedUser() {
  await loadSelectedUserData()
}

async function removeMemory(memoryId: number) {
  if (selectedUserId.value == null) {
    return
  }
  if (!window.confirm('确认删除这条长期记忆吗？')) {
    return
  }
  try {
    await memoryApi.deleteItem(memoryId, selectedUserId.value)
    memoryItems.value = memoryItems.value.filter(item => item.id !== memoryId)
    desktop.addToast('长期记忆已删除', 'success')
  } catch (error) {
    console.error('删除长期记忆失败', error)
    desktop.addToast(error instanceof Error ? error.message : '删除长期记忆失败', 'error')
  }
}

function normalizeMemoryType(memoryType: string | undefined) {
  return (memoryType || '').trim().toLowerCase() || 'fact'
}

function roleLabel(roleCode: string) {
  const labels: Record<string, string> = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    LEADER: '高层领导',
    DEPT_HEAD: '部门负责人',
    USER: '普通用户'
  }
  return labels[roleCode] || roleCode || '未知角色'
}

function buildAvatarLabel(name: string) {
  const normalizedName = (name || '').trim()
  return normalizedName ? normalizedName.charAt(0).toUpperCase() : 'U'
}

function toImportance(value: number | undefined) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 0
  }
  if (value < 0) {
    return 0
  }
  if (value > 1) {
    return 1
  }
  return value
}

function importanceLevel(importance: number) {
  if (importance >= 0.8) return 'high'
  if (importance >= 0.6) return 'mid'
  return 'low'
}

function formatImportance(value: number | undefined) {
  return `${Math.round(toImportance(value) * 100)}%`
}

function formatDate(value: string | undefined) {
  if (!value) {
    return '刚刚'
  }
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }
  return parsed.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.memory-app {
  height: 100%;
  display: flex;
  background: #0a0a14;
}

.mem-sidebar {
  width: 260px;
  min-width: 260px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.22);
}

.mem-sidebar-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.mem-sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.mem-sidebar-count {
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(10, 132, 255, 0.12);
  padding: 1px 8px;
  border-radius: 10px;
}

.mem-search {
  padding: 8px 12px;
}

.mem-search-input {
  width: 100%;
  height: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0 12px;
  font-size: 12px;
  color: #fff;
  outline: none;
  box-sizing: border-box;
}

.mem-search-input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.mem-search-input:focus {
  border-color: rgba(10, 132, 255, 0.45);
}

.mem-search-wide {
  height: 38px;
  font-size: 13px;
}

.mem-user-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px 10px;
}

.mem-user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 150ms ease;
}

.mem-user-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.mem-user-item.active {
  background: rgba(10, 132, 255, 0.1);
}

.mem-user-avatar,
.mem-detail-avatar {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #fff;
  flex-shrink: 0;
}

.mem-user-info {
  flex: 1;
  min-width: 0;
}

.mem-user-name,
.mem-detail-name {
  font-size: 13px;
  font-weight: 500;
  color: #fff;
}

.mem-user-subtitle,
.mem-detail-dept {
  margin-top: 3px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
}

.mem-role-tag,
.mem-type-badge {
  flex-shrink: 0;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(10, 132, 255, 0.12);
  color: rgba(255, 255, 255, 0.72);
  font-size: 10px;
}

.mem-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.mem-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.mem-detail-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mem-tabs {
  display: flex;
  gap: 8px;
}

.mem-tab {
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: transparent;
  color: rgba(255, 255, 255, 0.65);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
}

.mem-tab.active {
  background: rgba(10, 132, 255, 0.16);
  color: #fff;
}

.mem-tab-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.mem-file-card {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 16px;
  min-height: 100%;
}

.mem-file-header,
.mem-long-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mem-file-header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.mem-file-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.mem-file-actions,
.mem-long-toolbar-right,
.mem-long-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mem-file-meta {
  padding: 12px 18px 0;
  display: flex;
  justify-content: space-between;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

.mem-file-body {
  padding: 18px;
}

.mem-file-body pre,
.mem-file-editor {
  width: 100%;
  min-height: 420px;
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  color: #fff;
  background: transparent;
  border: none;
  outline: none;
}

.mem-file-editor {
  resize: vertical;
}

.mem-btn {
  border: none;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
}

.mem-btn.primary {
  background: #0A84FF;
  color: #fff;
}

.mem-btn.mem-btn-ghost {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.mem-btn.danger {
  background: rgba(255, 69, 58, 0.14);
  color: #fff;
}

.mem-select {
  height: 38px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
  padding: 0 10px;
}

.mem-long-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mem-long-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
}

.mem-long-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mem-type-badge.type-preference {
  background: rgba(255, 214, 10, 0.14);
}

.mem-type-badge.type-fact {
  background: rgba(10, 132, 255, 0.16);
}

.mem-type-badge.type-tool_config {
  background: rgba(48, 209, 88, 0.18);
}

.mem-long-content {
  line-height: 1.7;
}

.mem-long-right {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.mem-importance {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mem-importance-bar {
  width: 96px;
  height: 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.mem-importance-fill {
  height: 100%;
  border-radius: inherit;
}

.mem-importance-fill.imp-high {
  background: #30D158;
}

.mem-importance-fill.imp-mid {
  background: #FFD60A;
}

.mem-importance-fill.imp-low {
  background: #FF9F0A;
}

.mem-importance-val,
.mem-long-date {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.48);
}

.mem-placeholder,
.mem-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: rgba(255, 255, 255, 0.45);
}

.mem-placeholder-icon {
  font-size: 42px;
  margin-bottom: 10px;
}
</style>
