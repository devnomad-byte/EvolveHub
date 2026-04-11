<template>
  <div class="kb-app">
    <!-- Left sidebar -->
    <div class="kb-sidebar">
      <div class="sidebar-title">全部知识库</div>
      <div class="kb-tree">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          class="kb-item"
          :class="{ active: selectedKb === kb.id }"
          @click="selectedKb = kb.id"
        >
          <span class="kb-level-icon">{{ levelIcon(kb.level) }}</span>
          <span class="kb-name">{{ kb.name }}</span>
          <span class="kb-count">{{ kb.docCount }}</span>
        </div>
      </div>
    </div>

    <!-- Main content -->
    <div class="kb-main">
      <div v-for="kb in visibleKbs" :key="kb.id" class="kb-card">
        <div class="kb-card-header">
          <span class="kb-card-icon">📂</span>
          <span class="kb-card-name">{{ kb.name }}</span>
          <span class="kb-level-badge" :class="kb.level.toLowerCase()">{{ levelLabel(kb.level) }}</span>
        </div>
        <div class="kb-card-desc">{{ kb.description }}</div>
        <div class="doc-grid">
          <div v-for="doc in kb.docs" :key="doc.id" class="doc-card">
            <div class="doc-icon">📄</div>
            <div class="doc-name">{{ doc.name }}</div>
            <div class="doc-status" :class="doc.status.toLowerCase()">
              {{ doc.status === 'READY' ? '✅ 就绪' : doc.status === 'PROCESSING' ? '⏳ 处理中' : '❌ 失败' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Action bar -->
      <div class="kb-actions">
        <button class="btn btn-primary" @click="addToast('文档上传功能（原型）')">+ 上传文档</button>
        <button class="btn btn-outline" @click="addToast('创建知识库功能（原型）')">+ 创建知识库</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDesktopStore } from '../../../stores/desktop'
import type { KnowledgeBase } from '../../../types'

const desktop = useDesktopStore()
const selectedKb = ref('all')

function addToast(msg: string) {
  desktop.addToast(msg, 'info')
}

const knowledgeBases = ref<KnowledgeBase[]>([
  {
    id: '1',
    name: '全局知识库',
    level: 'GLOBAL',
    description: '公司制度 | 通用规范',
    docCount: 3,
    docs: [
      { id: '1', name: '员工手册', status: 'READY', chunkCount: 24 },
      { id: '2', name: '年假制度', status: 'READY', chunkCount: 8 },
      { id: '3', name: '差旅报销', status: 'PROCESSING', chunkCount: 0 }
    ]
  },
  {
    id: '2',
    name: '研发部知识库',
    level: 'DEPT',
    description: '5G项目文档 | 技术规范',
    docCount: 2,
    docs: [
      { id: '4', name: 'API 文档', status: 'READY', chunkCount: 16 },
      { id: '5', name: '架构设计', status: 'READY', chunkCount: 12 }
    ]
  }
])

const visibleKbs = computed(() =>
  selectedKb.value === 'all'
    ? knowledgeBases.value
    : knowledgeBases.value.filter(kb => kb.id === selectedKb.value)
)

function levelIcon(level: string) {
  const map: Record<string, string> = { GLOBAL: '🌐', DEPT: '🏢', PROJECT: '📁', SENSITIVE: '🔒' }
  return map[level] || '📁'
}

function levelLabel(level: string) {
  const map: Record<string, string> = { GLOBAL: '全局', DEPT: '部门', PROJECT: '项目', SENSITIVE: '敏感' }
  return map[level] || level
}
</script>

<style scoped>
.kb-app {
  display: flex;
  height: 100%;
}

.kb-sidebar {
  width: 240px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  padding: 12px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.kb-tree {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kb-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}

.kb-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.kb-item.active {
  background: rgba(10, 132, 255, 0.2);
}

.kb-count {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-disabled);
  background: rgba(255, 255, 255, 0.1);
  padding: 1px 6px;
  border-radius: 10px;
}

.kb-main {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.kb-card {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid var(--border-subtle);
}

.kb-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.kb-card-name {
  font-size: 15px;
  font-weight: 600;
}

.kb-level-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: auto;
}

.kb-level-badge.global { background: rgba(48, 209, 88, 0.2); color: #30D158; }
.kb-level-badge.dept { background: rgba(10, 132, 255, 0.2); color: #0A84FF; }
.kb-level-badge.project { background: rgba(191, 90, 242, 0.2); color: #BF5AF2; }
.kb-level-badge.sensitive { background: rgba(255, 69, 58, 0.2); color: #FF453A; }

.kb-card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.doc-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.doc-card {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
}

.doc-icon {
  font-size: 28px;
  margin-bottom: 4px;
}

.doc-name {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-status {
  font-size: 11px;
  margin-top: 4px;
}

.doc-status.ready { color: #30D158; }
.doc-status.processing { color: #FF9F0A; }
.doc-status.failed { color: #FF453A; }

.kb-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  padding: 8px 0;
}
</style>
