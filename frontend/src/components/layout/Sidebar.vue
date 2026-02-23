<template>
  <div class="app-sidebar">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      @select="handleSelect"
    >
      <el-menu-item index="/">
        <el-icon><Odometer /></el-icon>
        <span>Dashboard</span>
      </el-menu-item>
      <el-menu-item index="/wireguard">
        <el-icon><Lock /></el-icon>
        <span>WireGuard</span>
      </el-menu-item>
      <el-menu-item index="/proxy">
        <el-icon><Connection /></el-icon>
        <span>Proxy</span>
      </el-menu-item>
      <el-menu-item index="/routing">
        <el-icon><Guide /></el-icon>
        <span>Routing</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <span>Settings</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { Odometer, Lock, Connection, Guide, Setting } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

function handleSelect(index: string) {
  router.push(index)
}
</script>

<script lang="ts">
import { computed } from 'vue'
export default { components: { Odometer, Lock, Connection, Guide, Setting } }
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  background-color: #304156;
  overflow-y: auto;
}

.sidebar-menu {
  border: none;
  background-color: transparent;

  :deep(.el-menu-item) {
    color: #bfcbd9;

    &:hover,
    &.is-active {
      background-color: #263445;
      color: #409eff;
    }
  }

  :deep(.el-icon) {
    margin-right: 8px;
  }
}
</style>
