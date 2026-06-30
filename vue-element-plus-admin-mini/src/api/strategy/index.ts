import request from '@/axios'
import type { StrategyConfigItem, RebalanceAdvice } from './types'

export const getStrategyConfigsApi = (): Promise<IResponse<StrategyConfigItem[]>> => {
  return request.get({ url: '/api/strategy/configs' })
}

export const updateStrategyConfigApi = (
  id: number,
  data: StrategyConfigItem
): Promise<IResponse> => {
  return request.put({ url: `/api/strategy/configs/${id}`, data })
}

export const executeStrategyApi = (): Promise<IResponse<RebalanceAdvice>> => {
  return request.post({ url: '/api/strategy/execute' })
}
