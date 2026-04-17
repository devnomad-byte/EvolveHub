<template>
  <div class="dock-wrapper">
    <div class="dock glass">
      <div
        v-for="appId in visibleDockApps"
        :key="appId"
        class="dock-icon-wrapper"
        @mouseenter="hoveredApp = appId"
        @mouseleave="hoveredApp = null"
      >
        <div
          class="dock-icon"
          :class="{
            'is-active': isAppOpen(appId),
            'is-hovered': hoveredApp === appId,
            'is-neighbor': isNeighbor(appId)
          }"
          :style="{ background: getDef(appId).gradient }"
          @click="winStore.openApp(appId)"
        >
          <component :is="iconMap[getDef(appId).icon]" :size="24" color="#fff" />
        </div>
        <div v-if="isAppOpen(appId)" class="dock-dot"></div>
        <Transition name="tooltip">
          <div v-if="hoveredApp === appId" class="dock-tooltip">{{ getDef(appId).name }}</div>
        </Transition>
      </div>
      <div class="dock-separator"></div>
      <div class="dock-icon-wrapper" @mouseenter="hoveredApp = 'folder'" @mouseleave="hoveredApp = null">
        <div class="dock-icon dock-icon-default" :class="{ 'is-hovered': hoveredApp === 'folder' }">
          <Folder :size="24" color="#fff" />
        </div>
      </div>
      <div class="dock-icon-wrapper" @mouseenter="hoveredApp = 'search'" @mouseleave="hoveredApp = null">
        <div class="dock-icon dock-icon-default" :class="{ 'is-hovered': hoveredApp === 'search' }">
          <Search :size="24" color="#fff" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  MessageSquare, BookOpen, Wrench, Settings, Monitor, Cat,
  Folder, Search
} from 'lucide-vue-next'
import { useWindowStore } from '../../stores/window'
import { useDesktopStore } from '../../stores/desktop'
import { appDefinitions, dockApps } from '../../types/apps'
import type { AppId } from '../../types'

const winStore = useWindowStore()
const desktop = useDesktopStore()
const hoveredApp = ref<string | null>(null)

const visibleDockApps = computed(() =>
  dockApps.filter(id => {
    const def = appDefinitions[id]
    if (!def || !desktop.currentUser) return false
    const userRoles = desktop.currentUser.roles || ['USER']
    return userRoles.some(r => def.roles.includes(r))
  })
)

const iconMap: Record<string, ReturnType<typeof MessageSquare>> = {
  MessageSquare,
  BookOpen,
  Bot: Settings,
  Users: Settings,
  Wrench,
  Zap: Settings,
  Settings,
  Monitor,
  Cat,
  Folder,
  Search
}

function getDef(id: string) {
  return appDefinitions[id]
}

function isAppOpen(appId: AppId) {
  return Object.values(winStore.windows).some(w => w.appId === appId && w.isOpen)
}

function isNeighbor(appId: AppId) {
  if (!hoveredApp.value) return false
  const idx = visibleDockApps.value.indexOf(appId)
  const hIdx = visibleDockApps.value.indexOf(hoveredApp.value as AppId)
  return Math.abs(idx - hIdx) === 1
}
</script>

<style scoped>
.dock-wrapper {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 8000;
}

.dock {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid rgba(10, 132, 255, 0.1);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3), 0 0 40px rgba(10, 132, 255, 0.05);
}

.dock-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.dock-icon {
  width: 48px;
  height: 48px;
  border-radius: 22%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms var(--ease-spring);
}

.dock-icon-default {
  background: rgba(255, 255, 255, 0.1);
}

.dock-icon.is-hovered {
  transform: scale(1.5) translateY(-8px);
}

.dock-icon.is-neighbor {
  transform: scale(1.25) translateY(-4px);
}

.dock-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #0A84FF;
  margin-top: 2px;
  box-shadow: 0 0 6px rgba(10, 132, 255, 0.6);
  animation: dockDotPulse 2s ease-in-out infinite;
}

@keyframes dockDotPulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 6px rgba(10, 132, 255, 0.6); }
  50% { opacity: 0.5; box-shadow: 0 0 3px rgba(10, 132, 255, 0.3); }
}

.dock-separator {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 8px;
  align-self: center;
}

.dock-tooltip {
  position: absolute;
  bottom: 100%;
  margin-bottom: 8px;
  padding: 4px 10px;
  background: rgba(40, 40, 40, 0.95);
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  border: 1px solid var(--border-subtle);
}

.tooltip-enter-active,
.tooltip-leave-active {
  transition: opacity 100ms ease;
}
.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
}
</style>
