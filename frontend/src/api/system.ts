import client from './client'
import type { SystemStatus } from '@/store/types'

export async function getSystemStatusApi(): Promise<SystemStatus> {
  const response = await client.get('/system/status')
  return response.data
}

export async function getSystemInfoApi(): Promise<any> {
  const response = await client.get('/system/info')
  return response.data
}

export async function getSystemStatsApi(): Promise<any> {
  const response = await client.get('/system/stats')
  return response.data
}

export async function createBackupApi(name: string, description?: string): Promise<any> {
  const response = await client.post('/system/backup', { name, description })
  return response.data
}

export async function getBackupsApi(): Promise<any[]> {
  const response = await client.get('/system/backups')
  return response.data
}

export async function restoreBackupApi(id: number): Promise<void> {
  await client.post(`/system/backups/${id}/restore`)
}

export async function deleteBackupApi(id: number): Promise<void> {
  await client.delete(`/system/backups/${id}`)
}
