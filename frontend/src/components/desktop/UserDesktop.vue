<template>
  <div class="user-space">
    <!-- Aurora background -->
    <div class="aurora-bg">
      <div class="aurora-layer aurora-1"></div>
      <div class="aurora-layer aurora-2"></div>
      <div class="aurora-layer aurora-3"></div>
    </div>

    <!-- Floating particles -->
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

    <!-- Minimal top bar -->
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

    <!-- Main content -->
    <div class="user-main">
      <!-- Greeting -->
      <div class="greeting-section">
        <div class="greeting-text" :class="{ complete: greetingComplete }">
          {{ displayedGreeting }}
        </div>
        <div class="greeting-subtitle">AI 助手随时为你服务</div>
      </div>

      <!-- Central orb -->
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

      <!-- Quick stats -->
      <div class="quick-stats">
        <div v-for="stat in stats" :key="stat.label" class="stat-badge">
          <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
          {{ stat.label }}
        </div>
      </div>
    </div>

    <!-- Left sidebar -->
    <div class="user-sidebar">
      <!-- User avatar at top -->
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

      <!-- Logout at bottom -->
      <div class="sidebar-bottom">
        <div class="sidebar-item" style="--item-color: #FF453A" @click="handleLogout">
          <div class="sidebar-item-icon sidebar-item-icon-logout">
            <LogOut :size="20" color="#FF453A" />
          </div>
        </div>
      </div>
    </div>

    <!-- Side panel -->
    <Transition name="panel-slide">
      <div v-if="activePanel" class="user-panel">
        <div class="panel-header">
          <h3>{{ panelTitle }}</h3>
        </div>
        <div class="panel-body">
          <!-- Chat History -->
          <div v-if="activePanel === 'chat-history'" class="chat-history">
          <div
            v-for="chat in chatHistory"
            :key="chat.id"
            class="chat-item"
            @click="winStore.openApp('chat')"
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
          </div>

          <!-- Personal Profile -->
          <div v-if="activePanel === 'profile'" class="user-profile">
            <div class="profile-avatar-section">
              <div class="profile-avatar">{{ safeUserInfo.avatar }}</div>
              <div class="profile-name">{{ safeUserInfo.displayName }}</div>
              <div class="profile-role">{{ safeUserInfo.roles?.includes('SUPER_ADMIN') ? '超级管理员' : safeUserInfo.roles?.includes('ADMIN') ? '管理员' : '普通用户' }}</div>
              <div class="profile-dept">{{ safeUserInfo.deptName }}</div>
            </div>
            <div class="profile-section">
              <h4>角色设定</h4>
              <div class="profile-content" v-html="userProfileMd"></div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Status indicators -->
    <div class="status-indicators">
      <div class="status-dot" style="--dot-color: #30D158">系统正常</div>
      <div class="status-dot" style="--dot-color: #0A84FF">模型在线</div>
      <div class="status-dot" style="--dot-color: #FFD60A">3 条未读</div>
    </div>

    <!-- Desktop Pet -->
    <DesktopPet :user-id="safeUserInfo.id" :user-name="safeUserInfo.displayName" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { MessageSquare, BookOpen, Zap, Settings, LogOut, Bell, User } from 'lucide-vue-next'
import { gsap } from 'gsap'
import { useWindowStore } from '../../stores/window'
import { useDesktopStore } from '../../stores/desktop'
import { useAppearanceStore } from '@/composables/useAppearance'
import type { AppId } from '../../types'
import DesktopPet from './DesktopPet.vue'

const winStore = useWindowStore()
const desktop = useDesktopStore()

// 安全的用户信息访问
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

// Sidebar panel state
const activePanel = ref<'chat-history' | 'profile' | null>(null)

const panelTitle = computed(() => {
  if (activePanel.value === 'chat-history') return '对话历史'
  if (activePanel.value === 'profile') return '个人档案'
  return ''
})

function togglePanel(panel: 'chat-history' | 'profile') {
  activePanel.value = activePanel.value === panel ? null : panel
}

