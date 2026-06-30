export type UserLoginType = {
  username: string
  password: string
}

export type UserType = {
  username: string
  nickname?: string
  avatar?: string
  roles: string[]
  permissions?: string[]
}

export type LoginResult = {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserType
}
