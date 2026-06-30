import request from '@/axios'
import type { FundPageResult } from './types'

export const getFundPageApi = (params: {
  keyword?: string
  page?: number
  size?: number
}): Promise<IResponse<FundPageResult>> => {
  return request.get({ url: '/api/funds/page', params })
}
