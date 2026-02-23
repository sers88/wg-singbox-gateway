<template>
  <div class="dashboard-view">
    <h2 class="page-title">Dashboard</h2>
    <Dashboard :system-status="systemStatus" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Dashboard from '@/components/system/Dashboard.vue'
import { useSystemStore } from '@/store/modules/system'

const systemStore = useSystemStore()
const systemStatus = ref(systemStore.status || {
  wireGuardStatus: 'STOPPED',
  singBoxStatus: 'STOPPED',
  connectedPeers: 0,
  totalTransferRx: 0,
  totalTransferTx: 0,
  uptime: 0,
  cpuUsage: null,
  memoryUsage: null,
  diskUsage: null,
  timestamp: new Date().toISOString()
})

let statusInterval: number | null = null

onMounted(async () => {
  await systemStore.loadStatus()
  systemStatus.value = systemStore.status || systemStatus.value

  // Update status every 5 seconds
  statusInterval = window.setInterval(async () => {
    await systemStore.loadStatus()
    systemStatus.value = systemStore.status || systemStatus.value
  }, 5000)
})

onUnmounted(() => {
  if (statusInterval) {
    clearInterval(statusInterval)
  }
})
</script>

<style scoped>
.dashboard-view {
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}
</style>
