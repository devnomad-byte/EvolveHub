import { http } from '@/utils/request'
import type { ModelConfigInfo } from './adminModelConfig'
import { adminModelConfigApi } from './adminModelConfig'

export const availableResourceApi = {
  availableModels: () => {
    return http.get<ModelConfigInfo[]>('/available/models')
  },

  async resolveChatModels() {
    const availableModels = await availableResourceApi.availableModels().catch(() => [] as ModelConfigInfo[])
    if (availableModels.length > 0) {
      return availableModels
    }
    const fallback = await adminModelConfigApi.myList().catch(() => adminModelConfigApi.list(1, 100))
    return fallback.records
  }
}
