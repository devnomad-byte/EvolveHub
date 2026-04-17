<template>
  <div class="skill-manage">
    <!-- Header -->
    <div class="header">
      <div class="header-title">
        <LayoutGrid :size="20" />
        <span>技能管理</span>
      </div>
      <div class="header-actions">
        <button class="btn btn-sm btn-primary" @click="openMcpCreate()">
          <Plus :size="14" /> 创建 MCP
        </button>
        <button class="btn btn-sm btn-primary" @click="openSkillCreate()">
          <Plus :size="14" /> 创建技能
        </button>
        <button class="btn btn-sm btn-hub" @click="showSkillHubDialog = true">
          <Sparkles :size="14" /> 从 Hub 安装
        </button>
      </div>
    </div>

    <!-- Tab Bar -->
    <div class="tab-bar">
      <button
        class="tab"
        :class="{ active: activeTab === 'mcp' }"
        @click="activeTab = 'mcp'"
      >
        <Server :size="15" />
        MCP Servers
        <span class="tab-badge">{{ mcpList.length }}</span>
      </button>
      <button
        class="tab"
        :class="{ active: activeTab === 'skill' }"
        @click="activeTab = 'skill'"
      >
        <Sparkles :size="15" />
        Skills
        <span class="tab-badge">{{ skillList.length }}</span>
      </button>
    </div>

    <!-- Content -->
    <div class="content">
      <!-- MCP Servers Tab -->
      <template v-if="activeTab === 'mcp'">
        <div class="content-left">
          <div class="section-title">服务器列表</div>
          <div class="card-list">
            <div
              v-for="item in mcpList"
              :key="item.id"
              class="card"
              :class="{ active: selectedMcpId === item.id }"
              @click="selectMcp(item)"
            >
              <div class="card-icon mcp">
                <Server :size="18" />
              </div>
              <div class="card-info">
                <div class="card-name">{{ item.name }}</div>
                <div class="card-meta">{{ item.transportType === 'REMOTE' ? '远程' : '本地上传' }} · {{ getMcpStatusText(item) }}</div>
              </div>
              <div class="card-status" :class="getMcpDotClass(item)">
                <span class="status-dot"></span>
              </div>
            </div>
            <div v-if="mcpLoading" class="empty">加载中...</div>
            <div v-else-if="mcpList.length === 0" class="empty">
              <Server :size="32" />
              <span>暂无 MCP Server</span>
            </div>
          </div>
        </div>

        <div class="content-right">
          <template v-if="currentMcp">
            <div class="detail-header">
              <div class="detail-icon mcp">
                <Server :size="28" />
              </div>
              <div class="detail-title">
                <h2>{{ currentMcp.name }}</h2>
                <span class="detail-badge" :class="mcpInstances.get(currentMcp.id) ? 'enabled' : 'disabled'">
                  {{ getMcpStatusText(currentMcp) }}
                </span>
              </div>
            </div>

            <div class="detail-section">
              <h3>基本信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label><Server :size="14" /> 服务地址</label>
                  <value class="mono">{{ currentMcp.serverUrl }}</value>
                </div>
                <div class="info-item">
                  <label><Zap :size="14" /> 传输方式</label>
                  <value>{{ currentMcp.transportType === 'REMOTE' ? '远程连接' : 'STDIO 本地部署' }}</value>
                </div>
                <div class="info-item">
                  <label><Shield :size="14" /> 协议</label>
                  <value>{{ currentMcp.protocol === 'STREAMABLE_HTTP' ? 'Streamable HTTP' : 'SSE' }}</value>
                </div>
                <div class="info-item">
                  <label><Globe :size="14" /> 可见范围</label>
                  <value>{{ scopeText(currentMcp.scope) }}</value>
                </div>
              </div>
            </div>

            <div v-if="currentMcp.description" class="detail-section">
              <h3>描述</h3>
              <p class="description">{{ currentMcp.description }}</p>
            </div>

            <div class="detail-actions">
              <!-- 服务控制 -->
              <div class="action-group">
                <div class="action-group-label">服务控制</div>
                <div class="action-buttons">
                  <button
                    class="action-btn action-btn-start"
                    @click="startMcp(currentMcp.id)"
                    :disabled="!!mcpInstances.get(currentMcp.id) || !!mcpStarting.get(currentMcp.id)"
                  >
                    <div class="action-btn-icon">
                      <span v-if="mcpStarting.get(currentMcp.id)" class="btn-spinner-sm"></span>
                      <Play v-else :size="18" />
                    </div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">{{ mcpStarting.get(currentMcp.id) ? '启动中...' : '启动' }}</span>
                      <span class="action-btn-desc">{{ mcpStarting.get(currentMcp.id) ? '正在下载依赖并构建' : '启动 MCP 服务' }}</span>
                    </div>
                  </button>
                  <button
                    class="action-btn action-btn-stop"
                    @click="stopMcp(currentMcp.id)"
                    :disabled="!mcpInstances.get(currentMcp.id) || !!mcpStarting.get(currentMcp.id)"
                  >
                    <div class="action-btn-icon"><Square :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">停止</span>
                      <span class="action-btn-desc">停止运行中的服务</span>
                    </div>
                  </button>
                  <button
                    class="action-btn action-btn-restart"
                    @click="restartMcp(currentMcp.id)"
                    :disabled="!mcpInstances.get(currentMcp.id) || !!mcpStarting.get(currentMcp.id)"
                  >
                    <div class="action-btn-icon">
                      <span v-if="mcpStarting.get(currentMcp.id)" class="btn-spinner-sm"></span>
                      <RefreshCw v-else :size="18" />
                    </div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">{{ mcpStarting.get(currentMcp.id) ? '重启中...' : '重启' }}</span>
                      <span class="action-btn-desc">重启 MCP 服务</span>
                    </div>
                  </button>
                </div>
              </div>

              <!-- 工具操作 -->
              <div class="action-group">
                <div class="action-group-label">工具</div>
                <div class="action-buttons">
                  <button
                    class="action-btn action-btn-discover"
                    @click="discoverMcpTools(currentMcp.id)"
                    :disabled="!mcpInstances.get(currentMcp.id)"
                  >
                    <div class="action-btn-icon"><Search :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">发现工具</span>
                      <span class="action-btn-desc">扫描可用工具列表</span>
                    </div>
                  </button>
                </div>
              </div>

              <!-- 配置管理 -->
              <div class="action-group">
                <div class="action-group-label">管理</div>
                <div class="action-buttons">
                  <button class="action-btn" @click="openMcpEditor(currentMcp)">
                    <div class="action-btn-icon action-btn-icon-muted"><Pencil :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">编辑配置</span>
                      <span class="action-btn-desc">修改连接参数</span>
                    </div>
                  </button>
                  <button class="action-btn" @click="toggleMcpEnabled(currentMcp)">
                    <div class="action-btn-icon" :class="currentMcp.enabled ? 'action-btn-icon-warn' : 'action-btn-icon-muted'"><Power :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">{{ currentMcp.enabled ? '停用' : '启用' }}</span>
                      <span class="action-btn-desc">{{ currentMcp.enabled ? '禁用此服务' : '启用此服务' }}</span>
                    </div>
                  </button>
                  <button class="action-btn action-btn-danger" @click="deleteMcp(currentMcp.id)">
                    <div class="action-btn-icon"><Trash2 :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">删除</span>
                      <span class="action-btn-desc">永久删除此服务</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>

            <!-- MCP 实例状态 -->
            <div v-if="mcpInstances.get(currentMcp.id)" class="detail-section">
              <h3>运行状态</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label><Activity :size="14" /> 状态</label>
                  <value :class="'status-' + mcpInstances.get(currentMcp.id)!.status.toLowerCase()">
                    {{ mcpInstances.get(currentMcp.id)!.status }}
                  </value>
                </div>
                <div class="info-item">
                  <label><Clock :size="14" /> 启动时间</label>
                  <value>{{ formatTime(mcpInstances.get(currentMcp.id)!.startTime) }}</value>
                </div>
                <div class="info-item">
                  <label><Activity :size="14" /> 最后心跳</label>
                  <value>{{ formatTime(mcpInstances.get(currentMcp.id)!.lastHeartbeat) }}</value>
                </div>
                <div class="info-item">
                  <label><Wrench :size="14" /> 工具数量</label>
                  <value>{{ mcpInstances.get(currentMcp.id)!.toolCount || 0 }}</value>
                </div>
              </div>
            </div>

            <!-- MCP 工具列表 -->
            <div v-if="mcpTools.get(currentMcp.id)" class="detail-section">
              <h3>可用工具 ({{ mcpTools.get(currentMcp.id)!.length }})</h3>
              <div class="tool-list">
                <div v-for="tool in mcpTools.get(currentMcp.id)" :key="tool.id" class="tool-item">
                  <div class="tool-header">
                    <span class="tool-name">{{ tool.name }}</span>
                    <span class="tool-risk" :class="'risk-' + (tool.riskLevel || 'LOW').toLowerCase()">
                      {{ tool.riskLevel || 'LOW' }}
                    </span>
                  </div>
                  <div v-if="tool.description" class="tool-desc">{{ tool.description }}</div>
                </div>
              </div>
            </div>

            <!-- 授权管理 -->
            <div class="detail-section" v-if="currentMcp.scope === 'USER'">
              <div class="grant-header">
                <h3>授权用户</h3>
                <button class="btn btn-sm btn-primary" @click="openMcpGrantDialog">
                  <Plus :size="12" /> 添加用户
                </button>
              </div>
              <div v-if="grantUsers.length === 0" class="grant-empty">
                暂未授权任何用户
              </div>
              <div v-else class="grant-list">
                <div v-for="user in grantUsers" :key="user.id" class="grant-user">
                  <div class="grant-user-avatar">{{ (user.nickname || user.username).charAt(0) }}</div>
                  <div class="grant-user-info">
                    <span class="grant-user-name">{{ user.nickname || user.username }}</span>
                    <span class="grant-user-dept">{{ user.deptName }}</span>
                  </div>
                  <button class="grant-user-remove" @click="revokeGrant(user.id)" title="移除授权">
                    <Trash2 :size="14" />
                  </button>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="detail-empty">
            <Server :size="56" />
            <p>选择一个 MCP Server 查看详情</p>
          </div>
        </div>
      </template>

      <!-- Skills Tab -->
      <template v-if="activeTab === 'skill'">
        <div class="content-left">
          <div class="section-title">技能列表</div>
          <div class="card-list">
            <div
              v-for="item in skillList"
              :key="item.id"
              class="card"
              :class="{ active: selectedSkillId === item.id }"
              @click="selectSkill(item)"
            >
              <div class="card-icon skill" :style="{ background: skillGradient(item.skillType) }">
                <component :is="skillIcon(item.skillType)" :size="18" />
              </div>
              <div class="card-info">
                <div class="card-name">{{ item.name }}</div>
                <div class="card-meta">{{ item.skillType || '默认' }} · {{ item.enabled ? '启用' : '禁用' }}</div>
              </div>
              <div class="card-status" :class="item.enabled ? 'online' : 'offline'">
                <span class="status-dot"></span>
              </div>
            </div>
            <div v-if="skillLoading" class="empty">加载中...</div>
            <div v-else-if="skillList.length === 0" class="empty">
              <Sparkles :size="32" />
              <span>暂无 Skill</span>
            </div>
          </div>
        </div>

        <div class="content-right">
          <template v-if="currentSkill">
            <div class="detail-header">
              <div class="detail-icon skill" :style="{ background: skillGradient(currentSkill.skillType) }">
                <component :is="skillIcon(currentSkill.skillType)" :size="28" />
              </div>
              <div class="detail-title">
                <h2>{{ currentSkill.name }}</h2>
                <span class="detail-badge" :class="currentSkill.enabled ? 'enabled' : 'disabled'">
                  {{ currentSkill.enabled ? '启用' : '禁用' }}
                </span>
              </div>
            </div>

            <div class="detail-section">
              <h3>基本信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <label><Layers :size="14" /> 技能类型</label>
                  <value>{{ currentSkill.skillType || '默认' }}</value>
                </div>
                <div class="info-item">
                  <label><Tag :size="14" /> 来源</label>
                  <value>{{ sourceText(currentSkill.source) }}</value>
                </div>
                <div class="info-item">
                  <label><Globe :size="14" /> 可见范围</label>
                  <value>{{ scopeText(currentSkill.scope) }}</value>
                </div>
              </div>
            </div>

            <div v-if="currentSkill.tags && currentSkill.tags.length > 0" class="detail-section">
              <h3>标签</h3>
              <div class="tag-list">
                <span v-for="tag in parseTags(currentSkill.tags)" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>

            <div v-if="currentSkill.description" class="detail-section">
              <h3>描述</h3>
              <p class="description">{{ currentSkill.description }}</p>
            </div>

            <!-- Skill 授权管理 -->
            <div v-if="currentSkill.scope === 'USER'" class="detail-section">
              <div class="grant-header">
                <h3>授权用户</h3>
                <button class="btn btn-sm" @click="openSkillGrantDialog">添加授权</button>
              </div>
              <div v-if="skillGrantUsers.length === 0" class="grant-empty">
                暂未授权任何用户
              </div>
              <div v-else class="grant-list">
                <div v-for="user in skillGrantUsers" :key="user.id" class="grant-user">
                  <div class="grant-user-avatar">{{ (user.nickname || user.username).charAt(0) }}</div>
                  <div class="grant-user-info">
                    <span class="grant-user-name">{{ user.nickname || user.username }}</span>
                    <span class="grant-user-dept">{{ user.deptName }}</span>
                  </div>
                  <button class="grant-user-remove" @click="revokeSkillGrant(user.id)" title="移除授权">
                    <X :size="14" />
                  </button>
                </div>
              </div>
            </div>

            <div class="detail-actions">
              <div class="action-group">
                <div class="action-group-label">编辑</div>
                <div class="action-buttons">
                  <button class="action-btn" @click="openSkillEdit(currentSkill)">
                    <div class="action-btn-icon action-btn-icon-muted"><Pencil :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">编辑配置</span>
                      <span class="action-btn-desc">修改技能属性</span>
                    </div>
                  </button>
                  <button class="action-btn" @click="openSkillEditor">
                    <div class="action-btn-icon" style="background: rgba(94, 92, 230, 0.12); color: #5E5CE6;"><FileText :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">编辑内容</span>
                      <span class="action-btn-desc">修改 SKILL.md</span>
                    </div>
                  </button>
                </div>
              </div>
              <div class="action-group">
                <div class="action-group-label">管理</div>
                <div class="action-buttons">
                  <button class="action-btn" @click="toggleSkillEnabled(currentSkill)">
                    <div class="action-btn-icon" :class="currentSkill.enabled ? 'action-btn-icon-warn' : 'action-btn-icon-muted'"><Power :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">{{ currentSkill.enabled ? '禁用' : '启用' }}</span>
                      <span class="action-btn-desc">{{ currentSkill.enabled ? '停用此技能' : '启用此技能' }}</span>
                    </div>
                  </button>
                  <button class="action-btn action-btn-danger" @click="deleteSkill(currentSkill.id)">
                    <div class="action-btn-icon"><Trash2 :size="18" /></div>
                    <div class="action-btn-text">
                      <span class="action-btn-title">删除</span>
                      <span class="action-btn-desc">永久删除此技能</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="detail-empty">
            <Sparkles :size="56" />
            <p>选择一个 Skill 查看详情</p>
          </div>
        </div>
      </template>
    </div>

    <!-- Skill Hub Dialog -->
    <SkillHubDialog
      v-if="showSkillHubDialog"
      @close="showSkillHubDialog = false"
      @installed="handleSkillInstalled"
    />

    <!-- 授权用户选择对话框（MCP / Skill 共用） -->
    <div v-if="showGrantUserDialog" class="modal-overlay" @click.self="showGrantUserDialog = false">
      <div class="modal">
        <div class="modal-header">
          <h3>添加授权用户 ({{ grantResourceType === 'SKILL' ? 'Skill' : 'MCP' }})</h3>
          <button class="modal-close" @click="showGrantUserDialog = false"><X :size="18" /></button>
        </div>
        <div class="modal-body">
          <input v-model="grantUserSearch" type="text" class="grant-search" placeholder="搜索用户名或昵称..." />
          <div class="grant-user-list">
            <div
              v-for="user in availableUsers"
              :key="user.id"
              class="grant-user-select"
              :class="{ selected: grantSelectedUsers.has(user.id) }"
              @click="toggleGrantUser(user.id)"
            >
              <div class="grant-user-avatar">{{ (user.nickname || user.username).charAt(0) }}</div>
              <div class="grant-user-info">
                <span class="grant-user-name">{{ user.nickname || user.username }}</span>
                <span class="grant-user-dept">{{ user.deptName }}</span>
              </div>
              <div v-if="grantSelectedUsers.has(user.id)" class="grant-user-check">✓</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showGrantUserDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="grantSelectedUsers.size === 0" @click="confirmGrantUsers">
            授权 ({{ grantSelectedUsers.size }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import {
  Plus, Sparkles, Server, Zap, Shield, Globe, Tag, Layers,
  Pencil, Power, Trash2, Code, FileText, BarChart3, Languages,
  FlaskConical, Building2, Cog, ClipboardList, Play, Square,
  RefreshCw, Search, Activity, Clock, Wrench, X
} from 'lucide-vue-next'
import { useWindowStore } from '@/stores/window'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'
import SkillHubDialog from './SkillHubDialog.vue'
import { adminMcpConfigApi, type McpConfigInfo } from '@/api/adminMcpConfig'
import { adminMcpInstanceApi, type McpInstanceInfo, type McpToolInfo } from '@/api/adminMcpInstance'
import { adminSkillConfigApi, type SkillConfigInfo } from '@/api/adminSkillConfig'
import { resourceGrantApi, type ResourceGrantInfo } from '@/api/resourceGrant'
import { adminUserApi, type UserInfo } from '@/api/adminUser'

const winStore = useWindowStore()
const desktop = useDesktopStore()
const { confirm } = useConfirm()

const activeTab = ref<'mcp' | 'skill'>('mcp')

// MCP state
const mcpList = ref<McpConfigInfo[]>([])
const mcpLoading = ref(false)
const selectedMcpId = ref<number | null>(null)
const currentMcp = computed(() => mcpList.value.find(m => m.id === selectedMcpId.value))
const mcpInstances = ref<Map<number, McpInstanceInfo>>(new Map())
const mcpTools = ref<Map<number, McpToolInfo[]>>(new Map())
const mcpStarting = ref<Map<number, boolean>>(new Map())

// Skill state
const skillList = ref<SkillConfigInfo[]>([])
const skillLoading = ref(false)
const selectedSkillId = ref<number | null>(null)
const currentSkill = computed(() => skillList.value.find(s => s.id === selectedSkillId.value))
const showSkillHubDialog = ref(false)

// Grant state (MCP / Skill 共用)
const grantRecords = ref<ResourceGrantInfo[]>([])
const grantUsers = ref<UserInfo[]>([])
const allUsers = ref<UserInfo[]>([])
const showGrantUserDialog = ref(false)
const grantUserSearch = ref('')
const grantSelectedUsers = ref<Set<number>>(new Set())
const grantResourceType = ref<'MCP' | 'SKILL'>('MCP')

// Skill grant state
const skillGrantUsers = ref<UserInfo[]>([])

const availableUsers = computed(() => {
  const keyword = grantUserSearch.value.toLowerCase()
  const currentGranted = grantResourceType.value === 'SKILL' ? skillGrantUsers.value : grantUsers.value
  const grantedIds = new Set(currentGranted.map(u => u.id))
  return allUsers.value.filter(u => {
    if (grantedIds.has(u.id)) return false
    if (!keyword) return true
    return (u.nickname || '').toLowerCase().includes(keyword)
      || (u.username || '').toLowerCase().includes(keyword)
  })
})

// MCP methods
async function loadMcpList() {
  mcpLoading.value = true
  try {
    const res = await adminMcpConfigApi.list(1, 100)
    mcpList.value = res.records
    if (mcpList.value.length > 0 && selectedMcpId.value === null) {
      selectedMcpId.value = mcpList.value[0].id
    }
    // 加载每个 MCP 的实例状态
    for (const mcp of mcpList.value) {
      await loadMcpInstanceStatus(mcp.id)
    }
  } catch (e) {
    console.error('加载 MCP 列表失败', e)
  } finally {
    mcpLoading.value = false
  }
}

async function loadMcpInstanceStatus(mcpId: number) {
  try {
    const instance = await adminMcpInstanceApi.status(mcpId)
    const newMap = new Map(mcpInstances.value)
    if (instance) {
      newMap.set(mcpId, instance)
    } else {
      newMap.delete(mcpId)
    }
    mcpInstances.value = newMap
  } catch (e) {
    console.error('加载 MCP 实例状态失败', e)
  }
}

async function loadMcpTools(mcpId: number) {
  try {
    const tools = await adminMcpInstanceApi.listTools(mcpId)
    const newMap = new Map(mcpTools.value)
    newMap.set(mcpId, tools)
    mcpTools.value = newMap
  } catch (e) {
    console.error('加载 MCP 工具失败', e)
  }
}

async function startMcp(mcpId: number) {
  const newStarting = new Map(mcpStarting.value)
  newStarting.set(mcpId, true)
  mcpStarting.value = newStarting
  try {
    const instance = await adminMcpInstanceApi.start(mcpId)

    const newMap = new Map(mcpInstances.value)
    newMap.set(mcpId, instance)
    mcpInstances.value = newMap

    await loadMcpTools(mcpId)
    desktop.addToast('MCP Server 启动成功', 'success')
  } catch (e) {
    // 启动失败时，移除可能存在的错误实例
    const newMap = new Map(mcpInstances.value)
    newMap.delete(mcpId)
    mcpInstances.value = newMap
    desktop.addToast('启动失败: ' + (e as Error).message, 'error')
  } finally {
    const newStarting = new Map(mcpStarting.value)
    newStarting.delete(mcpId)
    mcpStarting.value = newStarting
  }
}

async function stopMcp(mcpId: number) {
  if (!await confirm('停止服务', '确定要停止这个 MCP Server 吗？运行中的工具将不可用。', { confirmText: '停止' })) return
  try {
    await adminMcpInstanceApi.stop(mcpId)

    const newMap = new Map(mcpInstances.value)
    newMap.delete(mcpId)
    mcpInstances.value = newMap

    const newToolsMap = new Map(mcpTools.value)
    newToolsMap.delete(mcpId)
    mcpTools.value = newToolsMap

    desktop.addToast('MCP Server 已停止', 'success')
  } catch (e) {
    desktop.addToast('停止失败: ' + (e as Error).message, 'error')
  }
}

async function restartMcp(mcpId: number) {
  const newStarting = new Map(mcpStarting.value)
  newStarting.set(mcpId, true)
  mcpStarting.value = newStarting
  try {
    const instance = await adminMcpInstanceApi.restart(mcpId)
    const newMap = new Map(mcpInstances.value)
    newMap.set(mcpId, instance)
    mcpInstances.value = newMap
    await loadMcpTools(mcpId)
    desktop.addToast('MCP Server 重启成功', 'success')
  } catch (e) {
    console.error('重启 MCP 失败', e)
    desktop.addToast('重启失败: ' + (e as Error).message, 'error')
  } finally {
    const newStarting = new Map(mcpStarting.value)
    newStarting.delete(mcpId)
    mcpStarting.value = newStarting
  }
}

async function discoverMcpTools(mcpId: number) {
  try {
    const tools = await adminMcpInstanceApi.discover(mcpId)
    const newMap = new Map(mcpTools.value)
    newMap.set(mcpId, tools)
    mcpTools.value = newMap
    desktop.addToast(`工具发现成功，共找到 ${tools.length} 个工具`, 'success')
  } catch (e) {
    console.error('工具发现失败', e)
    desktop.addToast('工具发现失败: ' + (e as Error).message, 'error')
  }
}

async function selectMcp(item: McpConfigInfo) {
  selectedMcpId.value = item.id
  // 自动加载该 MCP 的实例状态和工具列表
  await loadMcpInstanceStatus(item.id)
  await loadMcpTools(item.id)
  await loadGrantUsers(item.id)
}

async function toggleMcpEnabled(item: McpConfigInfo) {
  try {
    await adminMcpConfigApi.update({
      id: item.id,
      enabled: item.enabled ? 0 : 1
    })
    loadMcpList()
  } catch (e) {
    console.error('切换状态失败', e)
  }
}

async function deleteMcp(id: number) {
  if (!await confirm('删除 MCP', '确定要删除这个 MCP Server 吗？此操作不可恢复。')) return
  try {
    await adminMcpConfigApi.delete(id)
    if (selectedMcpId.value === id) {
      selectedMcpId.value = null
    }
    loadMcpList()
  } catch (e) {
    console.error('删除 MCP 失败', e)
    desktop.addToast('删除 MCP 失败', 'error')
  }
}

function openMcpEditor(item: McpConfigInfo) {
  winStore.pendingMcpToEdit = {
    mcpId: item.id,
    mcpName: item.name
  }
  winStore.openApp('mcp-editor')
}

function openMcpCreate() {
  winStore.openApp('mcp-create')
}

// Skill methods
async function loadSkillList() {
  skillLoading.value = true
  try {
    const res = await adminSkillConfigApi.list(1, 100)
    skillList.value = res.records
    if (skillList.value.length > 0 && selectedSkillId.value === null) {
      selectedSkillId.value = skillList.value[0].id
    }
  } catch (e) {
    console.error('加载 Skill 列表失败', e)
  } finally {
    skillLoading.value = false
  }
}

function selectSkill(item: SkillConfigInfo) {
  selectedSkillId.value = item.id
  if (item.scope === 'USER') {
    loadSkillGrantUsers(item.id)
  } else {
    skillGrantUsers.value = []
  }
}

function openSkillCreate() {
  winStore.pendingSkillToEdit = { skillId: null, skillName: '' }
  winStore.openApp('skill-edit')
}

async function toggleSkillEnabled(item: SkillConfigInfo) {
  try {
    await adminSkillConfigApi.update({
      id: item.id,
      enabled: item.enabled ? 0 : 1
    })
    loadSkillList()
  } catch (e) {
    console.error('切换状态失败', e)
  }
}

async function deleteSkill(id: number) {
  if (!await confirm('删除 Skill', '确定要删除这个 Skill 吗？此操作不可恢复。')) return
  try {
    await adminSkillConfigApi.delete(id)
    if (selectedSkillId.value === id) {
      selectedSkillId.value = null
      skillGrantUsers.value = []
    }
    loadSkillList()
  } catch (e) {
    console.error('删除 Skill 失败', e)
    desktop.addToast('删除 Skill 失败', 'error')
  }
}

function openSkillEditor() {
  if (currentSkill.value) {
    winStore.pendingSkillToEdit = {
      skillId: currentSkill.value.id,
      skillName: currentSkill.value.name
    }
    winStore.openApp('skill-editor')
  }
}

function openSkillEdit(item: SkillConfigInfo) {
  winStore.pendingSkillToEdit = {
    skillId: item.id,
    skillName: item.name
  }
  winStore.openApp('skill-edit')
}

function handleSkillInstalled(skillId: number) {
  loadSkillList()
  selectedSkillId.value = skillId
}

// Helpers
function scopeText(scope: string | undefined) {
  const map: Record<string, string> = {
    SYSTEM: '系统级',
    DEPT: '部门级',
    USER: '个人级'
  }
  return map[scope || ''] || scope || '-'
}

// Grant methods
async function loadGrantUsers(mcpId: number) {
  grantUsers.value = []
  try {
    const grants = await resourceGrantApi.listByResource('MCP', mcpId)
    grantRecords.value = grants
    if (grants.length === 0) return
    // Load user details for granted user IDs
    const users = await adminUserApi.list()
    const userIdSet = new Set(grants.map(g => g.userId))
    grantUsers.value = users.filter(u => userIdSet.has(u.id))
  } catch (e) {
    console.error('加载授权用户失败', e)
  }
}

async function loadAllUsers() {
  try {
    allUsers.value = await adminUserApi.list()
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

function openMcpGrantDialog() {
  grantResourceType.value = 'MCP'
  grantSelectedUsers.value = new Set()
  grantUserSearch.value = ''
  loadAllUsers()
  showGrantUserDialog.value = true
}

function toggleGrantUser(userId: number) {
  const newSet = new Set(grantSelectedUsers.value)
  if (newSet.has(userId)) {
    newSet.delete(userId)
  } else {
    newSet.add(userId)
  }
  grantSelectedUsers.value = newSet
}

async function confirmGrantUsers() {
  const resourceType = grantResourceType.value
  let resourceId: number | undefined
  if (resourceType === 'MCP') {
    resourceId = currentMcp.value?.id
  } else {
    resourceId = currentSkill.value?.id
  }
  if (!resourceId) return
  try {
    const promises = Array.from(grantSelectedUsers.value).map(userId =>
      resourceGrantApi.assign(userId, resourceType, resourceId!)
    )
    await Promise.all(promises)
    grantSelectedUsers.value = new Set()
    showGrantUserDialog.value = false
    if (resourceType === 'MCP') {
      await loadGrantUsers(resourceId)
    } else {
      await loadSkillGrantUsers(resourceId)
    }
    desktop.addToast('授权成功', 'success')
  } catch (e) {
    desktop.addToast('授权失败', 'error')
  }
}

async function revokeGrant(userId: number) {
  if (!currentMcp.value) return
  try {
    await resourceGrantApi.revoke(userId, 'MCP', currentMcp.value.id)
    await loadGrantUsers(currentMcp.value.id)
    desktop.addToast('已移除授权', 'success')
  } catch (e) {
    desktop.addToast('移除授权失败', 'error')
  }
}

// Skill grant methods
async function loadSkillGrantUsers(skillId: number) {
  try {
    const grants = await resourceGrantApi.listByResource('SKILL', skillId)
    if (grants.length === 0) {
      skillGrantUsers.value = []
      return
    }
    const users = await adminUserApi.list()
    const userIdSet = new Set(grants.map(g => g.userId))
    skillGrantUsers.value = users.filter(u => userIdSet.has(u.id))
  } catch (e) {
    console.error('加载 Skill 授权用户失败', e)
  }
}

function openSkillGrantDialog() {
  grantResourceType.value = 'SKILL'
  grantSelectedUsers.value = new Set()
  grantUserSearch.value = ''
  loadAllUsers()
  showGrantUserDialog.value = true
}

async function revokeSkillGrant(userId: number) {
  if (!currentSkill.value) return
  try {
    await resourceGrantApi.revoke(userId, 'SKILL', currentSkill.value.id)
    await loadSkillGrantUsers(currentSkill.value.id)
    desktop.addToast('已移除授权', 'success')
  } catch (e) {
    desktop.addToast('移除授权失败', 'error')
  }
}

function sourceText(source: string | undefined) {
  const map: Record<string, string> = {
    MANUAL: '手动创建',
    HUB: 'Hub 安装',
    BUILTIN: '内置'
  }
  return map[source || ''] || source || '-'
}

function skillIcon(type: string | undefined) {
  const icons: Record<string, any> = {
    CODER: Code,
    WRITER: FileText,
    ANALYST: BarChart3,
    TRANSLATOR: Languages,
    TESTER: FlaskConical,
    ARCHITECT: Building2,
    OPS: Cog,
    PM: ClipboardList
  }
  return icons[type || ''] || Sparkles
}

function skillGradient(type: string | undefined) {
  const gradients: Record<string, string> = {
    CODER: 'linear-gradient(135deg, #667eea, #764ba2)',
    WRITER: 'linear-gradient(135deg, #f093fb, #f5576c)',
    ANALYST: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    TRANSLATOR: 'linear-gradient(135deg, #43e97b, #38f9d7)',
    TESTER: 'linear-gradient(135deg, #fa709a, #fee140)',
    ARCHITECT: 'linear-gradient(135deg, #a18cd1, #fbc2eb)',
    OPS: 'linear-gradient(135deg, #fccb90, #d57eeb)',
    PM: 'linear-gradient(135deg, #84fab0, #8fd3f4)'
  }
  return gradients[type || ''] || 'linear-gradient(135deg, #667eea, #764ba2)'
}

function parseTags(tags: string | undefined) {
  if (!tags) return []
  try {
    return JSON.parse(tags)
  } catch {
    return tags.split(',').map(t => t.trim())
  }
}

function formatTime(timeStr: string | null | undefined) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getMcpStatusText(item: McpConfigInfo): string {
  const instance = mcpInstances.value.get(item.id)
  if (instance) {
    if (instance.status === 'ERROR') return '异常'
    return '运行中'
  }
  return item.enabled ? '已停止' : '已禁用'
}

function getMcpDotClass(item: McpConfigInfo): string {
  const instance = mcpInstances.value.get(item.id)
  if (instance) {
    if (instance.status === 'ERROR') return 'error'
    return 'online'
  }
  return item.enabled ? 'standby' : 'offline'
}

// Load users when grant dialog opens
watch(showGrantUserDialog, (val) => {
  if (val && allUsers.value.length === 0) {
    loadAllUsers()
  }
  if (!val) {
    grantSelectedUsers.value = new Set()
    grantUserSearch.value = ''
  }
})

onMounted(() => {
  loadMcpList()
  loadSkillList()
  window.addEventListener('mcp-list-changed', loadMcpList)
  window.addEventListener('skill-list-changed', loadSkillList)
})

onUnmounted(() => {
  window.removeEventListener('mcp-list-changed', loadMcpList)
  window.removeEventListener('skill-list-changed', loadSkillList)
})
</script>

<style scoped>
.skill-manage {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-title svg {
  color: #0A84FF;
}

.tab-bar {
  display: flex;
  gap: 4px;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid var(--border-subtle);
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-primary);
}

.tab.active {
  background: rgba(10, 132, 255, 0.15);
  border-color: rgba(10, 132, 255, 0.3);
  color: #0A84FF;
}

.tab-badge {
  font-size: 11px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.content-left {
  width: 280px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.section-title {
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
  border-bottom: 1px solid var(--border-subtle);
}

.card-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  margin-bottom: 4px;
}

.card:hover {
  background: rgba(255, 255, 255, 0.05);
}

.card.active {
  background: rgba(10, 132, 255, 0.15);
}

.card-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  color: white;
}

.card-icon.mcp {
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
}

.card-icon.skill {
  background: linear-gradient(135deg, #30D158, #34C759);
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  font-size: 11px;
  color: var(--text-disabled);
}

.card-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: block;
}

.card-status.online .status-dot {
  background: #30D158;
  box-shadow: 0 0 8px #30D158;
}

.card-status.standby .status-dot {
  background: #FF9F0A;
  box-shadow: 0 0 8px rgba(255, 159, 10, 0.4);
}

.card-status.error .status-dot {
  background: #FF453A;
  box-shadow: 0 0 8px rgba(255, 69, 58, 0.4);
}

.card-status.offline .status-dot {
  background: #8E8E93;
}

.content-right {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: white;
}

.detail-icon.mcp {
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
}

.detail-icon.skill {
  background: linear-gradient(135deg, #30D158, #34C759);
}

.detail-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-title h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.detail-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 6px;
  width: fit-content;
}

.detail-badge.enabled {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.detail-badge.disabled {
  background: rgba(142, 142, 147, 0.15);
  color: #8E8E93;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
  margin-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  background: rgba(255, 255, 255, 0.04);
  padding: 12px;
  border-radius: 10px;
}

.info-item label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-disabled);
  margin-bottom: 6px;
}

.info-item value {
  font-size: 13px;
  color: var(--text-primary);
}

.info-item value.mono {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
}

.description {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 12px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  color: var(--text-secondary);
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-subtle);
}

.action-group {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  padding: 14px;
}

.action-group-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
  margin-bottom: 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid transparent;
  border-radius: 10px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}

.action-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--border-subtle);
}

