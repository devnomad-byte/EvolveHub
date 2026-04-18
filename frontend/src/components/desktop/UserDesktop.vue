<template>
  <div class="user-space">
    <div class="aurora-bg">
      <div class="aurora-layer aurora-1"></div>
      <div class="aurora-layer aurora-2"></div>
      <div class="aurora-layer aurora-3"></div>
    </div>

    <div class="particles">
      <div
        v-for="(p, i) in particlePositions"
        :key="i"
        class="particle"
        :style="{
          left: p.left,
          top: p.top,
          '--duration': p.dur,
          '--delay': p.delay,
          width: p.size + 'px',
          height: p.size + 'px'
        }"
      />
    </div>

    <div class="user-topbar">
      <div class="topbar-left">
        <img src="/logo.svg" alt="EvolveHub" class="topbar-logo-img" />
        <span class="topbar-logo-text">EvolveHub</span>
      </div>
      <div class="topbar-right">
        <Bell :size="18" class="topbar-icon" @click="desktop.toggleNotifications" />
        <div class="topbar-avatar">{{ safeUserInfo.displayName.charAt(0) }}</div>
        <LogOut :size="16" class="topbar-icon topbar-logout" @click="handleLogout" />
      </div>
    </div>

    <div class="user-main">
      <div class="greeting-section">
        <div class="greeting-text" :class="{ complete: greetingComplete }">
          {{ displayedGreeting }}
        </div>
        <div class="greeting-subtitle">AI 助手随时为你服务</div>
      </div>

      <div class="orb-section">
        <div
          class="orb-container"
          @click="openChat"
          @mouseenter="orbHovered = true"
          @mouseleave="orbHovered = false"
        >
          <div class="orb-ring outer"></div>
          <div class="orb-ring inner"></div>
          <div class="orb-core">
            <MessageSquare :size="36" color="#fff" />
          </div>
        </div>
        <div class="orb-label">{{ orbHovered ? '点击开始' : '开始对话' }}</div>
      </div>

      <div class="quick-stats">
        <div v-for="stat in stats" :key="stat.label" class="stat-badge">
          <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
          {{ stat.label }}
        </div>
      </div>
    </div>

    <div class="user-sidebar">
      <div class="sidebar-user">
        <div class="sidebar-user-avatar">{{ safeUserInfo.avatar }}</div>
      </div>

      <div class="sidebar-nav">
        <div
          class="sidebar-item"
          :class="{ active: activePanel === 'chat-history' }"
          style="--item-color: #0A84FF"
          @click="togglePanel('chat-history')"
        >
          <div class="sidebar-item-icon">
            <MessageSquare :size="22" color="#fff" />
          </div>
          <span class="sidebar-item-label">对话历史</span>
        </div>
        <div
          class="sidebar-item"
          :class="{ active: activePanel === 'profile' }"
          style="--item-color: #BF5AF2"
          @click="togglePanel('profile')"
        >
          <div class="sidebar-item-icon">
            <User :size="22" color="#fff" />
          </div>
          <span class="sidebar-item-label">个人档案</span>
        </div>

        <div class="sidebar-sep"></div>

        <div
          class="sidebar-item"
          style="--item-color: #30D158"
          @click="openFeature('knowledge')"
        >
          <div class="sidebar-item-icon">
            <BookOpen :size="22" color="#fff" />
          </div>
          <span class="sidebar-item-label">知识库</span>
        </div>
        <div
          class="sidebar-item"
          style="--item-color: #FFD60A"
          @click="openFeature('memory')"
        >
          <div class="sidebar-item-icon">
            <Zap :size="22" color="#fff" />
          </div>
          <span class="sidebar-item-label">记忆管理</span>
        </div>
        <div
          class="sidebar-item"
          style="--item-color: #8E8E93"
          @click="openFeature('settings')"
        >
          <div class="sidebar-item-icon">
            <Settings :size="22" color="#fff" />
          </div>
          <span class="sidebar-item-label">设置</span>
        </div>
      </div>

      <div class="sidebar-bottom">
        <div class="sidebar-item" style="--item-color: #FF453A" @click="handleLogout">
          <div class="sidebar-item-icon sidebar-item-icon-logout">
            <LogOut :size="20" color="#FF453A" />
          </div>
        </div>
      </div>
    </div>

    <Transition name="panel-slide">
      <div v-if="activePanel" class="user-panel">
        <div class="panel-header">
          <h3>{{ panelTitle }}</h3>
        </div>
        <div class="panel-body">
          <div v-if="activePanel === 'chat-history'" class="chat-history">
            <div
              v-for="chat in chatHistory"
              :key="chat.id"
              class="chat-item"
              @click="openChatSession(chat.id)"
            >
              <div class="chat-item-header">
                <MessageSquare :size="14" color="#0A84FF" />
                <span class="chat-item-title">{{ chat.title }}</span>
              </div>
              <div class="chat-item-preview">{{ chat.preview }}</div>
              <div class="chat-item-meta">
                <span>{{ chat.date }}</span>
                <span>{{ chat.messages }} 条消息</span>
              </div>
            </div>
            <div v-if="!chatHistory.length" class="panel-empty">暂无对话历史</div>
          </div>

          <div v-if="activePanel === 'profile'" class="user-profile">
            <div class="profile-avatar-section">
              <div class="profile-avatar">{{ safeUserInfo.avatar }}</div>
              <div class="profile-name">{{ safeUserInfo.displayName }}</div>
              <div class="profile-role">{{ primaryRoleLabel }}</div>
              <div class="profile-dept">{{ safeUserInfo.deptName }}</div>
            </div>
            <div class="profile-section">
              <h4>角色设定</h4>
              <div class="profile-content" v-html="profileHtml"></div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <div class="status-indicators">
      <div class="status-dot" style="--dot-color: #30D158">系统正常</div>
      <div class="status-dot" style="--dot-color: #0A84FF">{{ modelStatusText }}</div>
      <div class="status-dot" style="--dot-color: #FFD60A">{{ memoryStatusText }}</div>
    </div>

    <DesktopPet :user-id="safeUserInfo.id" :user-name="safeUserInfo.displayName" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { MessageSquare, BookOpen, Zap, Settings, LogOut, Bell, User } from 'lucide-vue-next'
