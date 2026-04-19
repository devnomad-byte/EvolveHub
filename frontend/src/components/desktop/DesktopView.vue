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
          @contextmenu.prevent="openIconContextMenu($event, icon)"
        />
      </div>

      <!-- Organize button - bottom right -->
      <button class="organize-btn" @click="handleDesktopManager">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <rect x="3" y="3" width="7" height="7" rx="1"/>
          <rect x="14" y="3" width="7" height="7" rx="1"/>
          <rect x="3" y="14" width="7" height="7" rx="1"/>
          <rect x="14" y="14" width="7" height="7" rx="1"/>
        </svg>
        整理桌面
      </button>

      <!-- Icon context menu -->
      <teleport to="body">
        <div
          v-if="iconContextMenu.visible"
          class="icon-ctx-menu"
          :style="{ left: iconContextMenu.x + 'px', top: iconContextMenu.y + 'px' }"
        >
          <div class="ctx-item" @click="showIconInManager">📦 在管理器中显示</div>
          <div class="ctx-divider"></div>
          <div class="ctx-item danger" @click="removeIconFromDesktop">🗑️ 从桌面移除</div>
        </div>
      </teleport>

      <DockBar />

      <!-- Hint text -->
      <div class="desktop-hint">右键桌面图标或点击右下角整理桌面</div>
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

    <DesktopManagerOverlay ref="dmOverlay" />
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
        <div class="ctx-item" @click="handleDesktopManager">桌面整理</div>
        <div class="ctx-item" @click="handleShowSettings">个人设置</div>
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
import { desktopApi } from '../../api/desktop'
import type { IconResponse } from '../../api/desktop'
import type { AppId } from '../../types'
import MenuBar from './MenuBar.vue'
import DockBar from './DockBar.vue'
import DesktopIcon from './DesktopIcon.vue'
import AppWindow from '../window/AppWindow.vue'
import NotificationPanel from '../feedback/NotificationPanel.vue'
import UserDesktop from './UserDesktop.vue'
import DesktopManagerOverlay from '../apps/desktop/DesktopManagerOverlay.vue'

const dmOverlay = ref<InstanceType<typeof DesktopManagerOverlay> | null>(null)

const winStore = useWindowStore()
const desktop = useDesktopStore()
const appearance = useAppearanceStore()

const isAdmin = computed(() => desktop.isAdmin)

const contextMenu = ref({ visible: false, x: 0, y: 0 })
const iconContextMenu = ref({ visible: false, x: 0, y: 0, icon: null as any })

// 桌面图标列表（从桌面管理器 API 加载）
const desktopIcons = ref<IconResponse[]>([])

// 加载桌面图标（使用桌面管理器数据源）
async function loadDesktopIcons() {
  try {
    const res = await desktopApi.getAll()
    // 使用 eh_desktop_icon 表中 is_desktop=1 的图标
    desktopIcons.value = res.desktopIcons || []
  } catch (e) {
    console.error('Failed to load desktop icons:', e)
  }
}

// permCode 转 appId（如 'app:chat' -> 'chat'）
function permCodeToAppId(permCode: string): AppId {
  // 移除 'app:' 或 'system:' 前缀
  return permCode.replace(/^(app|system):/, '') as AppId
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
    .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    .map((icon, i) => ({
      appId: permCodeToAppId(icon.permCode),
      name: icon.menuName,
      icon: icon.icon,
      gradient: icon.gradient || 'linear-gradient(135deg, #8E8E93, #636366)',
      permId: icon.permId,
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

function openIconContextMenu(e: MouseEvent, icon: any) {
  e.preventDefault()
  e.stopPropagation()
  iconContextMenu.value = { visible: true, x: e.clientX, y: e.clientY, icon }
}

function closeIconContextMenu() {
  iconContextMenu.value.visible = false
}

function showIconInManager() {
  closeIconContextMenu()
  dmOverlay.value?.open()
}

async function removeIconFromDesktop() {
  closeIconContextMenu()
  const icon = iconContextMenu.value.icon
  if (icon && icon.permId) {
    try {
      await desktopApi.updateIcon({
        permId: icon.permId,
        isDesktop: 0
      })
      desktop.addToast('已从桌面移除', 'success')
      // 重新加载桌面图标
      loadDesktopIcons()
    } catch (e: any) {
      desktop.addToast(e.message || '操作失败', 'error')
    }
  }
}

function handleDesktopManager() {
  closeContextMenu()
  dmOverlay.value?.open()
}

function handleChangeWallpaper() {
  closeContextMenu()
  // 随机更换壁纸（1-6）
  const wallpaperList = [1, 2, 3, 4, 5, 6]
  const currentWallpaper = appearance.settings.value.wallpaper
  // 过滤掉当前壁纸，随机选择一个新的
  const available = wallpaperList.filter(w => w !== currentWallpaper)
  const newWallpaper = available[Math.floor(Math.random() * available.length)]
  appearance.setWallpaper(newWallpaper)
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
  window.addEventListener('click', () => {
    closeContextMenu()
    closeIconContextMenu()
  })
  // 监听桌面管理器更新事件
  window.addEventListener('desktop-icons-updated', loadDesktopIcons)
})

onUnmounted(() => {
  window.removeEventListener('contextmenu', handleContextMenu)
  window.removeEventListener('click', closeContextMenu)
  window.removeEventListener('desktop-icons-updated', loadDesktopIcons)
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

/* === Admin background: corner glow + stars + grid === */
.admin-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

/* Grid background like prototype */
.admin-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 245, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 245, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
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
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
  z-index: 1;
  width: calc(100% - 48px);
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

/* ========== Organize Button ========== */
.organize-btn {
  position: absolute;
  bottom: 28px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: linear-gradient(135deg, rgba(0, 245, 255, 0.1), rgba(255, 0, 255, 0.08));
  backdrop-filter: blur(20px);
  border-radius: 28px;
  border: 1px solid rgba(0, 245, 255, 0.3);
  color: #00f5ff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  z-index: 200;
  transition: all 0.3s ease;
  font-family: inherit;
}

.organize-btn:hover {
  border-color: #00f5ff;
  box-shadow: 0 0 20px rgba(0, 245, 255, 0.4);
  transform: translateY(-2px);
}

/* ========== Icon Context Menu ========== */
.icon-ctx-menu {
  position: fixed;
  z-index: 99999;
  width: 180px;
  background: rgba(10, 10, 20, 0.98);
  border: 1px solid rgba(0, 245, 255, 0.2);
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.5);
}

.icon-ctx-menu .ctx-item {
  padding: 8px 12px;
  font-size: 12px;
  color: var(--text-primary);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-ctx-menu .ctx-item:hover {
  background: rgba(0, 245, 255, 0.1);
}

.icon-ctx-menu .ctx-item.danger:hover {
  background: rgba(255, 69, 58, 0.1);
  color: #FF453A;
}

/* ========== Desktop Hint ========== */
.desktop-hint {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,245,255,0.08);
  border: 1px solid rgba(0,245,255,0.25);
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 12px;
  color: #00f5ff;
  z-index: 150;
  letter-spacing: 0.5px;
  pointer-events: none;
}
</style>
