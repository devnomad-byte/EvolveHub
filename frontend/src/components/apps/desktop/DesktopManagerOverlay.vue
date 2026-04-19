<template>
  <teleport to="body">
    <div :class="['dm-overlay', { visible }]" @click.self="onOverlayClick">
      <div class="dm-radar" v-if="radarVisible"></div>

      <div class="dm-panel">
        <div class="dm-accent"></div>

        <!-- Header -->
        <div class="dm-header">
          <div class="dm-title">
            <div class="dm-title-icon">📦</div>
            <div>
              桌面管理器
              <span class="dm-title-sub">DESKTOP MANAGER</span>
            </div>
          </div>
          <div class="dm-actions">
            <button class="dm-btn dm-btn-ghost" @click="openCreateModal">+ 新分类</button>
            <button class="dm-btn dm-btn-primary" @click="saveChanges">保存</button>
            <button class="dm-btn dm-btn-close" @click="close">×</button>
          </div>
        </div>

        <!-- Body -->
        <div class="dm-body">
          <!-- Left: Category List -->
          <div class="dm-category-list">
            <div class="dm-category-header">已分类</div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="dm-category-item"
              :class="{ active: selectedCategoryId === cat.id }"
              @click="selectCategory(cat)"
            >
              <div class="dm-category-icon" :style="{ background: hexToRgba(cat.color, 0.2) }">
                {{ cat.icon || '📁' }}
              </div>
              <div class="dm-category-info">
                <div class="dm-category-name">{{ cat.name }}</div>
                <div class="dm-category-count">{{ cat.iconCount }} 个图标</div>
              </div>
            </div>

            <div style="margin-top: 12px">
              <div class="dm-category-header">快捷操作</div>
              <div
                class="dm-category-item"
                :class="{ active: selectedCategoryId === 'desktop' }"
                @click="showDesktopIcons"
                style="border-style: dashed; border-color: rgba(0,245,255,0.15);"
              >
                <div class="dm-category-icon" style="background: rgba(0,245,255,0.1)">🖥️</div>
                <div class="dm-category-info">
                  <div class="dm-category-name">桌面图标</div>
                  <div class="dm-category-count">{{ desktopIcons.length }} 个（当前显示）</div>
                </div>
              </div>
              <div
                class="dm-category-item"
                :class="{ active: selectedCategoryId === 'uncategorized' }"
                @click="showUncategorized"
                style="border-style: dashed; border-color: rgba(255,159,10,0.15);"
              >
                <div class="dm-category-icon" style="background: rgba(255,159,10,0.1)">📁</div>
                <div class="dm-category-info">
                  <div class="dm-category-name">未分类</div>
                  <div class="dm-category-count">{{ uncategorizedCount }} 个图标</div>
                </div>
              </div>
            </div>

            <div class="dm-category-add" @click="openCreateModal">+</div>
          </div>

          <!-- Right: Icon Grid -->
          <div class="dm-icons-section">
            <div class="dm-section-header">
              <span class="dm-section-title">图标列表</span>
              <span class="dm-section-hint">拖拽排序 · 点击开关设为桌面图标</span>
            </div>

            <div class="dm-icons-grid">
              <div
                v-for="icon in displayIcons"
                :key="icon.id"
                class="dm-icon"
                :class="{ 'on-desktop': icon.isDesktop === 1 }"
                draggable="true"
                @dragstart="onDragStart($event, icon)"
                @dragover.prevent
                @drop="onDrop(icon)"
                @click="openApp(icon)"
              >
                <button class="dm-icon-remove" @click.stop="removeFromDesktop(icon)">×</button>
                <div class="dm-icon-img" :style="{ background: icon.gradient || 'linear-gradient(135deg, #636366, #48484a)' }">
                  <component :is="getIconComponent(icon.icon)" :size="24" color="#fff" />
                </div>
                <span class="dm-icon-name">{{ icon.menuName }}</span>
                <div class="dm-icon-toggle" @click.stop="toggleDesktop(icon)">
                  <div class="toggle" :class="{ on: icon.isDesktop === 1 }">
                    <div class="toggle-dot"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Category Modal -->
      <div v-if="showModal" class="dm-modal-overlay" @click.self="closeModal">
        <div class="dm-modal">
          <div class="dm-modal-header">
            <span class="dm-modal-title">{{ isEditingCategory ? '编辑分类' : '新建分类' }}</span>
            <button class="dm-modal-close" @click="closeModal">×</button>
          </div>
          <div class="dm-modal-body">
            <div class="dm-form-group">
              <label class="dm-form-label">分类名称</label>
              <input
                v-model="categoryForm.name"
                type="text"
                class="dm-form-input"
                :placeholder="isEditingCategory ? '' : '例如：AI 助手'"
              />
            </div>
            <div class="dm-form-group">
              <label class="dm-form-label">主题色</label>
              <div class="dm-color-presets">
                <div
                  v-for="color in colorPresets"
                  :key="color"
                  class="dm-color-preset"
                  :class="{ active: categoryForm.color === color }"
                  :style="{ background: color }"
                  @click="categoryForm.color = color"
                ></div>
              </div>
            </div>
          </div>
          <div class="dm-modal-footer">
            <button class="dm-btn dm-btn-ghost" @click="closeModal">取消</button>
            <button class="dm-btn dm-btn-primary" @click="handleCategorySubmit">
              {{ isEditingCategory ? '保存' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { desktopApi, type CategoryResponse, type IconResponse } from '@/api/desktop'
import { useDesktopStore } from '@/stores/desktop'
import { useWindowStore } from '@/stores/window'
import type { AppId } from '@/types'
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
  Wind, Snowflake, Thermometer, Archive, Cog, Sliders, LayoutGrid
} from 'lucide-vue-next'

const desktop = useDesktopStore()
const winStore = useWindowStore()

// ==================== State ====================
const visible = ref(false)
const radarVisible = ref(false)
const showModal = ref(false)
const isEditingCategory = ref(false)
const selectedCategoryId = ref<number | 'desktop' | 'uncategorized' | null>(null)
const draggedIcon = ref<IconResponse | null>(null)

const categories = ref<CategoryResponse[]>([])
const icons = ref<IconResponse[]>([])
const desktopIcons = ref<IconResponse[]>([])

const categoryForm = reactive({
  id: 0,
  name: '',
  color: '#0A84FF',
  icon: '📁'
})

const colorPresets = ['#0A84FF', '#30D158', '#BF5AF2', '#FF9F0A', '#FF453A', '#00f5ff']

// 图标映射表
const iconMap: Record<string, ReturnType<typeof MessageSquare>> = {
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
  Wind, Snowflake, Thermometer, Archive, Cog, Sliders, LayoutGrid
}

// permCode 转 appId（如 'app:chat' -> 'chat'）
function permCodeToAppId(permCode: string): AppId {
  return permCode.replace(/^(app|system):/, '') as AppId
}

// 点击图标打开应用
function openApp(icon: IconResponse) {
  const appId = permCodeToAppId(icon.permCode)
  close()
  winStore.openApp(appId)
}

// ==================== Computed ====================
const displayIcons = computed(() => {
  if (selectedCategoryId.value === 'desktop') {
    return desktopIcons.value
  }
  if (selectedCategoryId.value === 'uncategorized') {
    return icons.value.filter(i => !i.categoryId || Number(i.categoryId) === 0)
  }
  if (selectedCategoryId.value) {
    return icons.value.filter(i => Number(i.categoryId) === Number(selectedCategoryId.value))
  }
  return icons.value
})

const uncategorizedCount = computed(() => {
  return icons.value.filter(i => !i.categoryId || Number(i.categoryId) === 0).length
})

// ==================== Data Load ====================
async function loadData() {
  try {
    const res = await desktopApi.getAll()
    categories.value = res.categories || []
    icons.value = res.icons || []
    desktopIcons.value = res.desktopIcons || []
    // 通知桌面视图刷新图标
    window.dispatchEvent(new CustomEvent('desktop-icons-updated'))
  } catch (e: any) {
    desktop.addToast('加载数据失败', 'error')
  }
}

// ==================== Actions ====================
async function open() {
  visible.value = true
  radarVisible.value = true
  selectedCategoryId.value = null
  await loadData()
  setTimeout(() => { radarVisible.value = false }, 1000)
}

function close() {
  visible.value = false
}

function onOverlayClick(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('dm-overlay')) {
    close()
  }
}

