<template>
  <div v-if="visible" class="scan-overlay" @click.self="close">
    <div class="scan-dialog">
      <div class="scan-header">
        <div class="scan-title">
          <ShieldAlert :size="20" />
          <span>安全扫描报告</span>
        </div>
        <button class="scan-close" @click="close"><X :size="18" /></button>
      </div>

      <div class="scan-summary" :class="severityClass(maxSeverity)">
        <span class="summary-icon">{{ severityIcon(maxSeverity) }}</span>
        <span>发现 {{ findings.length }} 个问题，最高严重程度：<strong>{{ severityLabel(maxSeverity) }}</strong></span>
      </div>

      <!-- 阻断提示：CRITICAL/HIGH 不允许继续 -->
      <div v-if="isBlocked" class="scan-blocked">
        <AlertTriangle :size="16" />
        <span>存在严重/高危风险，不允许继续操作，请修改内容后重试</span>
      </div>

      <div class="scan-body">
        <div v-for="(finding, idx) in findings" :key="idx" class="finding-card" :class="'severity-' + finding.severity.toLowerCase()">
          <div class="finding-header">
            <span class="severity-badge" :class="'badge-' + finding.severity.toLowerCase()">
              {{ severityLabel(finding.severity) }}
            </span>
            <span class="finding-title">{{ finding.title }}</span>
          </div>
          <div class="finding-detail">
            <p class="finding-desc">{{ finding.description }}</p>
            <div class="finding-meta">
              <span v-if="finding.filePath" class="meta-item">
                <File :size="12" /> {{ finding.filePath }}
              </span>
              <span v-if="finding.lineNumber" class="meta-item">
                行 {{ finding.lineNumber }}
              </span>
              <span class="meta-item rule-id">{{ finding.ruleId }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="scan-footer">
        <button class="btn btn-outline" @click="close">关闭</button>
        <button v-if="canProceed" class="btn btn-danger" @click="forceProceed">
          无视风险继续
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ShieldAlert, X, File, AlertTriangle } from 'lucide-vue-next'

export interface ScanFinding {
  severity: string
  title: string
  description: string
  filePath?: string
  lineNumber?: number
  ruleId: string
}

export interface ScanResult {
  passed: boolean
  maxSeverity: string
  findings: ScanFinding[]
}

const props = defineProps<{
  visible: boolean
  result: ScanResult | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'forceProceed'): void
}>()

const findings = computed(() => props.result?.findings ?? [])
const maxSeverity = computed(() => props.result?.maxSeverity ?? 'LOW')

/** CRITICAL/HIGH 阻断，不允许继续 */
const isBlocked = computed(() => {
  const sev = maxSeverity.value
  return sev === 'CRITICAL' || sev === 'HIGH'
})

/** 仅 MEDIUM/LOW 允许用户选择无视风险继续 */
const canProceed = computed(() => {
  return !props.result?.passed && !isBlocked.value
})

function close() {
  emit('close')
}

function forceProceed() {
  emit('forceProceed')
}

function severityLabel(sev: string): string {
  const map: Record<string, string> = {
    CRITICAL: '严重',
    HIGH: '高危',
    MEDIUM: '中等',
    LOW: '低危'
  }
  return map[sev] ?? sev
}

function severityIcon(sev: string): string {
  const map: Record<string, string> = {
    CRITICAL: '🔴',
    HIGH: '🟠',
    MEDIUM: '🟡',
    LOW: '⚪'
  }
  return map[sev] ?? '⚪'
}

function severityClass(sev: string): string {
  return 'summary-' + (sev || 'low').toLowerCase()
}
</script>

<style scoped>
.scan-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.scan-dialog {
  background: #1E1E22;
  border-radius: 14px;
  width: 560px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.scan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.scan-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #FF453A;
}

.scan-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.scan-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.scan-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  font-size: 13px;
}

.summary-critical { background: rgba(255, 69, 58, 0.12); color: #FF453A; }
.summary-high { background: rgba(255, 159, 10, 0.12); color: #FF9F0A; }
.summary-medium { background: rgba(255, 214, 10, 0.12); color: #FFD60A; }
.summary-low { background: rgba(142, 142, 147, 0.12); color: #8E8E93; }

.summary-icon {
  font-size: 16px;
}

.scan-blocked {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 69, 58, 0.08);
  color: #FF6961;
  font-size: 12px;
  border-bottom: 1px solid rgba(255, 69, 58, 0.15);
}

.scan-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finding-card {
  border-radius: 10px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.severity-critical { background: rgba(255, 69, 58, 0.08); border-left: 3px solid #FF453A; }
.severity-high { background: rgba(255, 159, 10, 0.08); border-left: 3px solid #FF9F0A; }
.severity-medium { background: rgba(255, 214, 10, 0.08); border-left: 3px solid #FFD60A; }
.severity-low { background: rgba(142, 142, 147, 0.08); border-left: 3px solid #8E8E93; }

.finding-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.severity-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-critical { background: #FF453A; color: white; }
.badge-high { background: #FF9F0A; color: white; }
.badge-medium { background: #FFD60A; color: #1C1C1E; }
.badge-low { background: #8E8E93; color: white; }

.finding-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.finding-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 6px 0;
  line-height: 1.5;
  word-break: break-all;
}

.finding-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: var(--text-disabled);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rule-id {
  font-family: monospace;
  opacity: 0.7;
}

.scan-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-subtle);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-outline {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}

.btn-danger {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
  border: 1px solid rgba(255, 159, 10, 0.3);
}

.btn-danger:hover {
  background: rgba(255, 159, 10, 0.25);
}
</style>
