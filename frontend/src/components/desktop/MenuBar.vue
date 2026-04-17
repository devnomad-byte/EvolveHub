<template>
  <div class="menubar glass">
    <div class="menubar-left">
      <div class="menubar-logo">
        <div class="logo-icon"></div>
        <span class="logo-text">EvolveHub</span>
      </div>
      <div class="menubar-items">
        <span v-for="item in menuItems" :key="item" class="menu-item">{{ item }}</span>
      </div>
    </div>
    <div class="menubar-right">
      <div class="menubar-status">
        <span class="status-indicator online" title="系统正常"></span>
        <span class="status-indicator model" title="模型在线"></span>
      </div>
      <span class="menubar-time">{{ timeStr }}</span>
      <div class="menubar-bell" @click="desktop.toggleNotifications">
        🔔
        <span v-if="unreadCount > 0" class="bell-dot"></span>
      </div>
      <div class="menubar-user" @click="showUserMenu = !showUserMenu">
        <div class="user-avatar">
          <img v-if="userAvatarUrl" :src="userAvatarUrl" alt="头像" class="user-avatar-img" />
          <span v-else>{{ userAvatarChar }}</span>
        </div>
        <div v-if="showUserMenu" class="user-dropdown">
          <div class="dropdown-item">{{ userDisplayText }}</div>
          <div class="dropdown-item" @click="winStore.openApp('settings')">个人设置</div>
          <div class="dropdown-item" @click="openPasswordTab">修改密码</div>
          <div class="dropdown-divider"></div>
          <div class="dropdown-item logout" @click="handleLogout">退出登录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useDesktopStore } from '../../stores/desktop'
import { useWindowStore } from '../../stores/window'

const desktop = useDesktopStore()
const winStore = useWindowStore()
const showUserMenu = ref(false)

const menuItems = ['文件', '编辑', '显示', '帮助']

const unreadCount = computed(() => desktop.notifications.filter(n => !n.read).length)

// 安全的用户信息计算属性
const userAvatarChar = computed(() => {
  if (!desktop.currentUser?.displayName) return 'U'
  return desktop.currentUser.displayName.charAt(0) || 'U'
})

const userAvatarUrl = computed(() => {
  const avatar = desktop.currentUser?.avatar
  if (!avatar || avatar === '👤') return ''
  return avatar
})

const userDisplayText = computed(() => {
  if (!desktop.currentUser?.displayName) return '用户'
  const roleMap: Record<string, string> = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'DEPT_HEAD': '部门负责人',
    'LEADER': '领导',
    'USER': '普通用户'
  }
  const roles = desktop.currentUser.roles || ['USER']
  const roleName = roles.map(r => roleMap[r] || r).join('、')
  const dept = desktop.currentUser.deptName
  return dept ? `${desktop.currentUser.displayName} · ${dept} · ${roleName}` : `${desktop.currentUser.displayName} · ${roleName}`
})

const now = ref(new Date())
let timer = 0

const timeStr = computed(() => {
  const d = now.value
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
})

function handleLogout() {
  showUserMenu.value = false
  desktop.logout()
}

function openPasswordTab() {
  showUserMenu.value = false
  winStore.pendingSettingsTab = 'password'
  winStore.openApp('settings')
}

onMounted(() => {
  timer = window.setInterval(() => { now.value = new Date() }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.menubar {
  height: var(--menubar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  position: relative;
  z-index: 9000;
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 1px 8px rgba(10, 132, 255, 0.05);
}

.menubar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menubar-logo {
  display: flex;
  align-items: center;
  gap: 6px;
}

.logo-icon {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
}

.logo-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.menubar-items {
  display: flex;
  gap: 2px;
}

.menu-item {
  padding: 2px 8px;
  font-size: 13px;
  color: #E0E0E0;
  border-radius: 4px;
  cursor: default;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menubar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menubar-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  animation: statusBlink 2s ease-in-out infinite;
}

.status-indicator.online {
  background: #30D158;
  box-shadow: 0 0 4px rgba(48, 209, 88, 0.5);
}

.status-indicator.model {
  background: #0A84FF;
  box-shadow: 0 0 4px rgba(10, 132, 255, 0.5);
}

@keyframes statusBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.menubar-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}

.menubar-bell {
  position: relative;
  cursor: pointer;
  font-size: 14px;
}

.bell-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 6px;
  height: 6px;
  background: #FF453A;
  border-radius: 50%;
}

.menubar-user {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  overflow: hidden;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-dropdown {
  position: absolute;
  top: 24px;
  right: 0;
  width: 180px;
  background: rgba(40, 40, 40, 0.95);
  backdrop-filter: blur(30px);
  border-radius: 8px;
  padding: 4px 0;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-popup);
  z-index: 99999;
}

.dropdown-item {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 4px;
  margin: 0 4px;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.dropdown-item.logout {
  color: #FF453A;
}

.dropdown-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 4px 8px;
}
</style>
