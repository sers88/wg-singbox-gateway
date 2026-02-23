import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSystemStatusApi, getSystemInfoApi } from '@/api/system'
import type { SystemStatus } from '@/store/types'

export const useSystemStore = defineStore('system', () => {
  const status = ref<SystemStatus | null>(null)
  const info = ref<any>(null)
  const loading = ref(false)

  async function loadStatus() {
    const response = await getSystemStatusApi()
    status.value = response
  }

  async function loadInfo() {
    loading.value = true
    try {
      const response = await getSystemInfoApi()
      info.value = response
    } finally {
      loading.value = false
    }
  }

  return {
    status,
    info,
    loading,
    loadStatus,
    loadInfo
  }
})