import { gsap } from 'gsap'
import { availableResourceApi, chatApi, memoryApi } from '@/api'
import { useWindowStore } from '../../stores/window'
import { useDesktopStore } from '../../stores/desktop'
import { useAppearanceStore } from '@/composables/useAppearance'
import type { AppId } from '../../types'
import DesktopPet from './DesktopPet.vue'

interface DesktopChatHistoryItem {
  id: string
  title: string
  preview: string
  date: string
  messages: number
}

const winStore = useWindowStore()
const desktop = useDesktopStore()

const safeUserInfo = computed(() => {
  if (!desktop.currentUser) {
    return {
      id: 0,
      username: '',
      displayName: '用户',
      roles: ['USER'],
      deptName: '',
      email: '',
      avatar: '👤'
    }
  }
  return desktop.currentUser
})

const activePanel = ref<'chat-history' | 'profile' | null>(null)
const chatHistory = ref<DesktopChatHistoryItem[]>([])
const profileMarkdown = ref('')
const modelCount = ref(0)
const memoryCount = ref(0)

const panelTitle = computed(() => {
  if (activePanel.value === 'chat-history') return '对话历史'
  if (activePanel.value === 'profile') return '个人档案'
  return ''
})

const primaryRoleLabel = computed(() => {
  const role = safeUserInfo.value.roles?.[0] || 'USER'
  const labels: Record<string, string> = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    LEADER: '高层领导',
    DEPT_HEAD: '部门负责人',
    USER: '普通用户'
  }
  return labels[role] || role
})

const profileHtml = computed(() => renderMarkdownPreview(profileMarkdown.value || '暂无画像内容'))

const stats = computed(() => [
  { label: '今日对话', value: chatHistory.value.length, color: '#0A84FF' },
  { label: '可用模型', value: modelCount.value, color: '#30D158' },
  { label: '记忆条目', value: memoryCount.value, color: '#FFD60A' }
])

const modelStatusText = computed(() => modelCount.value > 0 ? '模型在线' : '暂无模型')
const memoryStatusText = computed(() => `记忆 ${memoryCount.value} 条`)

function togglePanel(panel: 'chat-history' | 'profile') {
  activePanel.value = activePanel.value === panel ? null : panel
}

async function loadDashboardData() {
  await Promise.all([
    loadChatHistory(),
    loadProfile(),
    loadStats()
  ])
}

