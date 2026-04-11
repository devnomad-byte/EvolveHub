<template>
  <div id="evolvehub-root">
    <LoginScreen v-if="!desktop.isLoggedIn" />
    <DesktopView v-else />
    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useDesktopStore } from './stores/desktop'
import LoginScreen from './components/login/LoginScreen.vue'
import DesktopView from './components/desktop/DesktopView.vue'
import ToastContainer from './components/feedback/ToastContainer.vue'

const desktop = useDesktopStore()

onMounted(() => {
  // 尝试从 localStorage 恢复会话
  desktop.restoreSession?.()
})
</script>

<style>
#evolvehub-root {
  width: 100%;
  height: 100%;
}
</style>
