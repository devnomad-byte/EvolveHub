<template>
  <div class="chat-app">
    <div class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-search">
        <input v-model="searchQuery" placeholder="搜索会话" />
      </div>
      <div class="session-list">
        <div
          v-for="session in filteredSessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          @click="switchSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-time">{{ session.time }}</div>
        </div>
      </div>
      <button class="btn-new-chat" @click="addSession">+ 新对话</button>
      <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        {{ sidebarCollapsed ? '▶' : '◀' }}
      </div>
    </div>

    <div class="chat-main">
      <div class="chat-messages" ref="msgContainer">
        <div v-if="messages.length === 0" class="chat-welcome">
          <div class="welcome-icon">💬</div>
          <div class="welcome-title">EvolveHub AI</div>
          <div v-if="isLoadingModels" class="welcome-greeting">正在加载可用模型...</div>
          <template v-else-if="hasAvailableChatModel">
            <div class="welcome-greeting">👋 你好，有什么可以帮你？</div>
            <div class="welcome-suggestions">
              <div class="suggestion-card" @click="sendPresetMessage('查询公司年假制度')">📋 查询公司制度</div>
              <div class="suggestion-card" @click="sendPresetMessage('帮我查今天的服务器监控')">🔧 执行工具任务</div>
              <div class="suggestion-card" @click="sendPresetMessage('知识库中有哪些文档？')">📚 检索知识库</div>
            </div>
          </template>
          <div v-else class="chat-empty-state">
            <div class="empty-title">当前没有可用的对话模型</div>
            <div class="empty-desc">请先前往模型管理添加或启用至少一个 LLM 模型。</div>
            <button class="empty-action" @click="openModelManager">前往模型管理</button>
          </div>
        </div>

        <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.role]">
          <div :class="['message-bubble', msg.role]">
            <div class="bubble-content">
              <span>{{ msg.content }}</span>
              <span v-if="msg.role === 'assistant' && msg.isTyping" class="typing-cursor"></span>
            </div>
            <div v-if="msg.toolCalls?.length" class="tool-calls">
              <div
                v-for="(toolCall, index) in msg.toolCalls"
                :key="index"
                class="tool-call-card"
                :class="toolCall.status"
              >
                <div class="tool-header">
                  <span class="tool-icon">🔧</span>
                  <span class="tool-name">{{ toolCall.name }}</span>
                  <span class="tool-status">{{ statusLabel(toolCall.status) }}</span>
                </div>
                <div v-if="toolCall.args" class="tool-args">{{ toolCall.args }}</div>
                <div v-if="toolCall.result" class="tool-result">{{ toolCall.result }}</div>
              </div>
            </div>
            <div class="message-time">{{ msg.timestamp }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <div class="chat-toolbar">
          <div class="toolbar-model">
            <span class="toolbar-label">当前模型</span>
            <select
              v-model="selectedModelConfigId"
              class="model-select"
              :disabled="isLoadingModels || availableChatModels.length === 0 || sending"
              @change="handleModelChange"
            >
              <option value="" disabled>{{ availableChatModels.length > 0 ? '请选择模型' : '暂无可用模型' }}</option>
              <option v-for="model in availableChatModels" :key="model.id" :value="String(model.id)">
                {{ model.name }}
              </option>
            </select>
          </div>
          <div class="toolbar-actions">
            <button class="toolbar-link" @click="openMemoryManager">查看记忆</button>
          </div>
          <div v-if="memoryWarning" class="memory-warning">{{ memoryWarning }}</div>
        </div>
        <div v-if="showSessionModelInvalidState" class="session-model-invalid">
          <div class="invalid-title">当前会话绑定的模型已失效</div>
          <div class="invalid-desc">请选择一个可用模型并保存后，再继续当前会话。</div>
          <div class="invalid-actions">
            <button class="invalid-btn primary" :disabled="!selectedModelConfigId || isSavingSessionModel" @click="saveSessionModelSelection">
              {{ isSavingSessionModel ? '保存中...' : '保存并恢复会话' }}
            </button>
            <button class="invalid-btn" @click="openModelManager">前往模型管理</button>
          </div>
        </div>
        <div class="chat-input-wrapper">
          <span class="input-attach">📎</span>
          <textarea
            v-model="inputText"
            :disabled="sending || disableInput"
            placeholder="在这里输入消息..."
            rows="1"
            @keydown.enter.exact.prevent="handleSend"
          />
          <span
            class="input-send"
            :class="{ active: inputText.trim() && !sending && !disableInput, disabled: disableInput }"
            @click="handleSend"
          >▶</span>
        </div>
      </div>

      <div class="chat-statusbar">
        <span>模型: {{ activeModelName }}</span>
        <span>上下文: {{ contextTurns }}/10 轮</span>
        <span>Token: {{ totalTokens }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { availableResourceApi, chatApi } from '@/api'
import type { ChatMessage, ChatSession } from '@/types'
import { useWindowStore } from '@/stores/window'
import { useDesktopStore } from '@/stores/desktop'
import type { ModelConfigInfo } from '@/api/adminModelConfig'
import type { ChatMessageRecord, ChatSessionRecord, ChatStreamEvent } from '@/api/chat'

const windowStore = useWindowStore()
const desktop = useDesktopStore()
const sidebarCollapsed = ref(false)
const searchQuery = ref('')
const inputText = ref('')
const msgContainer = ref<HTMLElement | null>(null)
const sending = ref(false)
const isLoadingModels = ref(false)
const isSavingSessionModel = ref(false)

const sessions = ref<ChatSession[]>([])
const messages = ref<ChatMessage[]>([])
const activeSessionId = ref('')
const activeSessionModelConfigId = ref<string>()
const currentModelConfigId = ref<string>()
const activeModelName = ref('未选择')
const availableChatModels = ref<ModelConfigInfo[]>([])
const selectedModelConfigId = ref('')
const memoryWarning = ref('')
const streamingRenderState = ref<{
  messageId: string
  pendingText: string
  streamDone: boolean
  rafId: number | null
  lastEmitAt: number
  resolveDrained: (() => void) | null
} | null>(null)

const filteredSessions = computed(() =>
  sessions.value.filter(session => session.title.includes(searchQuery.value))
)
const hasAvailableChatModel = computed(() => availableChatModels.value.length > 0)
const sessionModelInvalid = computed(() =>
  !!activeSessionId.value
  && !isLocalSession(activeSessionId.value)
  && !!activeSessionModelConfigId.value
  && !availableChatModels.value.some(model => normalizeId(model.id) === activeSessionModelConfigId.value)
)
const showSessionModelInvalidState = computed(() => sessionModelInvalid.value)
const disableInput = computed(() =>
  (isLocalSession(activeSessionId.value) && !hasAvailableChatModel.value)
  || showSessionModelInvalidState.value
)

const totalTokens = computed(() => {
  const total = messages.value.reduce((sum, message) => sum + message.content.length, 0)
  return String(Math.ceil(total / 4))
})

const contextTurns = computed(() => {
  const userCount = messages.value.filter(message => message.role === 'user').length
  const assistantCount = messages.value.filter(message => message.role === 'assistant').length
  return Math.min(userCount, assistantCount)
})

watch(messages, async () => {
  await nextTick()
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}, { deep: true })

onMounted(async () => {
  await Promise.all([
    loadAvailableModels(),
    loadSessions()
  ])
})

async function loadAvailableModels() {
  isLoadingModels.value = true
  try {
    const result = await availableResourceApi.resolveChatModels()
    const records = Array.isArray(result) ? result.filter(model => isUsableChatModel(model)) : []
    availableChatModels.value = records
    syncCurrentModelState()
  } catch (error) {
    console.error('加载模型列表失败', error)
    availableChatModels.value = []
    syncCurrentModelState()
  } finally {
    isLoadingModels.value = false
  }
}

async function loadSessions() {
  try {
    const result = await chatApi.listSessions()
    sessions.value = result.records.map(mapSessionRecord)
    const pendingSessionId = consumePendingSessionId()
    if (pendingSessionId && sessions.value.some(session => session.id === pendingSessionId)) {
      await switchSession(pendingSessionId)
      return
    }
    if (sessions.value.length > 0) {
      await switchSession(sessions.value[0].id)
      return
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
  addSession()
}

async function switchSession(sessionId: string) {
  clearStreamingRenderState()
  activeSessionId.value = sessionId
  memoryWarning.value = ''
  if (isLocalSession(sessionId)) {
    activeSessionModelConfigId.value = undefined
    syncCurrentModelState()
    messages.value = []
    return
  }
  try {
    const result = await chatApi.listMessages(sessionId)
    messages.value = result.records
      .slice()
      .sort((left: ChatMessageRecord, right: ChatMessageRecord) => left.id.localeCompare(right.id))
      .map(mapMessageRecord)
  } catch (error) {
    console.error('加载消息历史失败', error)
    messages.value = []
  }
  const currentSession = sessions.value.find(session => session.id === sessionId)
  activeSessionModelConfigId.value = currentSession?.modelConfigId
  syncCurrentModelState()
}

function addSession() {
  clearStreamingRenderState()
  const sessionId = `local-${Date.now()}`
  sessions.value.unshift({
    id: sessionId,
    title: '新对话',
    time: '刚刚',
    active: true,
    modelConfigId: undefined
  })
  activeSessionId.value = sessionId
  activeSessionModelConfigId.value = undefined
  memoryWarning.value = ''
  syncCurrentModelState()
  messages.value = []
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value || disableInput.value) {
    return
  }
  void sendMessage(text)
}

function sendPresetMessage(text: string) {
  if (sending.value || disableInput.value) {
    return
  }
  void sendMessage(text)
}

async function sendMessage(text: string) {
  const content = text.trim()
  if (!content || disableInput.value) {
    return
  }

  inputText.value = ''
  sending.value = true

  const userMessage: ChatMessage = {
    id: `user-${Date.now()}`,
    role: 'user',
    content,
    timestamp: formatNow()
  }
  const assistantMessage: ChatMessage = {
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '',
    timestamp: formatNow(),
    toolCalls: [],
    isTyping: true
  }
  messages.value.push(userMessage, assistantMessage)
  const typingDrainPromise = initializeStreamingRenderState(assistantMessage)

  try {
    await chatApi.sendMessage(buildSendPayload(content), {
      onEvent: (event: ChatStreamEvent) => handleStreamEvent(event, assistantMessage),
      onError: (error: Error) => console.error('发送消息失败', error)
    })
    await typingDrainPromise
    if (!assistantMessage.content.trim()) {
      assistantMessage.content = '未收到模型回复'
    }
    await refreshSessions()
  } catch (error) {
    clearStreamingRenderState()
    assistantMessage.isTyping = false
    assistantMessage.content = error instanceof Error ? error.message : '发送消息失败'
  } finally {
    sending.value = false
  }
}

function buildSendPayload(content: string) {
  const persistedSession = activeSessionId.value && !isLocalSession(activeSessionId.value)
  return {
    sessionId: persistedSession ? activeSessionId.value : undefined,
    modelConfigId: persistedSession && activeSessionModelConfigId.value
      ? undefined
      : currentModelConfigId.value,
    content
  }
}

function handleStreamEvent(event: ChatStreamEvent, assistantMessage: ChatMessage) {
  if (event.type === 'session_created' && event.sessionId) {
    replaceLocalSession(event.sessionId)
    return
  }
  if (event.type === 'chunk' || event.type === 'reasoning') {
    queueAssistantText(assistantMessage, event.content || '')
    return
  }
  if (event.type === 'done') {
    markStreamingDone(assistantMessage)
    return
  }
  if (event.type === 'tool_call') {
    assistantMessage.toolCalls = assistantMessage.toolCalls || []
    assistantMessage.toolCalls.push({
      name: event.toolName || 'unknown_tool',
      status: 'running',
      args: stringifyToolArgs(event.input)
    })
    return
  }
  if (event.type === 'tool_result') {
    assistantMessage.toolCalls = assistantMessage.toolCalls || []
    const existing = assistantMessage.toolCalls.find(call => call.name === (event.toolName || ''))
    if (existing) {
      existing.status = 'success'
      existing.result = event.result || ''
      return
    }
    assistantMessage.toolCalls.push({
      name: event.toolName || 'unknown_tool',
      status: 'success',
      args: '',
      result: event.result || ''
    })
    return
  }
  if (event.type === 'memory_warning') {
    memoryWarning.value = event.message || '本轮记忆已降级'
    desktop.addToast(memoryWarning.value, 'info')
    return
  }
  if (event.type === 'error') {
    clearStreamingRenderState()
    assistantMessage.isTyping = false
    assistantMessage.content = event.message || '请求失败'
  }
}

function replaceLocalSession(sessionId: string) {
  const currentId = activeSessionId.value
  const currentIndex = sessions.value.findIndex(session => session.id === currentId)
  const firstUserMessage = messages.value.find(message => message.role === 'user')?.content || '新对话'
  const session: ChatSession = {
    id: sessionId,
    title: buildSessionTitle(firstUserMessage),
    time: '刚刚',
    active: true,
    modelConfigId: currentModelConfigId.value
  }
  activeSessionId.value = sessionId
  activeSessionModelConfigId.value = currentModelConfigId.value
  syncCurrentModelState()
  if (currentIndex >= 0) {
    sessions.value.splice(currentIndex, 1, session)
    return
  }
  sessions.value.unshift(session)
}

async function refreshSessions() {
  try {
    const currentSessionId = activeSessionId.value
    const result = await chatApi.listSessions()
    sessions.value = result.records.map(mapSessionRecord)
    const currentSession = sessions.value.find(session => session.id === currentSessionId)
    if (!currentSession && sessions.value.length > 0) {
      activeSessionId.value = sessions.value[0].id
      activeSessionModelConfigId.value = sessions.value[0].modelConfigId
      syncCurrentModelState()
      return
    }
    activeSessionModelConfigId.value = currentSession?.modelConfigId
    syncCurrentModelState()
  } catch (error) {
    console.error('刷新会话列表失败', error)
  }
}

function mapSessionRecord(record: ChatSessionRecord): ChatSession {
  return {
    id: record.id,
    title: record.title?.trim() || `会话 ${record.id}`,
    time: formatDisplayTime(record.updateTime || record.createTime),
    active: String(record.id) === activeSessionId.value,
    modelConfigId: record.modelConfigId ? String(record.modelConfigId) : undefined
  }
}

function mapMessageRecord(record: ChatMessageRecord): ChatMessage {
  return {
    id: String(record.id),
    role: record.role === 'assistant' ? 'assistant' : 'user',
    content: record.content || '',
    timestamp: formatDisplayTime(record.createTime, true)
  }
}

function isLocalSession(sessionId: string) {
  return sessionId.startsWith('local-')
}

function consumePendingSessionId() {
  const pendingSessionId = windowStore.pendingChatSessionId
  if (!pendingSessionId) {
    return null
  }
  windowStore.pendingChatSessionId = null
  return String(pendingSessionId)
}

function syncCurrentModelState() {
  const fallbackModel = availableChatModels.value[0]
  if (isLocalSession(activeSessionId.value)) {
    const selectedId = selectedModelConfigId.value || normalizeId(fallbackModel?.id)
    currentModelConfigId.value = selectedId
    if (!selectedModelConfigId.value && fallbackModel) {
      selectedModelConfigId.value = normalizeId(fallbackModel.id)
    }
    const currentModel = availableChatModels.value.find(model => normalizeId(model.id) === selectedId) || fallbackModel
    activeModelName.value = currentModel?.name || '未选择'
    return
  }
  if (activeSessionModelConfigId.value) {
    const boundModel = availableChatModels.value.find(model => normalizeId(model.id) === activeSessionModelConfigId.value)
    if (boundModel) {
      selectedModelConfigId.value = activeSessionModelConfigId.value
      currentModelConfigId.value = normalizeId(boundModel.id)
      activeModelName.value = boundModel.name
      return
    }
    selectedModelConfigId.value = ''
    currentModelConfigId.value = undefined
    activeModelName.value = '模型已失效'
    return
  }
  selectedModelConfigId.value = fallbackModel ? normalizeId(fallbackModel.id) : ''
  currentModelConfigId.value = fallbackModel ? normalizeId(fallbackModel.id) : undefined
  activeModelName.value = fallbackModel?.name || '未选择'
}

async function handleModelChange() {
  const nextModelId = selectedModelConfigId.value || undefined
  if (!nextModelId) {
    return
  }
  const nextModel = availableChatModels.value.find(model => normalizeId(model.id) === nextModelId)
  if (!nextModel) {
    return
  }
  if (isLocalSession(activeSessionId.value)) {
    currentModelConfigId.value = nextModelId
    activeModelName.value = nextModel.name
    desktop.addToast(`已切换到模型：${nextModel.name}`, 'info')
    return
  }
  if (showSessionModelInvalidState.value) {
    return
  }
  await saveSessionModelSelection()
}

async function saveSessionModelSelection() {
  if (!selectedModelConfigId.value || isLocalSession(activeSessionId.value)) {
    return
  }
  const nextModelId = selectedModelConfigId.value
  const nextModel = availableChatModels.value.find(model => normalizeId(model.id) === nextModelId)
  if (!nextModel) {
    desktop.addToast('请选择可用模型', 'error')
    return
  }
  isSavingSessionModel.value = true
  try {
    await chatApi.updateSession({
      id: activeSessionId.value,
      modelConfigId: nextModelId
    })
    activeSessionModelConfigId.value = nextModelId
    currentModelConfigId.value = nextModelId
    activeModelName.value = nextModel.name
    sessions.value = sessions.value.map(session => session.id === activeSessionId.value
      ? { ...session, modelConfigId: nextModelId }
      : session)
    desktop.addToast(`已切换到模型：${nextModel.name}`, 'success')
  } catch (error) {
    desktop.addToast(error instanceof Error ? error.message : '切换模型失败', 'error')
    syncCurrentModelState()
  } finally {
    isSavingSessionModel.value = false
  }
}

function isUsableChatModel(model: { modelType?: string; enabled?: number | string | boolean }) {
  if (!isEnabledModel(model)) {
    return false
  }
  const modelType = (model.modelType || '').trim().toLowerCase()
  return modelType === '' || modelType === 'llm' || modelType === 'chat'
}

function isEnabledModel(model: { enabled?: number | string | boolean }) {
  return model.enabled === 1 || model.enabled === '1' || model.enabled === true
}

function initializeStreamingRenderState(assistantMessage: ChatMessage) {
  clearStreamingRenderState()
  return new Promise<void>((resolve) => {
    streamingRenderState.value = {
      messageId: assistantMessage.id,
      pendingText: '',
      streamDone: false,
      rafId: null,
      lastEmitAt: 0,
      resolveDrained: resolve
    }
  })
}

function queueAssistantText(assistantMessage: ChatMessage, content: string) {
  if (!content) {
    return
  }
  const state = streamingRenderState.value
  if (!state || state.messageId !== assistantMessage.id) {
    assistantMessage.content += content
    return
  }
  state.pendingText += content
  assistantMessage.isTyping = true
  ensureTypingLoop(assistantMessage)
}

function markStreamingDone(assistantMessage: ChatMessage) {
  const state = streamingRenderState.value
  if (!state || state.messageId !== assistantMessage.id) {
    assistantMessage.isTyping = false
    return
  }
  state.streamDone = true
  ensureTypingLoop(assistantMessage)
}

function ensureTypingLoop(assistantMessage: ChatMessage) {
  const state = streamingRenderState.value
  if (!state || state.messageId !== assistantMessage.id || state.rafId !== null) {
    return
  }

  const tick = (now: number) => {
    const currentState = streamingRenderState.value
    if (!currentState || currentState.messageId !== assistantMessage.id) {
      return
    }

    if (currentState.pendingText.length > 0) {
      const frameDelay = resolveTypingFrameDelay(currentState.pendingText.length)
      if (now - currentState.lastEmitAt < frameDelay) {
        currentState.rafId = requestAnimationFrame(tick)
        return
      }
      const batchSize = resolveTypingBatchSize(currentState.pendingText.length)
      assistantMessage.content += currentState.pendingText.slice(0, batchSize)
      currentState.pendingText = currentState.pendingText.slice(batchSize)
      currentState.lastEmitAt = now
      assistantMessage.isTyping = true
      currentState.rafId = requestAnimationFrame(tick)
      return
    }

    currentState.rafId = null
    if (currentState.streamDone) {
      assistantMessage.isTyping = false
      currentState.resolveDrained?.()
      streamingRenderState.value = null
      return
    }
  }

  state.rafId = requestAnimationFrame(tick)
}

function resolveTypingBatchSize(pendingLength: number) {
  if (pendingLength > 120) {
    return 4
  }
  if (pendingLength > 60) {
    return 3
  }
  if (pendingLength > 20) {
    return 2
  }
  return 1
}

function resolveTypingFrameDelay(pendingLength: number) {
  if (pendingLength > 120) {
    return 16
  }
  if (pendingLength > 60) {
    return 22
  }
  if (pendingLength > 20) {
    return 28
  }
  return 36
}

function clearStreamingRenderState() {
  const state = streamingRenderState.value
  if (!state) {
    return
  }
  if (state.rafId !== null) {
    cancelAnimationFrame(state.rafId)
  }
  state.resolveDrained?.()
  streamingRenderState.value = null
}

function normalizeId(value?: string | number | null) {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value)
}

function buildSessionTitle(content: string) {
  const normalized = content.trim()
  return normalized.length > 18 ? `${normalized.slice(0, 18)}...` : normalized
}

function formatDisplayTime(value?: string, withTime = false) {
  if (!value) {
    return '刚刚'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '刚刚'
  }
  if (withTime) {
    return date.toLocaleTimeString()
  }
  return date.toLocaleString([], {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatNow() {
  return new Date().toLocaleTimeString()
}

function stringifyToolArgs(input?: Record<string, unknown>) {
  if (!input) {
    return ''
  }
  try {
    return JSON.stringify(input)
  } catch {
    return ''
  }
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    running: '执行中...',
    success: '执行完成',
    failed: '执行失败',
    confirming: '等待确认'
  }
  return map[status] || status
}

function openModelManager() {
  windowStore.openApp('model')
  if (!desktop.currentUser?.roles?.includes('SUPER_ADMIN')) {
    desktop.addToast('请先添加并启用个人 LLM 模型', 'info')
  }
}

function openMemoryManager() {
  windowStore.openApp('memory')
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
}

.sidebar-toggle {
  position: absolute;
  right: 6px;
  top: 10px;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-secondary);
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
  padding: 28px;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  text-align: center;
}

.welcome-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.welcome-greeting {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.welcome-suggestions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  width: min(820px, 100%);
}

.suggestion-card {
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
}

.chat-empty-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
}

.empty-desc {
  color: var(--text-secondary);
}

.empty-action,
.toolbar-link,
.invalid-btn,
.btn-new-chat {
  cursor: pointer;
}

.empty-action,
.toolbar-link,
.invalid-btn.primary {
  border: none;
  background: #0A84FF;
  color: #fff;
  border-radius: 10px;
  padding: 10px 14px;
}

.message-row {
  display: flex;
  margin-bottom: 18px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: min(76%, 860px);
  border-radius: 16px;
  padding: 14px 16px;
}

.message-bubble.user {
  background: #0A84FF;
  color: #fff;
}

.message-bubble.assistant {
  background: rgba(255, 255, 255, 0.06);
}

.bubble-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.typing-cursor {
  display: inline-block;
  width: 8px;
  height: 1em;
  margin-left: 2px;
  vertical-align: -2px;
  background: currentColor;
  animation: typingCursorBlink 1s steps(1) infinite;
}

@keyframes typingCursorBlink {
  0%, 49% {
    opacity: 1;
  }
  50%, 100% {
    opacity: 0;
  }
}

.message-time {
  margin-top: 10px;
  font-size: 11px;
  color: var(--text-disabled);
}

.tool-calls {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tool-call-card {
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.18);
}

.tool-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.tool-name {
  font-weight: 600;
}

.tool-status {
  color: var(--text-secondary);
}

.tool-args,
.tool-result {
  margin-top: 6px;
  font-size: 12px;
  white-space: pre-wrap;
}

.chat-input-area {
  border-top: 1px solid var(--border-subtle);
  padding: 14px 18px;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.toolbar-model {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.model-select {
  min-width: 220px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  padding: 0 10px;
}

.toolbar-actions {
  margin-left: auto;
}

.memory-warning {
  font-size: 12px;
  color: #FFD60A;
}

.session-model-invalid {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 69, 58, 0.12);
}

.invalid-title {
  font-weight: 600;
}

.invalid-desc {
  margin-top: 4px;
  color: var(--text-secondary);
}

.invalid-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.invalid-btn {
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: transparent;
  color: var(--text-primary);
  border-radius: 10px;
  padding: 8px 12px;
}

.chat-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
}

.input-attach {
  font-size: 18px;
}

.chat-input-wrapper textarea {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  resize: none;
  outline: none;
  min-height: 24px;
  max-height: 180px;
}

.input-send {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.12);
  color: var(--text-disabled);
}

.input-send.active {
  background: #0A84FF;
  color: #fff;
  cursor: pointer;
}

.input-send.disabled {
  cursor: not-allowed;
}

.chat-statusbar {
  height: 32px;
  border-top: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 18px;
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 960px) {
  .welcome-suggestions {
    grid-template-columns: 1fr;
  }

  .message-bubble {
    max-width: 100%;
  }
}
</style>
