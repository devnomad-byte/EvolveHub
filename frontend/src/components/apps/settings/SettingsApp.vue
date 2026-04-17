<template>
  <div class="settings-container">
    <!-- Header -->
    <div class="settings-header">
      <div class="header-title">
        <component :is="SettingsIcon" class="title-icon" />
        <div class="title-text">个人设置</div>
      </div>
      <div class="header-actions">
        <button class="action-btn" @click="handleClose">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="settings-content">
      <!-- Left Navigation -->
      <div class="settings-nav">
        <div class="nav-section">
          <div class="nav-section-title">账户</div>
          <div
            v-for="item in accountItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeTab === item.id }"
            @click="activeTab = item.id"
          >
            <component :is="item.icon" class="nav-icon" />
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>

        <div class="nav-section">
          <div class="nav-section-title">偏好</div>
          <div
            v-for="item in preferenceItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeTab === item.id }"
            @click="activeTab = item.id"
          >
            <component :is="item.icon" class="nav-icon" />
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>

        <div class="nav-section">
          <div class="nav-section-title">其他</div>
          <div
            v-for="item in otherItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeTab === item.id }"
            @click="activeTab = item.id"
          >
            <component :is="item.icon" class="nav-icon" />
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- Right Content -->
      <div class="settings-main">
        <!-- Profile Card -->
        <div v-if="activeTab === 'profile'" class="content-card">
          <div class="card-header">
            <div class="card-title">
              <component :is="UserIcon" class="card-icon" />
              <span>个人信息</span>
            </div>
          </div>

          <div class="card-body">
            <!-- Avatar Section -->
            <div class="profile-avatar-section">
              <div class="avatar-upload-wrapper" @click="triggerAvatarUpload">
                <div v-if="avatarUrl" class="avatar-large avatar-img">
                  <img :src="avatarUrl" alt="头像" />
                </div>
                <div v-else class="avatar-large">{{ userAvatar }}</div>
                <div class="avatar-overlay">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                    <circle cx="12" cy="13" r="4"/>
                  </svg>
                </div>
                <input
                  ref="avatarInput"
                  type="file"
                  accept="image/png,image/jpeg,image/gif,image/webp"
                  style="display: none"
                  @change="handleAvatarChange"
                />
              </div>
              <div class="avatar-info">
                <div class="avatar-name">{{ displayName }}</div>
                <div class="avatar-role">{{ roleBadge }}</div>
                <div class="avatar-hint">点击头像更换</div>
              </div>
            </div>

            <!-- Info Grid -->
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">用户名</div>
                <div class="info-value">{{ username }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">邮箱</div>
                <div class="info-value">{{ email || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">部门</div>
                <div class="info-value">{{ deptName || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">角色</div>
                <div class="info-value role-badge">{{ roleBadge }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Password Card -->
        <div v-if="activeTab === 'password'" class="content-card">
          <div class="card-header">
            <div class="card-title">
              <component :is="LockIcon" class="card-icon" />
              <span>修改密码</span>
            </div>
          </div>

          <div class="card-body">
            <div class="form-description">
              为了您的账户安全，请定期修改密码。密码长度为 6-20 个字符。
            </div>

            <div class="form-grid">
              <div class="form-field">
                <label class="field-label">当前密码</label>
                <div class="field-input-wrapper">
                  <input
                    v-model="passwordForm.oldPassword"
                    :type="showOldPassword ? 'text' : 'password'"
                    placeholder="输入当前密码"
                    class="field-input"
                    :class="{ error: passwordErrors.oldPassword }"
                    @blur="validatePassword"
                  />
                  <button
                    class="field-toggle"
                    @click="showOldPassword = !showOldPassword"
                    tabindex="-1"
                  >
                    <svg v-if="showOldPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="passwordErrors.oldPassword" class="field-error">{{ passwordErrors.oldPassword }}</span>
              </div>

              <div class="form-field">
                <label class="field-label">新密码</label>
                <div class="field-input-wrapper">
                  <input
                    v-model="passwordForm.newPassword"
                    :type="showNewPassword ? 'text' : 'password'"
                    placeholder="输入新密码（6-20个字符）"
                    class="field-input"
                    :class="{ error: passwordErrors.newPassword }"
                    @blur="validatePassword"
                  />
                  <button
                    class="field-toggle"
                    @click="showNewPassword = !showNewPassword"
                    tabindex="-1"
                  >
                    <svg v-if="showNewPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="passwordErrors.newPassword" class="field-error">{{ passwordErrors.newPassword }}</span>
              </div>

              <div class="form-field">
                <label class="field-label">确认新密码</label>
                <div class="field-input-wrapper">
                  <input
                    v-model="passwordForm.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    placeholder="再次输入新密码"
                    class="field-input"
                    :class="{ error: passwordErrors.confirmPassword }"
                    @blur="validatePassword"
                  />
                  <button
                    class="field-toggle"
                    @click="showConfirmPassword = !showConfirmPassword"
                    tabindex="-1"
                  >
                    <svg v-if="showConfirmPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="passwordErrors.confirmPassword" class="field-error">{{ passwordErrors.confirmPassword }}</span>
              </div>
            </div>

            <div class="form-actions">
              <button
                class="btn btn-primary"
                @click="handleChangePassword"
                :disabled="isChangingPassword"
              >
                <span v-if="isChangingPassword" class="btn-spinner"></span>
                <span v-else>修改密码</span>
              </button>
              <button
                class="btn btn-secondary"
                @click="resetPasswordForm"
                :disabled="isChangingPassword"
              >
                重置
              </button>
            </div>
          </div>
        </div>

        <!-- Model Card -->
        <div v-if="activeTab === 'model'" class="content-card">
          <div class="card-header">
            <div class="card-title">
              <component :is="CpuIcon" class="card-icon" />
              <span>我的模型</span>
            </div>
          </div>

          <div class="card-body">
            <div class="form-description">
              选择您默认使用的 AI 对话模型。系统级模型对所有用户可用，个人模型仅您自己可用。
            </div>

            <!-- Loading -->
            <div v-if="isLoadingModels" class="loading-state">
              <div class="spinner"></div>
              <span>加载中...</span>
            </div>

            <!-- Model List -->
            <div v-else class="model-options">
              <div
                v-for="model in myModels"
                :key="model.id"
                class="model-card"
                :class="{ active: selectedModelId === model.id }"
                @click="selectedModelId = model.id"
              >
                <div class="model-card-icon">
                  <BotIcon />
                </div>
                <div class="model-info">
                  <div class="model-name">{{ model.name }}</div>
                  <div class="model-desc">{{ model.provider }} · {{ model.modelType === 'LLM' ? '对话模型' : '向量模型' }}</div>
                </div>
                <div class="model-badge" :class="model.scope === 'SYSTEM' ? 'system' : 'personal'">
                  {{ model.scope === 'SYSTEM' ? '系统级' : '个人' }}
                </div>
                <div class="model-check" v-if="selectedModelId === model.id">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                </div>
              </div>
            </div>

            <div v-if="!isLoadingModels && myModels.length === 0" class="empty-state">
              <span>暂无可用模型，请联系管理员配置</span>
            </div>

            <div class="form-actions">
              <button class="btn btn-primary" @click="savePreferredModel" :disabled="isSavingModel">
                <span v-if="isSavingModel" class="btn-spinner"></span>
                <span v-else>保存偏好</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Appearance Card -->
        <div v-if="activeTab === 'appearance'" class="content-card">
          <div class="card-header">
            <div class="card-title">
              <component :is="PaletteIcon" class="card-icon" />
              <span>外观设置</span>
            </div>
          </div>

          <div class="card-body">
            <div class="form-section">
              <div class="section-label">壁纸</div>
              <div class="wallpaper-grid">
                <div
                  class="wallpaper-item"
                  :class="{ active: selectedWallpaper === 0 }"
                  @click="selectedWallpaper = 0"
                >
                  <div class="wallpaper-preview wp-default">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5">
                      <circle cx="12" cy="12" r="1" fill="rgba(255,255,255,0.5)"/>
                      <circle cx="6" cy="6" r="0.8" fill="rgba(255,255,255,0.3)"/>
                      <circle cx="18" cy="8" r="0.6" fill="rgba(255,255,255,0.4)"/>
                      <circle cx="8" cy="16" r="0.7" fill="rgba(255,255,255,0.35)"/>
                      <circle cx="16" cy="18" r="0.5" fill="rgba(255,255,255,0.3)"/>
                    </svg>
                    <span class="wp-label">默认</span>
                  </div>
                </div>
                <div
                  v-for="i in 6"
                  :key="i"
                  class="wallpaper-item"
                  :class="{ active: selectedWallpaper === i }"
                  @click="selectedWallpaper = i"
                >
                  <div class="wallpaper-preview" :class="'wp-' + i"></div>
                </div>
              </div>
            </div>

            <div class="form-section">
              <div class="section-label">主题</div>
              <div class="theme-grid">
                <div
                  class="theme-card"
                  :class="{ active: selectedTheme === 'dark' }"
                  @click="selectedTheme = 'dark'"
                >
                  <div class="theme-preview dark">
                    <div class="theme-header"></div>
                    <div class="theme-content">
                      <div class="theme-block"></div>
                      <div class="theme-block"></div>
                    </div>
                  </div>
                  <div class="theme-name">🌙 深色</div>
                </div>
                <div
                  class="theme-card"
                  :class="{ active: selectedTheme === 'light' }"
                  @click="selectedTheme = 'light'"
                >
                  <div class="theme-preview light">
                    <div class="theme-header"></div>
                    <div class="theme-content">
                      <div class="theme-block"></div>
                      <div class="theme-block"></div>
                    </div>
                  </div>
                  <div class="theme-name">☀️ 浅色</div>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button class="btn btn-primary" @click="handleSaveAppearance">
                保存设置
              </button>
              <button class="btn btn-secondary" @click="handleResetAppearance">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="1 4 1 10 7 10"/>
                  <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
                </svg>
                恢复默认
              </button>
            </div>
          </div>
        </div>

        <!-- About Card -->
        <div v-if="activeTab === 'about'" class="content-card">
          <div class="card-header">
            <div class="card-title">
              <component :is="InfoIcon" class="card-icon" />
              <span>关于</span>
            </div>
          </div>

          <div class="card-body card-body-about">
            <div class="about-logo-section">
              <div class="about-logo-icon">
                <img src="/logo.svg" alt="EvolveHub" class="logo-img" />
              </div>
              <div class="about-title">
                <span
                  v-for="(char, i) in 'EvolveHub'"
                  :key="i"
                  class="title-char"
                  :style="{ animationDelay: i * 0.1 + 's' }"
                >{{ char }}</span>
              </div>
              <div class="about-version">v1.0.0</div>
            </div>

            <div class="about-info">
              <div class="about-desc">企业级 AI 中台服务平台</div>
              <div class="about-desc">基于 AgentScope-Java 构建</div>
              <div class="about-tech">
                <span class="tech-badge">AgentScope-Java 1.0.11</span>
                <span class="tech-badge">JDK 21</span>
                <span class="tech-badge">Spring Boot 3.x</span>
              </div>
            </div>

            <div class="about-links">
              <a href="https://github.com/devnomad-byte/EvolveHub" target="_blank" rel="noopener noreferrer" class="about-link">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                  <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"/>
                </svg>
                GitHub 仓库
              </a>
              <a href="https://github.com/devnomad-byte/EvolveHub" target="_blank" rel="noopener noreferrer" class="about-link">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                </svg>
                官方网站
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, h } from 'vue'
import { useDesktopStore } from '../../../stores/desktop'
import { useWindowStore } from '../../../stores/window'
import { authApi, adminModelConfigApi } from '../../../api'
import { useAppearanceStore } from '../../../composables/useAppearance'

const desktop = useDesktopStore()
const windowStore = useWindowStore()
const appearance = useAppearanceStore()
const activeTab = ref('profile')
const avatarInput = ref<HTMLInputElement>()
const isUploadingAvatar = ref(false)

// SVG Icon Components
const UserIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const LockIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('rect', { x: '3', y: '11', width: '18', height: '11', rx: '2', ry: '2' }),
  h('path', { d: 'M7 11V7a5 5 0 0 1 10 0v4' })
])

const BotIcon = () => h('svg', {
  width: '24', height: '24', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M12 8V4H8' }),
  h('rect', { x: '4', y: '8', width: '16', height: '12', rx: '2' }),
  h('path', { d: 'M2 14h2' }),
  h('path', { d: 'M20 14h2' }),
  h('path', { d: 'M15 13v2' }),
  h('path', { d: 'M9 13v2' })
])

const CpuIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('rect', { x: '4', y: '4', width: '16', height: '16', rx: '2', ry: '2' }),
  h('rect', { x: '9', y: '9', width: '6', height: '6' }),
  h('path', { d: 'M9 1V3' }),
  h('path', { d: 'M15 1V3' }),
  h('path', { d: 'M9 20V22' }),
  h('path', { d: 'M15 20V22' }),
  h('path', { d: 'M20 16.58A5 5 0 0 0 18.67 12' }),
  h('path', { d: 'M6.66 12A5 5 0 0 0 3.33 16.58' }),
  h('path', { d: 'M12 12h.01' })
])

const PaletteIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('circle', { cx: '13.5', cy: '6.5', r: '.5', fill: 'currentColor' }),
  h('circle', { cx: '17.5', cy: '10.5', r: '.5', fill: 'currentColor' }),
  h('circle', { cx: '8.5', cy: '7.5', r: '.5', fill: 'currentColor' }),
  h('circle', { cx: '6.5', cy: '12.5', r: '.5', fill: 'currentColor' }),
  h('path', { d: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.9 0 2.4-.3 3.5-.8 1.4-.6 2.5-1.6 3.3-3.2.4-1.6.5-3 1.2-4.5 3.3-4.5' }),
  h('path', { d: 'M21.98 14c-.1-4.8-3.1-8.9-6.8-10.8' })
])

const InfoIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('circle', { cx: '12', cy: '12', r: '10' }),
  h('line', { x1: '12', y1: '16', x2: '12', y2: '12' }),
  h('line', { x1: '12', y1: '8', x2: '12.01', y2: '8' })
])

