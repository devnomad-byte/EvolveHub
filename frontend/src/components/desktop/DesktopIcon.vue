<template>
  <div class="desktop-icon" @click="$emit('open')" @dblclick="$emit('open')">
    <div class="icon-image" :style="{ background: gradient }">
      <component :is="iconComponent" :size="28" color="#fff" />
    </div>
    <div class="icon-label">{{ name }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  MessageSquare, BookOpen, Bot, Users, Wrench, Zap, Settings, Monitor, Cat
} from 'lucide-vue-next'
import type { AppId } from '../../types'

const props = defineProps<{
  appId: AppId
  name: string
  icon: string
  gradient: string
}>()

defineEmits<{ open: [] }>()

const iconMap: Record<string, ReturnType<typeof MessageSquare>> = {
  MessageSquare,
  BookOpen,
  Bot,
  Users,
  Wrench,
  Zap,
  Settings,
  Monitor,
  Cat
}

const iconComponent = computed(() => iconMap[props.icon] || Settings)
</script>

<style scoped>
.desktop-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 150ms ease;
}

.desktop-icon:active {
  transform: scale(0.92);
}

.icon-image {
  width: 64px;
  height: 64px;
  border-radius: 22%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 200ms var(--ease-spring), box-shadow 200ms ease;
}

.desktop-icon:hover .icon-image {
  transform: scale(1.08);
  box-shadow: 0 0 20px rgba(10, 132, 255, 0.3);
}

.icon-label {
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  text-align: center;
  white-space: nowrap;
}
</style>
