<template>
  <Transition name="window" @after-leave="onClose">
    <div
      v-if="windowState.isOpen && !windowState.isMinimized"
      class="app-window"
      :class="{ 'is-maximized': windowState.isMaximized, 'is-active': isActive }"
      :style="windowStyle"
      @mousedown="winStore.focusWindow(windowState.id)"
    >
      <!-- Title bar -->
      <div class="window-titlebar" @mousedown.left="startDrag">
        <div class="traffic-lights">
          <span class="light light-close" @click.stop="winStore.closeWindow(windowState.id)">×</span>
          <span class="light light-minimize" @click.stop="winStore.minimizeWindow(windowState.id)">−</span>
          <span class="light light-maximize" @click.stop="winStore.maximizeWindow(windowState.id)">⤢</span>
        </div>
        <div class="window-title">{{ windowState.title }}</div>
        <div class="title-spacer"></div>
      </div>

      <!-- Content area -->
      <div class="window-content">
        <ChatApp v-if="windowState.appId === 'chat'" />
        <KnowledgeApp v-else-if="windowState.appId === 'knowledge'" />
        <ModelApp v-else-if="windowState.appId === 'model'" />
        <UsersApp v-else-if="windowState.appId === 'users'" />
        <ExtApp v-else-if="windowState.appId === 'mcp'" />
        <MemoryApp v-else-if="windowState.appId === 'memory'" />
        <SettingsApp v-else-if="windowState.appId === 'settings'" />
        <DashboardApp v-else-if="windowState.appId === 'dashboard'" />
        <PetGalleryApp v-else-if="windowState.appId === 'pets'" />
        <DesktopIconApp v-else-if="windowState.appId === 'desktopicon'" />
        <DeptApp v-else-if="windowState.appId === 'dept'" />
        <RoleApp v-else-if="windowState.appId === 'role'" />
        <PermissionApp v-else-if="windowState.appId === 'permission'" />
        <SkillEditorApp v-else-if="windowState.appId === 'skill-editor'" />
        <McpEditorApp v-else-if="windowState.appId === 'mcp-editor'" />
        <McpCreateApp v-else-if="windowState.appId === 'mcp-create'" />
        <SkillEditApp v-else-if="windowState.appId === 'skill-edit'" />
      </div>

      <!-- Resize handles -->
      <div v-if="!windowState.isMaximized" class="resize-handle resize-e" @mousedown.stop="startResize('e', $event)" />
      <div v-if="!windowState.isMaximized" class="resize-handle resize-s" @mousedown.stop="startResize('s', $event)" />
      <div v-if="!windowState.isMaximized" class="resize-handle resize-se" @mousedown.stop="startResize('se', $event)" />
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWindowStore } from '../../stores/window'
import type { WindowState } from '../../types'
import ChatApp from '../apps/chat/ChatApp.vue'
import KnowledgeApp from '../apps/knowledge/KnowledgeApp.vue'
import ModelApp from '../apps/model/ModelApp.vue'
import UsersApp from '../apps/users/UsersApp.vue'
import ExtApp from '../apps/ext/ExtApp.vue'
import SkillEditorApp from '../apps/ext/SkillEditorApp.vue'
import McpEditorApp from '../apps/ext/McpEditorApp.vue'
import McpCreateApp from '../apps/ext/McpCreateApp.vue'
import SkillEditApp from '../apps/ext/SkillEditApp.vue'
import MemoryApp from '../apps/memory/MemoryApp.vue'
import SettingsApp from '../apps/settings/SettingsApp.vue'
import DashboardApp from '../apps/dashboard/DashboardApp.vue'
import PetGalleryApp from '../apps/pets/PetGalleryApp.vue'
import DesktopIconApp from '../apps/desktopicon/DesktopIconApp.vue'
import DeptApp from '../apps/dept/DeptApp.vue'
import RoleApp from '../apps/role/RoleApp.vue'
import PermissionApp from '../apps/permission/PermissionApp.vue'

const props = defineProps<{
  windowState: WindowState
  isActive: boolean
}>()

const winStore = useWindowStore()

const windowStyle = computed(() => {
  if (props.windowState.isMaximized) {
    return {
      top: '28px',
      left: '0',
      width: '100%',
      height: 'calc(100% - 28px)',
      zIndex: props.windowState.zIndex,
      borderRadius: '0'
    }
  }
  return {
    top: `${props.windowState.y}px`,
    left: `${props.windowState.x}px`,
    width: `${props.windowState.width}px`,
    height: `${props.windowState.height}px`,
    zIndex: props.windowState.zIndex
  }
})

function onClose() {
  // cleanup if needed
}

// Drag
function startDrag(e: MouseEvent) {
  if (props.windowState.isMaximized) return
  const startX = e.clientX - props.windowState.x
  const startY = e.clientY - props.windowState.y

  function onMove(ev: MouseEvent) {
    winStore.updatePosition(props.windowState.id, ev.clientX - startX, ev.clientY - startY)
  }
  function onUp() {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

// Resize
function startResize(dir: string, e: MouseEvent) {
  const startX = e.clientX
  const startY = e.clientY
  const startW = props.windowState.width
  const startH = props.windowState.height

  function onMove(ev: MouseEvent) {
    const dx = ev.clientX - startX
    const dy = ev.clientY - startY
    if (dir.includes('e')) winStore.updateSize(props.windowState.id, startW + dx, props.windowState.height)
    if (dir.includes('s')) winStore.updateSize(props.windowState.id, props.windowState.width, startH + dy)
    if (dir === 'se') winStore.updateSize(props.windowState.id, startW + dx, startH + dy)
  }
  function onUp() {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}
</script>

<style scoped>
.app-window {
  position: absolute;
  border-radius: var(--radius-window);
  overflow: hidden;
  box-shadow: var(--shadow-window);
  border: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  background: rgba(30, 30, 30, 0.92);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
}

.app-window.is-active {
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.6), 0 0 1px rgba(255, 255, 255, 0.15);
}

.app-window.is-maximized {
  border-radius: 0;
}

.window-titlebar {
  height: 38px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  background: rgba(40, 40, 40, 0.85);
  cursor: default;
  flex-shrink: 0;
}

.traffic-lights {
  display: flex;
  gap: 8px;
  align-items: center;
}

.light {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  color: transparent;
  cursor: pointer;
  transition: color 100ms;
}

.light:hover {
  color: rgba(0, 0, 0, 0.6);
}

.light-close {
  background: #FF5F57;
}

.light-minimize {
  background: #FEBC2E;
}

.light-maximize {
  background: #28C840;
}

.window-title {
  flex: 1;
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: #E0E0E0;
  user-select: none;
}

.title-spacer {
  width: 68px;
}

.window-content {
  flex: 1;
  overflow: hidden;
}

.resize-handle {
  position: absolute;
  z-index: 1;
}

.resize-e {
  right: 0;
  top: 38px;
  bottom: 0;
  width: 4px;
  cursor: e-resize;
}

.resize-s {
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  cursor: s-resize;
}

.resize-se {
  right: 0;
  bottom: 0;
  width: 12px;
  height: 12px;
  cursor: se-resize;
}

/* Window open/close animation */
.window-enter-active {
  animation: windowOpen 300ms var(--ease-spring);
}
.window-leave-active {
  animation: windowClose 200ms ease-in;
}

@keyframes windowOpen {
  from {
    opacity: 0;
    transform: scale(0.3);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes windowClose {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.3);
  }
}
</style>
