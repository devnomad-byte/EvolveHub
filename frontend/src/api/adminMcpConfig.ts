import { http } from '@/utils/request'

/** 资源范围 */
export type McpScope = 'SYSTEM' | 'DEPT' | 'USER'
/** 传输类型 */
export type TransportType = 'UPLOADED' | 'REMOTE'
/** 协议类型 */
export type Protocol = 'STDIO' | 'SSE'

export interface McpConfigInfo {
  id: number
  name: string
  description?: string
  transportType?: TransportType
  serverUrl?: string
  packagePath?: string
  command?: string
  args?: string
  env?: string
  protocol?: Protocol
  config?: Record<string, any>
  enabled: number
  scope: McpScope
  deptId?: number | null
  ownerId?: number | null
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

export interface CreateMcpConfigRequest {
  name: string
  description?: string
  transportType?: TransportType
  serverUrl?: string
  packagePath?: string
  command?: string
  args?: string
  env?: string
  protocol?: Protocol
  config?: Record<string, any>
  enabled: number
  scope?: McpScope
  deptId?: number | null
}

export interface UpdateMcpConfigRequest {
  id: number
  name?: string
  description?: string
  transportType?: TransportType
  serverUrl?: string
  packagePath?: string
  command?: string
  args?: string
  env?: string
  protocol?: Protocol
  config?: Record<string, any>
  enabled?: number
  scope?: McpScope
  deptId?: number | null
}

export const adminMcpConfigApi = {
  create: (data: CreateMcpConfigRequest) => {
    return http.post<{ id: number }>('/admin/mcp-config/create', data)
  },

  get: (id: number) => {
    return http.get<McpConfigInfo>(`/admin/mcp-config/${id}`)
  },

  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: McpConfigInfo[]; total: number }>(`/admin/mcp-config/list`, {
      params: { pageNum, pageSize }
    })
  },

  update: (data: UpdateMcpConfigRequest) => {
    return http.put('/admin/mcp-config/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/admin/mcp-config/${id}`)
  },

  toggle: (id: number) => {
    return http.post<{ id: number }>(`/admin/mcp-config/${id}/toggle`)
  },

  listTools: (id: number) => {
    return http.get<McpToolInfo[]>(`/admin/mcp-config/${id}/tools`)
  }
}

// MCP Tool 类型
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type ToolScope = 'SYSTEM' | 'DEPT' | 'USER' | 'GRANT'

export interface McpToolInfo {
  id: number
  mcpConfigId: number
  name: string
  description?: string
  inputSchema?: string
  riskLevel?: RiskLevel
  toolScope?: ToolScope
  deptId?: number | null
  ownerId?: number | null
  enabled: number
}

export interface CreateMcpToolRequest {
  mcpConfigId: number
  name: string
  description?: string
  inputSchema?: string
  riskLevel?: RiskLevel
  toolScope?: ToolScope
  deptId?: number | null
  ownerId?: number | null
  enabled?: number
}

export interface UpdateMcpToolRequest {
  id: number
  name?: string
  description?: string
  inputSchema?: string
  riskLevel?: RiskLevel
  toolScope?: ToolScope
  deptId?: number | null
  ownerId?: number | null
  enabled?: number
}

export interface CreateMcpToolGrantRequest {
  toolId: number
  grantType: 'USER' | 'DEPT' | 'ROLE'
  targetId: number
}

export const adminMcpToolApi = {
  create: (data: CreateMcpToolRequest) => {
    return http.post<{ id: number }>('/admin/mcp-tool/create', data)
  },

  get: (id: number) => {
    return http.get<McpToolInfo>(`/admin/mcp-tool/${id}`)
  },

  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: McpToolInfo[]; total: number }>(`/admin/mcp-tool/list`, {
      params: { pageNum, pageSize }
    })
  },

  update: (data: UpdateMcpToolRequest) => {
    return http.put('/admin/mcp-tool/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/admin/mcp-tool/${id}`)
  },

  grant: (data: CreateMcpToolGrantRequest) => {
    return http.post<{ id: number }>('/admin/mcp-tool/grant', data)
  },

  revokeGrant: (grantId: number) => {
    return http.delete(`/admin/mcp-tool/grant/${grantId}`)
  }
}
