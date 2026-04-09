<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <i class="fas fa-robot"></i>
        </div>
        <span class="logo-text">EvolveHub</span>
      </div>

      <nav class="nav-menu">
        <div class="nav-section">
          <div class="nav-section-title">主菜单</div>
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <i :class="item.icon"></i>
            <span>{{ item.title }}</span>
          </router-link>
        </div>
      </nav>

      <div class="user-info">
        <div class="user-avatar">{{ userInitials }}</div>
        <div class="user-details">
          <div class="user-name">{{ userInfo?.nickname || userInfo?.username || '用户' }}</div>
          <div class="user-role">{{ userInfo?.roles?.[0] || '管理员' }}</div>
        </div>
        <button class="user-menu-btn" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <div class="breadcrumb">
            <span>首页</span>
          </div>
        </div>
        <div class="header-right">
          <button class="btn btn-ghost btn-icon">
            <i class="fas fa-bell"></i>
          </button>
          <button class="btn btn-ghost btn-icon">
            <i class="fas fa-cog"></i>
          </button>
        </div>
      </header>

      <div class="page-container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { path: '/home', title: '工作台', icon: 'fas fa-home' },
  { path: '/chat', title: 'AI 对话', icon: 'fas fa-comments' },
  { path: '/agents', title: '智能体', icon: 'fas fa-robot' },
  { path: '/knowledge', title: '知识库', icon: 'fas fa-book' },
  { path: '/tools', title: '工具管理', icon: 'fas fa-tools' },
  { path: '/training', title: '模型训练', icon: 'fas fa-brain' },
  { path: '/approval', title: '审批流程', icon: 'fas fa-tasks' },
  { path: '/data', title: '数据中心', icon: 'fas fa-chart-bar' },
  { path: '/settings', title: '系统设置', icon: 'fas fa-cog' }
]

const userInfo = computed(() => authStore.userInfo)

const userInitials = computed(() => {
  const name = userInfo.value?.nickname || userInfo.value?.username || '用户'
  return name.charAt(0).toUpperCase()
})

const isActive = (path: string) => {
  return route.path === path
}

const handleLogout = () => {
  authStore.logout()
}

onMounted(() => {
  authStore.fetchUserInfo()
})
</script>

<style scoped>
@import '@/styles/global.css';

.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: var(--sidebar-width);
  background: var(--bg-darker);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 20px;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  padding: 8px 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  margin-bottom: 4px;
  text-decoration: none;
}

.nav-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background: rgba(78, 205, 196, 0.15);
  color: var(--primary);
}

.nav-item i {
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.nav-item span {
  flex: 1;
}

.user-info {
  padding: 12px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  color: white;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--text-muted);
}

.user-menu-btn {
  background: transparent;
  color: var(--text-secondary);
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.user-menu-btn:hover {
  background: var(--bg-hover);
  color: var(--accent);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: var(--header-height);
  background: var(--bg-darker);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
}

.btn-ghost:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.btn-icon {
  width: 36px;
  height: 36px;
  padding: 0;
}

.page-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg-dark);
}
</style>