// Mock chat history
const chatHistory = ref([
  { id: '1', title: '项目进度查询', preview: '帮我查看一下本周各项目的完成情况...', date: '今天 14:30', messages: 8 },
  { id: '2', title: '周报生成', preview: '根据本周的工作记录生成一份周报...', date: '今天 10:15', messages: 12 },
  { id: '3', title: '数据分析', preview: '分析一下上个月的客户反馈数据...', date: '昨天 16:20', messages: 15 },
  { id: '4', title: '代码审查', preview: '帮我审查一下这个 PR 的代码质量...', date: '昨天 09:45', messages: 6 },
  { id: '5', title: '会议纪要整理', preview: '整理一下今天上午会议的要点...', date: '4月8日', messages: 10 },
  { id: '6', title: '技术方案评估', preview: '对比一下微服务和单体架构的优劣...', date: '4月7日', messages: 18 },
  { id: '7', title: '文档翻译', preview: '将这份英文技术文档翻译成中文...', date: '4月5日', messages: 4 },
  { id: '8', title: '学习计划制定', preview: '帮我制定一个 Rust 学习计划...', date: '4月3日', messages: 9 }
])

// Mock user profile (MD content rendered as HTML)
const userProfileMd = computed(() => {
  const profileData: Record<string, string> = {
    '2': `
      <div class="md-section">
        <p class="md-heading">## 性格特点</p>
        <p>注重细节，喜欢探索新技术。做事有条理，善于分析和总结。</p>
      </div>
      <div class="md-section">
        <p class="md-heading">## 兴趣爱好</p>
        <ul>
          <li>编程 & 技术博客写作</li>
          <li>开源项目贡献</li>
          <li>科幻小说阅读</li>
          <li>摄影 & 后期处理</li>
          <li>咖啡品鉴</li>
        </ul>
      </div>
      <div class="md-section">
        <p class="md-heading">## 沟通偏好</p>
        <p>喜欢简洁、结构化的回答。偏好中文交流，技术术语可用英文。</p>
      </div>
      <div class="md-section">
        <p class="md-heading">## 工作习惯</p>
        <ul>
          <li>上午精力最集中，适合处理复杂任务</li>
          <li>偏好使用 Markdown 格式的文档</li>
          <li>习惯使用 Git 进行版本管理</li>
        </ul>
      </div>
    `,
    '1': `
      <div class="md-section">
        <p class="md-heading">## 管理风格</p>
        <p>结果导向型管理，注重团队效率和个人成长。</p>
      </div>
      <div class="md-section">
        <p class="md-heading">## 关注领域</p>
        <ul>
          <li>AI 技术落地应用</li>
          <li>团队管理与流程优化</li>
          <li>产品战略规划</li>
        </ul>
      </div>
      <div class="md-section">
        <p class="md-heading">## 沟通偏好</p>
        <p>高效直接，偏好数据驱动的决策方式。</p>
      </div>
    `
  }
  return profileData[String(safeUserInfo.value.id)] || profileData['2']
})

// Particle positions
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

// Greeting typing animation
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

// Orb hover state
const orbHovered = ref(false)

// Quick stats (mock data)
const stats = ref([
  { label: '今日对话', value: 12, color: '#0A84FF' },
  { label: '知识文档', value: 48, color: '#30D158' },
  { label: '记忆条目', value: 156, color: '#FFD60A' }
])

function openChat() {
  winStore.openApp('chat')
}

function openFeature(appId: AppId) {
  winStore.openApp(appId)
}

function handleLogout() {
  desktop.logout()
}

onMounted(() => {
  // 初始化外观设置（自动应用）
  const { applySettings } = useAppearanceStore()
  applySettings()

  startGreetingAnimation()

  // GSAP entrance animation
  const tl = gsap.timeline({ defaults: { ease: 'power3.out' } })
  tl.from('.greeting-section', { y: 30, opacity: 0, duration: 0.8 })
    .from('.orb-container', { scale: 0.3, opacity: 0, duration: 1, ease: 'back.out(1.7)' }, '-=0.3')
    .from('.quick-stats', { y: 20, opacity: 0, duration: 0.5 }, '-=0.4')
})

onUnmounted(() => {
  clearInterval(greetingTimer)
})
</script>

<style scoped>
.user-space {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0a0a2e 0%, #1a1a3e 50%, #0d1b2a 100%);
}

/* === Aurora Background === */
.aurora-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

/* 当有壁纸时，降低aurora效果的透明度 */
body.wallpaper-1 .aurora-bg,
body.wallpaper-2 .aurora-bg,
body.wallpaper-3 .aurora-bg,
body.wallpaper-4 .aurora-bg,
body.wallpaper-5 .aurora-bg,
body.wallpaper-6 .aurora-bg {
  opacity: 0.3;
}

