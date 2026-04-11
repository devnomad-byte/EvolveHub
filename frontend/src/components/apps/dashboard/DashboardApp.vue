<template>
  <div class="dashboard">
    <!-- Background effects -->
    <div class="dash-bg-grid"></div>
    <div class="dash-bg-scanline"></div>

    <!-- Header -->
    <div class="dash-header">
      <div class="dash-header-left">
        <div class="dash-logo"></div>
        <h1 class="dash-title">EVOLVEHUB 数据驾驶舱</h1>
      </div>
      <div class="dash-header-right">
        <div class="dash-status-dot online"></div>
        <span class="dash-status-text">系统运行正常</span>
        <span class="dash-time">{{ timeStr }}</span>
        <span class="dash-date">{{ dateStr }}</span>
      </div>
    </div>

    <!-- Main grid -->
    <div class="dash-body">
      <!-- LEFT: KPI Cards -->
      <div class="dash-left">
        <div v-for="kpi in kpis" :key="kpi.label" class="kpi-card" :style="{ '--kpi-color': kpi.color }">
          <div class="kpi-bar"></div>
          <div class="kpi-content">
            <div class="kpi-header">
              <span class="kpi-icon"><component :is="kpiIcons[kpi.label]" :size="14" :color="kpi.color" /></span>
              <span class="kpi-label">{{ kpi.label }}</span>
            </div>
            <div class="kpi-value">{{ kpi.displayValue }}</div>
            <div class="kpi-trend" :class="kpi.trendUp ? 'up' : 'down'">
              {{ kpi.trendUp ? '↑' : '↓' }} {{ kpi.trend }}
            </div>
          </div>
        </div>
      </div>

      <!-- CENTER: Charts -->
      <div class="dash-center">
        <!-- Line chart: Model call trends -->
        <div class="dash-card chart-card">
          <div class="card-head">
            <span class="card-title">模型调用趋势（近7日）</span>
            <div class="chart-legend">
              <span class="legend-item" style="--leg: #0A84FF">GPT-4o</span>
              <span class="legend-item" style="--leg: #30D158">Qwen</span>
              <span class="legend-item" style="--leg: #BF5AF2">Gemini</span>
            </div>
          </div>
          <div class="line-chart">
            <svg viewBox="0 0 700 200" class="chart-svg">
              <!-- Grid lines -->
              <line v-for="i in 5" :key="'g'+i" x1="0" :y1="i*40" x2="700" :y2="i*40" stroke="rgba(255,255,255,0.06)" stroke-width="1" />
              <!-- Lines -->
              <polyline :points="linePoints[0]" fill="none" stroke="#0A84FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="chart-line" />
              <polyline :points="linePoints[1]" fill="none" stroke="#30D158" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="chart-line" style="animation-delay:0.2s" />
              <polyline :points="linePoints[2]" fill="none" stroke="#BF5AF2" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="chart-line" style="animation-delay:0.4s" />
              <!-- Area fills -->
              <polygon :points="'0,200 ' + linePoints[0] + ' 700,200'" fill="url(#grad-blue)" opacity="0.15" />
              <polygon :points="'0,200 ' + linePoints[1] + ' 700,200'" fill="url(#grad-green)" opacity="0.1" />
              <!-- Dots -->
              <g v-for="(line, li) in lineData" :key="'d'+li">
                <circle v-for="(v, di) in line" :key="di" :cx="di * 116.7" :cy="200 - v * 1.6" r="3.5" :fill="['#0A84FF','#30D158','#BF5AF2'][li]" class="chart-dot" />
              </g>
              <defs>
                <linearGradient id="grad-blue" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#0A84FF" />
                  <stop offset="100%" stop-color="transparent" />
                </linearGradient>
                <linearGradient id="grad-green" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#30D158" />
                  <stop offset="100%" stop-color="transparent" />
                </linearGradient>
              </defs>
            </svg>
            <div class="chart-x-labels">
              <span v-for="d in weekDates" :key="d">{{ d }}</span>
            </div>
          </div>
        </div>

        <!-- Donut chart: Knowledge base -->
        <div class="dash-card donut-card">
          <div class="card-head">
            <span class="card-title">知识库分布</span>
          </div>
          <div class="donut-row">
            <div class="donut-wrap">
              <div class="donut" :style="{ background: donutGradient }">
                <div class="donut-hole">
                  <div class="donut-total">2,456</div>
                  <div class="donut-total-label">总文档</div>
                </div>
              </div>
            </div>
            <div class="donut-legend">
              <div v-for="item in donutData" :key="item.label" class="legend-row">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                <span class="legend-text">{{ item.label }}</span>
                <span class="legend-val">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT: Activity + Health -->
      <div class="dash-right">
        <!-- Activity feed -->
        <div class="dash-card activity-card">
          <div class="card-head">
            <span class="card-title">实时动态</span>
            <span class="live-badge"><span class="live-dot"></span>LIVE</span>
          </div>
          <div class="activity-list" ref="activityList">
            <TransitionGroup name="activity-item">
              <div v-for="item in activities" :key="item.id" class="activity-row">
                <span class="act-time">{{ item.time }}</span>
                <span class="act-icon" :style="{ background: item.color }">{{ item.icon }}</span>
                <div class="act-content">
                  <span class="act-user">{{ item.user }}</span>
                  <span class="act-desc">{{ item.desc }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>

        <!-- System health -->
        <div class="dash-card health-card">
          <div class="card-head">
            <span class="card-title">系统健康</span>
          </div>
          <div class="health-bars">
            <div v-for="h in healthMetrics" :key="h.label" class="health-row">
              <div class="health-label">
                <span>{{ h.label }}</span>
                <span class="health-val">{{ h.displayValue }}</span>
              </div>
              <div class="health-track">
                <div class="health-fill" :style="{ width: h.percent + '%', '--fill-color': h.color }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="dash-bottom">
      <div v-for="m in bottomMetrics" :key="m.label" class="bottom-card">
        <div class="bc-label">{{ m.label }}</div>
        <div class="bc-value" :style="{ color: m.color }">{{ m.value }}</div>
        <div class="bc-sub">{{ m.sub }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { MessageSquare, Zap, BookOpen, Users } from 'lucide-vue-next'
import { gsap } from 'gsap'

const kpiIcons: Record<string, typeof MessageSquare> = {
  '今日对话': MessageSquare,
  'Token 消耗': Zap,
  '知识文档': BookOpen,
  '活跃用户': Users
}

// === Time ===
const now = ref(new Date())
let timer = 0

const timeStr = computed(() => {
  const d = now.value
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
})

const dateStr = computed(() => {
  const d = now.value
  return `${d.getFullYear()}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getDate().toString().padStart(2,'0')}`
})

// === KPI ===
const kpis = ref([
  { label: '今日对话', value: 1284, displayValue: '0', trend: '+12.5%', trendUp: true, color: '#0A84FF' },
  { label: 'Token 消耗', value: 528, displayValue: '0万', trend: '+8.3%', trendUp: true, color: '#BF5AF2' },
  { label: '知识文档', value: 2456, displayValue: '0', trend: '+156 新增', trendUp: true, color: '#30D158' },
  { label: '活跃用户', value: 89, displayValue: '0', trend: '在线', trendUp: true, color: '#FFD60A' }
])

// === Line Chart ===
const lineData = [
  [68, 85, 72, 95, 88, 78, 105],  // GPT-4o
  [45, 55, 62, 48, 70, 65, 58],   // Qwen
  [30, 38, 42, 35, 50, 45, 40]    // Gemini
]

const linePoints = computed(() =>
  lineData.map(line => line.map((v, i) => `${i * 116.7},${200 - v * 1.6}`).join(' '))
)

const weekDates = computed(() => {
  const d = now.value
  const dates: string[] = []
  for (let i = 6; i >= 0; i--) {
    const dd = new Date(d)
    dd.setDate(dd.getDate() - i)
    dates.push(`${dd.getMonth()+1}/${dd.getDate()}`)
  }
  return dates
})

// === Donut Chart ===
const donutData = [
  { label: '全局知识库', value: 892, color: '#0A84FF' },
  { label: '部门知识库', value: 654, color: '#30D158' },
  { label: '项目知识库', value: 523, color: '#BF5AF2' },
  { label: '敏感知识库', value: 387, color: '#FFD60A' }
]

const donutGradient = computed(() => {
  const total = donutData.reduce((s, d) => s + d.value, 0)
  let cum = 0
  const stops = donutData.map(d => {
    const start = (cum / total) * 360
    cum += d.value
    const end = (cum / total) * 360
    return `${d.color} ${start}deg ${end}deg`
  })
  return `conic-gradient(${stops.join(', ')})`
})

// === Activity Feed ===
const activityTemplates = [
  { icon: '💬', color: '#0A84FF', desc: '发起了 AI 对话' },
  { icon: '📄', color: '#30D158', desc: '上传了新文档到知识库' },
  { icon: '🔧', color: '#64D2FF', desc: '调用了 MCP 工具' },
  { icon: '📊', color: '#BF5AF2', desc: '查询了数据分析报告' },
  { icon: '🤖', color: '#FFD60A', desc: '更新了智能体配置' },
  { icon: '📝', color: '#FF9F0A', desc: '编辑了记忆条目' }
]
const userNames = ['张管理', '李研发', '王产品', '刘设计', '陈运维', '赵数据', '周测试', '吴安全']

interface Activity {
  id: string
  time: string
  icon: string
  color: string
  user: string
  desc: string
}

const activities = ref<Activity[]>([])
let actTimer = 0

function generateActivity(): Activity {
  const tpl = activityTemplates[Math.floor(Math.random() * activityTemplates.length)]
  const user = userNames[Math.floor(Math.random() * userNames.length)]
  const d = new Date()
  const time = `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')}`
  return { id: Date.now().toString() + Math.random(), time, icon: tpl.icon, color: tpl.color, user, desc: tpl.desc }
}

// === System Health ===
const healthMetrics = ref([
  { label: 'CPU 使用率', percent: 67, displayValue: '67%', color: '#30D158' },
  { label: '内存占用', percent: 52, displayValue: '4.2 / 8 GB', color: '#0A84FF' },
  { label: 'GPU 使用率', percent: 82, displayValue: '82%', color: '#FFD60A' },
  { label: '磁盘空间', percent: 60, displayValue: '1.2 / 2 TB', color: '#0A84FF' }
])

// === Bottom Metrics ===
const bottomMetrics = ref([
  { label: '模型性能 Top3', value: 'GPT-4o', sub: '平均响应 1.2s', color: '#0A84FF' },
  { label: '用户活跃度', value: '89 / 156', sub: '今日 / 本周', color: '#30D158' },
  { label: '今日任务', value: '234 / 280', sub: '完成率 83.6%', color: '#BF5AF2' },
  { label: '存储空间', value: '1.2 TB', sub: '总计 2 TB', color: '#FFD60A' },
  { label: '网络延迟', value: 'P50 12ms', sub: 'P99 89ms', color: '#64D2FF' }
])

// === Animated counter ===
function animateCounters() {
  kpis.value.forEach((kpi, index) => {
    const obj = { val: 0 }
    gsap.to(obj, {
      val: kpi.value,
      duration: 1.5,
      delay: index * 0.15,
      ease: 'power2.out',
      onUpdate: () => {
        const v = Math.round(obj.val)
        if (kpi.value === 528) {
          kpi.displayValue = `${v}万`
        } else {
          kpi.displayValue = v.toLocaleString()
        }
      }
    })
  })
}

// === Lifecycle ===
onMounted(() => {
  timer = window.setInterval(() => { now.value = new Date() }, 1000)

  // Init activities
  for (let i = 0; i < 8; i++) {
    activities.value.unshift(generateActivity())
  }
  // Auto add
  actTimer = window.setInterval(() => {
    activities.value.unshift(generateActivity())
    if (activities.value.length > 20) activities.value.pop()
  }, 3000)

  // Animate counters
  animateCounters()

  // Entrance animation
  gsap.from('.kpi-card', { y: 30, opacity: 0, duration: 0.6, stagger: 0.1, ease: 'power3.out', delay: 0.2 })
  gsap.from('.chart-card', { y: 30, opacity: 0, duration: 0.7, ease: 'power3.out', delay: 0.5 })
  gsap.from('.donut-card', { y: 30, opacity: 0, duration: 0.7, ease: 'power3.out', delay: 0.7 })
  gsap.from('.activity-card', { x: 30, opacity: 0, duration: 0.6, ease: 'power3.out', delay: 0.4 })
  gsap.from('.health-card', { x: 30, opacity: 0, duration: 0.6, ease: 'power3.out', delay: 0.6 })
  gsap.from('.bottom-card', { y: 20, opacity: 0, duration: 0.5, stagger: 0.08, ease: 'power3.out', delay: 0.8 })
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(actTimer)
})
</script>

<style scoped>
.dashboard {
  width: 100%;
  height: 100%;
  background: #060614;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* === Background effects === */
.dash-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(10, 132, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(10, 132, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 0;
}

.dash-bg-scanline {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent, rgba(10, 132, 255, 0.03) 4px, transparent 8px);
  background-size: 100% 100%;
  animation: scanDown 8s linear infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes scanDown {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

/* === Header === */
.dash-header {
  position: relative;
  z-index: 2;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(10, 10, 30, 0.6);
  border-bottom: 1px solid rgba(10, 132, 255, 0.1);
  flex-shrink: 0;
}

.dash-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dash-logo {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: linear-gradient(135deg, #0A84FF, #30D158);
  box-shadow: 0 0 12px rgba(10, 132, 255, 0.4);
}

.dash-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #0A84FF, #30D158, #0A84FF);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleShimmer 4s linear infinite;
}

@keyframes titleShimmer {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

.dash-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dash-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #30D158;
  box-shadow: 0 0 8px rgba(48, 209, 88, 0.6);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.dash-status-text {
  font-size: 12px;
  color: #30D158;
  letter-spacing: 1px;
}

.dash-time {
  font-size: 18px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 2px;
  font-variant-numeric: tabular-nums;
}

.dash-date {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 1px;
}

/* === Main body grid === */
.dash-body {
  flex: 1;
  display: grid;
  grid-template-columns: 220px 1fr 280px;
  gap: 12px;
  padding: 12px;
  position: relative;
  z-index: 1;
  min-height: 0;
}

/* === Card base === */
.dash-card {
  background: rgba(16, 16, 36, 0.7);
  border: 1px solid rgba(10, 132, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.dash-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(10, 132, 255, 0.2), transparent);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1px;
}

/* === LEFT: KPI Cards === */
.dash-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.kpi-card {
  background: rgba(16, 16, 36, 0.7);
  border: 1px solid rgba(10, 132, 255, 0.08);
  border-radius: 12px;
  padding: 16px 16px 16px 20px;
  display: flex;
  gap: 14px;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(10, 132, 255, 0.2), transparent);
}

.kpi-bar {
  width: 3px;
  border-radius: 3px;
  background: linear-gradient(180deg, var(--kpi-color), transparent);
  flex-shrink: 0;
}

.kpi-content {
  flex: 1;
  min-width: 0;
}

.kpi-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.kpi-icon {
  font-size: 14px;
}

.kpi-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 1px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  margin-bottom: 6px;
}

.kpi-trend {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.kpi-trend.up {
  color: #30D158;
}

.kpi-trend.down {
  color: #FF453A;
}

/* === CENTER === */
.dash-center {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.chart-card {
  flex: 1.2;
  display: flex;
  flex-direction: column;
}

.chart-legend {
  display: flex;
  gap: 14px;
}

.legend-item {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-item::before {
  content: '';
  width: 8px;
  height: 3px;
  border-radius: 2px;
  background: var(--leg);
}

.line-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chart-svg {
  width: 100%;
  flex: 1;
  min-height: 0;
}

.chart-line {
  stroke-dasharray: 1500;
  stroke-dashoffset: 1500;
  animation: drawLine 2s ease-out forwards;
}

@keyframes drawLine {
  to { stroke-dashoffset: 0; }
}

.chart-dot {
  opacity: 0;
  animation: dotAppear 0.3s ease forwards;
  animation-delay: 1.5s;
}

@keyframes dotAppear {
  to { opacity: 1; }
}

.chart-x-labels {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px 0;
}

.chart-x-labels span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
  font-variant-numeric: tabular-nums;
}

/* Donut */
.donut-card {
  flex: 0.8;
}

.donut-row {
  display: flex;
  align-items: center;
  gap: 24px;
}

.donut-wrap {
  flex-shrink: 0;
}

.donut {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  position: relative;
  animation: donutSpin 1s ease-out;
}

@keyframes donutSpin {
  from { transform: rotate(-90deg); opacity: 0; }
  to { transform: rotate(0deg); opacity: 1; }
}

.donut-hole {
  position: absolute;
  inset: 28px;
  border-radius: 50%;
  background: #0a0a20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-total {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.donut-total-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  margin-top: 2px;
}

.donut-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  flex: 1;
}

.legend-val {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  font-variant-numeric: tabular-nums;
}

/* === RIGHT === */
.dash-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

/* Activity */
.activity-card {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  font-weight: 600;
  color: #FF453A;
  letter-spacing: 1px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FF453A;
  animation: statusPulse 1.5s ease-in-out infinite;
}

.activity-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 6px;
  mask-image: linear-gradient(to bottom, black 85%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, black 85%, transparent 100%);
}

.activity-list::-webkit-scrollbar {
  width: 3px;
}

.activity-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.activity-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.02);
  transition: background 150ms;
}

.activity-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.act-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.25);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
  width: 56px;
}

