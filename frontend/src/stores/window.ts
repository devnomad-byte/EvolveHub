import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { WindowState, AppId } from '../types'
import { appDefinitions } from '../types/apps'

const STORAGE_KEY = 'evolvehub_windows'

let nextZIndex = 100

function loadFromStorage(): { windows: Record<string, WindowState>, activeId: string | null, maxZ: number } | null {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      return {
        windows: parsed.windows || {},
        activeId: parsed.activeId || null,
        maxZ: parsed.maxZ || 100
      }
    }
  } catch {
    // ignore parse errors
  }
  return null
}

function saveToStorage(windows: Record<string, WindowState>, activeId: string | null, maxZ: number) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      windows,
      activeId,
      maxZ
    }))
  } catch {
    // ignore storage errors
  }
}

export const useWindowStore = defineStore('window', () => {
  // 页面加载时从 localStorage 恢复窗口状态
  const restored = loadFromStorage()
  const windows = ref<Record<string, WindowState>>(restored?.windows || {})
  // 恢复时重置最大化状态和位置，确保窗口在可见范围内
  Object.values(windows.value).forEach(w => {
    w.isMaximized = false
    // 确保窗口位置至少距离顶部 40px，否则重置到居中
    if (w.y < 40) {
      w.x = Math.max(40, (window.innerWidth - w.width) / 2)
      w.y = Math.max(40, (window.innerHeight - w.height) / 2 - 40)
    }
  })
  const activeWindowId = ref<string | null>(restored?.activeId || null)
  nextZIndex = restored?.maxZ || 100

  // 设置应用的指定标签页（跨组件通信用）
  const pendingSettingsTab = ref<string | null>(null)

  // 传递给 skill-editor 窗口的参数
  const pendingSkillToEdit = ref<{ skillId: number | null, skillName: string } | null>(null)

  // 传递给 mcp-editor 窗口的参数
  const pendingMcpToEdit = ref<{ mcpId: number | null, mcpName: string } | null>(null)

  // 传递给 chat 窗口的待定位会话 ID
  const pendingChatSessionId = ref<string | null>(null)

  const windowList = computed(() => Object.values(windows.value))

  const openWindows = computed(() =>
    windowList.value.filter(w => w.isOpen && !w.isMinimized)
      .sort((a, b) => a.zIndex - b.zIndex)
  )

  const allWindows = computed(() =>
    windowList.value.filter(w => w.isOpen)
  )

  // 持久化到 localStorage
  watch(
    [windows, activeWindowId, () => nextZIndex],
    () => saveToStorage(windows.value, activeWindowId.value, nextZIndex),
    { deep: true }
  )

  function openApp(appId: AppId) {
    const existing = windowList.value.find(w => w.appId === appId && w.isOpen)
    if (existing) {
      if (existing.isMinimized) {
        existing.isMinimized = false
      }
      focusWindow(existing.id)
      return
    }

    const def = appDefinitions[appId]
    const id = `${appId}-${Date.now()}`
    const count = Object.keys(windows.value).length
    const offset = (count % 5) * 30
    const x = Math.max(40, (window.innerWidth - def.defaultWidth) / 2 + offset)
    const y = Math.max(40, (window.innerHeight - def.defaultHeight) / 2 + offset - 40)

    const ws: WindowState = {
      id,
      appId,
      title: def.name,
      x,
      y,
      width: def.defaultWidth,
      height: def.defaultHeight,
      minWidth: def.minWidth,
      minHeight: def.minHeight,
      zIndex: ++nextZIndex,
      isMinimized: false,
      isMaximized: false,
      isOpen: true
    }
    windows.value[id] = ws
    activeWindowId.value = id
  }

  function closeWindow(id: string) {
    const w = windows.value[id]
    if (w) w.isOpen = false
    if (activeWindowId.value === id) {
      const remaining = openWindows.value
      activeWindowId.value = remaining.length > 0
        ? remaining[remaining.length - 1].id
        : null
    }
  }

  function closeByAppId(appId: AppId) {
    const w = windowList.value.find(w => w.appId === appId && w.isOpen)
    if (w) closeWindow(w.id)
  }

  function minimizeWindow(id: string) {
    const w = windows.value[id]
    if (w) w.isMinimized = true
    if (activeWindowId.value === id) {
      const remaining = openWindows.value
      activeWindowId.value = remaining.length > 0
        ? remaining[remaining.length - 1].id
        : null
    }
  }

  function maximizeWindow(id: string) {
    const w = windows.value[id]
    if (w) w.isMaximized = !w.isMaximized
    focusWindow(id)
  }

  function focusWindow(id: string) {
    const w = windows.value[id]
    if (w) {
      w.zIndex = ++nextZIndex
      activeWindowId.value = id
    }
  }

  function updatePosition(id: string, x: number, y: number) {
    const w = windows.value[id]
    if (w) {
      w.x = x
      w.y = y
    }
  }

  function updateSize(id: string, width: number, height: number) {
    const w = windows.value[id]
    if (w) {
      w.width = Math.max(w.minWidth, width)
      w.height = Math.max(w.minHeight, height)
    }
  }

  return {
    windows,
    activeWindowId,
    openWindows,
    allWindows,
    pendingSettingsTab,
    pendingSkillToEdit,
    pendingMcpToEdit,
    pendingChatSessionId,
    openApp,
    closeWindow,
    closeByAppId,
    minimizeWindow,
    maximizeWindow,
    focusWindow,
    updatePosition,
    updateSize
  }
})
