import type { CreateOrderParams, LoveOrder } from '@/types/dish'
import { http } from '@/http/http'

/** 提交订单 */
export function createOrder(data: CreateOrderParams) {
  return http.post<LoveOrder>('/order/create', data)
}

/** 获取订单列表 */
export function getOrders(status?: string) {
  return http.get<LoveOrder[]>('/order/list', { status })
}

/** 获取订单详情 */
export function getOrderDetail(orderId: number) {
  return http.get<LoveOrder>(`/order/${orderId}`)
}

/** 更新订单状态 */
export function updateOrderStatus(orderId: number, status: string) {
  return http.put(`/order/${orderId}/status`, { status })
}
