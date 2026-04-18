<template>
  <div class="ch-app">
    <!-- Header -->
    <div class="ch-header">
      <div class="ch-header-left">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="ch-title">对话历史</span>
      </div>
      <div class="ch-header-right">
        <!-- Date Range -->
        <div class="ch-date-range">
          <input v-model="startDate" type="date" class="ch-date-input" title="开始日期" />
          <span class="ch-date-sep">至</span>
          <input v-model="endDate" type="date" class="ch-date-input" title="结束日期" />
          <button class="ch-date-btn" @click="applyDateFilter" title="应用筛选">筛选</button>
          <button v-if="startDate || endDate" class="ch-date-btn clear" @click="clearDateFilter" title="清除筛选">清除</button>
        </div>
        <!-- Export Button -->
        <button class="ch-export-btn" @click="handleExport" :disabled="exporting">
          <svg v-if="!exporting" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          <span v-if="exporting">导出中...</span>
          <span v-else>导出</span>
        </button>
      </div>
    </div>

    <!-- Main: Dept Tree + Users + Sessions + Messages -->
    <div class="ch-main">
      <!-- Left: Department Tree -->
      <div class="ch-dept-panel">
        <div class="ch-panel-header">组织架构</div>
        <div class="ch-dept-tree">
          <!-- Root: All Users -->
          <div
            class="tree-item tree-root"
            :class="{ active: selectedDeptId === null }"
            @click="selectedDeptId = null; loadUsers(1)"
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
              @select="onDeptSelect"
            />
          </template>
        </div>
      </div>

      <!-- Left-Middle: User List -->
      <div class="ch-users-panel">
        <div class="ch-panel-header">
          用户 ({{ userTotal }})
          <div class="ch-user-search">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="userKeyword" placeholder="搜索用户..." class="ch-search-input" @input="onUserSearch" />
          </div>
        </div>
        <div class="ch-user-list">
          <div v-if="usersLoading" class="ch-loading">加载中...</div>
          <template v-else>
            <div
              v-for="user in users"
              :key="user.userId"
              class="ch-user-card"
              :class="{ active: selectedUserId === user.userId }"
              @click="selectUser(user.userId)"
            >
              <div class="ch-user-avatar">{{ getInitials(user.nickname || user.username) }}</div>
              <div class="ch-user-info">
                <div class="ch-user-name" :title="user.nickname || user.username">{{ user.nickname || user.username }}</div>
                <div class="ch-user-meta">
                  <span class="ch-dept-name">{{ user.deptName || '未分配部门' }}</span>
                  <span class="ch-session-count">{{ user.sessionCount }} 条会话</span>
                </div>
              </div>
            </div>
            <div v-if="users.length === 0 && !usersLoading" class="ch-empty">无结果</div>
          </template>
        </div>
        <!-- User Pagination -->
        <div class="ch-pagination">
          <button class="ch-page-btn" :disabled="userPage <= 1" @click="loadUsers(userPage - 1)">上一页</button>
          <span class="ch-page-info">{{ userPage }} / {{ userTotalPages }}</span>
          <button class="ch-page-btn" :disabled="userPage >= userTotalPages" @click="loadUsers(userPage + 1)">下一页</button>
        </div>
      </div>

      <!-- Middle: Session List -->
      <div class="ch-sessions-panel">
        <div class="ch-panel-header">
          会话 {{ selectedUserId ? '— ' + (selectedUserNickname || '') : '' }}
          <div class="ch-session-search" v-if="selectedUserId">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="sessionKeyword" placeholder="搜索会话..." class="ch-search-input" @input="onSessionSearch" />
          </div>
        </div>
        <div class="ch-session-list">
          <div v-if="!selectedUserId" class="ch-empty-tip">← 请先选择用户</div>
          <div v-else-if="sessionsLoading" class="ch-loading">加载中...</div>
          <template v-else>
            <div
              v-for="session in sessions"
              :key="session.id"
              class="ch-session-card"
              :class="{ active: selectedSessionId === session.id }"
              @click="selectSession(session)"
            >
              <div class="ch-session-title" :title="session.title || '未命名会话'">
                {{ session.title || '未命名会话' }}
              </div>
              <div class="ch-session-meta">
                <span class="ch-session-model">{{ session.modelName || '未知模型' }}</span>
                <span class="ch-session-msgs">{{ session.messageCount }} 条</span>
              </div>
              <div class="ch-session-time">{{ formatTime(session.updateTime) }}</div>
            </div>
            <div v-if="sessions.length === 0 && !sessionsLoading" class="ch-empty">暂无会话</div>
          </template>
        </div>
        <!-- Session Pagination -->
        <div class="ch-pagination">
          <button class="ch-page-btn" :disabled="sessionPage <= 1" @click="loadSessions(sessionPage - 1)">上一页</button>
          <span class="ch-page-info">{{ sessionPage }} / {{ sessionTotalPages }}</span>
          <button class="ch-page-btn" :disabled="sessionPage >= sessionTotalPages" @click="loadSessions(sessionPage + 1)">下一页</button>
        </div>
      </div>

      <!-- Right: Message Detail -->
      <div class="ch-messages-panel">
        <template v-if="!selectedSessionId">
          <div class="ch-no-selection">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="opacity:0.15">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span>选择会话查看详情</span>
          </div>
        </template>
        <template v-else>
          <div class="ch-messages-header">
            <div class="ch-msg-title" :title="selectedSession?.title || '未命名会话'">{{ selectedSession?.title || '未命名会话' }}</div>
            <div class="ch-msg-meta">
              <span>用户: {{ selectedSession?.nickname }}</span>
              <span>{{ selectedSession?.messageCount }} 条消息</span>
              <span>{{ selectedSession?.modelName }}</span>
              <span>Tokens: {{ formatTokens(selectedSession?.totalTokens) }}</span>
            </div>
          </div>
          <div class="ch-messages-list">
            <div v-if="messagesLoading" class="ch-loading">加载消息中...</div>
            <template v-else>
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="ch-message-item"
                :class="msg.role"
              >
                <div class="ch-msg-role-badge" :class="msg.role">{{ roleLabel(msg.role) }}</div>
                <div class="ch-msg-bubble">
                  <div class="ch-msg-content" v-html="renderMarkdown(msg.content)"></div>
                  <div v-if="msg.modelName" class="ch-msg-model">{{ msg.modelName }}</div>
                  <div class="ch-msg-meta">
                    <span v-if="msg.promptTokens">输入: {{ msg.promptTokens }}</span>
                    <span v-if="msg.completionTokens">输出: {{ msg.completionTokens }}</span>
                    <span v-if="msg.durationMs">耗时: {{ msg.durationMs }}ms</span>
                    <span>{{ formatTime(msg.createTime) }}</span>
                  </div>
                </div>
              </div>
              <div v-if="messages.length === 0" class="ch-empty">暂无消息</div>
            </template>
          </div>
          <!-- Message Pagination -->
          <div class="ch-pagination">
            <button class="ch-page-btn" :disabled="msgPage <= 1" @click="loadMessages(msgPage - 1)">上一页</button>
            <span class="ch-page-info">{{ msgPage }} / {{ msgTotalPages }}</span>
            <button class="ch-page-btn" :disabled="msgPage >= msgTotalPages" @click="loadMessages(msgPage + 1)">下一页</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { adminChatHistoryApi, type UserChatActivity, type SessionItem, type ChatMessageInfo } from '@/api/adminChatHistory'
