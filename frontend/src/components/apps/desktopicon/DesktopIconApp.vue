<template>
  <div class="desktop-icon-app">
    <!-- Header -->
    <div class="app-header">
      <div class="header-left">
        <svg class="header-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7"/>
          <rect x="14" y="3" width="7" height="7"/>
          <rect x="14" y="14" width="7" height="7"/>
          <rect x="3" y="14" width="7" height="7"/>
        </svg>
        <span class="header-title">桌面图标管理</span>
      </div>
      <div class="header-stats">
        <span class="stat-badge">
          <span class="stat-num">{{ icons.length }}</span>
          <span class="stat-label">总图标</span>
        </span>
        <span class="stat-badge active">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">启用</span>
        </span>
        <span class="stat-badge disabled">
          <span class="stat-num">{{ icons.length - activeCount }}</span>
          <span class="stat-label">禁用</span>
        </span>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="app-toolbar">
      <div class="toolbar-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="searchQuery" placeholder="搜索图标名称、编码..." class="search-input" />
      </div>
      <button class="btn-create" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        创建图标
      </button>
    </div>

    <!-- Icon Table -->
    <div class="icon-list">
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else-if="filteredIcons.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7"/>
          <rect x="14" y="3" width="7" height="7"/>
          <rect x="14" y="14" width="7" height="7"/>
          <rect x="3" y="14" width="7" height="7"/>
        </svg>
        <span>暂无图标数据</span>
      </div>
      <table v-else class="icon-table">
        <thead>
          <tr>
            <th>图标</th>
            <th>名称</th>
            <th>编码</th>
            <th>渐变色</th>
            <th>尺寸</th>
            <th>排序</th>
            <th>Dock</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="icon in filteredIcons" :key="icon.id">
            <td>
              <div class="icon-preview" :style="{ background: icon.gradient }">
                <component :is="getIconComponent(icon.icon)" :size="20" color="#fff" />
              </div>
            </td>
            <td>{{ icon.permName }}</td>
            <td><code class="code-tag">{{ icon.permCode }}</code></td>
            <td>
              <div class="gradient-preview" :style="{ background: icon.gradient }"></div>
            </td>
            <td>{{ icon.defaultWidth }}×{{ icon.defaultHeight }}</td>
            <td>{{ icon.sort }}</td>
            <td>{{ icon.dockOrder >= 0 ? icon.dockOrder : '-' }}</td>
            <td>
              <span class="status-tag" :class="icon.status === 1 ? 'active' : 'disabled'">
                <span class="status-dot"></span>
                {{ icon.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="action-btn edit" title="编辑" @click="openEditModal(icon)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button class="action-btn delete" title="删除" @click="handleDelete(icon)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Modal -->
    <teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal modal-lg">
          <div class="modal-header">
            <span class="modal-title">{{ isEditing ? '编辑图标' : '创建图标' }}</span>
            <button class="modal-close" @click="showModal = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-field">
                <label>图标名称 <span class="required">*</span></label>
                <input v-model="form.permName" placeholder="请输入图标名称" />
              </div>
              <div class="form-field">
                <label>权限编码 <span class="required">*</span></label>
                <input v-model="form.permCode" :disabled="isEditing" placeholder="如: app-knowledge" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>图标 <span class="required">*</span></label>
                <select v-model="form.icon" class="form-select">
                  <option v-for="iconName in availableIcons" :key="iconName" :value="iconName">
                    {{ iconName }}
                  </option>
                </select>
              </div>
              <div class="form-field">
                <label>渐变色 <span class="required">*</span></label>
                <div class="gradient-picker">
                  <input type="color" v-model="gradientStart" @input="updateGradient" class="color-input" />
                  <input type="color" v-model="gradientEnd" @input="updateGradient" class="color-input" />
                  <div class="gradient-preview" :style="{ background: form.gradient }"></div>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>路径</label>
                <input v-model="form.path" placeholder="/knowledge" />
              </div>
              <div class="form-field">
                <label>排序</label>
                <input v-model.number="form.sort" type="number" placeholder="0" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>默认宽度</label>
                <input v-model.number="form.defaultWidth" type="number" placeholder="800" />
              </div>
              <div class="form-field">
                <label>默认高度</label>
                <input v-model.number="form.defaultHeight" type="number" placeholder="600" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>最小宽度</label>
                <input v-model.number="form.minWidth" type="number" placeholder="640" />
              </div>
              <div class="form-field">
                <label>最小高度</label>
                <input v-model.number="form.minHeight" type="number" placeholder="400" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-field">
                <label>Dock 顺序</label>
                <input v-model.number="form.dockOrder" type="number" placeholder="-1" />
              </div>
              <div class="form-field">
                <label>状态</label>
                <div class="toggle-wrapper">
                  <div class="toggle" :class="{ on: form.status === 1 }" @click="form.status = form.status === 1 ? 0 : 1">
                    <div class="toggle-dot"></div>
                  </div>
                  <span class="toggle-label">{{ form.status === 1 ? '正常' : '禁用' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showModal = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="btn-spinner"></span>
              <span v-else>{{ isEditing ? '保存修改' : '创建图标' }}</span>
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { desktopIconApi, type DesktopIconInfo } from '../../../api/desktopIcon'
import { useDesktopStore } from '../../../stores/desktop'
import { useConfirm } from '@/composables/useConfirm'
import {
  MessageSquare, BookOpen, Bot, Users, Wrench, Zap, Settings, Monitor, Cat,
  Building, FileText, Folder, Database, Cloud, Globe, Star, Heart, Flag, Tag,
  Bookmark, Gift, Gem, Crown, Home, Search, Bell, Mail, Lock, Key, Shield,
  Layers, Layout, Grid, List, Edit, Trash2, Copy, Download, Upload,
  Save, Share, Link, ExternalLink, Eye, EyeOff, Clipboard, MapPin, Phone,
  User, UserPlus, UserCheck, Code, Terminal, GitBranch, Package, Server,
  Cpu, HardDrive, Smartphone, Tablet, Watch, Tv, Battery, BatteryCharging,
  Plug, Power, RefreshCw, RotateCcw, Loader, AlertCircle, AlertTriangle,
  Info, HelpCircle, CheckCircle, XCircle, PlusCircle, MinusCircle, X, Check,
  ChevronDown, ChevronUp, ChevronLeft, ChevronRight, ArrowRight, ArrowLeft,
  ArrowUp, ArrowDown, ArrowUpDown, Maximize2, Minimize2, Minimize, Expand,
  Shrink, Focus, Crosshair, MousePointer, Hand, GripVertical, MoreHorizontal,
  Menu, Circle, Navigation, Compass, Flame, Leaf, Lightbulb, Rocket, Sparkles,
  Tent, TreeDeciduous, Umbrella, Sunrise, Sunset, Moon, Sun, Droplet, Waves,
  Wind, Snowflake, Thermometer, Archive, Cog, Sliders
} from 'lucide-vue-next'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

// ==================== Icon Map ====================
const iconMap: Record<string, typeof MessageSquare> = {
  MessageSquare, BookOpen, Bot, Users, Wrench, Zap, Settings, Monitor, Cat,
  Building, FileText, Folder, Database, Cloud, Globe, Star, Heart, Flag, Tag,
  Bookmark, Gift, Gem, Crown, Home, Search, Bell, Mail, Lock, Key, Shield,
  Layers, Layout, Grid, List, Edit, Trash2, Copy, Download, Upload,
  Save, Share, Link, ExternalLink, Eye, EyeOff, Clipboard, MapPin, Phone,
  User, UserPlus, UserCheck, Code, Terminal, GitBranch, Package, Server,
  Cpu, HardDrive, Smartphone, Tablet, Watch, Tv, Battery, BatteryCharging,
  Plug, Power, RefreshCw, RotateCcw, Loader, AlertCircle, AlertTriangle,
  Info, HelpCircle, CheckCircle, XCircle, PlusCircle, MinusCircle, X, Check,
  ChevronDown, ChevronUp, ChevronLeft, ChevronRight, ArrowRight, ArrowLeft,
  ArrowUp, ArrowDown, ArrowUpDown, Maximize2, Minimize2, Minimize, Expand,
  Shrink, Focus, Crosshair, MousePointer, Hand, GripVertical, MoreHorizontal,
  Menu, Circle, Navigation, Compass, Flame, Leaf, Lightbulb, Rocket, Sparkles,
  Tent, TreeDeciduous, Umbrella, Sunrise, Sunset, Moon, Sun, Droplet, Waves,
  Wind, Snowflake, Thermometer, Archive, Cog, Sliders
}

const availableIcons = Object.keys(iconMap)

function getIconComponent(name: string) {
  return iconMap[name] || Settings
}

// ==================== Data ====================
const icons = ref<DesktopIconInfo[]>([])
const isLoading = ref(true)
const searchQuery = ref('')

// ==================== Modal State ====================
const showModal = ref(false)
const isEditing = ref(false)
const isSubmitting = ref(false)
const form = ref({
  id: 0,
  permName: '',
  permCode: '',
  icon: 'Settings',
  gradient: 'linear-gradient(135deg, #8E8E93, #636366)',
  path: '',
  sort: 0,
  defaultWidth: 800,
  defaultHeight: 600,
  minWidth: 640,
  minHeight: 400,
  dockOrder: -1,
  status: 1
})

// 渐变色选择器
const gradientStart = ref('#8E8E93')
const gradientEnd = ref('#636366')

function updateGradient() {
  form.value.gradient = `linear-gradient(135deg, ${gradientStart.value}, ${gradientEnd.value})`
}

function parseGradient(gradient: string) {
  // 从 gradient 字符串中解析两个颜色值
  const match = gradient.match(/linear-gradient\(135deg,\s*(#[0-9A-Fa-f]{6}),\s*(#[0-9A-Fa-f]{6})\)/)
  if (match) {
    gradientStart.value = match[1]
    gradientEnd.value = match[2]
  }
}

// ==================== Computed ====================
const activeCount = computed(() => icons.value.filter(i => i.status === 1).length)

const filteredIcons = computed(() => {
  if (!searchQuery.value.trim()) return icons.value
  const q = searchQuery.value.toLowerCase()
  return icons.value.filter(i =>
    i.permName.toLowerCase().includes(q) ||
    i.permCode.toLowerCase().includes(q)
  )
})

// ==================== Load Data ====================
async function loadIcons() {
  try {
    icons.value = await desktopIconApi.list()
  } catch (e: any) {
    desktop.addToast(e.message || '加载图标列表失败', 'error')
  }
}

// ==================== Modal Actions ====================
function openCreateModal() {
  isEditing.value = false
  gradientStart.value = '#8E8E93'
  gradientEnd.value = '#636366'
  form.value = {
    id: 0,
    permName: '',
    permCode: '',
    icon: 'Settings',
    gradient: 'linear-gradient(135deg, #8E8E93, #636366)',
    path: '',
    sort: 0,
    defaultWidth: 800,
    defaultHeight: 600,
    minWidth: 640,
    minHeight: 400,
    dockOrder: -1,
    status: 1
  }
  showModal.value = true
}

function openEditModal(icon: DesktopIconInfo) {
  isEditing.value = true
  form.value = {
    id: icon.id,
    permName: icon.permName,
    permCode: icon.permCode,
    icon: icon.icon,
    gradient: icon.gradient,
    path: icon.path,
    sort: icon.sort,
    defaultWidth: icon.defaultWidth,
    defaultHeight: icon.defaultHeight,
    minWidth: icon.minWidth,
    minHeight: icon.minHeight,
    dockOrder: icon.dockOrder,
    status: icon.status
  }
  parseGradient(icon.gradient)
  showModal.value = true
}

async function handleSubmit() {
  if (isSubmitting.value) return

  if (!form.value.permName.trim()) {
    desktop.addToast('请输入图标名称', 'error')
    return
  }
  if (!form.value.permCode.trim()) {
    desktop.addToast('请输入权限编码', 'error')
    return
  }
  if (!form.value.icon) {
    desktop.addToast('请选择图标', 'error')
    return
  }
  if (!form.value.gradient.trim()) {
    desktop.addToast('请输入渐变色', 'error')
    return
  }

  isSubmitting.value = true
  try {
    if (isEditing.value) {
      await desktopIconApi.update({
        id: form.value.id,
        permName: form.value.permName,
        permCode: form.value.permCode,
        icon: form.value.icon,
        gradient: form.value.gradient,
        path: form.value.path,
        sort: form.value.sort,
        defaultWidth: form.value.defaultWidth,
        defaultHeight: form.value.defaultHeight,
        minWidth: form.value.minWidth,
        minHeight: form.value.minHeight,
        dockOrder: form.value.dockOrder,
        status: form.value.status
      })
      desktop.addToast('图标更新成功', 'success')
    } else {
      await desktopIconApi.create({
        permName: form.value.permName,
        permCode: form.value.permCode,
        icon: form.value.icon,
        gradient: form.value.gradient,
        path: form.value.path,
        sort: form.value.sort,
        defaultWidth: form.value.defaultWidth,
        defaultHeight: form.value.defaultHeight,
        minWidth: form.value.minWidth,
        minHeight: form.value.minHeight,
        dockOrder: form.value.dockOrder,
        status: form.value.status
      })
      desktop.addToast('图标创建成功', 'success')
    }
    showModal.value = false
    await loadIcons()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDelete(icon: DesktopIconInfo) {
  if (!await confirm('删除图标', `确定要删除图标「${icon.permName}」吗？`)) return
  try {
    await desktopIconApi.delete(icon.id)
    desktop.addToast('图标已删除', 'success')
    await loadIcons()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

// ==================== Init ====================
onMounted(async () => {
  isLoading.value = true
  await loadIcons()
  isLoading.value = false
})
</script>

<style scoped>
.desktop-icon-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ========== Header ========== */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  color: #0A84FF;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-stats {
  display: flex;
  gap: 8px;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 12px;
}

.stat-num {
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  color: var(--text-secondary);
}

.stat-badge.active .stat-num { color: #30D158; }
.stat-badge.disabled .stat-num { color: #FF453A; }

/* ========== Toolbar ========== */
.app-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.toolbar-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  transition: all 0.2s;
  width: 260px;
}

.toolbar-search:focus-within {
  border-color: #0A84FF;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.1);
}

.toolbar-search svg {
  color: var(--text-disabled);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  padding: 0;
}

.search-input::placeholder {
  color: var(--text-disabled);
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  color: white;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(10, 132, 255, 0.3);
}

.btn-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(10, 132, 255, 0.4);
}

/* ========== Icon List ========== */
.icon-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-disabled);
  font-size: 14px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #0A84FF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== Table ========== */
.icon-table {
  width: 100%;
  border-collapse: collapse;
}

.icon-table th {
  text-align: left;
  padding: 10px 12px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}

.icon-table td {
  padding: 12px 12px;
  font-size: 13px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.icon-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.icon-preview {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gradient-preview {
  width: 60px;
  height: 24px;
  border-radius: 4px;
}

.code-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 11px;
  font-family: monospace;
  color: var(--text-secondary);
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-tag.active {
  color: #30D158;
  background: rgba(48, 209, 88, 0.1);
}

.status-tag.disabled {
  color: #FF453A;
  background: rgba(255, 69, 58, 0.1);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-tag.active .status-dot { background: #30D158; }
.status-tag.disabled .status-dot { background: #FF453A; }

.action-btns {
  display: flex;
  gap: 4px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.action-btn.edit:hover { background: rgba(10, 132, 255, 0.12); color: #0A84FF; }
.action-btn.delete:hover { background: rgba(255, 69, 58, 0.12); color: #FF453A; }

/* ========== Modal ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.modal {
  width: 560px;
  max-height: 90vh;
  background: rgba(30, 30, 30, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}

.modal-lg {
  width: 620px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.modal-close:hover {
  background: rgba(255, 69, 58, 0.1);
  color: #FF453A;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-subtle);
}

/* ========== Form ========== */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-field label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

.required {
  color: #FF453A;
}

.form-field input, .form-select {
  height: 38px;
  padding: 0 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s;
  width: 100%;
}

.form-field input:focus, .form-select:focus {
  border-color: #0A84FF;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.1);
}

.form-select {
  appearance: none;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23888' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

.gradient-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-input {
  width: 38px;
  height: 38px;
  padding: 2px;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
}

.color-input::-webkit-color-swatch-wrapper {
  padding: 0;
}

.color-input::-webkit-color-swatch {
  border: none;
  border-radius: 6px;
}

.gradient-preview {
  flex: 1;
  height: 38px;
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
}

/* ========== Toggle ========== */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
}

.toggle {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.toggle.on {
  background: #30D158;
}

.toggle-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: all 0.2s;
}

.toggle.on .toggle-dot {
  left: 23px;
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ========== Buttons ========== */
.btn {
  height: 38px;
  padding: 0 20px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  color: white;
  box-shadow: 0 2px 8px rgba(10, 132, 255, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(10, 132, 255, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
</style>
