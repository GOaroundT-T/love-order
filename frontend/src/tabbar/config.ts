import type { TabBar } from '@uni-helper/vite-plugin-uni-pages'
import type { CustomTabBarItem, NativeTabBarItem } from './types'

export const TABBAR_STRATEGY_MAP = {
  NO_TABBAR: 0,
  NATIVE_TABBAR: 1,
  CUSTOM_TABBAR: 2,
}

export const selectedTabbarStrategy = TABBAR_STRATEGY_MAP.CUSTOM_TABBAR

export const nativeTabbarList: NativeTabBarItem[] = [
  { iconPath: 'static/tabbar/kitchen.png', selectedIconPath: 'static/tabbar/kitchenHL.png', pagePath: 'pages/index/index', text: '厨房' },
  { iconPath: 'static/tabbar/order.png', selectedIconPath: 'static/tabbar/orderHL.png', pagePath: 'pages/order/list', text: '订单' },
  { iconPath: 'static/tabbar/discover.png', selectedIconPath: 'static/tabbar/discoverHL.png', pagePath: 'pages/discover/index', text: '发现' },
  { iconPath: 'static/tabbar/me.png', selectedIconPath: 'static/tabbar/meHL.png', pagePath: 'pages/me/me', text: '我的' },
]

export const customTabbarList: CustomTabBarItem[] = [
  { text: '厨房', pagePath: 'pages/index/index', iconType: 'unocss', icon: 'i-carbon-restaurant' },
  { text: '订单', pagePath: 'pages/order/list', iconType: 'unocss', icon: 'i-carbon-list', badge: '' },
  { text: '发现', pagePath: 'pages/discover/index', iconType: 'unocss', icon: 'i-carbon-campsite' },
  { text: '我的', pagePath: 'pages/me/me', iconType: 'unocss', icon: 'i-carbon-user-avatar' },
]

export const tabbarCacheEnable = [TABBAR_STRATEGY_MAP.NATIVE_TABBAR, TABBAR_STRATEGY_MAP.CUSTOM_TABBAR].includes(selectedTabbarStrategy)
export const customTabbarEnable = [TABBAR_STRATEGY_MAP.CUSTOM_TABBAR].includes(selectedTabbarStrategy)
export const needHideNativeTabbar = selectedTabbarStrategy === TABBAR_STRATEGY_MAP.CUSTOM_TABBAR

const _tabbarList = customTabbarEnable ? customTabbarList.map(item => ({ text: item.text, pagePath: item.pagePath })) : nativeTabbarList
export const tabbarList = customTabbarEnable ? customTabbarList : nativeTabbarList

const _tabbar: TabBar = {
  custom: selectedTabbarStrategy === TABBAR_STRATEGY_MAP.CUSTOM_TABBAR,
  color: '#999999',
  selectedColor: '#e85d3a',
  backgroundColor: '#FFFFFF',
  borderStyle: 'black',
  height: '50px',
  fontSize: '10px',
  iconWidth: '24px',
  spacing: '3px',
  list: _tabbarList as unknown as TabBar['list'],
}

export const tabBar = tabbarCacheEnable ? _tabbar : {}