import { deptApi, type DeptInfo } from '@/api/dept'
import { useDesktopStore } from '@/stores/desktop'

const desktop = useDesktopStore()

// ==================== Search/Filter State ====================
const userKeyword = ref('')
const sessionKeyword = ref('')
const startDate = ref('')
const endDate = ref('')
const selectedDeptId = ref<number | null>(null)
const selectedUserId = ref<number | null>(null)
const selectedSessionId = ref<number | null>(null)
const selectedSession = ref<SessionItem | null>(null)
const selectedUserNickname = ref('')
const exporting = ref(false)

// ==================== Data State ====================
const deptTree = ref<DeptInfo[]>([])
const users = ref<UserChatActivity[]>([])
const sessions = ref<SessionItem[]>([])
const messages = ref<ChatMessageInfo[]>([])

const usersLoading = ref(false)
const sessionsLoading = ref(false)
const messagesLoading = ref(false)

// ==================== Pagination State ====================
const userPage = ref(1)
const sessionPage = ref(1)
const msgPage = ref(1)
const userTotal = ref(0)
const sessionTotal = ref(0)
const msgTotal = ref(0)
const userPageSize = 20
const sessionPageSize = 15
const msgPageSize = 20
const userTotalPages = computed(() => Math.max(1, Math.ceil(userTotal.value / userPageSize)))
const sessionTotalPages = computed(() => Math.max(1, Math.ceil(sessionTotal.value / sessionPageSize)))
const msgTotalPages = computed(() => Math.max(1, Math.ceil(msgTotal.value / msgPageSize)))

