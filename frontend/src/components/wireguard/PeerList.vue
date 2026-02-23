<template>
  <el-card class="peers-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">Peers</span>
        <el-button type="primary" @click="$emit('add')">
          <el-icon><Plus /></el-icon>
          Add Peer
        </el-button>
      </div>
    </template>

    <el-table
      :data="peers"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="name" label="Name" width="180" />
      <el-table-column prop="publicKey" label="Public Key" min-width="200">
        <template #default="{ row }">
          <span class="truncated-key">{{ truncatedKey(row.publicKey) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="allowedIps" label="Allowed IPs" width="150" />
      <el-table-column prop="lastHandshake" label="Last Handshake" width="180">
        <template #default="{ row }">
          <span v-if="row.lastHandshake">{{ formatDate(row.lastHandshake) }}</span>
          <span v-else class="never-connected">Never</span>
        </template>
      </el-table-column>
      <el-table-column label="Traffic" width="150">
        <template #default="{ row }">
          <span class="traffic">
            ↓ {{ formatBytes(row.transferRx) }} / ↑ {{ formatBytes(row.transferTx) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="Status" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button
              size="small"
              @click="$emit('qrcode', row.id)"
            >
              <el-icon><Picture /></el-icon>
            </el-button>
            <el-button
              size="small"
              @click="$emit('edit', row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              size="small"
              @click="$emit('toggle', row.id)"
            >
              <el-icon><Switch /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="$emit('delete', row.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { Plus, Picture, Edit, Switch, Delete } from '@element-plus/icons-vue'
import type { Peer } from '@/store/types'

const props = defineProps<{
  peers: Peer[]
  loading?: boolean
}>()

defineEmits<{
  add: []
  edit: [peer: Peer]
  delete: [id: number]
  toggle: [id: number]
  qrcode: [id: number]
}>()

function truncatedKey(key: string): string {
  return `${key.substring(0, 12)}...${key.substring(key.length - 12)}`
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString()
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}
</script>

<script lang="ts">
export default { components: { Plus, Picture, Edit, Switch, Delete } }
</script>

<style scoped>
.peers-card {
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

.truncated-key {
  font-family: monospace;
  font-size: 12px;
  color: #606266;
}

.never-connected {
  color: #909399;
  font-style: italic;
}

.traffic {
  font-size: 12px;
  color: #606266;
}
</style>