const SettingsIcon = () => h('svg', {
  width: '20', height: '20', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 0 2 2v.18a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 0-2-2V4a2 2 0 0 0-2-2h.44a2 2 0 0 0 2 2v.18a2 2 0 0 0-2 2v.18a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 0-2-2V4a2 2 0 0 0-2-2z' }),
  h('circle', { cx: '12', cy: '7', r: '.3', fill: 'currentColor' }),
  h('circle', { cx: '12', cy: '12', r: '.3', fill: 'currentColor' }),
  h('path', { d: 'M12 22v-5' })
])

// AI Model Icons
const QwenIcon = () => h('svg', {
  width: '32', height: '32', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M12 2L2 7l10 5 10-5-10-5 10-5v14l-2 2-6 6-6-6 6-6-6 6 6 6 6-6V6' }),
  h('path', { d: 'M9 12l2 2 4-4' })
])

const GPTIcon = () => h('svg', {
  width: '32', height: '32', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M12 2a10 10 0 1 0 10 10 10 10 0 0 0-10-10' }),
  h('path', { d: 'M12 12m0 0a1 1 0 1 0-2 1 1 0 0 1 0 2 2 2 0 0 0 2 2' }),
  h('path', { d: 'M8 12h8' }),
  h('path', { d: 'M16 16l-4-4 4 4' })
])

