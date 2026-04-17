<template>
  <div class="file-tree-node">
    <div
      class="node-item"
      :class="{ selected: isSelected, folder: node.type === 'folder' }"
      :style="{ paddingLeft: depth * 16 + 8 + 'px' }"
      @click="$emit('select', node)"
    >
      <component :is="node.type === 'folder' ? Folder : File" :size="14" />
      <span class="node-name">{{ node.name }}</span>
      <span v-if="node.type === 'file'" class="node-size">{{ formatSize(node.size) }}</span>
      <button class="node-delete" @click.stop="$emit('delete', node)">
        <Trash2 :size="12" />
      </button>
    </div>
    <div v-if="node.type === 'folder' && node.children" class="node-children">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :depth="depth + 1"
        :is-selected="selectedPath === child.path"
        @select="$emit('select', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Folder, File, Trash2 } from 'lucide-vue-next'
import type { FileNode } from '@/api/adminSkillFile'

const props = defineProps<{
  node: FileNode
  depth: number
  isSelected?: boolean
}>()

defineEmits<{
  select: [node: FileNode]
  delete: [node: FileNode]
}>()

const selectedPath = props.isSelected ? props.node.path : ''

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.node-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}
.node-item:hover { background: rgba(255, 255, 255, 0.05); }
.node-item.selected { background: rgba(10, 132, 255, 0.15); }
.node-item.folder { font-weight: 500; }
.node-name { flex: 1; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.node-size { font-size: 11px; color: var(--text-disabled); }
.node-delete { opacity: 0; background: none; border: none; color: var(--text-disabled); cursor: pointer; padding: 2px; }
.node-item:hover .node-delete { opacity: 1; }
.node-delete:hover { color: #FF453A; }
</style>
