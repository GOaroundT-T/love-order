import { getLastPage } from '@/utils'
import { debounce } from '@/utils/debounce'

interface ToLoginPageOptions {
  /**
   * 跳转模式, uni.navigateTo | uni.reLaunch
   * @default 'navigateTo'
   */
  mode?: 'navigateTo' | 'reLaunch'
  /**
   * 查询参数
   * @example '?redirect=/pages/index/index'
   */
  queryString?: string
}

const LOGIN_PAGE = '/pages/auth/login'

/**
 * 跳转到登录页, 带防抖处理
 */
export const toLoginPage = debounce((options: ToLoginPageOptions = {}) => {
  const { mode = 'navigateTo', queryString = '' } = options
  const url = `${LOGIN_PAGE}${queryString}`

  const currentPage = getLastPage()
  const currentPath = currentPage?.route ? `/${currentPage.route}` : ''
  if (currentPath === LOGIN_PAGE) return

  if (mode === 'navigateTo') uni.navigateTo({ url })
  else uni.reLaunch({ url })
}, 500)
