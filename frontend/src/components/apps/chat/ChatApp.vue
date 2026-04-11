<template>
  <div class="chat-app">
    <!-- Left sidebar -->
    <div class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-search">
        <input v-model="searchQuery" placeholder="搜索会话" />
      </div>
      <div class="session-list">
        <div
          v-for="s in filteredSessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === activeSession }"
          @click="activeSession = s.id"
        >
          <div class="session-title">{{ s.title }}</div>
          <div class="session-time">{{ s.time }}</div>
        </div>
      </div>
      <button class="btn-new-chat" @click="addSession">+ 新对话</button>
      <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        {{ sidebarCollapsed ? '▶' : '◀' }}
      </div>
    </div>

    <!-- Main chat area -->
    <div class="chat-main">
      <!-- Welcome or messages -->
      <div class="chat-messages" ref="msgContainer">
        <div v-if="messages.length === 0" class="chat-welcome">
          <div class="welcome-icon">💬</div>
          <div class="welcome-title">EvolveHub AI</div>
          <div class="welcome-greeting">👋 你好，管理员！有什么可以帮你？</div>
          <div class="welcome-suggestions">
            <div class="suggestion-card" @click="sendMessage('查询公司年假制度')">📋 查询公司制度</div>
            <div class="suggestion-card" @click="sendMessage('帮我查 DNA 服务状态')">🔧 执行工具任务</div>
            <div class="suggestion-card" @click="sendMessage('知识库中有哪些文档？')">📚 检索知识库</div>
          </div>
        </div>

        <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.role]">
          <div :class="['message-bubble', msg.role]">
            <div class="bubble-content">{{ msg.content }}</div>
            <div v-if="msg.toolCalls" class="tool-calls">
              <div v-for="(tc, i) in msg.toolCalls" :key="i" class="tool-call-card" :class="tc.status">
                <div class="tool-header">
                  <span class="tool-icon">🔧</span>
                  <span class="tool-name">{{ tc.name }}</span>
                  <span class="tool-status">{{ statusLabel(tc.status) }}</span>
                </div>
                <div class="tool-args">{{ tc.args }}</div>
                <div v-if="tc.result" class="tool-result">{{ tc.result }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div class="chat-input-area">
        <div class="chat-input-wrapper">
          <span class="input-attach">📎</span>
          <textarea
            v-model="inputText"
            placeholder="在这里输入消息..."
            rows="1"
            @keydown.enter.exact.prevent="handleSend"
          />
          <span class="input-send" :class="{ active: inputText.trim() }" @click="handleSend">▶</span>
        </div>
      </div>

      <!-- Status bar -->
      <div class="chat-statusbar">
        <span>模型: qwen-max</span>
        <span>上下文: {{ messages.length }}/10 轮</span>
        <span>Token: {{ totalTokens }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ChatSession, ChatMessage } from '../../../types'

const sidebarCollapsed = ref(false)
const searchQuery = ref('')
const activeSession = ref('1')
const inputText = ref('')
const msgContainer = ref<HTMLElement | null>(null)

const sessions = ref<ChatSession[]>([
  { id: '1', title: '今天的对话', time: '14:30', active: true },
  { id: '2', title: '项目周报', time: '昨天', active: false },
  { id: '3', title: '知识库问询', time: '周一', active: false }
])

const filteredSessions = computed(() =>
  sessions.value.filter(s => s.title.includes(searchQuery.value))
)

const messages = ref<ChatMessage[]>([])

const totalTokens = computed(() => {
  let t = 0
  for (const m of messages.value) t += m.content.length
  return (t / 4).toFixed(0) + ''
})

function addSession() {
  const id = Date.now().toString()
  sessions.value.unshift({ id, title: '新对话', time: '刚刚', active: true })
  activeSession.value = id
  messages.value = []
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  sendMessage(text)
}

function sendMessage(text: string) {
  inputText.value = ''
  const userMsg: ChatMessage = {
    id: Date.now().toString(),
    role: 'user',
    content: text,
    timestamp: new Date().toLocaleTimeString()
  }
  messages.value.push(userMsg)

  // Simulate AI response
  setTimeout(() => {
    const hasToolCall = text.includes('查') || text.includes('状态')

    if (hasToolCall) {
      const aiMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '好的，我来帮你处理。',
        timestamp: new Date().toLocaleTimeString(),
        toolCalls: [
          {
            name: text.includes('DNA') ? 'execute_shell_command' : 'knowledge_search',
            status: 'success',
            args: text.includes('DNA') ? 'systemctl status dna-service' : `query="${text}"`,
            result: text.includes('DNA')
              ? 'DNA 服务当前状态：运行正常\nCPU 使用率: 45%  内存: 62%'
              : '找到 3 条相关结果：\n1. 年假制度 v2.0\n2. 差旅报销规范\n3. 员工手册'
          }
        ]
      }
      messages.value.push(aiMsg)

      setTimeout(() => {
        messages.value.push({
          id: (Date.now() + 2).toString(),
          role: 'assistant',
          content: text.includes('DNA')
            ? 'DNA 服务当前状态：运行正常。CPU 使用率 45%，内存 62%。服务运行稳定，无需处理。'
            : '根据知识库检索结果，找到了相关文档。请问您需要查看具体哪份文档的详细内容？',
          timestamp: new Date().toLocaleTimeString()
        })
      }, 600)
    } else {
      messages.value.push({
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '收到您的消息。我是 EvolveHub AI 助手，可以帮您查询企业知识库、执行工具任务等。请问还有什么可以帮您的？',
        timestamp: new Date().toLocaleTimeString()
      })
    }
  }, 500)
}

