<template>
  <div class="memory-app">
    <!-- Left: User list -->
    <div class="mem-sidebar">
      <div class="mem-sidebar-header">
        <span class="mem-sidebar-title">用户列表</span>
        <span class="mem-sidebar-count">{{ userMemories.length }}</span>
      </div>
      <div class="mem-search">
        <input v-model="userFilter" class="mem-search-input" placeholder="搜索用户..." />
      </div>
      <div class="mem-user-list">
        <div
          v-for="u in filteredUsers"
          :key="u.id"
          class="mem-user-item"
          :class="{ active: selectedUserId === u.id }"
          @click="selectedUserId = u.id"
        >
          <div class="mem-user-avatar">{{ u.avatar }}</div>
          <div class="mem-user-info">
            <div class="mem-user-name">{{ u.name }}</div>
            <div class="mem-user-meta">
              <span class="mem-tag short">{{ u.shortCount }} 短期</span>
              <span class="mem-tag long">{{ u.longCount }} 长期</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Memory detail -->
    <div class="mem-main">
      <template v-if="selectedUser">
        <!-- Header -->
        <div class="mem-detail-header">
          <div class="mem-detail-user">
            <span class="mem-detail-avatar">{{ selectedUser.avatar }}</span>
            <div>
              <div class="mem-detail-name">{{ selectedUser.name }}</div>
              <div class="mem-detail-dept">{{ selectedUser.dept }}</div>
            </div>
          </div>
          <div class="mem-tabs">
            <button
              class="mem-tab"
              :class="{ active: activeTab === 'short' }"
              @click="activeTab = 'short'"
            >短期记忆</button>
            <button
              class="mem-tab"
              :class="{ active: activeTab === 'long' }"
              @click="activeTab = 'long'"
            >长期记忆</button>
          </div>
        </div>

        <!-- Short-term memory: MEMORY.md -->
        <div v-if="activeTab === 'short'" class="mem-tab-content">
          <div class="mem-file-card">
            <div class="mem-file-header">
              <div class="mem-file-name">
                <span class="mem-file-icon">📄</span>
                MEMORY.md
              </div>
              <div class="mem-file-actions">
                <button class="mem-btn mem-btn-ghost">编辑</button>
                <button class="mem-btn mem-btn-ghost">刷新</button>
              </div>
            </div>
            <div class="mem-file-meta">
              <span>{{ selectedUser.shortCount }} 条记忆</span>
              <span>最后更新: {{ selectedUser.lastUpdate }}</span>
            </div>
            <div class="mem-file-body">
              <pre>{{ selectedUser.mdContent }}</pre>
            </div>
          </div>
        </div>

        <!-- Long-term memory: vector memories -->
        <div v-if="activeTab === 'long'" class="mem-tab-content">
          <div class="mem-long-toolbar">
            <input v-model="memorySearch" class="mem-search-input mem-search-wide" placeholder="搜索长期记忆..." />
            <select v-model="memoryType" class="mem-select">
              <option value="all">全部类型</option>
              <option value="preference">偏好</option>
              <option value="fact">事实</option>
              <option value="tool_config">工具配置</option>
            </select>
          </div>
          <div class="mem-long-list">
            <div
              v-for="mem in filteredMemories"
              :key="mem.id"
              class="mem-long-item"
            >
              <div class="mem-long-left">
                <span class="mem-type-badge" :class="'type-' + mem.type">{{ typeLabel[mem.type] }}</span>
                <div class="mem-long-content">{{ mem.content }}</div>
              </div>
              <div class="mem-long-right">
                <div class="mem-importance">
                  <div class="mem-importance-bar">
                    <div class="mem-importance-fill" :class="'imp-' + importanceLevel(mem.importance)" :style="{ width: mem.importance * 100 + '%' }"></div>
                  </div>
                  <span class="mem-importance-val">{{ (mem.importance * 100).toFixed(0) }}%</span>
                </div>
                <span class="mem-long-date">{{ mem.date }}</span>
              </div>
            </div>
            <div v-if="filteredMemories.length === 0" class="mem-empty">暂无匹配的记忆条目</div>
          </div>
        </div>
      </template>

      <!-- No user selected -->
      <div v-else class="mem-placeholder">
        <span class="mem-placeholder-icon">🧠</span>
        <div class="mem-placeholder-text">选择用户查看记忆</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const selectedUserId = ref(1)