async function loadChatHistory() {
  try {
    const result = await chatApi.listSessions(1, 8)
    const records = result.records || []
    const previews = await Promise.all(records.map(async (session) => {
      try {
        const messageResult = await chatApi.listMessages(session.id, 1, 2)
        const latestMessage = [...messageResult.records]
          .sort((left, right) => right.id.localeCompare(left.id))[0]
        return {
          id: session.id,
          title: session.title?.trim() || `会话 ${session.id}`,
          preview: latestMessage?.content || '点击继续对话',
          date: formatDisplayTime(session.updateTime || session.createTime),
          messages: session.messageCount || messageResult.total || messageResult.records.length
        }
      } catch {
        return {
          id: session.id,
          title: session.title?.trim() || `会话 ${session.id}`,
          preview: '点击继续对话',
          date: formatDisplayTime(session.updateTime || session.createTime),
          messages: session.messageCount || 0
        }
      }
    }))
    chatHistory.value = previews
  } catch (error) {
    console.error('加载桌面对话历史失败', error)
    chatHistory.value = []
  }
}

async function loadProfile() {
  try {
    const profile = await memoryApi.getProfile()
    profileMarkdown.value = profile.markdownContent || ''
  } catch (error) {
    console.error('加载桌面画像失败', error)
    profileMarkdown.value = ''
  }
}

async function loadStats() {
  try {
    const [models, memories] = await Promise.all([
      availableResourceApi.resolveChatModels(),
      memoryApi.listItems()
    ])
    modelCount.value = Array.isArray(models) ? models.length : 0
    memoryCount.value = Array.isArray(memories) ? memories.length : 0
  } catch (error) {
    console.error('加载桌面统计失败', error)
    modelCount.value = 0
    memoryCount.value = 0
  }
}

function openChat() {
  winStore.openApp('chat')
}

function openChatSession(sessionId: string) {
  winStore.pendingChatSessionId = sessionId
  winStore.openApp('chat')
}

function openFeature(appId: AppId) {
  winStore.openApp(appId)
}

function handleLogout() {
  desktop.logout()
}

function formatDisplayTime(value?: string) {
  if (!value) {
    return '刚刚'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '刚刚'
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function renderMarkdownPreview(markdown: string) {
  const escaped = markdown
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return escaped
    .replace(/^##\s+(.*)$/gm, '<div class="md-heading">$1</div>')
    .replace(/^- (.*)$/gm, '<div class="md-item">• $1</div>')
    .replace(/\n/g, '<br>')
}

const particlePositions = [
  { left: '8%', top: '65%', dur: '9s', delay: '0s', size: 3 },
  { left: '22%', top: '75%', dur: '11s', delay: '1.5s', size: 2 },
  { left: '38%', top: '60%', dur: '8s', delay: '3s', size: 4 },
  { left: '55%', top: '70%', dur: '13s', delay: '0.8s', size: 2 },
  { left: '72%', top: '68%', dur: '10s', delay: '2.2s', size: 3 },
  { left: '88%', top: '72%', dur: '12s', delay: '4s', size: 2 },
  { left: '15%', top: '80%', dur: '14s', delay: '1s', size: 3 },
  { left: '45%', top: '82%', dur: '7s', delay: '2.5s', size: 2 },
  { left: '62%', top: '78%', dur: '10s', delay: '3.5s', size: 4 },
  { left: '80%', top: '85%', dur: '9s', delay: '0.5s', size: 3 },
  { left: '30%', top: '90%', dur: '11s', delay: '2s', size: 2 },
  { left: '50%', top: '88%', dur: '8s', delay: '4.5s', size: 3 }
]

const greetingText = computed(() => `你好, ${safeUserInfo.value.displayName}`)
const displayedGreeting = ref('')
const greetingComplete = ref(false)
let greetingTimer: number = 0

function startGreetingAnimation() {
  const text = greetingText.value
  let i = 0
  greetingTimer = window.setInterval(() => {
    if (i < text.length) {
      displayedGreeting.value = text.slice(0, i + 1)
      i++
    } else {
      clearInterval(greetingTimer)
      greetingComplete.value = true
    }
  }, 80)
}

const orbHovered = ref(false)

onMounted(() => {
  const { applySettings } = useAppearanceStore()
  applySettings()
  startGreetingAnimation()
  void loadDashboardData()

  const tl = gsap.timeline({ defaults: { ease: 'power3.out' } })
  tl.from('.greeting-section', { y: 30, opacity: 0, duration: 0.8 })
    .from('.orb-container', { scale: 0.3, opacity: 0, duration: 1, ease: 'back.out(1.7)' }, '-=0.3')
    .from('.quick-stats', { y: 20, opacity: 0, duration: 0.5 }, '-=0.4')
})

onUnmounted(() => {
  clearInterval(greetingTimer)
})
</script>