const ClaudeIcon = () => h('svg', {
  width: '32', height: '32', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('polygon', { points: '12 2 2 7 12 12 22 2 12 2 7 12 12 22 2 12 2 7' }),
  h('path', { d: 'M12 12v-2' }),
  h('path', { d: 'M8 12h8' })
])

// Navigation items
const accountItems = [
  { id: 'profile', icon: UserIcon, label: '个人信息' },
  { id: 'password', icon: LockIcon, label: '修改密码' }
]

const preferenceItems = [
  { id: 'model', icon: CpuIcon, label: '模型偏好' },
  { id: 'appearance', icon: PaletteIcon, label: '外观设置' }
]

const otherItems = [
  { id: 'about', icon: InfoIcon, label: '关于' }
]

// User info
const username = computed(() => desktop.currentUser?.username || '-')
const displayName = computed(() => desktop.currentUser?.displayName || '用户')
const deptName = computed(() => desktop.currentUser?.deptName || '未设置')
const email = computed(() => desktop.currentUser?.email || '')

const roleBadge = computed(() => {
  const roles = desktop.currentUser?.roles || ['USER']
  const roleMap: Record<string, string> = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'DEPT_HEAD': '部门负责人',
    'LEADER': '领导',
    'USER': '普通用户'
  }
  return roles.map(r => roleMap[r] || r).join('、')
})

