<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="status-card">
          <div class="card-content">
            <div class="stat-icon wireguard-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">WireGuard</div>
              <div
                :class="['stat-value', `status-${systemStatus.wireGuardStatus.toLowerCase()}`]"
              >
                {{ systemStatus.wireGuardStatus }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="status-card">
          <div class="card-content">
            <div class="stat-icon singbox-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">Singbox</div>
              <div
                :class="['stat-value', `status-${systemStatus.singBoxStatus.toLowerCase()}`]"
              >
                {{ systemStatus.singBoxStatus }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="status-card">
          <div class="card-content">
            <div class="stat-icon peers-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">Connected Peers</div>
              <div class="stat-value">{{ systemStatus.connectedPeers }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="status-card">
          <div class="card-content">
            <div class="stat-icon uptime-icon">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">Uptime</div>
              <div class="stat-value">{{ formatUptime(systemStatus.uptime) }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <div class="status-card">
          <div class="card-header">
            <span class="card-title">Network Traffic</span>
          </div>
          <div class="card-content">
            <div class="traffic-row">
              <div class="traffic-item">
                <div class="traffic-label">Download</div>
                <div class="traffic-value">
                  <el-icon class="traffic-icon download-icon"><Bottom /></el-icon>
                  {{ formatBytes(systemStatus.totalTransferRx) }}
                </div>
              </div>
              <div class="traffic-item">
                <div class="traffic-label">Upload</div>
                <div class="traffic-value">
                  <el-icon class="traffic-icon upload-icon"><Top /></el-icon>
                  {{ formatBytes(systemStatus.totalTransferTx) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="status-card">
          <div class="card-header">
            <span class="card-title">System Resources</span>
          </div>
          <div class="card-content">
            <div class="resource-row">
              <div class="resource-item">
                <div class="resource-label">CPU Usage</div>
                <el-progress
                  :percentage="systemStatus.cpuUsage || 0"
                  :color="getResourceColor(systemStatus.cpuUsage)"
                />
              </div>
              <div class="resource-item">
                <div class="resource-label">Memory Usage</div>
                <el-progress
                  :percentage="systemStatus.memoryUsage || 0"
                  :color="getResourceColor(systemStatus.memoryUsage)"
                />
              </div>
              <div class="resource-item">
                <div class="resource-label">Disk Usage</div>
                <el-progress
                  :percentage="systemStatus.diskUsage || 0"
                  :color="getResourceColor(systemStatus.diskUsage)"
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { Lock, Connection, User, Timer, Bottom, Top } from '@element-plus/icons-vue'
import type { SystemStatus } from '@/store/types'

defineProps<{
  systemStatus: SystemStatus
}>()

function formatUptime(ms: number): string {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days}d ${hours % 24}h`
  } else if (hours > 0) {
    return `${hours}h ${minutes % 60}m`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`
  } else {
    return `${seconds}s`
  }
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

function getResourceColor(percentage: number | null): string {
  if (!percentage) return '#909399'
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}
</script>

<script lang="ts">
export default { components: { Lock, Connection, User, Timer, Bottom, Top } }
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.status-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  height: 100%;
}

.card-header {
  margin-bottom: 15px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.wireguard-icon {
    background-color: #ecf5ff;
    color: #409eff;
  }

  &.singbox-icon {
    background-color: #f0f9ff;
    color: #10b981;
  }

  &.peers-icon {
    background-color: #fef2f2;
    color: #ef4444;
  }

  &.uptime-icon {
    background-color: #fffbeb;
    color: #f59e0b;
  }
}

.stat-info {
  flex: 1;
  margin-left: 15px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;

  &.status-running {
    color: #10b981;
  }

  &.status-stopped {
    color: #ef4444;
  }

  &.status-error {
    color: #dc2626;
  }

  &.status-restarting {
    color: #f59e0b;
  }
}

.traffic-row {
  display: flex;
  gap: 30px;
}

.traffic-item {
  flex: 1;
  text-align: center;
}

.traffic-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.traffic-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.traffic-icon {
  font-size: 20px;

  &.download-icon {
    color: #67c23a;
  }

  &.upload-icon {
    color: #409eff;
  }
}

.resource-row {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resource-item {
  .resource-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }
}
</style>