.aurora-layer {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  opacity: 0.6;
  filter: blur(80px);
  will-change: transform;
}

.aurora-1 {
  background: radial-gradient(ellipse at 30% 50%, var(--aurora-accent-1), transparent 70%);
  animation: auroraDrift1 12s ease-in-out infinite alternate;
}

.aurora-2 {
  background: radial-gradient(ellipse at 70% 40%, var(--aurora-accent-2), transparent 70%);
  animation: auroraDrift2 15s ease-in-out infinite alternate;
}

.aurora-3 {
  background: radial-gradient(ellipse at 50% 80%, var(--aurora-accent-3), transparent 70%);
  animation: auroraDrift3 18s ease-in-out infinite alternate;
}

@keyframes auroraDrift1 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(10%, -5%) scale(1.1); }
}

@keyframes auroraDrift2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-8%, 6%) scale(1.15); }
}

@keyframes auroraDrift3 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(5%, -8%) scale(1.05); }
}

/* === Particles === */
.particles {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  animation: particleFloat var(--duration, 8s) ease-in-out infinite;
  animation-delay: var(--delay, 0s);
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% { opacity: 0.6; }
  50% {
    transform: translateY(-120px) translateX(20px);
    opacity: 0.6;
  }
  51% {
    transform: translateY(-120px) translateX(-20px);
    opacity: 0.6;
  }
  90% { opacity: 0.6; }
}

/* === Top Bar === */
.user-topbar {
  position: relative;
  z-index: 10;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar-logo-img {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  filter: drop-shadow(0 0 8px rgba(10, 132, 255, 0.4));
}

.topbar-logo-text {
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-icon {
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 200ms;
}

.topbar-icon:hover {
  color: #fff;
}

.topbar-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
}

.topbar-logout {
  color: var(--text-disabled);
}

.topbar-logout:hover {
  color: var(--color-danger);
}

/* === Main Content === */
.user-main {
  position: relative;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: calc(100% - 48px);
  gap: 32px;
  padding-bottom: 60px;
}

/* === Greeting === */
.greeting-section {
  text-align: center;
}

.greeting-text {
  font-size: 32px;
  font-weight: 300;
  letter-spacing: 1px;
  color: #fff;
  min-height: 42px;
}

.greeting-text::after {
  content: '|';
  animation: cursorBlink 1s step-end infinite;
  color: var(--color-primary);
}

.greeting-text.complete::after {
  display: none;
}

@keyframes cursorBlink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.greeting-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin-top: 6px;
  letter-spacing: 2px;
}

/* === Orb === */
.orb-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.orb-container {
  position: relative;
  width: var(--orb-size);
  height: var(--orb-size);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.orb-core {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, #4da6ff, #0A84FF, #5E5CE6);
  box-shadow:
    0 0 40px var(--glow-primary),
    0 0 80px var(--glow-accent),
    inset 0 0 20px rgba(255, 255, 255, 0.1);
  animation: orbPulse 3s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: transform 300ms var(--ease-spring), box-shadow 300ms ease;
}

.orb-container:hover .orb-core {
  transform: scale(1.1);
  box-shadow:
    0 0 60px var(--glow-primary),
    0 0 120px var(--glow-accent),
    inset 0 0 30px rgba(255, 255, 255, 0.2);
}

.orb-container:active .orb-core {
  transform: scale(0.95);
}

@keyframes orbPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 40px var(--glow-primary), 0 0 80px var(--glow-accent);
  }
  50% {
    transform: scale(1.06);
    box-shadow: 0 0 60px var(--glow-primary), 0 0 100px var(--glow-accent);
  }
}

.orb-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(10, 132, 255, 0.2);
  animation: ringPulse 4s ease-in-out infinite;
}

.orb-ring.outer {
  width: var(--orb-glow-size);
  height: var(--orb-glow-size);
}

.orb-ring.inner {
  width: 140px;
  height: 140px;
  animation-delay: 1s;
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.15); opacity: 0.15; }
}

.orb-label {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 1px;
  transition: color 200ms;
}

.orb-container:hover + .orb-label {
  color: #fff;
}