.act-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.act-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.act-user {
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.act-desc {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-item-enter-active {
  transition: all 0.4s ease;
}

.activity-item-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

/* Health */
.health-card {
  flex: 0.8;
}

.health-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.health-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.health-label {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.health-val {
  font-variant-numeric: tabular-nums;
  color: rgba(255, 255, 255, 0.7);
}

.health-track {
  height: 4px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 4px;
  overflow: hidden;
}

.health-fill {
  height: 100%;
  border-radius: 4px;
  background: var(--fill-color);
  box-shadow: 0 0 8px color-mix(in srgb, var(--fill-color) 40%, transparent);
  animation: fillGrow 1.5s ease-out;
}

@keyframes fillGrow {
  from { width: 0 !important; }
}

/* === Bottom bar === */
.dash-bottom {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 0 12px 12px;
  flex-shrink: 0;
}

.bottom-card {
  background: rgba(16, 16, 36, 0.7);
  border: 1px solid rgba(10, 132, 255, 0.08);
  border-radius: 10px;
  padding: 12px 16px;
  position: relative;
  overflow: hidden;
}

.bottom-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(10, 132, 255, 0.15), transparent);
}

.bc-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 1px;
  margin-bottom: 6px;
}

.bc-value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
  font-variant-numeric: tabular-nums;
}

.bc-sub {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
}
</style>