// ==================== Debounce Timers ====================
let userSearchTimer: ReturnType<typeof setTimeout> | null = null
let sessionSearchTimer: ReturnType<typeof setTimeout> | null = null

// ==================== Load Functions ====================
async function loadDeptTree() {
  try {
    deptTree.value = await deptApi.tree()
  } catch (e: any) {
    desktop.addToast('加载部门树失败', 'error')
  }
}

async function loadUsers(page = 1) {
  usersLoading.value = true
  try {
    const res = await adminChatHistoryApi.getUsers(
      userKeyword.value || undefined,
      selectedDeptId.value || undefined,
      page,
      userPageSize
    )
    users.value = res.records
    userTotal.value = res.total
    userPage.value = page
  } catch (e: any) {
    desktop.addToast('加载用户列表失败', 'error')
  } finally {
    usersLoading.value = false
  }
}

async function loadSessions(page = 1) {
  if (!selectedUserId.value) return
  sessionsLoading.value = true
  try {
    const res = await adminChatHistoryApi.getSessions(
      selectedUserId.value,
      sessionKeyword.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined,
      page,
      sessionPageSize
    )
    sessions.value = res.records
    sessionTotal.value = res.total
    sessionPage.value = page
  } catch (e: any) {
    desktop.addToast('加载会话列表失败', 'error')
  } finally {
    sessionsLoading.value = false
  }
}

async function loadMessages(page = 1) {
  if (!selectedSessionId.value) return
  messagesLoading.value = true
  try {
    const res = await adminChatHistoryApi.getMessages(selectedSessionId.value, page, msgPageSize)
    messages.value = res.records
    msgTotal.value = res.total
    msgPage.value = page
  } catch (e: any) {
    desktop.addToast('加载消息列表失败', 'error')
  } finally {
    messagesLoading.value = false
  }
}

// ==================== Event Handlers ====================
function onUserSearch() {
  if (userSearchTimer) clearTimeout(userSearchTimer)
  userSearchTimer = setTimeout(() => {
    userPage.value = 1
    loadUsers(1)
  }, 300)
}

function onSessionSearch() {
  if (sessionSearchTimer) clearTimeout(sessionSearchTimer)
  sessionSearchTimer = setTimeout(() => {
    sessionPage.value = 1
    loadSessions(1)
  }, 300)
}

function onDeptSelect(deptId: number) {
  selectedDeptId.value = deptId
  selectedUserId.value = null
  sessions.value = []
  messages.value = []
  selectedSessionId.value = null
  selectedSession.value = null
  userPage.value = 1
  loadUsers(1)
}

function selectUser(userId: number) {
  selectedUserId.value = userId
  selectedSessionId.value = null
  selectedSession.value = null
  sessions.value = []
  messages.value = []
  sessionPage.value = 1
  msgPage.value = 1
  sessionKeyword.value = ''
  const user = users.value.find(u => u.userId === userId)
  selectedUserNickname.value = user?.nickname || user?.username || ''
  loadSessions(1)
}

function selectSession(session: SessionItem) {
  selectedSessionId.value = session.id
  selectedSession.value = session
  msgPage.value = 1
  loadMessages(1)
}

function applyDateFilter() {
  sessionPage.value = 1
  loadSessions(1)
}

function clearDateFilter() {
  startDate.value = ''
  endDate.value = ''
  sessionPage.value = 1
  loadSessions(1)
}