const activeTab = ref<'short' | 'long'>('short')
const userFilter = ref('')
const memorySearch = ref('')
const memoryType = ref('all')

const typeLabel: Record<string, string> = {
  preference: '偏好',
  fact: '事实',
  tool_config: '工具'
}

interface UserMemory {
  id: number
  name: string
  avatar: string
  dept: string
  shortCount: number
  longCount: number
  lastUpdate: string
  mdContent: string
}

const userMemories = ref<UserMemory[]>([
  {
    id: 1, name: '张管理', avatar: '🧑‍💼', dept: '总经办',
    shortCount: 12, longCount: 45, lastUpdate: '10分钟前',
    mdContent: `## 基本信息
- 姓名: 张管理
- 部门: 总经办
- 角色: 超级管理员

## 偏好设置
- 语言: 中文
- 模型: qwen-max
- 称呼: 老张

## 工具配置
- SSH: home-server -> 192.168.1.100
- Docker: 默认使用 docker compose

## 工作习惯
- 每日 9:00 查看系统运行报告
- 偏好表格化数据展示
- 习惯使用 GitFlow 分支策略`
  },
  {
    id: 2, name: '李用户', avatar: '👤', dept: '技术部',
    shortCount: 8, longCount: 32, lastUpdate: '1小时前',
    mdContent: `## 基本信息
- 姓名: 李用户
- 部门: 技术部

## 偏好设置
- 语言: 中文
- 模型: gpt-4o
- 回复风格: 简洁

## 兴趣爱好
- 编程 & 技术博客
- 开源项目贡献
- 科幻小说阅读
- 咖啡品鉴`
  },
  {
    id: 3, name: '王产品', avatar: '👩‍💻', dept: '产品部',
    shortCount: 6, longCount: 28, lastUpdate: '3小时前',
    mdContent: `## 基本信息
- 姓名: 王产品
- 部门: 产品部

## 偏好设置
- 语言: 中文
- 模型: claude-3.5

## 工作习惯
- 每周一更新需求文档
- 偏好 Markdown 格式
- 使用飞书管理任务`
  },
  {
    id: 4, name: '刘设计', avatar: '🎨', dept: '设计部',
    shortCount: 4, longCount: 19, lastUpdate: '昨天',
    mdContent: `## 基本信息
- 姓名: 刘设计
- 部门: 设计部

## 偏好设置
- 语言: 中文
- 模型: qwen-max

## 兴趣爱好
- UI/UX 设计
- 色彩心理学
- 极简主义`
  },
  {
    id: 5, name: '陈运维', avatar: '🔧', dept: '运维部',
    shortCount: 15, longCount: 67, lastUpdate: '30分钟前',
    mdContent: `## 基本信息
- 姓名: 陈运维
- 部门: 运维部

## 工具配置
- K8s 集群: prod-cluster-01
- 监控: Prometheus + Grafana
- 日志: ELK Stack
- CI/CD: Jenkins + ArgoCD

## 偏好设置
- 语言: 中文 + 英文术语
- 模型: deepseek-v3`
  },
  {
    id: 6, name: '赵数据', avatar: '📊', dept: '数据部',
    shortCount: 9, longCount: 41, lastUpdate: '2小时前',
    mdContent: `## 基本信息
- 姓名: 赵数据
- 部门: 数据部

## 偏好设置
- 语言: 中文
- 模型: gpt-4o
- 数据库: PostgreSQL + ClickHouse

## 工作习惯
- 每天处理数据报表
- 偏好 SQL 和 Python
- 使用 Jupyter 进行分析`
  }
])

interface LongMemory {
  id: string
  userId: number
  userName: string
  type: 'preference' | 'fact' | 'tool_config'
  content: string
  importance: number
  date: string
}