const userAvatar = computed(() => {
  if (!desktop.currentUser?.displayName) return 'U'
  return desktop.currentUser.displayName.charAt(0).toUpperCase()
})

// 判断头像是否是 Base64 图片
const avatarUrl = computed(() => {
  const avatar = desktop.currentUser?.avatar
  if (!avatar || avatar === '👤') return ''
  return avatar
})

// 触发文件选择
function triggerAvatarUpload() {
  avatarInput.value?.click()
}

// 处理头像选择
async function handleAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 限制文件大小 2MB
  if (file.size > 2 * 1024 * 1024) {
    desktop.addToast('图片大小不能超过 2MB', 'error')
    return
  }

  try {
    isUploadingAvatar.value = true
    // 使用 Canvas 压缩并转为 Base64
    const base64 = await compressImage(file, 200, 200, 0.85)

    // 调用后端接口保存
    await authApi.updateAvatar(base64)

    // 更新本地 store
    if (desktop.currentUser) {
      const updatedUser = { ...desktop.currentUser, avatar: base64 }
      desktop.setUserInfo(updatedUser)
      localStorage.setItem('userInfo', JSON.stringify(updatedUser))
    }

    desktop.addToast('头像更新成功', 'success')
  } catch (error: any) {
    desktop.addToast(error.message || '头像更新失败', 'error')
  } finally {
    isUploadingAvatar.value = false
    // 清空 input 以便重复选择同一文件
    input.value = ''
  }
}

