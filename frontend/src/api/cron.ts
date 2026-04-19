import { http } from '@/utils/request'

/** 定时任务 */
export interface CronJob {
  id: number
  name: string
  description: string
  enabled: number
  cronExpression: string
  timezone: string
  taskType: string
  promptTemplate: string
  targetUserId: number
  targetSessionId: number
  timeoutSeconds: number
  maxRetries: number
  misfireGraceSeconds: number
  maxConcurrency: number
  lastRunTime: string
  nextRunTime: string
  lastRunStatus: string
  lastRunError: string
  deptId: number
  createBy: number
  createTime: string
  updateTime: string
}

/** 定时任务历史 */
export interface CronJobHistory {
  id: number
  jobId: number
  startTime: string
  endTime: string
  status: string
  triggerType: string
  sessionId: string
  promptContent: string
  responseContent: string
  errorMessage: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  createTime: string
}

export const cronApi = {
  /** 获取任务列表 */
  listJobs: (params?: {
    pageNum?: number
    pageSize?: number
    targetUserId?: number
    enabled?: number
  }) => {
    return http.get<{ records: CronJob[]; total: number; pageNum: number; pageSize: number }>('/admin/cron/jobs', { params })
  },

  /** 创建任务 */
  createJob: (data: {
    name: string
    description?: string
    cronExpression: string
    timezone?: string
    taskType?: string
    promptTemplate?: string
    targetUserId?: number
    targetSessionId?: number
    timeoutSeconds?: number
    maxRetries?: number
    misfireGraceSeconds?: number
    maxConcurrency?: number
    deptId?: number
  }) => {
    return http.post<CronJob>('/admin/cron/jobs', data)
  },

  /** 更新任务 */
  updateJob: (id: number, data: {
    name?: string
    description?: string
    cronExpression?: string
    timezone?: string
    taskType?: string
    promptTemplate?: string
    targetUserId?: number
    targetSessionId?: number
    timeoutSeconds?: number
    maxRetries?: number
    misfireGraceSeconds?: number
    maxConcurrency?: number
    enabled?: number
    deptId?: number
  }) => {
    return http.put<CronJob>(`/admin/cron/jobs/${id}`, { id, ...data })
  },

  /** 删除任务 */
  deleteJob: (id: number) => {
    return http.delete<boolean>(`/admin/cron/jobs/${id}`)
  },

  /** 暂停任务 */
  pauseJob: (id: number) => {
    return http.post<CronJob>(`/admin/cron/jobs/${id}/pause`)
  },

  /** 恢复任务 */
  resumeJob: (id: number) => {
    return http.post<CronJob>(`/admin/cron/jobs/${id}/resume`)
  },

  /** 手动触发任务 */
  runJob: (id: number) => {
    return http.post<CronJob>(`/admin/cron/jobs/${id}/run`)
  },

  /** 获取任务历史 */
  listJobHistory: (jobId: number, params?: {
    pageNum?: number
    pageSize?: number
  }) => {
    return http.get<{ records: CronJobHistory[]; total: number; pageNum: number; pageSize: number }>(`/admin/cron/jobs/${jobId}/history`, { params })
  },

  /** 获取全部历史 */
  listHistory: (params?: {
    pageNum?: number
    pageSize?: number
  }) => {
    return http.get<{ records: CronJobHistory[]; total: number; pageNum: number; pageSize: number }>('/admin/cron/history', { params })
  }
}
