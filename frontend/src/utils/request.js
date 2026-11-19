import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('cnr_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code === 200) {
      return body.data
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message
    if (status === 401) {
      localStorage.removeItem('cnr_token')
      localStorage.removeItem('cnr_profile')
      ElMessage.error('登录已过期，请重新登录')
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      window.location.href = `/login?redirect=${redirect}`
    } else if (status === 403) {
      ElMessage.error(message || '没有权限执行该操作')
    } else if (status === 500) {
      ElMessage.error('服务器异常，请稍后重试')
    } else {
      ElMessage.error(message || '网络异常')
    }
    return Promise.reject(error)
  },
)

export default request