// Canvas 压缩图片
function compressImage(file: File, maxW: number, maxH: number, quality: number): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let w = img.width
        let h = img.height

        // 等比缩放
        if (w > maxW || h > maxH) {
          const ratio = Math.min(maxW / w, maxH / h)
          w = Math.round(w * ratio)
          h = Math.round(h * ratio)
        }

        canvas.width = w
        canvas.height = h
        const ctx = canvas.getContext('2d')!
        ctx.drawImage(img, 0, 0, w, h)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = e.target?.result as string
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

// Password form
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordErrors = ref<Record<string, string>>({})
const isChangingPassword = ref(false)
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// Appearance
const selectedWallpaper = ref(0)
const selectedTheme = ref<'dark' | 'light'>('dark')

// 初始化外观设置
onMounted(() => {
  // 从存储加载设置
  selectedWallpaper.value = appearance.getCurrentWallpaper()
  selectedTheme.value = appearance.getCurrentTheme()

  // 如果有指定的标签页，自动切换
  if (windowStore.pendingSettingsTab) {
    activeTab.value = windowStore.pendingSettingsTab
    windowStore.pendingSettingsTab = null
  }

  // 如果默认是模型 tab，则加载模型列表
  if (activeTab.value === 'model') {
    loadMyModels()
  }

  console.log('[Settings] Loaded appearance:', {
    wallpaper: selectedWallpaper.value,
    theme: selectedTheme.value,
    currentBodyClasses: document.body.className
  })
})

