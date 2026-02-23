<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? 'Edit Proxy' : 'Add Proxy'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="Type" prop="type">
        <el-select v-model="form.type" placeholder="Select proxy type">
          <el-option label="Trojan" value="TROJAN" />
          <el-option label="VLESS" value="VLESS" />
          <el-option label="VMess" value="VMESS" />
          <el-option label="Shadowsocks" value="SHADOWSOCKS" />
        </el-select>
      </el-form-item>

      <el-form-item label="Server" prop="server">
        <el-input v-model="form.server" placeholder="Server address" />
      </el-form-item>

      <el-form-item label="Port" prop="serverPort">
        <el-input-number v-model="form.serverPort" :min="1" :max="65535" />
      </el-form-item>

      <!-- Trojan-specific fields -->
      <template v-if="form.type === 'TROJAN'">
        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="Trojan password"
          />
        </el-form-item>
        <el-form-item label="SNI">
          <el-input v-model="form.serverName" placeholder="Server name for TLS" />
        </el-form-item>
        <el-form-item label="Network">
          <el-select v-model="form.network">
            <el-option label="TCP" value="TCP" />
            <el-option label="QUIC" value="QUIC" />
          </el-select>
        </el-form-item>
      </template>

      <!-- VLESS-specific fields -->
      <template v-if="form.type === 'VLESS'">
        <el-form-item label="UUID" prop="uuid">
          <el-input v-model="form.uuid" placeholder="VLESS UUID" />
        </el-form-item>
        <el-form-item label="Flow">
          <el-input v-model="form.flow" placeholder="e.g., xtls-rprx-vision" />
        </el-form-item>
        <el-form-item label="SNI">
          <el-input v-model="form.serverName" placeholder="Server name for TLS" />
        </el-form-item>
        <el-form-item label="Network">
          <el-select v-model="form.network">
            <el-option label="TCP" value="TCP" />
            <el-option label="QUIC" value="QUIC" />
          </el-select>
        </el-form-item>
      </template>

      <!-- VMess-specific fields -->
      <template v-if="form.type === 'VMESS'">
        <el-form-item label="UUID" prop="uuid">
          <el-input v-model="form.uuid" placeholder="VMess UUID" />
        </el-form-item>
        <el-form-item label="Alter ID">
          <el-input-number v-model="form.alterId" :min="0" />
        </el-form-item>
        <el-form-item label="Security">
          <el-select v-model="form.security">
            <el-option label="Auto" value="AUTO" />
            <el-option label="AES-128-GCM" value="aes-128-gcm" />
            <el-option label="AES-256-GCM" value="aes-256-gcm" />
            <el-option label="CHACHA20-POLY1305" value="chacha20-poly1305" />
          </el-select>
        </el-form-item>
        <el-form-item label="Network">
          <el-select v-model="form.network">
            <el-option label="TCP" value="TCP" />
            <el-option label="QUIC" value="QUIC" />
          </el-select>
        </el-form-item>
      </template>

      <!-- Shadowsocks-specific fields -->
      <template v-if="form.type === 'SHADOWSOCKS'">
        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="Shadowsocks password"
          />
        </el-form-item>
        <el-form-item label="Method">
          <el-select v-model="form.method">
            <el-option label="AES-128-GCM" value="aes-128-gcm" />
            <el-option label="AES-256-GCM" value="aes-256-gcm" />
            <el-option label="CHACHA20-POLY1305" value="chacha20-poly1305" />
          </el-select>
        </el-form-item>
      </template>

      <!-- Common fields -->
      <el-form-item label="Insecure TLS" v-if="['TROJAN', 'VLESS', 'VMESS'].includes(form.type)">
        <el-switch v-model="form.insecure" />
      </el-form-item>

      <el-form-item label="Priority">
        <el-input-number v-model="form.priority" :min="1" />
        <span class="form-hint">Lower number = higher priority</span>
      </el-form-item>

      <el-form-item label="Enabled">
        <el-switch v-model="form.enabled" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">Cancel</el-button>
      <el-button type="primary" @click="handleSave" :loading="loading">
        Save
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { ProxyConfig } from '@/store/types'

const props = defineProps<{
  modelValue: boolean
  config?: ProxyConfig | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [config: any]
}>()

const formRef = ref()
const loading = ref(false)
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const isEdit = computed(() => !!props.config?.id)

const form = ref<any>({
  type: 'TROJAN',
  server: '',
  serverPort: 443,
  password: '',
  uuid: '',
  serverName: '',
  insecure: false,
  network: 'TCP',
  flow: '',
  alterId: 0,
  security: 'AUTO',
  method: 'AES_256_GCM',
  enabled: true,
  priority: 1
})

const rules = {
  type: [{ required: true, message: 'Type is required', trigger: 'change' }],
  server: [{ required: true, message: 'Server is required', trigger: 'blur' }],
  serverPort: [{ required: true, message: 'Port is required', trigger: 'blur' }]
}

watch(
  () => props.config,
  (config) => {
    if (config) {
      form.value = { ...config }
    } else {
      form.value = {
        type: 'TROJAN',
        server: '',
        serverPort: 443,
        password: '',
        uuid: '',
        serverName: '',
        insecure: false,
        network: 'TCP',
        flow: '',
        alterId: 0,
        security: 'AUTO',
        method: 'AES_256_GCM',
        enabled: true,
        priority: 1
      }
    }
  },
  { immediate: true }
)

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  emit('save', form.value)
  handleClose()
}

function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
}
</script>

<style scoped>
.form-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
