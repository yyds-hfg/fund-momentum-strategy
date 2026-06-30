import request from '@/axios'
import type { UserLoginType, LoginResult } from './types'

export const loginApi = (data: UserLoginType): Promise<IResponse<LoginResult>> => {
  return request.post({ url: '/api/auth/login', data })
}

export const refreshTokenApi = (): Promise<IResponse<LoginResult>> => {
  return request.post({ url: '/api/auth/refresh' })
}

export const loginOutApi = (): Promise<IResponse> => {
  return request.post({ url: '/api/auth/logout' })
}

// 保留接口以兼容现有代码，实际路由改为前端静态配置
export const getAdminRoleApi = (
  params: { roleName: string }
): Promise<IResponse<AppCustomRouteRecordRaw[]>> => {
  return request.get({ url: '/api/auth/role', params })
}

export const getTestRoleApi = (params: { roleName: string }): Promise<IResponse<string[]>> => {
  return request.get({ url: '/api/auth/role2', params })
}