function selectCategory(cat: CategoryResponse) {
  selectedCategoryId.value = selectedCategoryId.value === cat.id ? null : cat.id
}

function showDesktopIcons() {
  selectedCategoryId.value = 'desktop'
}

function showUncategorized() {
  selectedCategoryId.value = 'uncategorized'
}

function onDragStart(e: DragEvent, icon: IconResponse) {
  draggedIcon.value = icon
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
  }
  ;(e.target as HTMLElement).style.opacity = '0.4'
}

function onDrop(target: IconResponse) {
  if (!draggedIcon.value || draggedIcon.value.id === target.id) return
  const draggedIdx = icons.value.findIndex(i => i.id === draggedIcon.value!.id)
  const targetIdx = icons.value.findIndex(i => i.id === target.id)
  if (draggedIdx !== -1 && targetIdx !== -1) {
    const temp = icons.value[draggedIdx].sort
    icons.value[draggedIdx] = { ...icons.value[draggedIdx], sort: icons.value[targetIdx].sort }
    icons.value[targetIdx] = { ...icons.value[targetIdx], sort: temp }
  }
  draggedIcon.value = null
  document.querySelectorAll('.dm-icon').forEach(el => (el as HTMLElement).style.opacity = '')
}

async function removeFromDesktop(icon: IconResponse) {
  try {
    await desktopApi.updateIcon({
      permId: icon.permId,
      isDesktop: 0,
      categoryId: icon.categoryId,
      sort: icon.sort
    })
    desktop.addToast('已从桌面移除', 'success')
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  }
}

