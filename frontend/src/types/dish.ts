export type SpicyLevel = 'none' | 'mild' | 'medium' | 'hot' | string

/** 菜品分类 */
export interface DishCategory {
  id: number
  name: string
  sort?: number
  icon?: string
}

/** 菜品 */
export interface Dish {
  id: number
  name: string
  categoryId?: number
  image?: string
  price: number
  rating: number
  description?: string
  spicyLevel?: SpicyLevel
  onShelf?: boolean
  sort?: number
}

/** 购物车中的菜品 */
export interface CartItem {
  dishId: number
  dishName: string
  dishImage?: string
  price: number
  quantity: number
}

export type OrderStatus = 'pending' | 'confirmed' | 'cooking' | 'finished' | 'cancelled'

export const OrderStatusText: Record<OrderStatus, string> = {
  pending: '等 TA 确认',
  confirmed: '已接单',
  cooking: '烹饪中',
  finished: '已完成',
  cancelled: '已取消',
}

export const OrderStatusColor: Record<OrderStatus, string> = {
  pending: '#e85d3a',
  confirmed: '#8b7355',
  cooking: '#f0ad4e',
  finished: '#4caf50',
  cancelled: '#999999',
}

/** 订单项 */
export interface OrderItem {
  dishId: number
  dishName: string
  dishImage?: string
  price: number
  quantity: number
}

/** 订单 */
export interface LoveOrder {
  id: number
  userId: number
  status: OrderStatus
  totalAmount: number
  remark?: string
  loveNote?: string
  items: OrderItem[]
  createTime?: string
}

/** 提交订单参数 */
export interface CreateOrderParams {
  items: Array<{
    dishId: number
    quantity: number
  }>
  remark?: string
  loveNote?: string
}

export interface OrderSocketMessage {
  type: 'order' | 'order_status' | string
  message: string
  orderId?: number
  status?: OrderStatus
}
