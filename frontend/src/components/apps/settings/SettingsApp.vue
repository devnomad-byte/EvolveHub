<template>
  <div class="settings-app">
    <!-- Left nav -->
    <div class="settings-nav">
      <div
        v-for="tab in tabs"
        :key="tab.id"
        class="nav-item"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        <span class="nav-icon">{{ tab.icon }}</span>
        <span class="nav-label">{{ tab.label }}</span>
      </div>
    </div>

    <!-- Right content -->
    <div class="settings-main">
      <!-- Personal info -->
      <div v-if="activeTab === 'profile'" class="settings-section">
        <h3>个人信息</h3>
        <div class="info-row"><span class="label">用户名:</span><span class="value">admin (不可修改)</span></div>
        <div class="info-row"><span class="label">角色:</span><span class="value">超级管理员</span></div>
        <div class="info-row"><span class="label">部门:</span><span class="value">总经办</span></div>
      </div>

      <!-- Change password -->
      <div v-if="activeTab === 'password'" class="settings-section">
        <h3>修改密码</h3>
        <div class="form-group">
          <label>旧密码</label>
          <input type="password" placeholder="输入旧密码" />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" placeholder="输入新密码" />
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input type="password" placeholder="再次输入新密码" />
        </div>
        <button class="btn btn-primary" @click="desktop.addToast('密码修改成功（原型）', 'success')">保存</button>
      </div>

      <!-- Model preference -->
      <div v-if="activeTab === 'model'" class="settings-section">
        <h3>模型偏好</h3>
        <div class="form-group">
          <label>偏好对话模型</label>
          <select class="select-input">
            <option>qwen-max</option>
            <option>gpt-4o</option>
            <option>claude-3.5-sonnet</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="desktop.addToast('偏好已保存（原型）', 'success')">保存</button>
      </div>

      <!-- Appearance -->
      <div v-if="activeTab === 'appearance'" class="settings-section">
        <h3>外观设置</h3>
        <div class="form-group">
          <label>壁纸</label>
          <div class="wallpaper-options">
            <div v-for="i in 4" :key="i" class="wallpaper-thumb" :class="{ active: selectedWallpaper === i }" @click="selectedWallpaper = i">
              <div class="wp-preview" :class="'wp-' + i"></div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>主题</label>
          <div class="theme-options">
            <label class="theme-option">
              <input type="radio" name="theme" checked /> 🌙 深色
            </label>
            <label class="theme-option">
              <input type="radio" name="theme" /> ☀️ 浅色
            </label>
          </div>
        </div>
      </div>

      <!-- About -->
      <div v-if="activeTab === 'about'" class="settings-section">
        <h3>关于</h3>
        <div class="about-content">
          <div class="about-logo">
            <div class="about-logo-icon"></div>
            <div class="about-title">
              <span v-for="(char, i) in 'EvolveHub'" :key="i" class="title-char" :style="{ animationDelay: i * 0.1 + 's' }">{{ char }}</span>
            </div>
            <div class="about-version">v1.0.0</div>
          </div>
          <p class="about-desc">企业级 AI 中台服务平台</p>
          <p class="about-desc">基于 AgentScope-Java 构建</p>
          <p class="about-link">GitHub: github.com/devnomad-byte/EvolveHub</p>
        </div>
      </div>
    </div>

    <!-- Status bar -->
    <div class="settings-statusbar">
      EvolveHub v1.0.0 · AgentScope-Java 1.0.11 · JDK 21
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useDesktopStore } from '../../../stores/desktop'

const desktop = useDesktopStore()
const activeTab = ref('profile')
const selectedWallpaper = ref(1)

const tabs = [
  { id: 'profile', icon: '👤', label: '个人信息' },
  { id: 'password', icon: '🔑', label: '修改密码' },
  { id: 'model', icon: '🤖', label: '模型偏好' },
  { id: 'appearance', icon: '🎨', label: '外观' },
  { id: 'about', icon: 'ℹ️', label: '关于' }
]
</script>

<style scoped>
.settings-app {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.settings-app > :not(.settings-statusbar) {
  display: flex;
}

.settings-nav {
  width: 180px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  padding: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  margin-bottom: 2px;
}

.nav-item:hover { background: rgba(255, 255, 255, 0.06); }
.nav-item.active {
  background: rgba(10, 132, 255, 0.15);
  border-left: 3px solid #0A84FF;
}

.nav-icon { font-size: 16px; }

.settings-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.settings-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row .label { color: var(--text-secondary); min-width: 80px; }

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-group input,
.select-input {
  width: 280px;
  height: 36px;
}

.select-input {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid transparent;
  border-radius: 8px;
  color: var(--text-primary);
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  appearance: auto;
}

.wallpaper-options {
  display: flex;
  gap: 8px;
}

.wallpaper-thumb {
  width: 80px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
}

.wallpaper-thumb.active {
  border-color: #0A84FF;
}

.wp-preview {
  width: 100%;
  height: 100%;
}

.wp-1 { background: linear-gradient(180deg, #0d1b2a, #1b2838); }
.wp-2 { background: linear-gradient(180deg, #1a0a2e, #2d1b4e); }
.wp-3 { background: linear-gradient(180deg, #0a2e1a, #1b4e2d); }
.wp-4 { background: linear-gradient(180deg, #2e1a0a, #4e2d1b); }

.theme-options {
  display: flex;
  gap: 16px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  cursor: pointer;
}

.about-content {
  text-align: center;
  padding: 20px;
}

.about-logo {
  margin-bottom: 16px;
}

.about-logo-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  margin: 0 auto 8px;
}

.about-title {
  font-size: 28px;
  font-weight: 700;
  display: flex;
  justify-content: center;
  gap: 1px;
}

.title-char {
  display: inline-block;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6, #BF5AF2, #FF6B6B, #0A84FF);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: charGradient 4s ease infinite, charFloat 3s ease-in-out infinite;
}

@keyframes charGradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes charFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.about-version {
  font-size: 13px;
  color: var(--text-secondary);
}

.about-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.about-link {
  font-size: 13px;
  color: #0A84FF;
}

.settings-statusbar {
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 30, 30, 0.5);
  font-size: 11px;
  color: var(--text-disabled);
  border-top: 1px solid var(--border-subtle);
}
</style>
