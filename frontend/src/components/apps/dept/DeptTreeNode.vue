<template>
  <div class="tree-node">
    <div
      class="tree-item"
      :class="{ active: selectedId === dept.id }"
      :style="{ paddingLeft: (depth * 16 + 12) + 'px' }"
      @click="$emit('select', dept.id)"
    >
      <span v-if="hasChildren" class="toggle-icon" @click.stop="toggle">
        <component :is="isOpen ? 'ChevronDown' : 'ChevronRight'" :size="14" />
      </span>
      <span v-else class="toggle-placeholder"></span>
      <Building :size="14" class="tree-icon" />
      <span class="tree-label">{{ dept.deptName }}</span>
    </div>
    <div v-if="hasChildren && isOpen" class="tree-children">
      <DeptTreeNode
        v-for="child in dept.children"
        :key="child.id"
        :dept="child"
        :selected-id="selectedId"
        :depth="depth + 1"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChevronRight, ChevronDown, Building } from 'lucide-vue-next'
import type { DeptInfo } from '../../../api/dept'

const props = defineProps<{
  dept: DeptInfo
  selectedId: number | null
  depth: number
}>()

defineEmits<{
  select: [id: number]
}>()

const isOpen = ref(true)
const hasChildren = computed(() => props.dept.children && props.dept.children.length > 0)

function toggle() {
  isOpen.value = !isOpen.value
}
</script>

<style scoped>
.tree-item {
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

.tree-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.tree-item.active {
  background: rgba(100, 210, 255, 0.15);
  color: #64D2FF;
}

.toggle-icon {
  display: flex;
  align-items: center;
  color: var(--text-disabled);
}

.toggle-placeholder {
  width: 14px;
  flex-shrink: 0;
}

.tree-icon {
  color: #64D2FF;
  flex-shrink: 0;
}

.tree-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
