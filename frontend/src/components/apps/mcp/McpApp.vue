<template>
  <div class="mcp-app">
    <!-- Left: server list -->
    <div class="mcp-sidebar">
      <div class="sidebar-label">MCP Servers</div>
      <div
        v-for="srv in servers"
        :key="srv.id"
        class="server-card"
        :class="{ active: selectedServer === srv.id }"
        @click="selectedServer = srv.id"
      >
        <span class="server-dot" :class="srv.status"></span>
        <div class="server-info">
          <div class="server-name">{{ srv.name }}</div>
          <div class="server-tools">{{ srv.toolCount }} 个工具</div>
        </div>
      </div>
      <button class="btn btn-outline btn-sm" style="margin-top: auto;">+ 注册 Server</button>
    </div>

    <!-- Right: detail -->
    <div class="mcp-detail">
      <template v-if="currentServer">
        <div class="detail-header">
          <div class="detail-icon">📡</div>
          <div class="detail-title">{{ currentServer.name }} MCP Server</div>
        </div>
        <div class="detail-info">
          <div class="info-row"><span class="info-label">端点:</span><span class="info-value mono">{{ currentServer.endpoint }}</span></div>
          <div class="info-row"><span class="info-label">状态:</span><span class="info-value status-text" :class="currentServer.status">{{ currentServer.status === 'connected' ? '🟢 已连接' : '🔴 断开' }}</span></div>
        </div>
        <div class="detail-actions">
          <button class="btn btn-outline btn-sm">测试连接</button>
          <button class="btn btn-outline btn-sm">发现工具</button>
          <button class="btn btn-outline btn-sm">编辑</button>
          <button class="btn btn-outline btn-sm">删除</button>
        </div>

        <!-- Tool table -->
        <div class="tool-section">
          <div class="tool-title">发现的工具 ({{ currentTools.length }} 个)</div>
          <table class="tool-table">
            <thead>
              <tr>
                <th>工具名称</th>
                <th>风险等级</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in currentTools" :key="t.id">
                <td class="mono">{{ t.name }}</td>
                <td>
                  <span class="risk-badge" :class="t.riskLevel.toLowerCase()">
                    {{ t.riskLevel === 'LOW' ? '🟢 低' : t.riskLevel === 'MEDIUM' ? '🟡 中' : '🔴 高' }}
                  </span>
                </td>
                <td>
                  <label class="toggle-switch">
                    <input type="checkbox" v-model="t.enabled" />
                    <span class="toggle-slider"></span>
                  </label>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>

    <!-- Status bar -->
    <div class="mcp-statusbar">
      {{ servers.length }} 个 Server · {{ totalTools }} 个工具 · {{ enabledTools }} 个已启用
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { McpServer, McpTool } from '../../../types'

const selectedServer = ref('1')

const servers = ref<McpServer[]>([
  { id: '1', name: 'OA 系统', endpoint: 'http://oa.internal:3001', status: 'connected', toolCount: 6 },
  { id: '2', name: 'CRM 系统', endpoint: 'http://crm.internal:3002', status: 'connected', toolCount: 4 },
  { id: '3', name: 'Git 仓库', endpoint: 'http://git.internal:3003', status: 'disconnected', toolCount: 4 }
])

const allTools = ref<Record<string, McpTool[]>>({
  '1': [
    { id: '1', name: 'query_leave', serverName: 'OA', riskLevel: 'LOW', enabled: true },
    { id: '2', name: 'submit_form', serverName: 'OA', riskLevel: 'MEDIUM', enabled: true },
    { id: '3', name: 'delete_data', serverName: 'OA', riskLevel: 'HIGH', enabled: false },
    { id: '4', name: 'query_salary', serverName: 'OA', riskLevel: 'MEDIUM', enabled: true },
    { id: '5', name: 'create_report', serverName: 'OA', riskLevel: 'LOW', enabled: true },
    { id: '6', name: 'send_notification', serverName: 'OA', riskLevel: 'LOW', enabled: true }
  ],
  '2': [
    { id: '7', name: 'query_customer', serverName: 'CRM', riskLevel: 'LOW', enabled: true },
    { id: '8', name: 'update_record', serverName: 'CRM', riskLevel: 'MEDIUM', enabled: true },
    { id: '9', name: 'export_data', serverName: 'CRM', riskLevel: 'HIGH', enabled: false },
    { id: '10', name: 'create_lead', serverName: 'CRM', riskLevel: 'LOW', enabled: true }
  ],
  '3': [
    { id: '11', name: 'list_repos', serverName: 'Git', riskLevel: 'LOW', enabled: true },
    { id: '12', name: 'read_file', serverName: 'Git', riskLevel: 'LOW', enabled: true },
    { id: '13', name: 'commit_change', serverName: 'Git', riskLevel: 'MEDIUM', enabled: false },
    { id: '14', name: 'merge_branch', serverName: 'Git', riskLevel: 'HIGH', enabled: false }
  ]
})

const currentServer = computed(() => servers.value.find(s => s.id === selectedServer.value))
const currentTools = computed(() => allTools.value[selectedServer.value] || [])
const totalTools = computed(() => Object.values(allTools.value).flat().length)
const enabledTools = computed(() => Object.values(allTools.value).flat().filter(t => t.enabled).length)
</script>

<style scoped>
.mcp-app {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mcp-app > :not(.mcp-statusbar) {
  display: flex;
}

.mcp-sidebar {
  width: 200px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.server-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
}

.server-card:hover { background: rgba(255, 255, 255, 0.06); }
.server-card.active { background: rgba(10, 132, 255, 0.2); }

.server-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.server-dot.connected { background: #30D158; }
.server-dot.disconnected { background: #8E8E93; }

.server-name { font-size: 13px; }
.server-tools { font-size: 11px; color: var(--text-disabled); }

.mcp-detail {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-icon { font-size: 24px; }
.detail-title { font-size: 18px; font-weight: 600; }

.detail-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 13px;
}

.info-label { color: var(--text-secondary); }
.info-value { color: var(--text-primary); }
.mono { font-family: monospace; font-size: 12px; }
.status-text.connected { color: #30D158; }
.status-text.disconnected { color: #8E8E93; }

.detail-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tool-section { margin-top: 8px; }
.tool-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; }

.tool-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  overflow: hidden;
}

.tool-table th {
  text-align: left;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
  font-size: 12px;
}

.tool-table td {
  padding: 8px 12px;
  border-top: 1px solid var(--border-subtle);
}

.risk-badge { font-size: 12px; }

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
}

.toggle-switch input { opacity: 0; width: 0; height: 0; }

.toggle-slider {
  position: absolute;
  inset: 0;
  background: #3A3A3C;
  border-radius: 20px;
  cursor: pointer;
  transition: 200ms;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background: white;
  border-radius: 50%;
  transition: 200ms;
}

.toggle-switch input:checked + .toggle-slider { background: #30D158; }
.toggle-switch input:checked + .toggle-slider::before { transform: translateX(16px); }

.mcp-statusbar {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 30, 30, 0.5);
  font-size: 11px;
  color: var(--text-disabled);
  border-top: 1px solid var(--border-subtle);
}

.btn-sm { padding: 4px 10px; font-size: 12px; }
</style>