.action-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.action-btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(10, 132, 255, 0.12);
  color: #0A84FF;
  flex-shrink: 0;
}

.action-btn-icon-muted {
  background: rgba(142, 142, 147, 0.12);
  color: #8E8E93;
}

.action-btn-icon-warn {
  background: rgba(255, 159, 10, 0.12);
  color: #FF9F0A;
}

.action-btn-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.action-btn-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.action-btn-desc {
  font-size: 11px;
  color: var(--text-disabled);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Colored action buttons */
.action-btn-start .action-btn-icon {
  background: rgba(48, 209, 88, 0.12);
  color: #30D158;
}

.action-btn-stop .action-btn-icon {
  background: rgba(255, 159, 10, 0.12);
  color: #FF9F0A;
}

.action-btn-restart .action-btn-icon {
  background: rgba(10, 132, 255, 0.12);
  color: #0A84FF;
}

.action-btn-discover .action-btn-icon {
  background: rgba(94, 92, 230, 0.12);
  color: #5E5CE6;
}

.action-btn-danger {
  border-color: transparent;
}

.action-btn-danger .action-btn-icon {
  background: rgba(255, 69, 58, 0.12);
  color: #FF453A;
}

.action-btn-danger:hover:not(:disabled) {
  background: rgba(255, 69, 58, 0.08);
  border-color: rgba(255, 69, 58, 0.3);
}

.btn-spinner-sm {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin-sm 0.6s linear infinite;
}

@keyframes spin-sm {
  to { transform: rotate(360deg); }
}

.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-disabled);
  gap: 16px;
}

