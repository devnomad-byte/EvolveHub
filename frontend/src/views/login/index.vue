<template>
  <div class="login-page">
    <div class="login-left">
      <div class="login-left-content">
        <div class="logo-container">
          <div class="logo-icon">
            <i class="fas fa-robot"></i>
          </div>
        </div>

        <div class="title-container">
          <h1 class="title">EvolveHub</h1>
          <p class="subtitle">企业级 AI 智能中台</p>
        </div>

        <p class="slogan">
          <span class="highlight">配置即用，连接一切</span><br>
          让 AI 理解并操作您的企业系统
        </p>

        <div class="login-features">
          <div class="login-feature">
            <div class="login-feature-icon">
              <i class="fas fa-plug"></i>
            </div>
            <div class="login-feature-text">
              <h4>零代码接入</h4>
              <p>通过 MCP/A2A 协议快速连接企业系统</p>
            </div>
          </div>

          <div class="login-feature">
            <div class="login-feature-icon">
              <i class="fas fa-brain"></i>
            </div>
            <div class="login-feature-text">
              <h4>智能进化</h4>
              <p>AI 越用越懂您的业务，知识持续积累</p>
            </div>
          </div>

          <div class="login-feature">
            <div class="login-feature-icon">
              <i class="fas fa-shield-halved"></i>
            </div>
            <div class="login-feature-text">
              <h4>企业级安全</h4>
              <p>私有化部署，数据完全自主可控</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-form-container">
        <div class="form-logo">
          <div class="form-logo-icon">
            <i class="fas fa-robot"></i>
          </div>
        </div>

        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">登录到 EvolveHub 控制台</p>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="input-group">
            <label class="input-label">用户名 / 邮箱</label>
            <input
              v-model="loginForm.username"
              type="text"
              class="input"
              placeholder="请输入用户名或邮箱"
              required
            />
          </div>

          <div class="input-group">
            <label class="input-label">密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              class="input"
              placeholder="请输入密码"
              required
            />
          </div>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button type="submit" class="btn btn-primary btn-lg w-full" :disabled="loading">
            <i class="fas fa-sign-in-alt"></i>
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="login-divider">
          <span>或</span>
        </div>

        <div class="quick-actions">
          <button class="btn btn-secondary" @click="handleGuestLogin">
            <i class="fas fa-eye"></i>
            游客体验
          </button>
          <button class="btn btn-secondary" @click="handleDemoLogin">
            <i class="fas fa-key"></i>
            演示账号
          </button>
        </div>

        <div class="login-footer">
          <p>还没有账号？ <a href="#">立即注册</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return

  loading.value = true
  try {
    await authStore.login(loginForm)
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = async () => {
  loading.value = true
  try {
    await authStore.login({ username: 'guest', password: 'guest' })
  } finally {
    loading.value = false
  }
}

const handleDemoLogin = async () => {
  loading.value = true
  try {
    await authStore.login({ username: 'admin', password: 'admin' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import '@/styles/global.css';

.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--gradient-dark);
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(69, 183, 209, 0.05) 100%);
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 30%, rgba(78, 205, 196, 0.08) 0%, transparent 50%);
  animation: floatBg 20s ease-in-out infinite;
}

@keyframes floatBg {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(2%, 2%) rotate(5deg); }
}

.login-left-content {
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.logo-container {
  text-align: center;
  margin: 20px 0;
}

.logo-icon {
  width: 140px;
  height: 140px;
  margin: 0 auto;
  background: var(--gradient-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  box-shadow: var(--shadow-glow);
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 20px rgba(78, 205, 196, 0.3); }
  50% { box-shadow: 0 0 40px rgba(78, 205, 196, 0.6); }
}

.title-container {
  text-align: center;
  margin: 24px 0;
}

.title {
  font-size: 42px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.subtitle {
  font-size: 16px;
  color: var(--accent);
  margin-top: 8px;
}

.slogan {
  font-size: 15px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.8;
  margin-bottom: 32px;
}

.highlight {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-features {
  margin-top: 40px;
}

.login-feature {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.login-feature-icon {
  width: 44px;
  height: 44px;
  background: rgba(78, 205, 196, 0.15);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--primary);
}

.login-feature-text h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.login-feature-text p {
  font-size: 13px;
  color: var(--text-muted);
}

.login-right {
  width: 480px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  background: var(--bg-darker);
}

.login-form-container {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}

.form-logo {
  text-align: center;
  margin-bottom: 32px;
}

.form-logo-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto;
  background: var(--gradient-primary);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  box-shadow: var(--shadow-glow);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  text-align: center;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 32px;
  text-align: center;
}

.login-form {
  margin-bottom: 24px;
}

.input-group {
  margin-bottom: 16px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.input {
  width: 100%;
  padding: 12px 14px;
  background: var(--bg-dark);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.15);
  outline: none;
}

.input::placeholder {
  color: var(--text-muted);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
}

.remember-me input {
  width: 16px;
  height: 16px;
  accent-color: var(--primary);
}

.forgot-link {
  font-size: 13px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-sm);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: var(--bg-card);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  flex: 1;
}

.btn-secondary:hover {
  background: var(--bg-hover);
  border-color: var(--border-light);
}

.btn-lg {
  padding: 14px 28px;
  font-size: 16px;
}

.w-full {
  width: 100%;
}

.login-divider {
  text-align: center;
  margin: 24px 0;
  position: relative;
}

.login-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-color);
}

.login-divider span {
  background: var(--bg-darker);
  padding: 0 16px;
  position: relative;
  color: var(--text-muted);
  font-size: 13px;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.login-footer {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 32px;
}

@media (max-width: 992px) {
  .login-left {
    display: none;
  }
  .login-right {
    width: 100%;
  }
}
</style>
