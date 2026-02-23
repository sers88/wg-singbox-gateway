<template>
  <el-card class="status-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">WireGuard Status</span>
        <span
          :class="['status-badge', `status-${status.toLowerCase()}`]"
        >
          {{ status }}
        </span>
      </div>
    </template>
    <div class="card-content">
      <div class="info-row">
        <span class="info-label">Interface:</span>
        <span class="info-value">{{ config?.interfaceName || 'wg0' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Listen Port:</span>
        <span class="info-value">{{ config?.listenPort || 51820 }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Address:</span>
        <span class="info-value">{{ config?.address || '10.0.0.1/24' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">Public Key:</span>
        <span class="info-value">{{ truncatedPublicKey }}</span>
      </div>
      <div class="actions">
        <el-button
          v-if="status === 'STOPPED'"
          type="success"
          @click="$emit('start')"
        >
          Start
        </el-button>
        <el-button
          v-else
          type="danger"
          @click="$emit('stop')"
        >
          Stop
        </el-button>
        <el-button @click="$emit('refresh')">
          <el-icon><Refresh /></el-icon>
          Refresh
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import type { WireGuardConfig } from '@/store/types'

const props = defineProps<{
  config?: WireGuardConfig | null
  status: string
}>()

defineEmits<{
  start: []
  stop: []
  refresh: []
}>()

const truncatedPublicKey = computed(() => {
  if (!props.config?.publicKey) return ''
  const key = props.config.publicKey
  return `${key.substring(0, 12)}...${key.substring(key.length - 12)}`
})
</script>

<script lang="ts">
import { computed } from 'vue'
export default { components: { Refresh } }
</script>

<style scoped>
.status-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-content {
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .info-label {
    color: #909399;
    font-size: 14px;
  }

  .info-value {
    color: #303133;
    font-size: 14px;
    font-family: monospace;
  }
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}
</style>
