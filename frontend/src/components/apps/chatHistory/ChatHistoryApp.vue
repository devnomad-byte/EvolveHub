<template>
  <div class="chat-history-app">
    <!-- Header -->
    <div class="ch-header">
      <div class="ch-header-left">
        <svg class="ch-header-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="ch-header-title">对话历史</span>
      </div>
      <div class="ch-header-stats">
        <span class="ch-stat-badge">
          <span class="ch-stat-num">{{ totalSessions }}</span>
          <span class="ch-stat-label">会话总数</span>
        </span>
      </div>
    </div>

    <!-- Main: Left Sessions + Right Messages -->
    <div class="ch-main">
      <!-- Left: Session List -->
      <div class="ch-sessions-panel">
        <div class="ch-sessions-toolbar">
          <div class="ch-search-box">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="searchQuery" placeholder="搜索会话标题..." class="ch-search-input" />
          </div>
        </div>

        <div class="ch-sessions-list">
          <div v-if="sessionsLoading" class="ch-loading">加载中...</div>
          <template v-else>
            <div
              v-for="session in filteredSessions"
              :key="session.id"
              class="ch-session-card"
              :class="{ active: selectedSession?.id === session.id }"
              @click="selectSession(session)"
            >
              <div class="ch-session-title">{{ session.title || '未命名会话' }}</div>
              <div class="ch-session-meta">
                <span class="ch-meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  {{ session.messageCount || 0 }}
                </span>
                <span class="ch-meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                  {{ formatTokens(session.totalTokens) }}
                </span>
              </div>
              <div class="ch-session-time">{{ formatTime(session.createTime) }}</div>
            </div>
            <div v-if="filteredSessions.length === 0" class="ch-empty">暂无会话记录</div>
          </template>
        </div>

        <!-- Pagination -->
        <div class="ch-sessions-pagination">
          <button class="ch-page-btn" :disabled="sessionPage <= 1" @click="sessionPage--; loadSessions()">上一页</button>
          <span class="ch-page-info">{{ sessionPage }} / {{ totalSessionPages }}</span>
          <button class="ch-page-btn" :disabled="sessionPage >= totalSessionPages" @click="sessionPage++; loadSessions()">下一页</button>
        </div>
      </div>

      <!-- Right: Messages -->
      <div class="ch-messages-panel">
        <template v-if="selectedSession">
          <div class="ch-messages-header">
            <div class="ch-msg-session-title">{{ selectedSession.title || '未命名会话' }}</div>
            <div class="ch-msg-session-info">
              用户 ID: {{ selectedSession.userId }} · 消息 {{ selectedSession.messageCount || 0 }} 条 · Tokens: {{ formatTokens(selectedSession.totalTokens) }}
            </div>
          </div>
          <div class="ch-messages-list" ref="messagesListRef">
            <div v-if="messagesLoading" class="ch-loading">加载消息中...</div>
            <template v-else>
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="ch-message-item"
                :class="msg.role"
              >
                <div class="ch-msg-role-badge" :class="msg.role">
                  {{ roleLabel(msg.role) }}
                </div>
                <div class="ch-msg-content">
                  <div class="ch-msg-text" v-html="renderContent(msg.content)"></div>
                  <div v-if="msg.modelName" class="ch-msg-model">{{ msg.modelName }}</div>
                  <div class="ch-msg-meta">
                    <span v-if="msg.promptTokens">输入: {{ msg.promptTokens }}</span>
                    <span v-if="msg.completionTokens">输出: {{ msg.completionTokens }}</span>
                    <span v-if="msg.durationMs">耗时: {{ msg.durationMs }}ms</span>
                    <span>{{ formatTime(msg.createTime) }}</span>
                  </div>
                </div>
              </div>
              <div v-if="messages.length === 0" class="ch-empty">暂无消息记录</div>
            </template>
          </div>
          <!-- Message Pagination -->
          <div class="ch-messages-pagination">
            <button class="ch-page-btn" :disabled="msgPage <= 1" @click="msgPage--; loadMessages()">上一页</button>
            <span class="ch-page-info">{{ msgPage }} / {{ totalMsgPages }}</span>
            <button class="ch-page-btn" :disabled="msgPage >= totalMsgPages" @click="msgPage++; loadMessages()">下一页</button>
          </div>
        </template>
        <div v-else class="ch-no-selection">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="opacity:0.2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>选择左侧会话查看详情</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminChatHistoryApi, type ChatSessionInfo, type ChatMessageInfo } from '@/api/adminChatHistory'

const searchQuery = ref('')
const sessions = ref<ChatSessionInfo[]>([])
const messages = ref<ChatMessageInfo[]>([])
const selectedSession = ref<ChatSessionInfo | null>(null)
const sessionsLoading = ref(false)
const messagesLoading = ref(false)
const sessionPage = ref(1)
const msgPage = ref(1)
const totalSessions = ref(0)
const totalMessages = ref(0)
const sessionPageSize = 15
const msgPageSize = 20
const messagesListRef = ref<HTMLElement | null>(null)

