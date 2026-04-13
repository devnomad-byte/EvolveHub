<template>
  <div class="desktop">
    <!-- Admin background effects -->
    <template v-if="isAdmin">
      <div class="admin-bg">
        <div class="admin-glow admin-glow-bl"></div>
        <div class="admin-glow admin-glow-tr"></div>
        <div class="admin-stars">
          <div
            v-for="s in stars"
            :key="s.id"
            class="star"
            :style="{ left: s.x, top: s.y, width: s.size + 'px', height: s.size + 'px', '--star-opacity': s.starOpacity, animationDelay: s.delay + 's', animationDuration: s.dur + 's' }"
          ></div>
        </div>
      </div>
    </template>

    <!-- Admin layout -->
    <template v-if="isAdmin">
      <MenuBar />
      <div class="desktop-icons">
        <DesktopIcon
          v-for="icon in visibleIcons"
          :key="icon.appId"
          :app-id="icon.appId"
          :name="icon.name"
          :icon="icon.icon"
          :gradient="icon.gradient"
          @open="winStore.openApp(icon.appId)"
        />
      </div>
      <DockBar />
    </template>

    <!-- User layout -->
    <UserDesktop v-else />

    <!-- All open windows (shared) -->
    <AppWindow
      v-for="w in winStore.allWindows"
      :key="w.id"
      :window-state="w"
      :is-active="w.id === winStore.activeWindowId"
    />

    <NotificationPanel v-if="desktop.showNotifications" />

    <!-- Right-click context menu (admin only) -->
    <teleport v-if="isAdmin" to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      >
        <div class="ctx-item" @click="handleChangeWallpaper">更换壁纸</div>
        <div class="ctx-divider"></div>
        <div class="ctx-item" @click="handleShowSettings">显示设置</div>
        <div class="ctx-item" @click="handleAbout">关于 EvolveHub</div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useWindowStore } from '../../stores/window'
import { useDesktopStore } from '../../stores/desktop'
import { useAppearanceStore } from '../../composables/useAppearance'
import { menuApi } from '../../api/menu'
import type { MenuInfo } from '../../api/menu'
import MenuBar from './MenuBar.vue'
import DockBar from './DockBar.vue'
import DesktopIcon from './DesktopIcon.vue'
import AppWindow from '../window/AppWindow.vue'
import NotificationPanel from '../feedback/NotificationPanel.vue'
import UserDesktop from './UserDesktop.vue'

const winStore = useWindowStore()
const desktop = useDesktopStore()
const appearance = useAppearanceStore()

const isAdmin = computed(() => desktop.currentUser.role !== 'USER')

const contextMenu = ref({ visible: false, x: 0, y: 0 })

// 桌面图标列表（从 API 加载）
const desktopIcons = ref<MenuInfo[]>([])

// 加载桌面图标
async function loadDesktopIcons() {
  try {
    // 拦截器已 unwrap，直接返回 data
    const menus = await menuApi.getMenus()
    // parentId === 0 的为桌面图标
    desktopIcons.value = (menus || []).filter(menu => menu.parentId === 0 && menu.status === 1)
  } catch (e) {
    console.error('Failed to load desktop icons:', e)
  }
}

// permCode 转 appId（如 'app:chat' -> 'chat'）
function permCodeToAppId(permCode: string): string {
  // 移除 'app:' 或 'system:' 前缀
  return permCode.replace(/^(app|system):/, '')
}

const stars = Array.from({ length: 40 }, (_, i) => ({
  id: i,
  x: Math.random() * 100 + '%',
  y: Math.random() * 100 + '%',
  size: Math.random() > 0.8 ? 2 : 1,
  starOpacity: (0.15 + Math.random() * 0.35).toFixed(2),
  delay: (Math.random() * 6).toFixed(1),
  dur: (3 + Math.random() * 4).toFixed(1)
}))

const visibleIcons = computed(() => {
  return desktopIcons.value
    .filter(icon => icon.status === 1)
    .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    .map((icon, i) => ({
      appId: permCodeToAppId(icon.permCode),
      name: icon.menuName,
      icon: icon.icon,
      gradient: icon.gradient || 'linear-gradient(135deg, #8E8E93, #636366)',
      col: i % 4,
      row: Math.floor(i / 4)
    }))
})

function handleContextMenu(e: MouseEvent) {
  e.preventDefault()
  contextMenu.value = { visible: true, x: e.clientX, y: e.clientY }
}

function closeContextMenu() {
  contextMenu.value.visible = false
}

function handleChangeWallpaper() {
  closeContextMenu()
  desktop.addToast('壁纸已更换', 'success')
}

function handleShowSettings() {
  closeContextMenu()
  winStore.openApp('settings')
}

function handleAbout() {
  closeContextMenu()
  desktop.addToast('EvolveHub v1.0.0', 'info')
}

// 监听登录状态，加载桌面图标
watch(() => desktop.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    loadDesktopIcons()
  }
}, { immediate: true })

onMounted(() => {
  // 初始化外观设置
  appearance.applySettings()

  // 如果已登录，加载图标
  if (desktop.isLoggedIn) {
    loadDesktopIcons()
  }

  window.addEventListener('contextmenu', handleContextMenu)
  window.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  window.removeEventListener('contextmenu', handleContextMenu)
  window.removeEventListener('click', closeContextMenu)
})
</script>

<style scoped>
.desktop {
  width: 100%;
  height: 100%;
  background: #08080f;
  position: relative;
  overflow: hidden;
}

/* === Admin background: corner glow + stars === */
.admin-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.admin-glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.admin-glow-bl {
  width: 50vmax;
  height: 50vmax;
  bottom: -30%;
  left: -20%;
  background: radial-gradient(circle, rgba(10, 132, 255, 0.07), transparent 65%);
  animation: glowDrift1 20s ease-in-out infinite alternate;
}

.admin-glow-tr {
  width: 45vmax;
  height: 45vmax;
  top: -25%;
  right: -15%;
  background: radial-gradient(circle, rgba(94, 92, 230, 0.05), transparent 65%);
  animation: glowDrift2 24s ease-in-out infinite alternate;
}

@keyframes glowDrift1 {
  0% { transform: translate(0, 0); }
  100% { transform: translate(40px, -30px); }
}

@keyframes glowDrift2 {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-30px, 20px); }
}

.admin-stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.star {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  animation: starTwinkle var(--dur, 4s) ease-in-out infinite;
}

@keyframes starTwinkle {
  0%, 100% { opacity: var(--star-opacity, 0.2); }
  50% { opacity: 0.05; }
}

.desktop-icons {
  position: absolute;
  top: 48px;
  left: 24px;
  display: grid;
  grid-template-columns: repeat(3, 80px);
  gap: 8px 12px;
  z-index: 1;
}

.context-menu {
  position: fixed;
  z-index: 99999;
  width: 200px;
  background: rgba(40, 40, 40, 0.95);
  backdrop-filter: blur(30px);
  border-radius: 8px;
  padding: 4px 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: var(--shadow-popup);
}

.ctx-item {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 4px;
  margin: 0 4px;
}

.ctx-item:hover {
  background: #0A84FF;
}

.ctx-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 4px 8px;
}
</style>
