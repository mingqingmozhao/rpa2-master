import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 对于文件上传等二进制响应，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    
    // 统一包装：仅当存在 code 字段且不等于 200 时视为业务错误
    if (Object.prototype.hasOwnProperty.call(res, 'code') && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: 未授权
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    console.error('[Request] Network error:', error)
    return Promise.reject(error)
  }
)

export default request
