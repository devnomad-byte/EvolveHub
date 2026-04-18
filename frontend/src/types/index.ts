export type AppId = 'chat' | 'knowledge' | 'model' | 'users' | 'mcp' | 'memory' | 'settings' | 'dashboard' | 'pets' | 'dept' | 'role' | 'permission' | 'skill-editor' | 'mcp-editor' | 'mcp-create' | 'skill-edit' | 'desktopicon'

export interface WindowState {
  id: string
  appId: AppId
  title: string
  x: number
  y: number
  width: number
  height: number
  minWidth: number
  minHeight: number
  zIndex: number
  isMinimized: boolean
  isMaximized: boolean
  isOpen: boolean
}

export interface AppDefinition {
  id: AppId
  name: string
  icon: string
  gradient: string
  defaultWidth: number
  defaultHeight: number
  minWidth: number
  minHeight: number
  dockOrder: number
  roles: string[]
}

export interface DesktopIcon {
  appId: AppId
  name: string
  icon: string
  gradient: string
  col: number
  row: number
}

export interface AppNotification {
  id: string
  type: 'success' | 'info' | 'warning' | 'error'
  title: string
  message: string
  time: string
  read: boolean
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  toolCalls?: ToolCall[]
  isTyping?: boolean
}

export interface ToolCall {
  name: string
  status: 'running' | 'success' | 'failed' | 'confirming'
  args: string
  result?: string
}

export interface ChatSession {
  id: string
  title: string
  time: string
  active: boolean
  modelConfigId?: string
}

export interface KnowledgeBase {
  id: string
  name: string
  level: 'GLOBAL' | 'DEPT' | 'PROJECT' | 'SENSITIVE'
  description: string
  docCount: number
  docs: KbDocument[]
}

export interface KbDocument {
  id: string
  name: string
  status: 'READY' | 'PROCESSING' | 'FAILED'
  chunkCount: number
}

export interface ModelConfig {
  id: string
  name: string
  provider: string
  modelType: 'LLM' | 'EMBEDDING'
  enabled: boolean
  isPreferred?: boolean
}

export interface McpServer {
  id: string
  name: string
  endpoint: string
  status: 'connected' | 'disconnected'
  toolCount: number
}

export interface McpTool {
  id: string
  name: string
  serverName: string
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  enabled: boolean
}

export interface UserInfo {
  id: number
  username: string
  displayName: string
  role: string
  deptName: string
  status: boolean
}

export interface MemoryItem {
  id: string
  content: string
  type: 'preference' | 'fact' | 'tool_config'
  importance: number
}

export type UserRole = 'SUPER_ADMIN' | 'LEADER' | 'DEPT_HEAD' | 'ADMIN' | 'USER'
