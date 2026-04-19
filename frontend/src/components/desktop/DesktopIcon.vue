<template>
  <div class="desktop-icon" @click="$emit('open')" @dblclick="$emit('open')">
    <div class="icon-image" :style="{ background: gradient }">
      <component :is="iconComponent" :size="32" color="#fff" />
    </div>
    <div class="icon-label">{{ name }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
import type { AppId } from '../../types'

const props = defineProps<{
  appId: AppId
  name: string
  icon: string
  gradient: string
}>()

defineEmits<{ open: [] }>()

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

const iconComponent = computed(() => {
  return iconMap[props.icon] || Settings
})
</script>

<style scoped>
.desktop-icon {
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 12px;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid transparent;
}

.desktop-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(0, 245, 255, 0.12), rgba(255, 0, 255, 0.08));
  opacity: 0;
  transition: opacity 0.3s;
}

.desktop-icon:hover::before {
  opacity: 1;
}

.desktop-icon:hover {
  border-color: rgba(0, 245, 255, 0.3);
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 245, 255, 0.15);
}

.desktop-icon:active {
  transform: scale(0.95);
}

.icon-image {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  transition: transform 200ms ease;
}

.desktop-icon:hover .icon-image {
  transform: scale(1.08);
}

.icon-label {
  font-size: 13px;
  font-weight: 500;
  color: #ffffff;
  text-align: center;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  position: relative;
  z-index: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
}
</style>
