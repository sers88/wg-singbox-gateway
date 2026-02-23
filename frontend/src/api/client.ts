import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/store/types'

const client = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
client.interceptors.response.use(
  (response) => {
    return response.data as ApiResponse<any>
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred'

    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default client