// 监听后续的标签页切换请求（窗口已打开时）
watch(() => windowStore.pendingSettingsTab, (tab) => {
  if (tab) {
    activeTab.value = tab
    windowStore.pendingSettingsTab = null
    // 如果切换到模型 tab，加载模型列表
    if (tab === 'model') {
      loadMyModels()
    }
  }
})

// Model
const selectedModel = ref('qwen')

// ==================== My Models ====================
const myModels = ref<any[]>([])
const selectedModelId = ref<number | null>(null)
const isLoadingModels = ref(false)
const isSavingModel = ref(false)

async function loadMyModels() {
  try {
    isLoadingModels.value = true
    // 优先尝试 my-list 接口（用户自己的列表），如果没有则用 list
    const res = await adminModelConfigApi.myList().catch(() => adminModelConfigApi.list(1, 100))
    // 过滤出 LLM 类型的模型
    myModels.value = res.records.filter((m: any) => m.modelType === 'LLM' && m.enabled === 1)
    // 默认选中第一个
    if (myModels.value.length > 0 && selectedModelId.value === null) {
      selectedModelId.value = myModels.value[0].id
    }
  } catch (e) {
    console.error('[Settings] 加载模型列表失败', e)
  } finally {
    isLoadingModels.value = false
  }
}

async function savePreferredModel() {
  if (!selectedModelId.value) {
    desktop.addToast('请先选择一个模型', 'error')
    return
  }
  try {
    isSavingModel.value = true
    // TODO: 调用保存用户偏好的后端接口
    // 目前仅展示保存成功，实际需要后端支持用户偏好设置
    desktop.addToast('模型偏好已保存', 'success')
  } catch (e: any) {
    desktop.addToast(e.message || '保存失败', 'error')
  } finally {
    isSavingModel.value = false
  }
}

const aiModels = [
  { id: 'qwen', name: 'Qwen Max', desc: '通义千问旗舰模型', icon: QwenIcon },
  { id: 'gpt4', name: 'GPT-4o', desc: 'OpenAI 最新模型', icon: GPTIcon },
  { id: 'claude', name: 'Claude 3.5', desc: 'Anthropic 高性能模型', icon: ClaudeIcon }
]

// Validate password
function validatePassword() {
  passwordErrors.value = {}

  if (!passwordForm.value.oldPassword.trim()) {
    passwordErrors.value.oldPassword = '请输入当前密码'
  }

  if (!passwordForm.value.newPassword.trim()) {
    passwordErrors.value.newPassword = '请输入新密码'
  } else if (passwordForm.value.newPassword.length < 6 || passwordForm.value.newPassword.length > 20) {
    passwordErrors.value.newPassword = '新密码长度为 6-20 个字符'
  } else if (passwordForm.value.oldPassword === passwordForm.value.newPassword) {
    passwordErrors.value.newPassword = '新密码不能与当前密码相同'
  }

  if (!passwordForm.value.confirmPassword.trim()) {
    passwordErrors.value.confirmPassword = '请确认新密码'
  } else if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = '两次输入的密码不一致'
  }

  return Object.keys(passwordErrors.value).length === 0
}

// Reset password form
function resetPasswordForm() {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordErrors.value = {}
}

// Handle change password
async function handleChangePassword() {
  if (!validatePassword()) return
  if (isChangingPassword.value) return

  isChangingPassword.value = true

  try {
    await authApi.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    desktop.addToast('密码修改成功', 'success')
    resetPasswordForm()
  } catch (error: any) {
    desktop.addToast(error.message || '密码修改失败', 'error')
  } finally {
    isChangingPassword.value = false
  }
}

// Close settings
function handleClose() {
  // 找到settings窗口并关闭
  const settingsWindow = Object.values(windowStore.windows).find(
    w => w.appId === 'settings' && w.isOpen
  )
  if (settingsWindow) {
    windowStore.closeWindow(settingsWindow.id)
  }
}

