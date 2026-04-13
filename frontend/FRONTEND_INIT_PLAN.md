# EvolveHub 前端初始化计划

## 📋 当前状态

- ✅ evolvehub-console 已存在（完整的前端界面原型）
- ❌ 依赖未安装（UNMET DEPENDENCY）
- ❌ 使用 Mock 数据（未对接真实后端）
- ❌ 需要配置 API 代理

## 🎯 初始化步骤

### 1. 安装依赖 ⏳
```bash
cd frontend
npm install
```

### 2. 配置 API 代理
修改 `vite.config.ts`，添加后端 API 代理：
- /api/auth/* -> http://localhost:8081
- /api/admin/* -> http://localhost:8083
- /api/v1/* -> http://localhost:8082

### 3. 创建 API 服务层
创建 `src/api/` 目录，包含：
- `auth.ts` - 认证相关 API
- `user.ts` - 用户管理 API
- `dept.ts` - 部门管理 API
- `chat.ts` - 对话 API
- `knowledge.ts` - 知识库 API

### 4. 对接真实后端
替换 Mock 数据为真实 API 调用：
- 登录接口 -> POST /api/auth/login
- 用户列表 -> GET /api/admin/users
- 部门树 -> GET /api/admin/dept/tree
- 菜单树 -> GET /api/admin/system/menus

### 5. Token 管理
使用 Sa-Token 的 Token 机制：
- 登录后保存 Token 到 localStorage
- 所有 API 请求携带 Token
- Token 过期自动跳转登录

### 6. 权限控制
根据后端返回的角色控制模块访问：
- SUPER_ADMIN: 所有模块
- ADMIN: chat, knowledge, settings
- USER: chat, knowledge, settings

## 📦 需要新增的依赖

```json
{
  "dependencies": {
    "axios": "^1.6.0",           // HTTP 客户端
    "eventsource-parser": "^1.1.0" // SSE 解析（流式输出）
  }
}
```

## 🔧 配置文件

### vite.config.ts
```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/admin': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/v1': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
```

## 📝 开发流程

1. **安装依赖** - npm install
2. **配置代理** - 修改 vite.config.ts
3. **创建 API 层** - src/api/*.ts
4. **对接登录** - 替换 Mock 登录为真实 API
5. **测试验证** - npm run dev
6. **构建打包** - npm run build

## ⏱️ 预计时间

- 安装依赖：2-3 分钟
- 配置 API：10-15 分钟
- 对接后端：30-40 分钟
- 测试验证：10-15 分钟

**总计**：约 1 小时
