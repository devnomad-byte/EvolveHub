import { ref, watch } from 'vue'

const APPEARANCE_STORAGE_KEY = 'evolvehub_appearance_settings'

export interface AppearanceSettings {
  wallpaper: number
  theme: 'dark' | 'light'
}

const defaultSettings: AppearanceSettings = {
  wallpaper: 0,
  theme: 'dark'
}

// 全局状态
const settings = ref<AppearanceSettings>({ ...defaultSettings })

// 从localStorage加载
function loadSettings(): AppearanceSettings {
  try {
    const saved = localStorage.getItem(APPEARANCE_STORAGE_KEY)
    if (saved) {
      return { ...defaultSettings, ...JSON.parse(saved) }
    }
  } catch (error) {
    console.error('Failed to load appearance settings:', error)
  }
  return { ...defaultSettings }
}

// 保存到localStorage
function saveSettings(newSettings: AppearanceSettings) {
  try {
    localStorage.setItem(APPEARANCE_STORAGE_KEY, JSON.stringify(newSettings))
  } catch (error) {
    console.error('Failed to save appearance settings:', error)
  }
}

// 初始化设置（只执行一次）
let initialized = false

function initializeSettings() {
  if (initialized) return
  settings.value = loadSettings()
  applySettings()
  initialized = true
}

// 应用外观设置
function applySettings() {
  const root = document.documentElement

  // 应用壁纸到管理员背景元素（而不是body）
  const adminBg = document.querySelector('.admin-bg')
  if (adminBg) {
    // 移除所有壁纸类
    for (let i = 1; i <= 6; i++) {
      adminBg.classList.remove(`wallpaper-${i}`)
    }
    // wallpaper 为 0 时不上色，恢复原始背景
    if (settings.value.wallpaper > 0) {
      adminBg.classList.add(`wallpaper-${settings.value.wallpaper}`)
    }
  }

  // 应用主题到html
  root.setAttribute('data-theme', settings.value.theme)

  // 调试日志
  console.log('[Appearance] Applied settings:', {
    wallpaper: settings.value.wallpaper,
    theme: settings.value.theme,
    adminBgClasses: adminBg?.className,
    adminBgExists: !!adminBg,
    htmlTheme: root.getAttribute('data-theme')
  })
}

// 监听设置变化并应用
watch(settings, (newSettings) => {
  applySettings()
  saveSettings(newSettings)
}, { deep: true })

// 更新壁纸
function setWallpaper(wallpaperId: number) {
  settings.value.wallpaper = wallpaperId
}

// 更新主题
function setTheme(theme: 'dark' | 'light') {
  settings.value.theme = theme
}

// 获取当前壁纸
function getCurrentWallpaper(): number {
  return settings.value.wallpaper
}

// 获取当前主题
function getCurrentTheme(): 'dark' | 'light' {
  return settings.value.theme
}

// 恢复默认设置
function resetSettings() {
  localStorage.removeItem(APPEARANCE_STORAGE_KEY)
  settings.value = { ...defaultSettings }
  applySettings()
}

export function useAppearanceStore() {
  // 确保初始化
  initializeSettings()

  return {
    settings,
    setWallpaper,
    setTheme,
    getCurrentWallpaper,
    getCurrentTheme,
    applySettings,
    resetSettings
  }
}