function statusLabel(s: string) {
  const map: Record<string, string> = { running: '执行中...', success: '执行完成', failed: '执行失败', confirming: '等待确认' }
  return map[s] || s
}
</script>

<style scoped>
.chat-app {
  display: flex;
  height: 100%;
}

.chat-sidebar {
  width: 240px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
  transition: width 200ms ease;
  position: relative;
}

.chat-sidebar.collapsed {
  width: 0;
  overflow: hidden;
  border-right: none;
}

.sidebar-search {
  padding: 10px;
}

.sidebar-search input {
  width: 100%;
  height: 32px;
  font-size: 12px;
  padding: 0 10px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.session-item.active {
  background: rgba(10, 132, 255, 0.2);
  border-left: 3px solid #0A84FF;
}

.session-title {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  color: var(--text-disabled);
  margin-top: 2px;
}

.btn-new-chat {
  margin: 8px 10px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: transparent;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
  transition: background 150ms;
}

.btn-new-chat:hover {
  background: rgba(255, 255, 255, 0.06);
}

.sidebar-toggle {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: var(--text-disabled);
  cursor: pointer;
  background: rgba(30, 30, 30, 0.6);
  border-radius: 0 4px 4px 0;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
}

.welcome-icon {
  font-size: 48px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
}

.welcome-greeting {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.welcome-suggestions {
  display: flex;
  gap: 12px;
}

.suggestion-card {
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 13px;
  cursor: pointer;
  transition: transform 150ms, background 150ms;
}

.suggestion-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  border-radius: 16px;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
}

.message-bubble.user {
  background: #0A84FF;
  color: #fff;
  border-radius: 16px 16px 4px 16px;
}

.message-bubble.assistant {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px 16px 16px 4px;
}

.tool-calls {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tool-call-card {
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 12px;
  border-left: 3px solid;
}

.tool-call-card.running { border-color: #FF9F0A; background: rgba(255, 159, 10, 0.1); }
.tool-call-card.success { border-color: #30D158; background: rgba(48, 209, 88, 0.1); }
.tool-call-card.failed { border-color: #FF453A; background: rgba(255, 69, 58, 0.1); }

.tool-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.tool-name { font-weight: 600; }
.tool-status { color: var(--text-secondary); margin-left: auto; }

.tool-args, .tool-result {
  color: var(--text-secondary);
  font-family: monospace;
  font-size: 11px;
  white-space: pre-wrap;
}

.chat-input-area {
  padding: 8px 16px 12px;
}

.chat-input-wrapper {
  display: flex;
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 4px;
}

.chat-input-wrapper textarea {
  flex: 1;
  background: transparent;
  border: none;
  resize: none;
  padding: 8px;
  max-height: 120px;
  font-size: 14px;
  outline: none;
}

.input-attach, .input-send {
  padding: 8px;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-disabled);
  transition: color 150ms;
}

.input-send.active {
  color: #0A84FF;
}

.input-send.active:hover {
  color: #409CFF;
}

.chat-statusbar {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(30, 30, 30, 0.5);
  font-size: 11px;
  color: var(--text-disabled);
  border-top: 1px solid var(--border-subtle);
}
</style>
