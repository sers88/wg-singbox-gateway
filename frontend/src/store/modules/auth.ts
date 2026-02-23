import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, logoutApi, getCurrentUserApi } from '@/api/auth'
import type { User, LoginRequest } from '@/store/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials: LoginRequest) {
    loading.value = true
    try {
      const response = await loginApi(credentials)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      if (response.refreshToken) {
        localStorage.setItem('refreshToken', response.refreshToken)
      }
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    loading.value = true
    try {
      await logoutApi()
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!isAuthenticated.value) return
    loading.value = true
    try {
      const response = await getCurrentUserApi()
      user.value = response
    } finally {
      loading.value = false
    }
  }

  function setUser(newUser: User) {
    user.value = newUser
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    logout,
    fetchCurrentUser,
    setUser
  }
})
