<template>
  <div class="lock-screen" :class="{ 'logging-in': isLoggingIn }">
    <!-- Aurora background -->
    <div class="login-aurora">
      <div class="aurora-blob aurora-blob-1"></div>
      <div class="aurora-blob aurora-blob-2"></div>
      <div class="aurora-blob aurora-blob-3"></div>
    </div>

    <!-- Grid pattern -->
    <div class="grid-overlay"></div>

    <!-- LEFT: Brand showcase -->
    <div class="login-brand">
      <!-- Large logo -->
      <div class="brand-logo">
        <div class="brand-logo-ring"></div>
        <img src="/logo.svg" alt="EvolveHub" class="brand-logo-img" />
      </div>

      <!-- Animated brand name with gradient -->
      <div class="brand-name">
        <span
          v-for="(char, i) in 'EvolveHub'"
          :key="i"
          class="brand-char"
          :data-index="i"
        >{{ char }}</span>
      </div>

      <div class="brand-tagline">企业级 AI 中台服务平台</div>

      <!-- Time -->
      <div class="brand-time">{{ timeStr }}</div>
      <div class="brand-date">{{ dateStr }} {{ weekDayStr }}</div>
    </div>

    <!-- RIGHT: Login card -->
    <div class="login-right-panel" :class="{ fadeout: isLoggingIn }">
      <div class="login-card">
        <div class="card-title">欢迎回来</div>
        <div class="card-subtitle">登录以继续使用 EvolveHub</div>

        <!-- Username input -->
        <div class="login-field">
          <div class="input-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <input
            v-model="loginForm.username"
            class="login-input"
            :class="{ shake: showError }"
            placeholder="请输入用户名"
            @keydown.enter="focusPassword"
          />
        </div>

        <!-- Password -->
        <div class="login-field">
          <div class="input-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>
          <input
            ref="passwordInput"
            v-model="loginForm.password"
            :type="showPwd ? 'text' : 'password'"
            class="login-input"
            :class="{ shake: showError }"
            placeholder="请输入密码"
            @keydown.enter="handleLogin"
          />
          <button class="pwd-toggle" @click="showPwd = !showPwd" tabindex="-1">
            <svg v-if="showPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
              <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
              <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
        </div>

        <div v-if="showError" class="login-error">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ errorMessage }}
        </div>

        <!-- Login button -->
        <button class="login-btn" @click="handleLogin" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>登 录</span>
        </button>

        <div class="login-footer">
          <span>EvolveHub v1.0.0</span>
          <span class="sep">·</span>
          <span>AgentScope-Java 1.0.11</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useDesktopStore } from '../../stores/desktop'
import { authApi } from '../../api'

const desktop = useDesktopStore()
const passwordInput = ref<HTMLInputElement>()

const loginForm = ref({
  username: '',
  password: ''
})
const showPwd = ref(false)
const showError = ref(false)
const errorMessage = ref('')
const isLoading = ref(false)
const isLoggingIn = ref(false)

const now = ref(new Date())
let timer = 0

const timeStr = computed(() => {
  const d = now.value
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
})

const dateStr = computed(() => {
  const d = now.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const weekDayStr = computed(() => {
  const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weeks[now.value.getDay()]
})

function focusPassword() {
  passwordInput.value?.focus()
}

async function handleLogin() {
  if (isLoading.value) return

  if (!loginForm.value.username.trim()) {
    showError.value = true
    errorMessage.value = '请输入用户名'
    return
  }

  if (!loginForm.value.password.trim()) {
    showError.value = true
    errorMessage.value = '请输入密码'
    return
  }

  isLoading.value = true
  showError.value = false
  errorMessage.value = ''

  try {
    // 开发环境：直接发送明文密码
    // 生产环境：使用 HTTPS 加密传输
    const response = await authApi.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    localStorage.setItem('token', response.token)

    // 登录成功后，调用 /me 获取完整用户信息
    const userInfo = await authApi.getCurrentUser()

    const userData = {
      id: userInfo.id,
      username: userInfo.username,
      displayName: userInfo.nickname || response.nickname || response.username,
      roles: userInfo.roles?.length ? userInfo.roles : (response.roles?.length ? response.roles : ['USER']),
      deptName: userInfo.deptName || '',
      email: userInfo.email || '',
      avatar: userInfo.avatar || '👤'
    }

    localStorage.setItem('userInfo', JSON.stringify(userData))
    desktop.setUserInfo(userData)

    isLoggingIn.value = true
    setTimeout(() => {
      desktop.login()
    }, 600)

  } catch (error: any) {
    showError.value = true
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
    isLoading.value = false
  }
}

onMounted(() => {
  timer = window.setInterval(() => { now.value = new Date() }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.lock-screen {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f0f23 100%);
  overflow: hidden;
}

.login-aurora {
  position: absolute;
  inset: 0;
  overflow: hidden;
  filter: blur(80px);
  opacity: 0.6;
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  animation: float 20s infinite ease-in-out;
}

.aurora-blob-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #667eea 0%, transparent 70%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.aurora-blob-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #764ba2 0%, transparent 70%);
  bottom: -150px;
  right: -150px;
  animation-delay: -7s;
}

.aurora-blob-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #f093fb 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.9); }
  75% { transform: translate(20px, 30px) scale(1.05); }
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.login-brand {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 1;
  padding: 40px;
}

.brand-logo {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 40px;
}

.brand-logo-ring {
  position: absolute;
  inset: -10px;
  border: 2px solid rgba(255,255,255,0.2);
  border-radius: 50%;
  animation: pulse-ring 3s infinite;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 0.2; }
  50% { transform: scale(1.1); opacity: 0.4; }
}

.brand-logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 10px 30px rgba(0,0,0,0.3));
}

