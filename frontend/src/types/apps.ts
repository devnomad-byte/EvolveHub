import type { AppDefinition } from './index'

export const appDefinitions: Record<string, AppDefinition> = {
  chat: {
    id: 'chat',
    name: 'AI 对话',
    icon: 'MessageSquare',
    gradient: 'linear-gradient(135deg, #0A84FF, #5E5CE6)',
    defaultWidth: 900,
    defaultHeight: 640,
    minWidth: 700,
    minHeight: 480,
    dockOrder: 0,
    roles: ['SUPER_ADMIN', 'LEADER', 'DEPT_HEAD', 'ADMIN', 'USER']
  },
  knowledge: {
    id: 'knowledge',
    name: '知识库',
    icon: 'BookOpen',
    gradient: 'linear-gradient(135deg, #30D158, #34C759)',
    defaultWidth: 880,
    defaultHeight: 600,
    minWidth: 700,
    minHeight: 460,
    dockOrder: 1,
    roles: ['SUPER_ADMIN', 'LEADER', 'DEPT_HEAD', 'ADMIN', 'USER']
  },
  model: {
    id: 'model',
    name: '模型管理',
    icon: 'Bot',
    gradient: 'linear-gradient(135deg, #BF5AF2, #9B59B6)',
    defaultWidth: 800,
    defaultHeight: 560,
    minWidth: 640,
    minHeight: 400,
    dockOrder: -1,
    roles: ['SUPER_ADMIN']
  },
  users: {
    id: 'users',
    name: '用户管理',
    icon: 'Users',
    gradient: 'linear-gradient(135deg, #FF9F0A, #FF6B00)',
    defaultWidth: 900,
    defaultHeight: 640,
    minWidth: 700,
    minHeight: 480,
    dockOrder: -1,
    roles: ['SUPER_ADMIN']
  },
  mcp: {
    id: 'mcp',
    name: 'MCP 工具',
    icon: 'Wrench',
    gradient: 'linear-gradient(135deg, #64D2FF, #5AC8FA)',
    defaultWidth: 920,
    defaultHeight: 600,
    minWidth: 720,
    minHeight: 460,
    dockOrder: 2,
    roles: ['SUPER_ADMIN']
  },
  memory: {
    id: 'memory',
    name: '记忆管理',
    icon: 'Zap',
    gradient: 'linear-gradient(135deg, #FFD60A, #FF9F0A)',
    defaultWidth: 800,
    defaultHeight: 560,
    minWidth: 640,
    minHeight: 400,
    dockOrder: -1,
    roles: ['SUPER_ADMIN', 'LEADER', 'DEPT_HEAD', 'ADMIN', 'USER']
  },
  settings: {
    id: 'settings',
    name: '系统设置',
    icon: 'Settings',
    gradient: 'linear-gradient(135deg, #8E8E93, #636366)',
    defaultWidth: 780,
    defaultHeight: 560,
    minWidth: 640,
    minHeight: 400,
    dockOrder: 3,
    roles: ['SUPER_ADMIN', 'LEADER', 'DEPT_HEAD', 'ADMIN', 'USER']
  },
  dashboard: {
    id: 'dashboard',
    name: '数据大屏',
    icon: 'Monitor',
    gradient: 'linear-gradient(135deg, #0A84FF, #30D158)',
    defaultWidth: 1280,
    defaultHeight: 800,
    minWidth: 1024,
    minHeight: 600,
    dockOrder: 4,
    roles: ['SUPER_ADMIN']
  },
  pets: {
    id: 'pets',
    name: '宠物管理',
    icon: 'Cat',
    gradient: 'linear-gradient(135deg, #FF6B9D, #BF5AF2)',
    defaultWidth: 900,
    defaultHeight: 640,
    minWidth: 700,
    minHeight: 480,
    dockOrder: -1,
    roles: ['SUPER_ADMIN']
  }
}

export const dockApps = ['chat', 'knowledge', 'mcp', 'settings', 'dashboard']