async function toggleDesktop(icon: IconResponse) {
  try {
    await desktopApi.updateIcon({
      permId: icon.permId,
      isDesktop: icon.isDesktop === 1 ? 0 : 1,
      categoryId: icon.categoryId,
      sort: icon.sort
    })
    desktop.addToast(icon.isDesktop === 1 ? '已从桌面移除' : '已添加到桌面', 'success')
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  }
}

function saveChanges() {
  desktop.addToast('配置已保存', 'success')
}

function openCreateModal() {
  isEditingCategory.value = false
  Object.assign(categoryForm, { id: 0, name: '', color: '#0A84FF', icon: '📁' })
  showModal.value = true
}

function openEditCategoryModal(cat: CategoryResponse) {
  isEditingCategory.value = true
  Object.assign(categoryForm, { id: cat.id, name: cat.name, color: cat.color, icon: cat.icon })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleCategorySubmit() {
  if (!categoryForm.name.trim()) {
    desktop.addToast('请填写分类名称', 'error')
    return
  }
  try {
    if (isEditingCategory.value) {
      await desktopApi.updateCategory({
        id: categoryForm.id,
        name: categoryForm.name,
        icon: categoryForm.icon,
        color: categoryForm.color
      })
      desktop.addToast('分类已更新', 'success')
    } else {
      await desktopApi.createCategory({
        name: categoryForm.name,
        icon: categoryForm.icon,
        color: categoryForm.color
      })
      desktop.addToast('分类已创建', 'success')
    }
    closeModal()
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  }
}

async function deleteCategory(cat: CategoryResponse) {
  try {
    await desktopApi.deleteCategory(cat.id)
    desktop.addToast('分类已删除', 'success')
    if (selectedCategoryId.value === cat.id) {
      selectedCategoryId.value = null
    }
    await loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && visible.value) {
    close()
  }
}

// 颜色工具函数：将 HEX 颜色转为 RGBA
function hexToRgba(hex: string, alpha: number): string {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 获取图标组件
function getIconComponent(iconName: string): ReturnType<typeof MessageSquare> {
  return iconMap[iconName] || Settings
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})

defineExpose({ open })
</script>

<style scoped>
/* ========== 全屏管理器 ========== */
.dm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(5, 5, 10, 0.94);
  backdrop-filter: blur(20px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s, visibility 0.4s;
}

/* 科幻背景网格 */
.dm-overlay::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 245, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 245, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

/* 科幻背景光晕 */
.dm-overlay::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(0, 245, 255, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(255, 0, 255, 0.06) 0%, transparent 50%);
  pointer-events: none;
}

