import type { CustomRequestOptions, HttpError, IResponse } from '@/http/types'
import { useTokenStore } from '@/store/token'
import { toLoginPage } from '@/utils/toLoginPage'
import { createHttpError, getResponseMessage, HttpErrorType, isSuccessResultCode, ResultEnum, ShowMessage } from './tools/enum'

export function http<T>(options: CustomRequestOptions) {
  return new Promise<T>((resolve, reject) => {
    uni.request({
      ...options,
      dataType: 'json',
      // #ifndef MP-WEIXIN
      responseType: 'json',
      // #endif
      success: async (res) => {
        const responseData = res.data as Partial<IResponse<T>>
        const code = responseData?.code
        const isTokenExpired = res.statusCode === 401 || code === ResultEnum.Unauthorized

        if (isTokenExpired) {
          const tokenStore = useTokenStore()
          await tokenStore.logout()
          toLoginPage()
          return reject(createHttpError({
            type: HttpErrorType.Auth,
            code,
            statusCode: res.statusCode,
            message: getResponseMessage(responseData, '登录已过期，请重新登录'),
            data: responseData?.data,
            raw: res,
          }))
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (!isSuccessResultCode(code as number)) {
            const httpError = createHttpError({
              type: HttpErrorType.Business,
              code,
              statusCode: res.statusCode,
              message: getResponseMessage(responseData),
              data: responseData?.data,
              raw: responseData,
            })

            if (!options.hideErrorToast) {
              uni.showToast({ icon: 'none', title: httpError.message })
            }
            return reject(httpError)
          }
          return resolve(responseData.data as T)
        }

        const httpError = createHttpError({
          type: HttpErrorType.Http,
          code,
          statusCode: res.statusCode,
          message: getResponseMessage(responseData, ShowMessage(res.statusCode)),
          data: responseData?.data,
          raw: res,
        })

        if (!options.hideErrorToast) {
          uni.showToast({ icon: 'none', title: httpError.message })
        }
        reject(httpError)
      },
      fail(err) {
        const httpError = createHttpError({
          type: HttpErrorType.Network,
          message: '网络错误，换个网络试试',
          raw: err,
        } satisfies HttpError)

        if (!options.hideErrorToast) {
          uni.showToast({ icon: 'none', title: httpError.message })
        }
        reject(httpError)
      },
    })
  })
}

export function httpGet<T>(url: string, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({ url, query, method: 'GET', header, ...options })
}

export function httpPost<T>(url: string, data?: Record<string, any>, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({ url, query, data, method: 'POST', header, ...options })
}

export function httpPut<T>(url: string, data?: Record<string, any>, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({ url, data, query, method: 'PUT', header, ...options })
}

export function httpDelete<T>(url: string, query?: Record<string, any>, header?: Record<string, any>, options?: Partial<CustomRequestOptions>) {
  return http<T>({ url, query, method: 'DELETE', header, ...options })
}

http.get = httpGet
http.post = httpPost
http.put = httpPut
http.delete = httpDelete
http.Get = httpGet
http.Post = httpPost
http.Put = httpPut
http.Delete = httpDelete
