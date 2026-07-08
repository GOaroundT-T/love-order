import { getAllPages } from '@/utils'

export const LOGIN_STRATEGY_MAP = {
  DEFAULT_NO_NEED_LOGIN: 0,
  DEFAULT_NEED_LOGIN: 1,
}

// 默认可浏览菜品和发现页；下单、订单、我的等接口由后端登录态保护。
export const LOGIN_STRATEGY = LOGIN_STRATEGY_MAP.DEFAULT_NO_NEED_LOGIN
export const isNeedLoginMode = LOGIN_STRATEGY === LOGIN_STRATEGY_MAP.DEFAULT_NEED_LOGIN

export const LOGIN_PAGE = '/pages/auth/login'
export const REGISTER_PAGE = '/pages/auth/register'

export const LOGIN_PAGE_LIST = [LOGIN_PAGE, REGISTER_PAGE]

const excludeLoginPathList = getAllPages('excludeLoginPath').map(page => page.path)

export const EXCLUDE_LOGIN_PATH_LIST = [
  ...excludeLoginPathList,
]

export const LOGIN_PAGE_ENABLE_IN_MP = true
