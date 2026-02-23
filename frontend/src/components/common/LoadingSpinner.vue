<template>
  <div class="loading-spinner" v-if="loading">
    <el-icon class="is-loading"><Loading /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

defineProps<{
  loading: boolean
}>()
</script>

<script lang="ts">
export default { components: { Loading } }
</script>

<style scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;

  .el-icon {
    font-size: 32px;
    color: #409eff;
  }
}
</style>
