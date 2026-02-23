<template>
  <div class="routing-view">
    <h2 class="page-title">Routing Rules</h2>

    <el-card>
      <template #header>
        <span class="card-title">Routing Rules</span>
        <el-button type="primary" @click="handleAddRule">
          <el-icon><Plus /></el-icon>
          Add Rule
        </el-button>
      </template>

      <el-table
        :data="rules"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="type" label="Type" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="Value" min-width="200">
          <template #default="{ row }">
            <el-text truncated>{{ row.value }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="outboundTag" label="Outbound" width="120">
          <template #default="{ row }">
            <el-tag :type="getOutboundTagType(row.outboundTag)" size="small">
              {{ row.outboundTag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="Priority" width="80" />
        <el-table-column prop="description" label="Description" min-width="150" />
        <el-table-column prop="enabled" label="Status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? 'Active' : 'Disabled' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                @click="handleEditRule(row)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDeleteRule(row.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getRoutingRulesApi, deleteRoutingRuleApi } from '@/api/proxy'
import type { RoutingRule } from '@/store/types'

const rules = ref<RoutingRule[]>([])
const loading = ref(false)

onMounted(async () => {
  await loadRules()
})

async function loadRules() {
  loading.value = true
  try {
    rules.value = await getRoutingRulesApi()
  } finally {
    loading.value = false
  }
}

function getOutboundTagType(tag: string) {
  switch (tag) {
    case 'proxy': return 'primary'
    case 'direct': return 'success'
    case 'block': return 'danger'
    default: return 'info'
  }
}

function handleAddRule() {
  ElMessage.info('Add rule dialog would open here')
}

function handleEditRule(_rule: RoutingRule) {
  ElMessage.info('Edit rule dialog would open here')
}

async function handleDeleteRule(id: number | null | undefined) {
  if (!id) return
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this rule?', 'Warning', {
      type: 'warning'
    })
    await deleteRoutingRuleApi(id)
    ElMessage.success('Rule deleted')
    await loadRules()
  } catch {
    // User cancelled
  }
}
</script>

<style scoped>
.routing-view {
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