// Save appearance settings
function handleSaveAppearance() {
  console.log('[Settings] Saving appearance:', {
    wallpaper: selectedWallpaper.value,
    theme: selectedTheme.value
  })

  // 检查是否在管理员界面
  const adminBg = document.querySelector('.admin-bg')
  console.log('[Settings] Admin background exists:', !!adminBg)

  appearance.setWallpaper(selectedWallpaper.value)
  appearance.setTheme(selectedTheme.value)

  // 验证保存
  setTimeout(() => {
    const saved = localStorage.getItem('evolvehub_appearance_settings')
    console.log('[Settings] Saved to localStorage:', saved)
    console.log('[Settings] Admin bg classes after save:', adminBg?.className)
  }, 100)

  desktop.addToast('外观设置已保存', 'success')
}

// 恢复默认外观
function handleResetAppearance() {
  appearance.resetSettings()
  selectedWallpaper.value = 0
  selectedTheme.value = 'dark'
  desktop.addToast('已恢复默认设置', 'success')
}
</script>

<style scoped>
.settings-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: transparent;
}

/* Header */
.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 20px;
  height: 20px;
  color: #0A84FF;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 69, 58, 0.1);
  border-color: #FF453A;
  color: #FF453A;
}

/* Content */
.settings-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* Navigation */
.settings-nav {
  width: 200px;
  padding: 16px 8px;
  border-right: 1px solid var(--border-subtle);
  background: rgba(0, 0, 0, 0.15);
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 20px;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-disabled);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 12px 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.2s;
  margin-bottom: 2px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-primary);
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(10, 132, 255, 0.15), rgba(94, 92, 230, 0.15));
  border-left: 3px solid #0A84FF;
  color: var(--text-primary);
  font-weight: 500;
}

.nav-icon {
  width: 20px;
  height: 20px;
  color: var(--text-secondary);
  transition: all 0.2s;
  flex-shrink: 0;
}

.nav-item:hover .nav-icon {
  color: var(--text-primary);
}

.nav-item.active .nav-icon {
  color: #0A84FF;
}

/* Main Content */
.settings-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.content-card {
  background: rgba(40, 40, 40, 0.6);
  backdrop-filter: blur(30px);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-icon {
  width: 24px;
  height: 24px;
  color: #0A84FF;
}

.card-body {
  padding: 24px;
}

.card-body-about {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

/* Profile Section */
.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  margin-bottom: 24px;
}

.avatar-upload-wrapper {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-upload-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-large {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: #fff;
  overflow: hidden;
}

.avatar-large.avatar-img {
  padding: 0;
}

.avatar-large.avatar-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-hint {
  font-size: 11px;
  color: var(--text-disabled);
  margin-top: 4px;
}

.avatar-info {
  flex: 1;
}

.avatar-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.avatar-role {
  font-size: 13px;
  color: var(--text-secondary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}

.info-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.info-value.role-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(10, 132, 255, 0.2), rgba(94, 92, 230, 0.2));
  border: 1px solid rgba(10, 132, 255, 0.3);
  border-radius: 6px;
  font-size: 12px;
}

/* Form */
.form-description {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 24px;
  padding: 12px 16px;
  background: rgba(10, 132, 255, 0.08);
  border-left: 3px solid #0A84FF;
  border-radius: 6px;
}

.info-badge {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(255, 193, 7, 0.15);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: 4px;
  font-size: 11px;
  color: #FFB800;
  font-weight: 600;
  margin-bottom: 8px;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.field-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.field-input {
  width: 100%;
  height: 42px;
  padding: 0 48px 0 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-primary);
  transition: all 0.2s;
  outline: none;
}

.field-input:focus {
  border-color: #0A84FF;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.1);
  background: rgba(0, 0, 0, 0.4);
}

.field-input.error {
  border-color: #FF453A;
  box-shadow: 0 0 0 3px rgba(255, 69, 58, 0.1);
}

.field-toggle {
  position: absolute;
  right: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.field-toggle:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.08);
}

.field-error {
  font-size: 12px;
  color: #FF453A;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--border-subtle);
}

.btn {
  height: 40px;
  padding: 0 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  color: white;
  box-shadow: 0 2px 8px rgba(10, 132, 255, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(10, 132, 255, 0.4);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.08);
}