.brand-name {
  font-size: 56px;
  font-weight: 700;
  margin-bottom: 16px;
  display: flex;
  gap: 4px;
  position: relative;
}

/* 为每个字符设置不同的渐变 */
.brand-char:nth-child(1) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite;
}

.brand-char:nth-child(2) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.1s;
}

.brand-char:nth-child(3) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.2s;
}

.brand-char:nth-child(4) {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.3s;
}

.brand-char:nth-child(5) {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.4s;
}

.brand-char:nth-child(6) {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.5s;
}

.brand-char:nth-child(7) {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.6s;
}

.brand-char:nth-child(8) {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.7s;
}

.brand-char:nth-child(9) {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: char-bounce 2s ease-in-out infinite 0.8s;
}

@keyframes char-bounce {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.5);
  }
  10% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  30% {
    transform: translateY(-8px);
  }
  50% {
    transform: translateY(0);
  }
  70% {
    transform: translateY(-8px);
  }
  90% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(0);
  }
}

.brand-tagline {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 60px;
  font-weight: 300;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.brand-time {
  font-size: 64px;
  font-weight: 200;
  margin-bottom: 8px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  text-shadow: 0 0 30px rgba(102, 126, 234, 0.5);
}

.brand-date {
  font-size: 16px;
  opacity: 0.8;
  letter-spacing: 1px;
}

.login-right-panel {
  width: 480px;
  padding: 40px;
  z-index: 1;
  transition: opacity 0.6s, transform 0.6s;
}

.login-right-panel.fadeout {
  opacity: 0;
  transform: scale(0.95);
  pointer-events: none;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #86868b;
  margin-bottom: 32px;
}

.login-field {
  position: relative;
  margin-bottom: 20px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #86868b;
  pointer-events: none;
  transition: color 0.2s;
}

.login-input:focus ~ .input-icon {
  color: #667eea;
}

.login-input {
  width: 100%;
  height: 48px;
  padding: 0 48px 0 48px;
  border: 2px solid #e5e5e7;
  border-radius: 12px;
  font-size: 15px;
  color: #1d1d1f;
  transition: all 0.2s;
  outline: none;
  background: rgba(255, 255, 255, 0.5);
}

.login-input:focus {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.login-input::placeholder {
  color: #86868b;
}

/* 修复自动填充时的文字颜色 */
.login-input:-webkit-autofill,
.login-input:-webkit-autofill:hover,
.login-input:-webkit-autofill:focus {
  -webkit-text-fill-color: #1d1d1f;
  -webkit-box-shadow: 0 0 0px 1000px rgba(255, 255, 255, 0.5) inset;
  transition: background-color 5000s ease-in-out 0s;
}

.login-input.shake {
  animation: shake 0.4s;
  border-color: #ff453a;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}

.pwd-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #86868b;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.pwd-toggle:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.login-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff453a;
  font-size: 13px;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: rgba(255, 69, 58, 0.1);
  border-radius: 8px;
  border-left: 3px solid #ff453a;
}

.login-btn {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 32px;
  font-size: 12px;
  color: #86868b;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.sep {
  color: #d1d1d6;
}
</style>
