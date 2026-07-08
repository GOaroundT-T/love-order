import type { OrderSocketMessage } from '@/types/dish'
import { useTokenStore } from '@/store/token'
import { getEnvBaseUrl } from '@/utils'

let socketTask: UniApp.SocketTask | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let manuallyClosed = false
let messageHandler: ((message: OrderSocketMessage) => void) | null = null

function buildWsUrl(token: string) {
  const baseUrl = getEnvBaseUrl().replace(/\/$/, '')
  const wsBase = baseUrl.replace(/^https:/, 'wss:').replace(/^http:/, 'ws:')
  return `${wsBase}/ws/order?token=${encodeURIComponent(token)}`
}

export function setOrderSocketMessageHandler(handler: (message: OrderSocketMessage) => void) {
  messageHandler = handler
}

export function connectOrderSocket() {
  const tokenStore = useTokenStore()
  const token = tokenStore.updateNowTime().validToken
  if (!token || socketTask) return

  manuallyClosed = false
  socketTask = uni.connectSocket({
    url: buildWsUrl(token),
    complete: () => {},
  })

  socketTask.onOpen(() => {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = null
  })

  socketTask.onMessage((event) => {
    if (event.data === 'pong') return
    try {
      const data = JSON.parse(String(event.data)) as OrderSocketMessage
      messageHandler?.(data)
      if (data.message) uni.showToast({ title: data.message, icon: 'none' })
    }
    catch {
      // ignore malformed socket message
    }
  })

  socketTask.onClose(() => {
    socketTask = null
    if (!manuallyClosed && tokenStore.updateNowTime().hasLogin) scheduleReconnect()
  })

  socketTask.onError(() => {
    socketTask = null
    if (!manuallyClosed && tokenStore.updateNowTime().hasLogin) scheduleReconnect()
  })
}

export function closeOrderSocket() {
  manuallyClosed = true
  if (reconnectTimer) clearTimeout(reconnectTimer)
  reconnectTimer = null
  socketTask?.close({ code: 1000, reason: 'logout' })
  socketTask = null
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectOrderSocket()
  }, 3000)
}
