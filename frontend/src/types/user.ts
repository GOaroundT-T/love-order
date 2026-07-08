export type UserRole = 'chef' | 'girlfriend' | string

export interface LoveUser {
  userId: number
  username: string
  nickname: string
  avatar?: string
  role?: UserRole
  roles?: UserRole[]
  kitchenName?: string
  signature?: string
  partnerId?: number | null
}

export interface UpdateProfileParams {
  nickname?: string
  avatar?: string
  kitchenName?: string
  signature?: string
}

export interface BindPartnerParams {
  partnerId: number
}