.dm-overlay.visible {
  opacity: 1;
  visibility: visible;
}

.dm-radar {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  pointer-events: none;
  animation: radarPulse 1s ease-out forwards;
}

@keyframes radarPulse {
  0% { transform: scale(0); opacity: 0.8; }
  100% { transform: scale(3.5); opacity: 0; }
}

.dm-radar::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid #00f5ff;
  box-shadow: 0 0 30px rgba(0, 245, 255, 0.3);
}

.dm-panel {
  width: 92%;
  max-width: 1100px;
  max-height: 85vh;
  background: rgba(8, 8, 16, 0.98);
  border: 1px solid rgba(0, 245, 255, 0.2);
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transform: scale(0.9) translateY(20px);
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dm-overlay.visible .dm-panel {
  transform: scale(1) translateY(0);
  opacity: 1;
  transition-delay: 0.15s;
}

/* 边框流光 */
.dm-panel::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 26px;
  background: linear-gradient(135deg, #00f5ff, #ff00ff, #00f5ff);
  background-size: 200% 200%;
  animation: borderFlow 3s linear infinite;
  z-index: -1;
  opacity: 0.6;
}

@keyframes borderFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.dm-accent {
  height: 3px;
  background: linear-gradient(90deg, #00f5ff, #ff00ff, #00f5ff);
  background-size: 200% 100%;
  animation: accentFlow 2s linear infinite;
  flex-shrink: 0;
}

@keyframes accentFlow {
  0% { background-position: 0% 0; }
  100% { background-position: 200% 0; }
}

.dm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 26px;
  border-bottom: 1px solid rgba(0, 245, 255, 0.08);
  flex-shrink: 0;
}

.dm-title {
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #e0e0e8;
}

.dm-title-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(0, 245, 255, 0.2), rgba(255, 0, 255, 0.1));
  border: 1px solid rgba(0, 245, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.dm-title-sub {
  font-size: 9px;
  color: #00f5ff;
  letter-spacing: 1.5px;
  opacity: 0.7;
  display: block;
  margin-top: 2px;
}

.dm-actions {
  display: flex;
  gap: 10px;
}

.dm-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px 26px;
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 28px;
}

.dm-body::-webkit-scrollbar { width: 6px; }
.dm-body::-webkit-scrollbar-track { background: rgba(0,0,0,0.2); border-radius: 3px; }
.dm-body::-webkit-scrollbar-thumb { background: rgba(0,245,255,0.3); border-radius: 3px; }

.dm-category-header {
  font-size: 10px;
  color: #8e8e93;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 12px;
  margin-bottom: 6px;
}

.dm-category-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dm-category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  background: rgba(255,255,255,0.02);
}

.dm-category-item:hover {
  background: rgba(0,245,255,0.05);
  border-color: rgba(0,245,255,0.1);
}

.dm-category-item.active {
  background: linear-gradient(135deg, rgba(0,245,255,0.12), rgba(255,0,255,0.06));
  border-color: rgba(0,245,255,0.25);
}

.dm-category-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.dm-category-info { flex: 1; }

.dm-category-name {
  font-size: 13px;
  font-weight: 600;
  color: #e0e0e8;
}

.dm-category-count {
  font-size: 10px;
  color: #8e8e93;
}

.dm-category-item.active .dm-category-count {
  color: #00f5ff;
}

.dm-category-add {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(0,245,255,0.06);
  border: 1px dashed rgba(0,245,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #00f5ff;
  font-size: 16px;
  transition: all 0.2s;
  margin-top: 8px;
}

.dm-category-add:hover {
  background: rgba(0,245,255,0.12);
  border-style: solid;
}

.dm-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.dm-section-title {
  font-size: 12px;
  color: #e0e0e8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.dm-section-hint {
  font-size: 11px;
  color: #00f5ff;
  background: rgba(0,245,255,0.1);
  padding: 4px 10px;
  border-radius: 10px;
}

.dm-icons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
}

