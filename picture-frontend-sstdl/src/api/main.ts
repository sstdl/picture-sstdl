// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** hello GET /api/main/hello */
export async function helloUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/main/hello', {
    method: 'GET',
    ...(options || {}),
  })
}
