<template>
  <div class="proxy-view">
    <h2 class="page-title">Proxy Configuration</h2>

    <ProxyConfigComponent
      :configs="proxyStore.configs"
      :loading="proxyStore.loading"
      @add="showProxyForm = true"
      @edit="handleEditProxy"
      @delete="handleDeleteProxy"
      @toggle="handleToggleProxy"
    />

    <ProxyForm
      v-model="showProxyForm"
      :config="editingProxy"
      @save="handleSaveProxy"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProxyConfigComponent from '@/components/proxy/ProxyConfig.vue'
import ProxyForm from '@/components/proxy/ProxyForm.vue'
import { useProxyStore } from '@/store/modules/proxy'
import type { ProxyConfig } from '@/store/types'

const proxyStore = useProxyStore()

const showProxyForm = ref(false)
const editingProxy = ref<ProxyConfig | null>(null)

onMounted(async () => {
  await proxyStore.loadConfigs()
})

function handleEditProxy(config: ProxyConfig) {
  editingProxy.value = config
  showProxyForm.value = true
}

async function handleSaveProxy(config: any) {
  try {
    if (editingProxy.value?.id) {
      await proxyStore.updateConfig(editingProxy.value.id!, config)
      ElMessage.success('Proxy updated')
    } else {
      await proxyStore.createConfig(config)
      ElMessage.success('Proxy created')
    }
    editingProxy.value = null
    await proxyStore.loadConfigs()
  } catch (error) {
    ElMessage.error('Failed to save proxy')
  }
}

async function handleDeleteProxy(id: number) {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this proxy?', 'Warning', {
      type: 'warning'
    })
    await proxyStore.deleteConfig(id)
    ElMessage.success('Proxy deleted')
    await proxyStore.loadConfigs()
  } catch {
    // User cancelled
  }
}

async function handleToggleProxy(id: number) {
  try {
    await proxyStore.toggleConfig(id)
    ElMessage.success('Proxy toggled')
  } catch (error) {
    ElMessage.error('Failed to toggle proxy')
  }
}
</script>

<style scoped>
.proxy-view {
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}
</style>
