<template>
  <div id="evolvehub-root">
    <LoginScreen v-if="!desktop.isLoggedIn" />
    <DesktopView v-else />
    <ToastContainer />
    <ConfirmDialog />
    <AlertDialog />
  </div>
</template>

<script setup lang="ts">
import { useDesktopStore } from './stores/desktop'
import LoginScreen from './components/login/LoginScreen.vue'
import DesktopView from './components/desktop/DesktopView.vue'
import ToastContainer from './components/feedback/ToastContainer.vue'
import ConfirmDialog from './components/common/ConfirmDialog.vue'
import AlertDialog from './components/common/AlertDialog.vue'

const desktop = useDesktopStore()
</script>

<style>
#evolvehub-root {
  width: 100%;
  height: 100%;
}
</style>
