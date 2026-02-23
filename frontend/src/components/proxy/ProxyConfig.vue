<template>
  <el-card class="proxy-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">Proxy Configurations</span>
        <el-button type="primary" @click="$emit('add')">
          <el-icon><Plus /></el-icon>
          Add Proxy
        </el-button>
      </div>
    </template>

    <el-table
      :data="configs"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="type" label="Type" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="server" label="Server" min-width="200" />
      <el-table-column prop="serverPort" label="Port" width="80" />
      <el-table-column prop="enabled" label="Status" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="Priority" width="80" />
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
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
import { Plus, Edit, Switch, Delete } from '@element-plus/icons-vue'
import type { ProxyConfig } from '@/store/types'

defineProps<{
  configs: ProxyConfig[]
  loading?: boolean
}>()

defineEmits<{
  add: []
  edit: [config: ProxyConfig]
  delete: [id: number]
  toggle: [id: number]
}>()
</script>

<style scoped>
.proxy-card {
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
</style>
