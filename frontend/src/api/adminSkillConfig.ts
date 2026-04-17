import { http } from '@/utils/request'

/** 资源范围 */
export type SkillScope = 'SYSTEM' | 'DEPT' | 'USER'
/** 来源 */
export type SkillSource = 'MANUAL' | 'HUB' | 'BUILTIN'

export interface SkillConfigInfo {
  id: number
  name: string
  description?: string
  skillType?: string
  content?: string
  packagePath?: string
  source?: SkillSource
  sourceUrl?: string
  tags?: string[]
  config?: Record<string, any>
  enabled: number
  scope: SkillScope
  deptId?: number | null
  ownerId?: number | null
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

export interface CreateSkillConfigRequest {
  name: string
  description?: string
  skillType?: string
  content?: string
  source?: SkillSource
  sourceUrl?: string
  tags?: string[]
  config?: Record<string, any>
  enabled: number
  scope?: SkillScope
  deptId?: number | null
}

export interface UpdateSkillConfigRequest {
  id: number
  name?: string
  description?: string
  skillType?: string
  content?: string
  source?: SkillSource
  sourceUrl?: string
  tags?: string[]
  config?: Record<string, any>
  enabled?: number
  scope?: SkillScope
  deptId?: number | null
}

export const adminSkillConfigApi = {
  create: (data: CreateSkillConfigRequest) => {
    return http.post<{ id: number }>('/admin/skill-config/create', data)
  },

  get: (id: number) => {
    return http.get<SkillConfigInfo>(`/admin/skill-config/${id}`)
  },

  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: SkillConfigInfo[]; total: number }>(`/admin/skill-config/list`, {
      params: { pageNum, pageSize }
    })
  },

  update: (data: UpdateSkillConfigRequest) => {
    return http.put('/admin/skill-config/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/admin/skill-config/${id}`)
  },

  updateContent: (id: number, content: string, skipScan = false) => {
    return http.put(`/admin/skill-config/${id}/content`, { content }, {
      params: skipScan ? { skipScan: true } : undefined
    })
  }
}