const totalSessionPages = computed(() => Math.max(1, Math.ceil(totalSessions.value / sessionPageSize)))
const totalMsgPages = computed(() => Math.max(1, Math.ceil(totalMessages.value / msgPageSize)))

const filteredSessions = computed(() => {
  if (!searchQuery.value) return sessions.value
  const q = searchQuery.value.toLowerCase()
  return sessions.value.filter(s => (s.title || '').toLowerCase().includes(q))
})

async function loadSessions() {
  sessionsLoading.value = true
  try {
    const res = await adminChatHistoryApi.getSessions(sessionPage.value, sessionPageSize)
    sessions.value = res.records
    totalSessions.value = res.total
  } catch (e) {
    console.error('Failed to load sessions', e)
  } finally {
    sessionsLoading.value = false
  }
}

async function loadMessages() {
  if (!selectedSession.value) return
  messagesLoading.value = true
  try {
    const res = await adminChatHistoryApi.getMessages(selectedSession.value.id, msgPage.value, msgPageSize)
    messages.value = res.records
    totalMessages.value = res.total
  } catch (e) {
    console.error('Failed to load messages', e)
  } finally {
    messagesLoading.value = false
  }
}

function selectSession(session: ChatSessionInfo) {
  selectedSession.value = session
  msgPage.value = 1
  loadMessages()
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

function renderContent(content: string): string {
  if (!content) return ''
  return content.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br/>')
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chat-history-app {
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
}

.ch-header-icon {
  color: #0A84FF;
}

.ch-header-title {
  font-size: 14px;
  font-weight: 600;
}

.ch-header-stats {
  display: flex;
  gap: 8px;
}

.ch-stat-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 12px;
}

.ch-stat-num {
  font-weight: 600;
  color: #0A84FF;
}

.ch-stat-label {
  color: var(--text-secondary);
}

/* Main layout */
.ch-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Left panel: sessions */
.ch-sessions-panel {
  width: 300px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.ch-sessions-toolbar {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-subtle);
}

.ch-search-box {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.ch-search-box:focus-within {
  border-color: rgba(10, 132, 255, 0.3);
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.1);
}

.ch-search-box svg {
  color: var(--text-secondary);
  flex-shrink: 0;
}

.ch-search-input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 12px;
  width: 100%;
}

.ch-search-input::placeholder {
  color: var(--text-disabled);
}

.ch-sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.ch-sessions-list::-webkit-scrollbar {
  width: 3px;
}

.ch-sessions-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.ch-session-card {
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.ch-session-card:hover {
  background: rgba(255, 255, 255, 0.04);
}

.ch-session-card.active {
  background: rgba(10, 132, 255, 0.1);
  border-color: rgba(10, 132, 255, 0.2);
}

.ch-session-title {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.ch-session-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 4px;
}

.ch-meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--text-secondary);
}

.ch-meta-item svg {
  opacity: 0.5;
}

.ch-session-time {
  font-size: 11px;
  color: var(--text-disabled);
}

/* Session pagination */
.ch-sessions-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
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

.ch-page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.ch-page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.ch-page-info {
  font-size: 11px;
  color: var(--text-secondary);
}

/* Right panel: messages */
.ch-messages-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ch-messages-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.ch-msg-session-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.ch-msg-session-info {
  font-size: 11px;
  color: var(--text-secondary);
}

.ch-messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.ch-messages-list::-webkit-scrollbar {
  width: 3px;
}

.ch-messages-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

/* Message item */
.ch-message-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.ch-message-item.user {
  align-items: flex-end;
}

.ch-message-item.assistant {
  align-items: flex-start;
}

.ch-message-item.system,
.ch-message-item.tool {
  align-items: center;
}

.ch-msg-role-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
}

.ch-msg-role-badge.user {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.ch-msg-role-badge.assistant {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.ch-msg-role-badge.system {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
}

.ch-msg-role-badge.tool {
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
}

.ch-msg-content {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.ch-message-item.user .ch-msg-content {
  background: rgba(10, 132, 255, 0.15);
  border: 1px solid rgba(10, 132, 255, 0.2);
}

.ch-message-item.assistant .ch-msg-content {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
}

.ch-message-item.system .ch-msg-content,
.ch-message-item.tool .ch-msg-content {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  max-width: 90%;
  font-size: 12px;
}

.ch-msg-text {
  word-break: break-word;
}

.ch-msg-model {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-secondary);
}

.ch-msg-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-disabled);
}

/* Message pagination */
.ch-messages-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

/* Empty / loading states */
.ch-loading {
  text-align: center;
  padding: 40px 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

.ch-empty {
  text-align: center;
  padding: 40px 16px;
  color: var(--text-disabled);
  font-size: 12px;
}

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
</style>
