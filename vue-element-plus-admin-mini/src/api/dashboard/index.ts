import request from '@/axios'
import type { DashboardData } from './types'

export const getDashboardDataApi = (): Promise<IResponse<DashboardData>> => {
  return request.get({ url: '/dashboard/api/data' })
}

export const getVolumeTrendApi = (days = 60): Promise<IResponse<any[]>> => {
  return request.get({ url: '/dashboard/api/volume-trend', params: { days } })
}

export const getCapitalFlowTrendApi = (days = 60): Promise<IResponse<any[]>> => {
  return request.get({ url: '/dashboard/api/capital-flow-trend', params: { days } })
}
