import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getProxyConfigsApi,
  createProxyConfigApi,
  updateProxyConfigApi,
  deleteProxyConfigApi,
  toggleProxyConfigApi,
  setActiveProxyConfigApi
} from '@/api/proxy'
import type { ProxyConfig } from '@/store/types'

export const useProxyStore = defineStore('proxy', () => {
  const configs = ref<ProxyConfig[]>([])
  const loading = ref(false)

  async function loadConfigs() {
    loading.value = true
    try {
      const response = await getProxyConfigsApi()
      configs.value = response
    } finally {
      loading.value = false
    }
  }

  async function createConfig(config: Partial<ProxyConfig>) {
    const created = await createProxyConfigApi(config as any)
    configs.value.push(created)
    return created
  }

  async function updateConfig(id: number, config: Partial<ProxyConfig>) {
    const updated = await updateProxyConfigApi(id, config as any)
    const index = configs.value.findIndex(c => c.id === id)
    if (index !== -1) {
      configs.value[index] = updated
    }
    return updated
  }

  async function deleteConfig(id: number) {
    await deleteProxyConfigApi(id)
    configs.value = configs.value.filter(c => c.id !== id)
  }

  async function toggleConfig(id: number) {
    const toggled = await toggleProxyConfigApi(id)
    const index = configs.value.findIndex(c => c.id === id)
    if (index !== -1) {
      configs.value[index] = toggled
    }
    return toggled
  }

  async function setActiveConfig(id: number) {
    const active = await setActiveProxyConfigApi(id)
    // Disable all others
    configs.value.forEach(c => c.enabled = c.id === id)
    return active
  }

  const activeConfig = computed(() => configs.value.find(c => c.enabled))

  return {
    configs,
    loading,
    activeConfig,
    loadConfigs,
    createConfig,
    updateConfig,
    deleteConfig,
    toggleConfig,
    setActiveConfig
  }
})

import { computed } from 'vue'
