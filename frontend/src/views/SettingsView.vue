<template>
  <div class="settings-view">
    <h2 class="page-title">Settings</h2>

    <el-card>
      <template #header>
        <span class="card-title">System Settings</span>
      </template>

      <el-form label-width="150px">
        <el-form-item label="Application Name">
          <el-input value="WG Singbox Gateway" disabled />
        </el-form-item>
        <el-form-item label="Version">
          <el-input value="1.0.0" disabled />
        </el-form-item>
        <el-form-item label="Backend URL">
          <el-input value="http://localhost:8080" disabled />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// Settings view
</script>

<style scoped>
.settings-view {
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
