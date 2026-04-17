import { http } from '@/utils/request'

/** 实例状态 */
export type InstanceStatus = 'STARTING' | 'RUNNING' | 'STOPPING' | 'STOPPED' | 'ERROR'

/** MCP 实例信息 */
export interface McpInstanceInfo {
  id: number
  mcpConfigId: number
  instanceKey: string
  processId?: number | null
  serverUrl?: string | null
  status: InstanceStatus
  lastHeartbeat?: string | null
  startTime: string
  stopTime?: string | null
  failCount?: number
  errorMsg?: string | null
  transportType: string
  toolCount?: number
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

/** MCP 工具信息 */
export interface McpToolInfo {
  id: number
  mcpConfigId: number
  name: string
  description?: string
  inputSchema?: string
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH'
  toolScope?: 'SYSTEM' | 'DEPT' | 'USER' | 'GRANT'
  deptId?: number | null
  ownerId?: number | null
  enabled: number
}

export const adminMcpInstanceApi = {
  /**
   * 启动 MCP Server（超时 5 分钟，首次需要下载依赖+构建）
   */
  start: (configId: number) => {
    return http.post<McpInstanceInfo>(`/admin/mcp-instance/${configId}/start`, null, {
      timeout: 300000
    })
  },

  /**
   * 停止 MCP Server
   */
  stop: (configId: number) => {
    return http.post(`/admin/mcp-instance/${configId}/stop`)
  },

  /**
   * 重启 MCP Server（超时 2 分钟）
   */
  restart: (configId: number) => {
    return http.post<McpInstanceInfo>(`/admin/mcp-instance/${configId}/restart`, null, {
      timeout: 120000
    })
  },

  /**
   * 获取 MCP 运行状态
   */
  status: (configId: number) => {
    return http.get<McpInstanceInfo | null>(`/admin/mcp-instance/${configId}/status`)
  },

  /**
   * 触发心跳检测
   */
  heartbeat: (configId: number) => {
    return http.post<boolean>(`/admin/mcp-instance/${configId}/heartbeat`)
  },

  /**
   * 触发工具发现
   */
  discover: (configId: number) => {
    return http.post<McpToolInfo[]>(`/admin/mcp-instance/${configId}/discover`)
  },

  /**
   * 获取 MCP 工具列表
   */
  listTools: (configId: number) => {
    return http.get<McpToolInfo[]>(`/admin/mcp-instance/${configId}/tools`)
  },

  /**
   * 获取所有运行中的实例
   */
  listRunning: () => {
    return http.get<McpInstanceInfo[]>(`/admin/mcp-instance/running`)
  }
}
