import client from './client'
import type { LoginRequest, AuthResponse, User } from '@/store/types'

export async function loginApi(credentials: LoginRequest): Promise<AuthResponse> {
  const response = await client.post('/auth/login', credentials)
  return response.data
}

export async function logoutApi(): Promise<void> {
  await client.post('/auth/logout')
}

export async function getCurrentUserApi(): Promise<User> {
  const response = await client.get('/auth/me')
  return response.data
}

export async function changePasswordApi(
  currentPassword: string,
  newPassword: string
): Promise<User> {
  const response = await client.post('/auth/change-password', {
    currentPassword,
    newPassword
  })
  return response.data
}
