<template>
  <div class="mcp-editor-app">
    <McpEditorPanel
      v-if="mcpData && mcpData.mcpId !== null"
      :mcp-id="mcpData.mcpId"
      :mcp-name="mcpData.mcpName"
    />
    <div v-else class="no-mcp">
      <Server :size="48" />
      <p>未指定要编辑的 MCP Server</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Server } from 'lucide-vue-next'
import { useWindowStore } from '@/stores/window'
import McpEditorPanel from './McpEditorPanel.vue'

const winStore = useWindowStore()
const mcpData = ref<{ mcpId: number | null; mcpName: string } | null>(null)

onMounted(() => {
  mcpData.value = winStore.pendingMcpToEdit
  winStore.pendingMcpToEdit = null
})
</script>

<style scoped>
.mcp-editor-app {
  height: 100%;
  background: #1C1C1E;
}

.no-mcp {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-disabled);
  gap: 16px;
}

.no-mcp svg {
  opacity: 0.3;
}

.no-mcp p {
  font-size: 14px;
  margin: 0;
}
</style>