const longMemories = ref<LongMemory[]>([
  { id: '1', userId: 1, userName: '张管理', type: 'preference', content: '偏好使用 Python 进行数据分析，不擅长 R 语言', importance: 0.8, date: '2026-04-10' },
  { id: '2', userId: 1, userName: '张管理', type: 'fact', content: '每周五下午需要提交周报给上级领导', importance: 0.7, date: '2026-04-09' },
  { id: '3', userId: 1, userName: '张管理', type: 'tool_config', content: 'Docker Compose 部署，默认使用 docker compose 而非 docker-compose', importance: 0.6, date: '2026-04-08' },
  { id: '4', userId: 2, userName: '李用户', type: 'preference', content: '回复风格偏好简洁，不需要过多客套话', importance: 0.85, date: '2026-04-10' },
  { id: '5', userId: 2, userName: '李用户', type: 'fact', content: '目前正在学习 Rust 编程语言', importance: 0.5, date: '2026-04-09' },
  { id: '6', userId: 2, userName: '李用户', type: 'preference', content: '技术文档偏好使用英文原文，其他内容偏好中文', importance: 0.75, date: '2026-04-08' },
  { id: '7', userId: 3, userName: '王产品', type: 'fact', content: '负责的产品线是 AI 中台和智能客服', importance: 0.6, date: '2026-04-10' },
  { id: '8', userId: 3, userName: '王产品', type: 'preference', content: '需求文档使用 Markdown 格式，不用 Word', importance: 0.7, date: '2026-04-07' },
  { id: '9', userId: 4, userName: '刘设计', type: 'fact', content: '设计工具主要使用 Figma，偶尔使用 Sketch', importance: 0.4, date: '2026-04-09' },
  { id: '10', userId: 4, userName: '刘设计', type: 'preference', content: '配色偏好低饱和度，设计风格偏极简主义', importance: 0.65, date: '2026-04-06' },
  { id: '11', userId: 5, userName: '陈运维', type: 'tool_config', content: 'K8s 集群名称 prod-cluster-01，命名空间按项目划分', importance: 0.9, date: '2026-04-10' },
  { id: '12', userId: 5, userName: '陈运维', type: 'fact', content: '监控告警通过钉钉群发送，P0 级别同时电话通知', importance: 0.85, date: '2026-04-09' },
  { id: '13', userId: 5, userName: '陈运维', type: 'tool_config', content: 'CI/CD 流水线使用 Jenkins + ArgoCD，GitOps 模式', importance: 0.8, date: '2026-04-08' },
  { id: '14', userId: 6, userName: '赵数据', type: 'preference', content: '数据分析优先使用 SQL，复杂场景使用 Python pandas', importance: 0.7, date: '2026-04-10' },
  { id: '15', userId: 6, userName: '赵数据', type: 'fact', content: '每天上午 9:00 需要生成前一日的数据报表', importance: 0.75, date: '2026-04-09' }
])

const filteredUsers = computed(() =>
  userMemories.value.filter(u =>
    u.name.includes(userFilter.value) || u.dept.includes(userFilter.value)
  )
)

const selectedUser = computed(() =>
  userMemories.value.find(u => u.id === selectedUserId.value)
)

const filteredMemories = computed(() =>
  longMemories.value.filter(m => {
    if (selectedUserId.value && m.userId !== selectedUserId.value) return false
    if (memoryType.value !== 'all' && m.type !== memoryType.value) return false
    if (memorySearch.value && !m.content.includes(memorySearch.value)) return false
    return true
  })
)

function importanceLevel(imp: number): string {
  if (imp >= 0.8) return 'high'
  if (imp >= 0.6) return 'mid'
  return 'low'
}
</script>

<style scoped>
.memory-app {
  height: 100%;
  display: flex;
  background: #0a0a14;
}

/* === Left Sidebar === */
.mem-sidebar {
  width: 240px;
  min-width: 240px;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.2);
}

.mem-sidebar-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.mem-sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.mem-sidebar-count {
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(10, 132, 255, 0.12);
  padding: 1px 8px;
  border-radius: 10px;
}

