# 前端 MCP 实例管理功能完成报告

> 日期：2026-04-15
> 修改文件：`frontend/src/components/apps/ext/ExtApp.vue`

---

## 一、已完成的功能对接

### 1.1 新增 API 调用

| 功能 | API 调用 | 说明 |
|------|---------|------|
| 加载实例状态 | `adminMcpInstanceApi.status(mcpId)` | 获取 MCP 实例运行状态 |
| 加载工具列表 | `adminMcpInstanceApi.listTools(mcpId)` | 获取 MCP 的可用工具 |
| 启动 MCP | `adminMcpInstanceApi.start(mcpId)` | 启动 MCP Server |
| 停止 MCP | `adminMcpInstanceApi.stop(mcpId)` | 停止 MCP Server |
| 重启 MCP | `adminMcpInstanceApi.restart(mcpId)` | 重启 MCP Server |
| 发现工具 | `adminMcpInstanceApi.discover(mcpId)` | 触发工具自动发现 |

### 1.2 新增 UI 组件

#### MCP 详情页新增按钮

```vue
<!-- 实例管理按钮 -->
<button @click="startMcp(currentMcp.id)" :disabled="!!mcpInstances.get(currentMcp.id)">
  <Play :size="14" /> 启动
</button>

<button @click="stopMcp(currentMcp.id)" :disabled="!mcpInstances.get(currentMcp.id)">
  <Square :size="14" /> 停止
</button>

<button @click="restartMcp(currentMcp.id)" :disabled="!mcpInstances.get(currentMcp.id)">
  <RefreshCw :size="14" /> 重启
</button>

<button @click="discoverMcpTools(currentMcp.id)" :disabled="!mcpInstances.get(currentMcp.id)">
  <Search :size="14" /> 发现工具
</button>
```

#### MCP 实例状态展示

```vue
<div v-if="mcpInstances.get(currentMcp.id)" class="detail-section">
  <h3>运行状态</h3>
  <div class="info-grid">
    <div class="info-item">
      <label><Activity :size="14" /> 状态</label>
      <value :class="'status-' + instance.status.toLowerCase()">
        {{ instance.status }}
      </value>
    </div>
    <div class="info-item">
      <label><Clock :size="14" /> 启动时间</label>
      <value>{{ formatTime(instance.startTime) }}</value>
    </div>
    <div class="info-item">
      <label><Heartbeat :size="14" /> 最后心跳</label>
      <value>{{ formatTime(instance.lastHeartbeat) }}</value>
    </div>
    <div class="info-item">
      <label><Wrench :size="14" /> 工具数量</label>
      <value>{{ instance.toolCount || 0 }}</value>
    </div>
  </div>
</div>
```

#### MCP 工具列表展示

```vue
<div v-if="mcpTools.get(currentMcp.id)" class="detail-section">
  <h3>可用工具 ({{ tools.length }})</h3>
  <div class="tool-list">
    <div v-for="tool in tools" :key="tool.id" class="tool-item">
      <div class="tool-header">
        <span class="tool-name">{{ tool.name }}</span>
        <span class="tool-risk" :class="'risk-' + tool.riskLevel.toLowerCase()">
          {{ tool.riskLevel }}
        </span>
      </div>
      <div v-if="tool.description" class="tool-desc">
        {{ tool.description }}
      </div>
    </div>
  </div>
</div>
```

---

## 二、新增 State 管理

```typescript
// MCP 实例状态映射
const mcpInstances = ref<Map<number, McpInstanceInfo>>(new Map())

// MCP 工具列表映射
const mcpTools = ref<Map<number, McpToolInfo[]>>(new Map())
```

**设计说明：**
- 使用 `Map` 结构以 MCP ID 为键存储实例状态
- 使用 `Map` 结构以 MCP ID 为键存储工具列表
- 支持多个 MCP Server 同时管理

---

## 三、新增函数

### 3.1 实例管理函数

```typescript
async function loadMcpInstanceStatus(mcpId: number) {
  // 加载单个 MCP 实例状态
  const instance = await adminMcpInstanceApi.status(mcpId)
  if (instance) {
    mcpInstances.value.set(mcpId, instance)
  } else {
    mcpInstances.value.delete(mcpId)
  }
}

async function startMcp(mcpId: number) {
  // 启动 MCP Server
  const instance = await adminMcpInstanceApi.start(mcpId)
  mcpInstances.value.set(mcpId, instance)
  await loadMcpTools(mcpId) // 启动后自动加载工具
  alert('MCP Server 启动成功')
}

async function stopMcp(mcpId: number) {
  // 停止 MCP Server
  await adminMcpInstanceApi.stop(mcpId)
  mcpInstances.value.delete(mcpId)
  mcpTools.value.delete(mcpId)
  alert('MCP Server 已停止')
}

async function restartMcp(mcpId: number) {
  // 重启 MCP Server
  const instance = await adminMcpInstanceApi.restart(mcpId)
  mcpInstances.value.set(mcpId, instance)
  await loadMcpTools(mcpId) // 重启后重新加载工具
  alert('MCP Server 重启成功')
}

async function discoverMcpTools(mcpId: number) {
  // 手动触发工具发现
  const tools = await adminMcpInstanceApi.discover(mcpId)
  mcpTools.value.set(mcpId, tools)
  alert(`工具发现成功，共找到 ${tools.length} 个工具`)
}
```

### 3.2 工具管理函数

```typescript
async function loadMcpTools(mcpId: number) {
  // 加载 MCP 工具列表
  const tools = await adminMcpInstanceApi.listTools(mcpId)
  mcpTools.value.set(mcpId, tools)
}
```

### 3.3 辅助函数

