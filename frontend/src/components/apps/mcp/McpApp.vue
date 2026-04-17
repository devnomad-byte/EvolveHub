<template>
  <div class="skill-manage">
    <!-- Header with tabs -->
    <div class="skill-header">
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: activeTab === 'mcp' }"
          @click="activeTab = 'mcp'"
        >
          <span class="tab-icon">🔧</span>
          MCP Servers
        </button>
        <button
          class="tab"
          :class="{ active: activeTab === 'skill' }"
          @click="activeTab = 'skill'"
        >
          <span class="tab-icon">✨</span>
          Skills
        </button>
      </div>
      <div class="header-actions" v-if="activeTab === 'mcp'">
        <button class="btn btn-primary btn-sm" @click="showMcpDialog = true">
          + 添加 Server
        </button>
      </div>
      <div class="header-actions" v-else>
        <button class="btn btn-primary btn-sm" @click="showSkillDialog = true">
          + 创建 Skill
        </button>
        <button class="btn btn-outline btn-sm" @click="showHubDialog = true">
          + 从 Hub 安装
        </button>
      </div>
    </div>

    <!-- MCP Servers Tab -->
    <div v-if="activeTab === 'mcp'" class="tab-content">
      <div class="split-view">
        <!-- Left: Server list -->
        <div class="server-list">
          <div class="list-header">
            <span class="list-title">服务器列表</span>
            <span class="list-count">{{ mcpServers.length }} 个</span>
          </div>
          <div class="list-items">
            <div
              v-for="server in mcpServers"
              :key="server.id"
              class="list-item"
              :class="{ active: selectedMcpId === server.id }"
              @click="selectMcp(server)"
            >
              <div class="item-main">
                <span class="status-dot" :class="server.enabled ? 'online' : 'offline'"></span>
                <span class="item-name">{{ server.name }}</span>
              </div>
              <div class="item-sub">{{ server.name }}</div>
            </div>
            <div v-if="mcpServers.length === 0" class="empty-tip">
              暂无 MCP Server
            </div>
          </div>
        </div>

        <!-- Right: Detail panel -->
        <div class="detail-panel">
          <template v-if="selectedMcp">
            <div class="detail-header">
              <div class="detail-title">
                <span class="status-dot large" :class="selectedMcp.enabled ? 'online' : 'offline'"></span>
                {{ selectedMcp.name }}
              </div>
              <div class="detail-actions">
                <button class="btn btn-outline btn-sm" @click="testConnection">测试连接</button>
                <button class="btn btn-outline btn-sm" @click="discoverTools">发现工具</button>
                <button class="btn btn-outline btn-sm" @click="editMcp">编辑</button>
                <button class="btn btn-outline btn-sm danger" @click="deleteMcp">删除</button>
              </div>
            </div>

            <div class="detail-info">
              <div class="info-row">
                <span class="info-label">传输方式:</span>
                <span class="info-value">{{ selectedMcp.transportType === 'UPLOADED' ? '本地上传 (STDIO)' : '远程 URL (SSE)' }}</span>
              </div>
              <div class="info-row" v-if="selectedMcp.serverUrl">
                <span class="info-label">服务器地址:</span>
                <span class="info-value mono">{{ selectedMcp.serverUrl }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">范围:</span>
                <span class="info-value scope-badge" :class="selectedMcp.scope.toLowerCase()">
                  {{ scopeText(selectedMcp.scope) }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">状态:</span>
                <span class="info-value">{{ selectedMcp.enabled ? '已启用' : '已禁用' }}</span>
              </div>
            </div>

            <!-- Tools section -->
            <div class="tools-section">
              <div class="section-header">
                <span class="section-title">工具列表 ({{ mcpTools.length }})</span>
              </div>
              <table class="data-table" v-if="mcpTools.length > 0">
                <thead>
                  <tr>
                    <th>工具名称</th>
                    <th>描述</th>
                    <th>风险等级</th>
                    <th>范围</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="tool in mcpTools" :key="tool.id">
                    <td class="mono">{{ tool.name }}</td>
                    <td class="desc">{{ tool.description || '-' }}</td>
                    <td>
                      <span class="risk-badge" :class="tool.riskLevel?.toLowerCase()">
                        {{ riskText(tool.riskLevel) }}
                      </span>
                    </td>
                    <td>
                      <span class="scope-badge" :class="(tool.toolScope || 'system').toLowerCase()">
                        {{ scopeText(tool.toolScope) }}
                      </span>
                    </td>
                    <td>
                      <label class="toggle-switch">
                        <input type="checkbox" :checked="tool.enabled === 1" @change="toggleTool(tool)" />
                        <span class="toggle-slider"></span>
                      </label>
                    </td>
                    <td>
                      <button class="btn-link" @click="editTool(tool)">编辑</button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="empty-tip">暂无工具，请点击"发现工具"自动获取</div>
            </div>
          </template>
          <div v-else class="empty-state">
            <div class="empty-icon">📡</div>
            <div class="empty-text">选择一个 MCP Server 查看详情</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Skills Tab -->
    <div v-if="activeTab === 'skill'" class="tab-content">
      <div class="split-view">
        <!-- Left: Skill list -->
        <div class="server-list">
          <div class="list-header">
            <span class="list-title">技能列表</span>
            <span class="list-count">{{ skills.length }} 个</span>
          </div>
          <div class="list-items">
            <div
              v-for="skill in skills"
              :key="skill.id"
              class="list-item"
              :class="{ active: selectedSkillId === skill.id }"
              @click="selectSkill(skill)"
            >
              <div class="item-main">
                <span class="skill-icon">{{ skillIcon(skill.skillType) }}</span>
                <span class="item-name">{{ skill.name }}</span>
              </div>
              <div class="item-sub">{{ skill.skillType }}</div>
            </div>
            <div v-if="skills.length === 0" class="empty-tip">
              暂无 Skill
            </div>
          </div>
        </div>

        <!-- Right: Detail panel -->
        <div class="detail-panel">
          <template v-if="selectedSkill">
            <div class="detail-header">
              <div class="detail-title">
                <span class="skill-icon large">{{ skillIcon(selectedSkill.skillType) }}</span>
                {{ selectedSkill.name }}
              </div>
              <div class="detail-actions">
                <button class="btn btn-outline btn-sm" @click="editSkillContent">编辑内容</button>
                <button class="btn btn-outline btn-sm" @click="toggleSkill">切换状态</button>
                <button class="btn btn-outline btn-sm danger" @click="deleteSkill">删除</button>
              </div>
            </div>

            <div class="detail-info">
              <div class="info-row">
                <span class="info-label">类型:</span>
                <span class="info-value">{{ selectedSkill.skillType }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">来源:</span>
                <span class="info-value">{{ sourceText(selectedSkill.source) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">标签:</span>
                <span class="info-value">
                  <span v-for="tag in parseTags(selectedSkill.tags)" :key="tag" class="tag">{{ tag }}</span>
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">范围:</span>
                <span class="info-value scope-badge" :class="selectedSkill.scope?.toLowerCase()">
                  {{ scopeText(selectedSkill.scope) }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">状态:</span>
                <span class="info-value">{{ selectedSkill.enabled ? '已启用' : '已禁用' }}</span>
              </div>
            </div>

            <!-- Content preview -->
            <div class="content-section">
              <div class="section-header">
                <span class="section-title">SKILL.md 内容</span>
                <button class="btn btn-outline btn-sm" @click="editSkillContent">编辑</button>
              </div>
              <div class="content-preview">
                <pre>{{ selectedSkill.content || '暂无内容' }}</pre>
              </div>
            </div>
          </template>
          <div v-else class="empty-state">
            <div class="empty-icon">✨</div>
            <div class="empty-text">选择一个 Skill 查看详情</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Status bar -->
    <div class="skill-statusbar">
      <span v-if="activeTab === 'mcp'">
        {{ mcpServers.length }} 个 Server · {{ mcpTools.length }} 个工具 · {{ enabledCount }} 个已启用
      </span>
      <span v-else>
        {{ skills.length }} 个 Skill · {{ enabledSkillCount }} 个已启用
      </span>
    </div>

    <!-- MCP Dialog -->
    <div v-if="showMcpDialog" class="dialog-overlay" @click.self="showMcpDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">{{ editingMcp ? '编辑 MCP Server' : '添加 MCP Server' }}</span>
          <button class="dialog-close" @click="showMcpDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">服务名称 *</label>
            <input v-model="mcpForm.name" class="form-input" placeholder="输入服务名称" />
          </div>
          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="mcpForm.description" class="form-textarea" placeholder="输入服务描述"></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">传输方式 *</label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="mcpForm.transportType" value="REMOTE" /> 远程 URL (SSE)
              </label>
              <label class="radio-item">
                <input type="radio" v-model="mcpForm.transportType" value="UPLOADED" /> 本地上传 (STDIO)
              </label>
            </div>
          </div>
          <div class="form-group" v-if="mcpForm.transportType === 'REMOTE'">
            <label class="form-label">服务器地址 *</label>
            <input v-model="mcpForm.serverUrl" class="form-input" placeholder="https://mcp.example.com" />
          </div>
          <div class="form-group">
            <label class="form-label">可见范围 *</label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="mcpForm.scope" value="SYSTEM" /> 系统级 - 所有用户可用
              </label>
              <label class="radio-item">
                <input type="radio" v-model="mcpForm.scope" value="DEPT" /> 部门级 - 指定部门可用
              </label>
              <label class="radio-item">
                <input type="radio" v-model="mcpForm.scope" value="USER" /> 个人级 - 仅自己可用
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">启用状态</label>
            <label class="toggle-switch">
              <input type="checkbox" v-model="mcpForm.enabled" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label">{{ mcpForm.enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showMcpDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveMcp">{{ editingMcp ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Skill Dialog -->
    <div v-if="showSkillDialog" class="dialog-overlay" @click.self="showSkillDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">{{ editingSkill ? '编辑 Skill' : '创建 Skill' }}</span>
          <button class="dialog-close" @click="showSkillDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">技能名称 *</label>
            <input v-model="skillForm.name" class="form-input" placeholder="输入技能名称" />
          </div>
          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="skillForm.description" class="form-textarea" placeholder="输入技能描述"></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">类型</label>
            <select v-model="skillForm.skillType" class="form-select">
              <option value="CODER">CODER - 代码助手</option>
              <option value="WRITER">WRITER - 文档撰写</option>
              <option value="ANALYST">ANALYST - 数据分析</option>
              <option value="TRANSLATOR">TRANSLATOR - 翻译助手</option>
              <option value="TESTER">TESTER - 测试工程师</option>
              <option value="ARCHITECT">ARCHITECT - 架构师</option>
              <option value="OPS">OPS - 运维助手</option>
              <option value="PM">PM - 产品经理</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">可见范围 *</label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="skillForm.scope" value="SYSTEM" /> 系统级
              </label>
              <label class="radio-item">
                <input type="radio" v-model="skillForm.scope" value="DEPT" /> 部门级
              </label>
              <label class="radio-item">
                <input type="radio" v-model="skillForm.scope" value="USER" /> 个人级
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">启用状态</label>
            <label class="toggle-switch">
              <input type="checkbox" v-model="skillForm.enabled" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label">{{ skillForm.enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showSkillDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveSkill">{{ editingSkill ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Hub Dialog (placeholder) -->
    <div v-if="showHubDialog" class="dialog-overlay" @click.self="showHubDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">从 Hub 安装</span>
          <button class="dialog-close" @click="showHubDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <div class="empty-text">Hub 功能开发中...</div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showHubDialog = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- Tool Edit Dialog -->
    <div v-if="showToolDialog" class="dialog-overlay" @click.self="showToolDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <span class="dialog-title">编辑工具 - {{ toolForm.name }}</span>
          <button class="dialog-close" @click="showToolDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">工具名称</label>
            <input v-model="toolForm.name" class="form-input" disabled />
          </div>
          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea v-model="toolForm.description" class="form-textarea" placeholder="工具描述"></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">风险等级</label>
            <select v-model="toolForm.riskLevel" class="form-select">
              <option value="LOW">低风险</option>
              <option value="MEDIUM">中风险</option>
              <option value="HIGH">高风险</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">可见范围</label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="toolForm.toolScope" value="SYSTEM" /> 系统级 - 所有用户可用
              </label>
              <label class="radio-item">
                <input type="radio" v-model="toolForm.toolScope" value="DEPT" /> 部门级 - 指定部门可用
              </label>
              <label class="radio-item">
                <input type="radio" v-model="toolForm.toolScope" value="GRANT" /> 授权访问 - 指定用户/部门/角色
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">启用状态</label>
            <label class="toggle-switch">
              <input type="checkbox" v-model="toolForm.enabled" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label">{{ toolForm.enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showToolDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveTool">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminMcpConfigApi, adminMcpToolApi, adminSkillConfigApi, type McpConfigInfo, type McpToolInfo, type SkillConfigInfo } from '@/api'
import { useConfirm } from '@/composables/useConfirm'

const { confirm } = useConfirm()
const activeTab = ref<'mcp' | 'skill'>('mcp')
const mcpServers = ref<McpConfigInfo[]>([])
const mcpTools = ref<McpToolInfo[]>([])
const skills = ref<SkillConfigInfo[]>([])
const selectedMcpId = ref<number | null>(null)
const selectedSkillId = ref<number | null>(null)

// Dialogs
const showMcpDialog = ref(false)
const showSkillDialog = ref(false)
const showHubDialog = ref(false)
const showToolDialog = ref(false)
const editingMcp = ref<McpConfigInfo | null>(null)
const editingSkill = ref<SkillConfigInfo | null>(null)
const editingToolId = ref<number | null>(null)

// Forms
const mcpForm = ref({
  name: '',
  description: '',
  transportType: 'REMOTE',
  serverUrl: '',
  scope: 'SYSTEM',
  enabled: true
})

const skillForm = ref({
  name: '',
  description: '',
  skillType: 'CODER',
  scope: 'SYSTEM',
  enabled: true
})

const toolForm = ref({
  name: '',
  description: '',
  riskLevel: 'LOW' as 'LOW' | 'MEDIUM' | 'HIGH',
  toolScope: 'SYSTEM' as 'SYSTEM' | 'DEPT' | 'USER' | 'GRANT',
  enabled: true
})

// Computed
const selectedMcp = computed(() => mcpServers.value.find(s => s.id === selectedMcpId.value) || null)
const selectedSkill = computed(() => skills.value.find(s => s.id === selectedSkillId.value) || null)
const enabledCount = computed(() => mcpServers.value.filter(s => s.enabled).length)
const enabledSkillCount = computed(() => skills.value.filter(s => s.enabled).length)

// Methods
const loadMcpServers = async () => {
  try {
    const res = await adminMcpConfigApi.list(1, 100)
    console.log('MCP servers response:', res)
    mcpServers.value = res.records || []
    if (mcpServers.value.length > 0 && !selectedMcpId.value) {
      selectMcp(mcpServers.value[0])
    }
  } catch (e) {
    console.error('Failed to load MCP servers:', e)
  }
}

const loadMcpTools = async (mcpId: number) => {
  try {
    const res = await adminMcpConfigApi.listTools(mcpId)
    console.log('MCP tools response:', res)
    mcpTools.value = res || []
  } catch (e) {
    console.error('Failed to load MCP tools:', e)
    mcpTools.value = []
  }
}

const loadSkills = async () => {
  try {
    const res = await adminSkillConfigApi.list(1, 100)
    skills.value = res.records || []
    if (skills.value.length > 0 && !selectedSkillId.value) {
      selectSkill(skills.value[0])
    }
  } catch (e) {
    console.error('Failed to load skills:', e)
  }
}

const selectMcp = (server: McpConfigInfo) => {
  selectedMcpId.value = server.id
  loadMcpTools(server.id)
}

const selectSkill = (skill: SkillConfigInfo) => {
  selectedSkillId.value = skill.id
}

const testConnection = () => {
  alert('测试连接功能开发中...')
}

const discoverTools = async () => {
  if (!selectedMcp.value) return
  alert('自动发现工具功能开发中...')
}

const editMcp = () => {
  if (!selectedMcp.value) return
  editingMcp.value = selectedMcp.value
  mcpForm.value = {
    name: selectedMcp.value.name,
    description: selectedMcp.value.description || '',
    transportType: selectedMcp.value.transportType || 'REMOTE',
    serverUrl: selectedMcp.value.serverUrl || '',
    scope: selectedMcp.value.scope,
    enabled: selectedMcp.value.enabled === 1
  }
  showMcpDialog.value = true
}

const deleteMcp = async () => {
  if (!selectedMcp.value) return
  if (!await confirm('删除 MCP', `确定删除 MCP Server "${selectedMcp.value.name}" 吗？此操作不可恢复。`)) return
  try {
    await adminMcpConfigApi.delete(selectedMcp.value.id)
    await loadMcpServers()
  } catch (e) {
    console.error('Failed to delete MCP:', e)
  }
}

const saveMcp = async () => {
  try {
    if (editingMcp.value) {
      await adminMcpConfigApi.update({
        id: editingMcp.value.id,
        name: mcpForm.value.name,
        description: mcpForm.value.description,
        transportType: mcpForm.value.transportType as any,
        serverUrl: mcpForm.value.serverUrl,
        scope: mcpForm.value.scope as any,
        enabled: mcpForm.value.enabled ? 1 : 0
      })
    } else {
      await adminMcpConfigApi.create({
        name: mcpForm.value.name,
        description: mcpForm.value.description,
        transportType: mcpForm.value.transportType as any,
        serverUrl: mcpForm.value.serverUrl,
        scope: mcpForm.value.scope as any,
        enabled: mcpForm.value.enabled ? 1 : 0
      })
    }
    showMcpDialog.value = false
    editingMcp.value = null
    await loadMcpServers()
  } catch (e) {
    console.error('Failed to save MCP:', e)
  }
}

const toggleTool = async (tool: McpToolInfo) => {
  try {
    await adminMcpToolApi.update({
      id: tool.id,
      enabled: tool.enabled === 1 ? 0 : 1
    })
    await loadMcpTools(tool.mcpConfigId)
  } catch (e) {
    console.error('Failed to toggle tool:', e)
  }
}

const editTool = (tool: McpToolInfo) => {
  editingToolId.value = tool.id
  toolForm.value = {
    name: tool.name,
    description: tool.description || '',
    riskLevel: tool.riskLevel || 'LOW',
    toolScope: tool.toolScope || 'SYSTEM',
    enabled: tool.enabled === 1
  }
  showToolDialog.value = true
}

const saveTool = async () => {
  if (!editingToolId.value) return
  try {
    await adminMcpToolApi.update({
      id: editingToolId.value,
      description: toolForm.value.description,
      riskLevel: toolForm.value.riskLevel,
      toolScope: toolForm.value.toolScope,
      enabled: toolForm.value.enabled ? 1 : 0
    })
    showToolDialog.value = false
    if (selectedMcp.value) {
      await loadMcpTools(selectedMcp.value.id)
    }
  } catch (e) {
    console.error('Failed to save tool:', e)
  }
}

const editSkillContent = () => {
  alert('Skill 内容编辑功能开发中...')
}

const toggleSkill = async () => {
  if (!selectedSkill.value) return
  try {
    await adminSkillConfigApi.update({
      id: selectedSkill.value.id,
      enabled: selectedSkill.value.enabled === 1 ? 0 : 1
    } as any)
    await loadSkills()
  } catch (e) {
    console.error('Failed to toggle skill:', e)
  }
}

const deleteSkill = async () => {
  if (!selectedSkill.value) return
  if (!await confirm('删除 Skill', `确定删除 Skill "${selectedSkill.value.name}" 吗？此操作不可恢复。`)) return
  try {
    await adminSkillConfigApi.delete(selectedSkill.value.id)
    await loadSkills()
  } catch (e) {
    console.error('Failed to delete skill:', e)
  }
}

const saveSkill = async () => {
  try {
    if (editingSkill.value) {
      await adminSkillConfigApi.update({
        id: editingSkill.value.id,
        name: skillForm.value.name,
        description: skillForm.value.description,
        skillType: skillForm.value.skillType,
        scope: skillForm.value.scope as any,
        enabled: skillForm.value.enabled ? 1 : 0
      } as any)
    } else {
      await adminSkillConfigApi.create({
        name: skillForm.value.name,
        description: skillForm.value.description,
        skillType: skillForm.value.skillType,
        scope: skillForm.value.scope as any,
        enabled: skillForm.value.enabled ? 1 : 0
      } as any)
    }
    showSkillDialog.value = false
    editingSkill.value = null
    await loadSkills()
  } catch (e) {
    console.error('Failed to save skill:', e)
  }
}

// Helpers
const scopeText = (scope: string | undefined) => {
  const map: Record<string, string> = {
    SYSTEM: '系统级',
    DEPT: '部门级',
    USER: '个人级',
    GRANT: '授权访问'
  }
  return map[scope || ''] || scope || '-'
}

const riskText = (risk: string | undefined) => {
  const map: Record<string, string> = {
    LOW: '🟢 低',
    MEDIUM: '🟡 中',
    HIGH: '🔴 高'
  }
  return map[risk || ''] || risk || '-'
}

const sourceText = (source: string | undefined) => {
  const map: Record<string, string> = {
    MANUAL: '手动创建',
    HUB: 'Hub 安装',
    BUILTIN: '内置'
  }
  return map[source || ''] || source || '-'
}

const skillIcon = (type: string | undefined) => {
  const map: Record<string, string> = {
    CODER: '💻',
    WRITER: '📝',
    ANALYST: '📊',
    TRANSLATOR: '🌐',
    TESTER: '🧪',
    ARCHITECT: '🏗️',
    OPS: '⚙️',
    PM: '📋'
  }
  return map[type || ''] || '✨'
}

const parseTags = (tags: any): string[] => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  if (typeof tags === 'string') {
    try {
      return JSON.parse(tags)
    } catch {
      return []
    }
  }
  return []
}

// Load data on mount
onMounted(() => {
  loadMcpServers()
  loadSkills()
})
</script>

<style scoped>
.skill-manage {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid var(--border-subtle);
}

.tabs {
  display: flex;
  gap: 4px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-primary);
}

.tab.active {
  background: rgba(10, 132, 255, 0.2);
  color: #0A84FF;
}

.tab-icon {
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.tab-content {
  flex: 1;
  overflow: hidden;
}

.split-view {
  display: flex;
  height: 100%;
}

.server-list {
  width: 220px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid var(--border-subtle);
}

.list-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.list-count {
  font-size: 11px;
  color: var(--text-disabled);
}

.list-items {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.list-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
}

.list-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.list-item.active {
  background: rgba(10, 132, 255, 0.2);
}

.item-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-name {
  font-size: 13px;
  font-weight: 500;
}

.item-sub {
  font-size: 11px;
  color: var(--text-disabled);
  padding-left: 16px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.online {
  background: #30D158;
}

.status-dot.offline {
  background: #8E8E93;
}

.status-dot.large {
  width: 12px;
  height: 12px;
}

.skill-icon {
  font-size: 14px;
}

.skill-icon.large {
  font-size: 20px;
}

.detail-panel {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.detail-info {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: var(--text-secondary);
  min-width: 70px;
}

.info-value {
  color: var(--text-primary);
}

.info-value.mono {
  font-family: monospace;
  font-size: 12px;
}

.scope-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.scope-badge.system {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.scope-badge.dept {
  background: rgba(255, 159, 10, 0.2);
  color: #FF9F0A;
}

.scope-badge.user {
  background: rgba(191, 90, 242, 0.2);
  color: #BF5AF2;
}

.scope-badge.grant {
  background: rgba(255, 214, 10, 0.2);
  color: #FFD60A;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
}

.tools-section {
  margin-top: 16px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  overflow: hidden;
}

.data-table th {
  text-align: left;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.data-table td {
  padding: 10px 12px;
  border-top: 1px solid var(--border-subtle);
}

.data-table .mono {
  font-family: monospace;
  font-size: 12px;
}

.data-table .desc {
  color: var(--text-secondary);
  font-size: 12px;
}

.risk-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.risk-badge.low {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.risk-badge.medium {
  background: rgba(255, 214, 10, 0.2);
  color: #FFD60A;
}

.risk-badge.high {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.content-section {
  margin-top: 16px;
}

.content-preview {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.content-preview pre {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  color: var(--text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-disabled);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  padding: 20px;
  color: var(--text-disabled);
  font-size: 13px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  font-size: 11px;
  margin-right: 4px;
}

.skill-statusbar {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 30, 30, 0.5);
  font-size: 11px;
  color: var(--text-disabled);
  border-top: 1px solid var(--border-subtle);
}

/* Dialog styles */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: var(--bg-primary);
  border-radius: 12px;
  width: 480px;
  max-height: 80vh;
  overflow: hidden;
  border: 1px solid var(--border-subtle);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
}

.dialog-body {
  padding: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid var(--border-subtle);
}

/* Form styles */
.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  cursor: pointer;
}

.radio-item input {
  accent-color: #0A84FF;
}

/* Toggle switch */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
  margin-right: 8px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

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

.toggle-switch input:checked + .toggle-slider {
  background: #30D158;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(16px);
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background: #0A84FF;
  color: white;
}

.btn-primary:hover {
  background: #0070E0;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.06);
}

.btn-outline.danger {
  color: #FF453A;
  border-color: rgba(255, 69, 58, 0.3);
}

.btn-outline.danger:hover {
  background: rgba(255, 69, 58, 0.1);
}

.btn-link {
  background: none;
  border: none;
  color: #0A84FF;
  cursor: pointer;
  font-size: 12px;
}

.btn-link:hover {
  text-decoration: underline;
}
</style>
