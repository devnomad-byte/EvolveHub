<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>工作台</h1>
      <p class="welcome-text">欢迎回来，{{ userInfo?.nickname || userInfo?.username || '管理员' }}</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <i class="fas fa-comments"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">对话总数</div>
          <div class="stat-value">1,234</div>
          <div class="stat-change up">
            <i class="fas fa-arrow-up"></i> 12.5%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon green">
          <i class="fas fa-robot"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">活跃智能体</div>
          <div class="stat-value">28</div>
          <div class="stat-change up">
            <i class="fas fa-arrow-up"></i> 8.2%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon purple">
          <i class="fas fa-book"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">知识库文档</div>
          <div class="stat-value">856</div>
          <div class="stat-change up">
            <i class="fas fa-arrow-up"></i> 15.3%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon red">
          <i class="fas fa-tasks"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">待处理任务</div>
          <div class="stat-value">12</div>
          <div class="stat-change down">
            <i class="fas fa-arrow-down"></i> 3.1%
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <i class="fas fa-chart-line"></i>
            近期活动
          </div>
        </div>
        <div class="card-body">
          <div class="activity-list">
            <div class="activity-item">
              <div class="activity-icon">
                <i class="fas fa-sign-in-alt"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">用户登录系统</div>
                <div class="activity-time">5 分钟前</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="activity-icon">
                <i class="fas fa-comments"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">新建 AI 对话</div>
                <div class="activity-time">15 分钟前</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="activity-icon">
                <i class="fas fa-upload"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">上传知识文档</div>
                <div class="activity-time">1 小时前</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <i class="fas fa-rocket"></i>
            快速开始
          </div>
        </div>
        <div class="card-body">
          <div class="quick-start-grid">
            <div class="quick-start-item">
              <div class="quick-start-icon">
                <i class="fas fa-plus"></i>
              </div>
              <span>新建对话</span>
            </div>
            <div class="quick-start-item">
              <div class="quick-start-icon">
                <i class="fas fa-upload"></i>
              </div>
              <span>上传文档</span>
            </div>
            <div class="quick-start-item">
              <div class="quick-start-icon">
                <i class="fas fa-robot"></i>
              </div>
              <span>创建智能体</span>
            </div>
            <div class="quick-start-item">
              <div class="quick-start-icon">
                <i class="fas fa-cog"></i>
              </div>
              <span>系统配置</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.welcome-text {
  color: var(--text-muted);
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon.blue { background: rgba(69, 183, 209, 0.15); color: var(--secondary); }
.stat-icon.green { background: rgba(109, 179, 63, 0.15); color: var(--success); }
.stat-icon.purple { background: rgba(155, 89, 182, 0.15); color: #9b59b6; }
.stat-icon.red { background: rgba(231, 76, 60, 0.15); color: var(--accent); }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-change {
  font-size: 12px;
  margin-top: 4px;
}

.stat-change.up { color: var(--success); }
.stat-change.down { color: var(--error); }

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title i {
  color: var(--primary);
}

.card-body {
  padding: 20px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: rgba(78, 205, 196, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: var(--text-primary);
}

.activity-time {
  font-size: 12px;
  color: var(--text-muted);
}

.quick-start-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-start-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.quick-start-item:hover {
  background: rgba(78, 205, 196, 0.15);
}

.quick-start-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.quick-start-item span {
  font-size: 13px;
  color: var(--text-secondary);
}

.quick-start-item:hover span {
  color: var(--primary);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