/* === Left Sidebar === */
.user-sidebar {
  position: absolute;
  left: 0;
  top: 48px;
  bottom: 0;
  width: 72px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  background: rgba(10, 10, 30, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

/* Sidebar user avatar */
.sidebar-user {
  display: flex;
  justify-content: center;
  padding: 16px 0 12px;
}

.sidebar-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 0 16px rgba(10, 132, 255, 0.3);
}

/* Sidebar nav */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
}

/* Sidebar item */
.sidebar-item {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  position: relative;
  transition: all 250ms var(--ease-spring);
}

.sidebar-item:hover {
  transform: translateY(-2px);
}

.sidebar-item:active {
  transform: translateY(0) scale(0.95);
}

/* Icon circle with gradient */
.sidebar-item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--item-color);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 250ms var(--ease-spring);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.sidebar-item:hover .sidebar-item-icon {
  box-shadow: 0 4px 16px color-mix(in srgb, var(--item-color) 40%, transparent);
  transform: scale(1.08);
}

.sidebar-item.active .sidebar-item-icon {
  box-shadow: 0 4px 20px color-mix(in srgb, var(--item-color) 50%, transparent);
}

.sidebar-item-icon-logout {
  background: rgba(255, 69, 58, 0.15);
}

/* Label text */
.sidebar-item-label {
  font-size: 9px;
  line-height: 1;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  transition: color 200ms;
}

.sidebar-item:hover .sidebar-item-label {
  color: rgba(255, 255, 255, 0.85);
}

.sidebar-item.active .sidebar-item-label {
  color: var(--item-color);
}

/* Active indicator pill */
.sidebar-item.active::after {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  border-radius: 0 3px 3px 0;
  background: var(--item-color);
  box-shadow: 0 0 8px color-mix(in srgb, var(--item-color) 50%, transparent);
}

/* Separator */
.sidebar-sep {
  width: 32px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.12), transparent);
  margin: 6px 0;
}

/* Bottom section */
.sidebar-bottom {
  padding: 8px;
  display: flex;
  justify-content: center;
}

/* === Slide-out Panel === */
.user-panel {
  position: absolute;
  left: 72px;
  top: 48px;
  bottom: 0;
  width: 360px;
  z-index: 15;
  background: rgba(20, 20, 35, 0.85);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-right: 1px solid var(--user-glass-border);
  display: flex;
  flex-direction: column;
  box-shadow: 20px 0 60px rgba(0, 0, 0, 0.4);
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: transform 300ms var(--ease-spring), opacity 200ms ease;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--user-glass-border);
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.panel-close {
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 150ms;
}

.panel-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* === Chat History === */
.chat-history {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chat-item {
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 200ms ease;
}

.chat-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--user-glass-border);
}

.chat-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  color: #fff;
}

.chat-item-title {
  font-size: 13px;
  font-weight: 500;
}

.chat-item-preview {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-disabled);
}

/* === User Profile === */
.user-profile {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-avatar-section {
  text-align: center;
  padding: 16px 0;
}

.profile-avatar {
  font-size: 48px;
  margin-bottom: 8px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.profile-role {
  font-size: 12px;
  color: var(--color-primary);
  margin-top: 2px;
}

.profile-dept {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.profile-section {
  padding: 0 4px;
}

.profile-section h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--user-glass-border);
}

.profile-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.profile-content :deep(.md-section) {
  margin-bottom: 14px;
}

.profile-content :deep(.md-heading) {
  color: #fff;
  font-weight: 500;
  font-size: 13px;
  margin-bottom: 4px;
}

.profile-content :deep(ul) {
  list-style: none;
  padding: 0;
}

.profile-content :deep(li) {
  padding: 2px 0 2px 16px;
  position: relative;
}

.profile-content :deep(li)::before {
  content: '·';
  position: absolute;
  left: 4px;
  color: var(--color-primary);
  font-weight: bold;
}

/* === Quick Stats === */
.quick-stats {
  display: flex;
  gap: 16px;
}

.stat-badge {
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-value {
  font-weight: 600;
  font-size: 14px;
}

/* === Status Indicators === */
.status-indicators {
  position: absolute;
  bottom: 24px;
  left: 24px;
  display: flex;
  gap: 16px;
  z-index: 10;
}

.status-dot {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-secondary);
}

.status-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--dot-color, #30D158);
  box-shadow: 0 0 6px var(--dot-color, #30D158);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
