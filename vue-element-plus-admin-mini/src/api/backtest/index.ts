import request from '@/axios'
import type { BacktestResponse } from './types'

export const runBacktestApi = (data: {
  startDate: string
  endDate: string
}): Promise<IResponse<BacktestResponse>> => {
  return request.post({ url: '/api/backtest/run', data })
}