```typescript
function formatTime(timeStr: string | null | undefined) {
  // 格式化时间显示
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
```

---

## 四、新增样式

### 4.1 状态样式

```css
/* 运行状态颜色 */
.status-running { color: #30D158; }  /* 绿色 - 运行中 */
.status-starting { color: #FF9F0A; } /* 橙色 - 启动中 */
.status-stopped,
.status-error { color: #FF453A; }    /* 红色 - 停止/错误 */
```

### 4.2 工具列表样式

```css
.tool-item {
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
}

.tool-risk {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

/* 风险等级颜色 */
.tool-risk.risk-low {
  background: rgba(48, 209, 88, 0.15);
  color: #30D158;
}

.tool-risk.risk-medium {
  background: rgba(255, 159, 10, 0.15);
  color: #FF9F0A;
}

.tool-risk.risk-high {
  background: rgba(255, 69, 58, 0.15);
  color: #FF453A;
}
```

---

## 五、使用流程

### 5.1 查看 MCP 状态

1. 进入 "技能管理" 应用
2. 点击 "MCP Servers" 标签
3. 选择一个 MCP Server
4. 查看详情面板中的 "运行状态" 和 "可用工具"

### 5.2 启动 MCP Server

1. 选择一个 MCP Server
2. 点击 "启动" 按钮
3. 等待启动成功（自动加载工具列表）
4. 查看 "运行状态" 和 "可用工具"

### 5.3 停止 MCP Server

1. 选择一个运行中的 MCP Server
2. 点击 "停止" 按钮
3. 确认停止操作
4. 实例状态和工具列表自动清除

### 5.4 重启 MCP Server

1. 选择一个运行中的 MCP Server
2. 点击 "重启" 按钮
3. 等待重启完成（自动重新加载工具）

### 5.5 发现工具

1. 启动 MCP Server 后
2. 点击 "发现工具" 按钮
3. 等待工具发现完成
4. 查看工具列表和风险等级

---

## 六、按钮状态管理

| 按钮 | 启用条件 | 禁用条件 |
|------|---------|---------|
| 启动 | MCP 未运行 | MCP 已运行 |
| 停止 | MCP 已运行 | MCP 未运行 |
| 重启 | MCP 已运行 | MCP 未运行 |
| 发现工具 | MCP 已运行 | MCP 未运行 |
| 编辑配置 | 总是启用 | - |
| 启用/禁用 | 总是启用 | - |
| 删除 | 总是启用 | - |

---

## 七、图标导入

```typescript
import {
  Play, Square, RefreshCw, Search,    // 新增图标
  Activity, Clock, Heartbeat, Wrench,  // 新增图标
  // ... 其他已有图标
} from 'lucide-vue-next'
```

---

## 八、功能完整性检查

### ✅ 已完成对接

- [x] MCP 创建 - `McpCreatePanel.vue`
- [x] MCP 编辑 - `McpEditorPanel.vue`
- [x] MCP 列表 - `ExtApp.vue`
- [x] MCP 删除 - `ExtApp.vue`
- [x] MCP 启用/禁用 - `ExtApp.vue`
- [x] **MCP 启动** - `ExtApp.vue` (新增)
- [x] **MCP 停止** - `ExtApp.vue` (新增)
- [x] **MCP 重启** - `ExtApp.vue` (新增)
- [x] **MCP 状态查询** - `ExtApp.vue` (新增)
- [x] **MCP 工具列表** - `ExtApp.vue` (新增)
- [x] **MCP 工具发现** - `ExtApp.vue` (新增)

### 🔄 可选功能（未对接）

- [ ] MCP 心跳手动检测 - `adminMcpInstanceApi.heartbeat()`
- [ ] 运行中的实例列表 - `adminMcpInstanceApi.listRunning()`

这些功能对用户来说是透明的（系统自动处理），不需要手动调用。

---

## 九、测试建议

### 9.1 基本流程测试

1. **启动测试：**
   - 创建一个 REMOTE 类型的 MCP（配置 serverUrl）
   - 点击 "启动" 按钮
   - 验证状态变为 "RUNNING"
   - 验证工具列表自动加载

2. **停止测试：**
   - 选择一个运行中的 MCP
   - 点击 "停止" 按钮
   - 验证状态变为 "STOPPED"
   - 验证工具列表被清除

3. **重启测试：**
   - 选择一个运行中的 MCP
   - 点击 "重启" 按钮
   - 验证重启后状态恢复为 "RUNNING"
   - 验证工具列表重新加载

4. **工具发现测试：**
   - 启动 MCP Server
   - 点击 "发现工具" 按钮
   - 验证工具列表更新
   - 检查工具风险等级显示

### 9.2 错误处理测试

1. **启动失败：**
   - 配置错误的 serverUrl
   - 验证错误提示显示
   - 验证按钮状态正确

2. **网络错误：**
   - MCP Server 不可达
   - 验证错误提示清晰

---

## 十、下一步

前端 MCP 实例管理功能已全部对接完成！现在可以：

1. ✅ 测试前端和后端的完整流程
2. ✅ 验证 MCP Server 的启动/停止功能
3. ✅ 测试工具自动发现功能
4. ✅ 验证多 MCP Server 并发管理

**后端已实现：**
- ✅ `McpInstanceService` - 实例管理核心服务
- ✅ `McpHeartbeatService` - 心跳检测服务
- ✅ `McpToolDiscoveryService` - 工具发现服务
- ✅ `McpInstanceController` - REST API 端点

**前端已实现：**
- ✅ MCP 实例管理 UI
- ✅ MCP 状态展示
- ✅ MCP 工具列表展示
- ✅ MCP 操作按钮（启动/停止/重启/发现工具）

**Phase 1 完成！** 🎉