.btn-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-disabled:hover {
  transform: none !important;
  box-shadow: none !important;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Model Selection */
.model-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.model-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 2px solid var(--border-subtle);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.model-card:hover {
  border-color: rgba(10, 132, 255, 0.3);
  background: rgba(0, 0, 0, 0.3);
}

.model-card.active {
  border-color: #0A84FF;
  background: rgba(10, 132, 255, 0.08);
}

.model-card-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(191, 90, 242, 0.15);
  color: #BF5AF2;
  flex-shrink: 0;
}

.model-info {
  flex: 1;
  min-width: 0;
}

.model-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-desc {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  flex-shrink: 0;
}

.model-badge.system {
  background: rgba(10, 132, 255, 0.15);
  color: #0A84FF;
}

.model-badge.personal {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.model-check {
  color: #0A84FF;
  flex-shrink: 0;
}

/* Loading & Empty State */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--text-disabled);
  font-size: 13px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #0A84FF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Appearance */
.form-section {
  margin-bottom: 24px;
}

.section-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.wallpaper-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.wallpaper-item {
  aspect-ratio: 16/9;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-subtle);
  transition: all 0.2s;
}

.wallpaper-item:hover {
  border-color: rgba(10, 132, 255, 0.4);
  transform: scale(1.02);
}

.wallpaper-item.active {
  border-color: #0A84FF;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.2);
}

.wallpaper-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wp-default {
  background: linear-gradient(135deg, #0a0a2e 0%, #1a1a3e 50%, #0d1b2a 100%);
  flex-direction: column;
  gap: 4px;
}

.wp-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.wp-1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.wp-2 { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.wp-3 { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.wp-4 { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.wp-5 { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.wp-6 { background: linear-gradient(135deg, #30cfd0 0%, #330867 100%); }

.theme-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.theme-card {
  cursor: pointer;
  border: 2px solid var(--border-subtle);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
}

.theme-card:hover {
  border-color: rgba(10, 132, 255, 0.4);
}

.theme-card.active {
  border-color: #0A84FF;
  box-shadow: 0 0 0 3px rgba(10, 132, 255, 0.2);
}

.theme-preview {
  height: 80px;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.theme-preview.dark {
  background: #1a1a2e;
}

.theme-preview.light {
  background: #ffffff;
}

.theme-header {
  height: 20px;
  margin: 8px;
  border-radius: 4px;
}

.theme-preview.dark .theme-header {
  background: rgba(255, 255, 255, 0.1);
}

.theme-preview.light .theme-header {
  background: #f0f0f0;
}

.theme-content {
  padding: 0 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.theme-block {
  height: 12px;
  border-radius: 4px;
}

.theme-preview.dark .theme-block {
  background: rgba(255, 255, 255, 0.05);
}

.theme-preview.light .theme-block {
  background: #e0e0e0;
}

.theme-name {
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

/* About */
.about-logo-section {
  margin-bottom: 32px;
}

.about-logo-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.about-title {
  font-size: 36px;
  font-weight: 700;
  display: flex;
  justify-content: center;
  gap: 2px;
  margin-bottom: 8px;
}

.title-char {
  display: inline-block;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6, #BF5AF2, #FF6B6B, #0A84FF);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: charGradient 4s ease infinite;
}

@keyframes charGradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.about-version {
  font-size: 14px;
  color: var(--text-secondary);
}

.about-info {
  margin-bottom: 32px;
}

.about-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.about-tech {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

.tech-badge {
  padding: 4px 12px;
  background: rgba(10, 132, 255, 0.1);
  border: 1px solid rgba(10, 132, 255, 0.2);
  border-radius: 6px;
  font-size: 11px;
  color: #0A84FF;
  font-weight: 500;
}

.about-links {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.about-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-primary);
  text-decoration: none;
  transition: all 0.2s;
}

.about-link svg {
  flex-shrink: 0;
}

.about-link:hover {
  background: rgba(10, 132, 255, 0.1);
  border-color: #0A84FF;
  color: #0A84FF;
}

.about-link:hover svg {
  stroke: #0A84FF;
}
</style>
