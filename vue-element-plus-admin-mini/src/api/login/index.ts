import request from '@/axios'
import type { UserLoginType, UserType } from './types'

export interface LoginResponse {
  code: number
  message: string
  data: {
    accessToken: string
    refreshToken: string
    username: string
    nickname: string
    roles: string[]
  }
}

export const loginApi = (data: UserLoginType): Promise<LoginResponse> => {
  return request.post({ url: '/api/auth/login', data })
}

export const refreshTokenApi = (): Promise<LoginResponse> => {
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
