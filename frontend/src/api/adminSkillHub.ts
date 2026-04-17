import { http } from '@/utils/request'

export interface HubSearchResult {
  name: string
  description: string
  author: string
  hubName: string
  bundleUrl: string
  tags: string[]
  downloads: number
  version: string
}

export const adminSkillHubApi = {
  /**
   * 获取支持的 Hub 列表
   */
  getAdapters(): Promise<string[]> {
    return http.get<string[]>('/admin/skill-config/hub/adapters')
  },

  /**
   * 搜索 Hub
   */
  search(keyword: string, hub?: string, page = 1, pageSize = 20): Promise<HubSearchResult[]> {
    return http.get<HubSearchResult[]>('/admin/skill-config/hub/search', {
      params: { keyword, hub, page, pageSize }
    })
  },

  /**
   * 安装技能
   */
  install(hubName: string, bundleUrl: string, skipScan = false): Promise<number> {
    return http.post<number>('/admin/skill-config/hub/install', { hubName, bundleUrl, skipScan })
  }
}
