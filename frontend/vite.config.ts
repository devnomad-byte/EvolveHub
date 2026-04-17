import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  build: {
    // Vite 8 Oxc minifier breaks Monaco workers; use esbuild until upstream fix
    minify: 'esbuild'
  },
  server: {
    host: '0.0.0.0',
    port: 5178,
    proxy: {
      // 认证服务 -> 8081
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 后台管理服务 -> 8083 (包含 mcp-config, skill-config, user, model 等)
      '/api/admin': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      // AI 平台服务 -> 8082
      '/api/v1': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      // 用户界面菜单
      '/api/app': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  },
})
