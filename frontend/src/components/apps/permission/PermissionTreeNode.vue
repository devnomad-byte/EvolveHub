<template>
  <div class="perm-tree-node">
    <div
      class="perm-tree-item"
      :class="{ active: selectedId === perm.id }"
      :style="{ paddingLeft: (depth * 16 + 12) + 'px' }"
      @click="$emit('select', perm)"
    >
      <span v-if="hasChildren" class="toggle-icon" @click.stop="toggle">
        <component :is="isOpen ? 'ChevronDown' : 'ChevronRight'" :size="14" />
      </span>
      <span v-else class="toggle-placeholder"></span>
      <component :is="getTypeIcon(perm.permType)" :size="14" class="perm-type-icon" />
      <span class="perm-name">{{ perm.permName }}</span>
      <span class="perm-type-badge" :class="perm.permType.toLowerCase()">{{ perm.permType }}</span>
    </div>
    <div v-if="hasChildren && isOpen" class="perm-children">
      <PermissionTreeNode
        v-for="child in perm.children"
        :key="child.id"
        :perm="child"
        :selected-id="selectedId"
        :depth="depth + 1"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChevronRight, ChevronDown, Menu, MousePointerClick, Globe } from 'lucide-vue-next'
import type { PermissionInfo } from '../../../api/adminPermission'

const props = defineProps<{
  perm: PermissionInfo & { children?: PermissionInfo[] }
  selectedId: number | null
  depth: number
}>()

defineEmits<{
  select: [perm: PermissionInfo]
}>()

const isOpen = ref(true)
const hasChildren = computed(() => props.perm.children && props.perm.children.length > 0)

function toggle() {
  isOpen.value = !isOpen.value
}

function getTypeIcon(type: string) {
  switch (type) {
    case 'MENU': return Menu
    case 'BUTTON': return MousePointerClick
    case 'API': return Globe
    default: return Menu
  }
}
</script>

<style scoped>
.perm-tree-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 6px;
  margin: 0 8px;
  font-size: 13px;
  color: var(--text-primary);
  transition: all 0.15s;
  user-select: none;
}

.perm-tree-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.perm-tree-item.active {
  background: rgba(255, 214, 10, 0.15);
  color: #FFD60A;
}

.toggle-icon {
  display: flex;
  align-items: center;
  color: var(--text-disabled);
  flex-shrink: 0;
}

.toggle-placeholder {
  width: 14px;
  flex-shrink: 0;
}

.perm-type-icon {
  color: #FFD60A;
  flex-shrink: 0;
}

.perm-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.perm-type-badge {
  font-size: 9px;
  padding: 1px 5px;
  border-radius: 3px;
  font-weight: 600;
  flex-shrink: 0;
}

.perm-type-badge.menu {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.perm-type-badge.button {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.perm-type-badge.api {
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
}
</style>