async function handleExport() {
  exporting.value = true
  try {
    const result = await adminChatHistoryApi.exportChatHistory(
      selectedUserId.value || undefined,
      startDate.value || undefined,
      endDate.value || undefined,
      'md'
    )
    // Download file
    const blob = new Blob([result.content], { type: 'text/markdown;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = result.filename || `chat_history_${new Date().toISOString().substring(0, 10)}.md`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    desktop.addToast('导出成功', 'success')
  } catch (e: any) {
    desktop.addToast('导出失败: ' + (e.message || '未知错误'), 'error')
  } finally {
    exporting.value = false
  }
}

// ==================== Utility Functions ====================
function getInitials(name: string): string {
  if (!name) return '?'
  return name.slice(0, 1).toUpperCase()
}

function roleLabel(role: string): string {
  const map: Record<string, string> = { user: '用户', assistant: '助手', system: '系统', tool: '工具' }
  return map[role] || role
}

function formatTokens(n: number | undefined): string {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function formatTime(t: string | undefined): string {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function escapeHtml(text: string): string {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function renderMarkdown(content: string): string {
  if (!content) return ''
  let text = escapeHtml(content)
  // Code blocks (```...```)
  text = text.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="md-code"><code>$2</code></pre>')
  // Inline code (`...`)
  text = text.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')
  // Bold (**...**)
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  // Italic (*...*)
  text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  // Headers (# ...)
  text = text.replace(/^### (.+)$/gm, '<h4 class="md-h4">$1</h4>')
  text = text.replace(/^## (.+)$/gm, '<h3 class="md-h3">$1</h3>')
  text = text.replace(/^# (.+)$/gm, '<h2 class="md-h2">$1</h2>')
  // Lists (- ...)
  text = text.replace(/^- (.+)$/gm, '<li class="md-li">$1</li>')
  // Numbered lists (1. ...)
  text = text.replace(/^\d+\. (.+)$/gm, '<li class="md-li">$1</li>')
  // Line breaks
  text = text.replace(/\n/g, '<br/>')
  return text
}

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

// ==================== Init ====================
onMounted(async () => {
  await Promise.all([loadDeptTree(), loadUsers(1)])
})
</script>

<style scoped>
.ch-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  color: var(--text-primary);
}

/* Header */
.ch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.ch-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0A84FF;
}

.ch-title {
  font-size: 14px;
  font-weight: 600;
}

.ch-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Date Range */
.ch-date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ch-date-input {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  color: var(--text-primary);
  outline: none;
  width: 110px;
  color-scheme: dark;
}

.ch-date-input:focus {
  border-color: rgba(10, 132, 255, 0.4);
}

.ch-date-sep {
  color: var(--text-disabled);
  font-size: 12px;
}

.ch-date-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.15s;
}

.ch-date-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.ch-date-btn.clear {
  color: #FF9F0A;
}

/* Export Button */
.ch-export-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #0A84FF, #5AC8FA);
  border: none;
  color: white;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.ch-export-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.ch-export-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Main layout */
.ch-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Dept Panel */
.ch-dept-panel {
  width: 200px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.ch-dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.ch-dept-tree::-webkit-scrollbar { width: 3px; }
.ch-dept-tree::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

/* Tree Items */
.tree-item {
  height: 30px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding-right: 10px;
  cursor: pointer;
  font-size: 12px;
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

.tree-item.active {
  background: rgba(10, 132, 255, 0.08);
  color: #0A84FF;
}

.tree-root {
  padding-left: 14px !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  margin-bottom: 2px;
}

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
  background: #0A84FF;
}

.dot-solid::after {
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.5);
}

.tree-item.active .dot-solid::after {
  background: #0A84FF;
}

.dot-hollow::after {
  width: 6px;
  height: 6px;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  background: transparent;
  box-sizing: border-box;
}

.tree-item.active .dot-hollow::after {
  border-color: #0A84FF;
}

.tree-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Users Panel */
.ch-users-panel {
  width: 220px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.ch-sessions-panel {
  width: 280px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.ch-messages-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ch-panel-header {
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ch-user-search,
.ch-session-search {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid transparent;
}

.ch-user-search:focus-within,
.ch-session-search:focus-within {
  border-color: rgba(10, 132, 255, 0.3);
}

.ch-user-search svg,
.ch-session-search svg { color: var(--text-secondary); flex-shrink: 0; }

.ch-search-input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 11px;
  width: 100%;
}

.ch-search-input::placeholder { color: var(--text-disabled); }

/* User list */
.ch-user-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.ch-user-list::-webkit-scrollbar { width: 3px; }
.ch-user-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.ch-user-card {
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

.ch-user-card:hover { background: rgba(255, 255, 255, 0.04); }
.ch-user-card.active { background: rgba(10, 132, 255, 0.1); border-color: rgba(10, 132, 255, 0.25); }

.ch-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.ch-user-info { flex: 1; min-width: 0; }

.ch-user-name {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ch-user-meta {
  display: flex;
  gap: 6px;
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-secondary);
}

.ch-session-count {
  background: rgba(10, 132, 255, 0.12);
  color: #0A84FF;
  padding: 1px 6px;
  border-radius: 8px;
  font-size: 10px;
}

/* Session list */
.ch-session-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.ch-session-list::-webkit-scrollbar { width: 3px; }
.ch-session-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.ch-empty-tip {
  text-align: center;
  padding: 40px 16px;
  color: var(--text-disabled);
  font-size: 12px;
}

.ch-session-card {
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.ch-session-card:hover { background: rgba(255, 255, 255, 0.04); }
.ch-session-card.active { background: rgba(10, 132, 255, 0.1); border-color: rgba(10, 132, 255, 0.25); }

.ch-session-title {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 5px;
}

.ch-session-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.ch-session-model { color: #BF5AF2; }
.ch-session-msgs { color: #30D158; }

.ch-session-time {
  font-size: 11px;
  color: var(--text-disabled);
}

/* Messages */
.ch-messages-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.ch-msg-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ch-msg-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 11px;
  color: var(--text-secondary);
}

.ch-messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.ch-messages-list::-webkit-scrollbar { width: 3px; }
.ch-messages-list::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }

.ch-message-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.ch-message-item.user { align-items: flex-end; }
.ch-message-item.assistant { align-items: flex-start; }
.ch-message-item.system, .ch-message-item.tool { align-items: center; }

.ch-msg-role-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
}

.ch-msg-role-badge.user { background: rgba(10, 132, 255, 0.15); color: #0A84FF; }
.ch-msg-role-badge.assistant { background: rgba(48, 209, 88, 0.15); color: #30D158; }
.ch-msg-role-badge.system { background: rgba(255, 159, 10, 0.15); color: #FF9F0A; }
.ch-msg-role-badge.tool { background: rgba(191, 90, 242, 0.15); color: #BF5AF2; }

.ch-msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.ch-message-item.user .ch-msg-bubble { background: rgba(10, 132, 255, 0.12); border: 1px solid rgba(10, 132, 255, 0.2); }
.ch-message-item.assistant .ch-msg-bubble { background: rgba(255, 255, 255, 0.04); border: 1px solid var(--border-subtle); }
.ch-message-item.system .ch-msg-bubble, .ch-message-item.tool .ch-msg-bubble { background: rgba(255, 255, 255, 0.03); border: 1px solid var(--border-subtle); max-width: 90%; font-size: 12px; }

.ch-msg-content { word-break: break-word; }
.ch-msg-model { margin-top: 6px; font-size: 11px; color: var(--text-secondary); }
.ch-msg-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 6px; font-size: 11px; color: var(--text-disabled); }

/* Markdown Styles */
:deep(.md-code) {
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 10px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
}

:deep(.md-inline-code) {
  background: rgba(0, 0, 0, 0.3);
  padding: 1px 5px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

:deep(.md-h4) {
  font-size: 14px;
  font-weight: 600;
  margin: 12px 0 6px;
  color: var(--text-primary);
}

:deep(.md-h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 14px 0 8px;
  color: var(--text-primary);
}

:deep(.md-h2) {
  font-size: 18px;
  font-weight: 700;
  margin: 16px 0 10px;
  color: var(--text-primary);
}

:deep(.md-li) {
  margin: 4px 0;
  padding-left: 8px;
}

/* No selection */
.ch-no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-disabled);
  font-size: 13px;
}

/* Pagination */
.ch-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.ch-page-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 11px;
  padding: 4px 10px;
  cursor: pointer;
  transition: background 0.15s;
}

.ch-page-btn:hover:not(:disabled) { background: rgba(255, 255, 255, 0.1); }
.ch-page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.ch-page-info { font-size: 11px; color: var(--text-secondary); }

/* States */
.ch-loading, .ch-empty {
  text-align: center;
  padding: 30px 16px;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