.detail-empty svg {
  opacity: 0.3;
}

.detail-empty p {
  font-size: 14px;
  margin: 0;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-disabled);
  gap: 12px;
}

.empty svg {
  opacity: 0.3;
}

.empty span {
  font-size: 13px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1E1E22;
  border-radius: 16px;
  width: 480px;
  max-width: 90%;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-subtle);
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-group textarea {
  resize: vertical;
  min-height: 70px;
}

.radio-group {
  display: flex;
  gap: 8px;
}

.radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.15s;
}

.radio-item input {
  display: none;
}

.radio-item:hover {
  border-color: rgba(10, 132, 255, 0.5);
}

.radio-item.active {
  background: rgba(10, 132, 255, 0.1);
  border-color: #0A84FF;
  color: #0A84FF;
}

/* Toggle */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  vertical-align: middle;
  margin-right: 10px;
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
  border-radius: 24px;
  cursor: pointer;
  transition: 0.2s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.2s;
}

.toggle-switch input:checked + .toggle-slider {
  background: #30D158;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
  vertical-align: middle;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-sm {
  padding: 8px 14px;
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
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}

.btn-hub {
  background: linear-gradient(135deg, rgba(48, 209, 88, 0.15), rgba(52, 199, 89, 0.15));
  color: #30D158;
  border: 1px solid rgba(48, 209, 88, 0.3);
}

.btn-hub:hover {
  background: linear-gradient(135deg, rgba(48, 209, 88, 0.25), rgba(52, 199, 89, 0.25));
  border-color: rgba(48, 209, 88, 0.5);
}

.btn-warning {
  color: #FF9F0A;
}

.btn-danger {
  color: #FF453A;
}

.btn-danger:hover {
  background: rgba(255, 69, 58, 0.15);
}

/* MCP Instance Status */
.status-running {
  color: #30D158;
}

.status-starting {
  color: #FF9F0A;
}

.status-stopped,
.status-error {
  color: #FF453A;
}

/* Tool List */
.tool-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-item {
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.tool-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.tool-risk {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.tool-risk.risk-low {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.tool-risk.risk-medium {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
}

.tool-risk.risk-high {
  background: rgba(255, 69, 58, 0.15);
  color: #FF453A;
}

.tool-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}

/* Grant Management */
.grant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grant-header h3 {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
}

.grant-empty {
  padding: 16px;
  text-align: center;
  color: var(--text-disabled);
  font-size: 13px;
}

.grant-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.grant-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.grant-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5AC8FA);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.grant-user-info {
  flex: 1;
  min-width: 0;
}

.grant-user-name {
  display: block;
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.grant-user-dept {
  display: block;
  font-size: 11px;
  color: var(--text-disabled);
}

.grant-user-remove {
  background: none;
  border: none;
  color: var(--text-disabled);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  transition: all 0.15s;
}

.grant-user-remove:hover {
  color: #FF453A;
  background: rgba(255, 69, 58, 0.1);
}

/* Grant Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1E1E22;
  border-radius: 12px;
  width: 420px;
  max-width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 16px 20px;
  flex: 1;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

.grant-search {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
  margin-bottom: 12px;
}

.grant-search:focus {
  outline: none;
  border-color: #0A84FF;
}

.grant-user-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.grant-user-select {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.grant-user-select:hover {
  background: rgba(255, 255, 255, 0.05);
}

.grant-user-select.selected {
  background: rgba(10, 132, 255, 0.1);
}

.grant-user-check {
  color: #0A84FF;
  font-weight: 600;
  font-size: 14px;
}

</style>