.dm-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  border-radius: 16px;
  cursor: grab;
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.05);
  transition: all 0.25s;
  position: relative;
}

.dm-icon:hover {
  background: rgba(0, 245, 255, 0.08);
  border-color: rgba(0, 245, 255, 0.2);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,245,255,0.12);
}

.dm-icon:active { cursor: grabbing; }

.dm-icon.on-desktop {
  background: rgba(0, 245, 255, 0.05);
  border-color: rgba(0, 245, 255, 0.15);
}

.dm-icon.on-desktop::after {
  content: '桌面';
  position: absolute;
  top: 6px;
  right: 6px;
  font-size: 8px;
  color: #00f5ff;
  background: rgba(0,245,255,0.15);
  padding: 2px 5px;
  border-radius: 4px;
}

.dm-icon-img {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.dm-icon-name {
  font-size: 12px;
  color: #e0e0e8;
  text-align: center;
  max-width: 85px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dm-icon-toggle { margin-top: 4px; }

.dm-icon-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(255,69,58,0.6);
  border: none;
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dm-icon:hover .dm-icon-remove { opacity: 1; }

.toggle {
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.toggle.on { background: #30D158; }

.toggle-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: all 0.2s;
}

.toggle.on .toggle-dot { left: 19px; }

.dm-btn {
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
  font-family: inherit;
}

.dm-btn-ghost {
  background: rgba(0, 245, 255, 0.05);
  color: #00f5ff;
  border: 1px solid rgba(0, 245, 255, 0.2);
}

.dm-btn-ghost:hover { background: rgba(0, 245, 255, 0.1); }

.dm-btn-primary {
  background: linear-gradient(135deg, #00f5ff, #0A84FF);
  color: #000;
}

.dm-btn-primary:hover { box-shadow: 0 0 20px rgba(0, 245, 255, 0.4); }

.dm-btn-close {
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(255, 69, 58, 0.1);
  color: #FF453A;
  border: 1px solid rgba(255, 69, 58, 0.2);
  border-radius: 10px;
}

.dm-btn-close:hover { background: rgba(255, 69, 58, 0.2); }

/* ========== Modal ========== */
.dm-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.25s, visibility 0.25s;
}

.dm-modal-overlay.open {
  opacity: 1;
  visibility: visible;
}

.dm-modal {
  width: 400px;
  background: rgba(10, 10, 20, 0.98);
  backdrop-filter: blur(40px);
  border-radius: 20px;
  border: 1px solid rgba(0,245,255,0.2);
  overflow: hidden;
}

.dm-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(0,245,255,0.1);
}

.dm-modal-title { font-size: 15px; font-weight: 700; color: #e0e0e8; }

.dm-modal-close {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: rgba(255,69,58,0.1);
  color: #FF453A;
  border: 1px solid rgba(255,69,58,0.2);
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.dm-modal-close:hover { background: rgba(255,69,58,0.2); }

.dm-modal-body { padding: 22px; }

.dm-form-group { margin-bottom: 16px; }

.dm-form-label {
  display: block;
  font-size: 11px;
  color: #6e6e80;
  margin-bottom: 8px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dm-form-input {
  width: 100%;
  padding: 12px 14px;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(0,245,255,0.1);
  border-radius: 10px;
  color: #e0e0e8;
  font-size: 14px;
  transition: all 0.2s;
  font-family: inherit;
}

.dm-form-input:focus {
  outline: none;
  border-color: #00f5ff;
  box-shadow: 0 0 12px rgba(0,245,255,0.2);
}

.dm-color-presets {
  display: flex;
  gap: 8px;
}

.dm-color-preset {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.dm-color-preset:hover { transform: scale(1.1); }
.dm-color-preset.active { border-color: #fff; box-shadow: 0 0 12px currentColor; }

.dm-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px;
  border-top: 1px solid rgba(0,245,255,0.1);
}
</style>