.mem-search {
  padding: 8px 12px;
}

.mem-search-input {
  width: 100%;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0 12px;
  font-size: 12px;
  color: #fff;
  outline: none;
  transition: border-color 200ms;
  box-sizing: border-box;
}

.mem-search-input:focus {
  border-color: rgba(10, 132, 255, 0.4);
}

.mem-search-input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.mem-search-wide {
  height: 36px;
  font-size: 13px;
}

.mem-user-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.mem-user-list::-webkit-scrollbar {
  width: 3px;
}

.mem-user-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.mem-user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 150ms;
}

.mem-user-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.mem-user-item.active {
  background: rgba(10, 132, 255, 0.1);
}

.mem-user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.mem-user-info {
  flex: 1;
  min-width: 0;
}

.mem-user-name {
  font-size: 13px;
  font-weight: 500;
  color: #fff;
}

.mem-user-meta {
  display: flex;
  gap: 6px;
  margin-top: 3px;
}

.mem-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
}

.mem-tag.short {
  color: #FFD60A;
  background: rgba(255, 214, 10, 0.1);
}

.mem-tag.long {
  color: #30D158;
  background: rgba(48, 209, 88, 0.1);
}

/* === Right Main === */
.mem-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.mem-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.mem-detail-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mem-detail-avatar {
  font-size: 28px;
}

.mem-detail-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.mem-detail-dept {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 1px;
}

.mem-tabs {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.04);
  padding: 3px;
  border-radius: 8px;
}

.mem-tab {
  padding: 5px 14px;
  border-radius: 6px;
  border: none;
  background: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
}

.mem-tab:hover {
  color: rgba(255, 255, 255, 0.7);
}

.mem-tab.active {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

/* === Tab content === */
.mem-tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.mem-tab-content::-webkit-scrollbar {
  width: 4px;
}

.mem-tab-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

/* === Short-term: MEMORY.md === */
.mem-file-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  overflow: hidden;
}

.mem-file-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.mem-file-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.mem-file-icon {
  font-size: 16px;
}

.mem-file-actions {
  display: flex;
  gap: 6px;
}

.mem-btn {
  padding: 4px 12px;
  border-radius: 6px;
  border: none;
  font-size: 12px;
  cursor: pointer;
  transition: all 150ms;
}

.mem-btn-ghost {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.6);
}

.mem-btn-ghost:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.mem-file-meta {
  display: flex;
  gap: 16px;
  padding: 8px 16px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.3);
}

.mem-file-body {
  padding: 16px;
}

.mem-file-body pre {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
}

/* === Long-term memory === */
.mem-long-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.mem-long-toolbar .mem-search-input {
  flex: 1;
}

.mem-select {
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  outline: none;
  cursor: pointer;
}

.mem-select option {
  background: #1a1a2e;
  color: #fff;
}

.mem-long-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mem-long-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  transition: background 150ms;
}

.mem-long-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.mem-long-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.mem-type-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;
}

.mem-type-badge.type-preference {
  color: #0A84FF;
  background: rgba(10, 132, 255, 0.12);
}

.mem-type-badge.type-fact {
  color: #30D158;
  background: rgba(48, 209, 88, 0.12);
}

.mem-type-badge.type-tool_config {
  color: #FFD60A;
  background: rgba(255, 214, 10, 0.12);
}

.mem-long-content {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mem-long-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.mem-importance {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mem-importance-bar {
  width: 50px;
  height: 3px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
}

.mem-importance-fill {
  height: 100%;
  border-radius: 2px;
  background: #30D158;
}

.mem-importance-fill.imp-mid {
  background: #FFD60A;
}

.mem-importance-fill.imp-high {
  background: #FF453A;
}

.mem-importance-val {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  width: 30px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.mem-long-date {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.2);
}

.mem-empty {
  text-align: center;
  padding: 40px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
}

/* === Placeholder === */
.mem-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.mem-placeholder-icon {
  font-size: 40px;
  opacity: 0.3;
}

.mem-placeholder-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.25);
}
</style>
