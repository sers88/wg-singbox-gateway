<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? 'Edit Peer' : 'Add Peer'"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="Name" prop="name">
        <el-input v-model="form.name" placeholder="Peer name" />
      </el-form-item>

      <el-form-item label="Public Key" prop="publicKey">
        <el-input
          v-model="form.publicKey"
          type="textarea"
          :rows="2"
          placeholder="Peer's public key"
        />
      </el-form-item>

      <el-form-item label="Allowed IPs" prop="allowedIps">
        <el-input
          v-model="form.allowedIps"
          placeholder="e.g., 10.0.0.2/32"
        />
      </el-form-item>

      <el-form-item label="Pre-shared Key">
        <el-input
          v-model="form.presharedKey"
          type="textarea"
          :rows="2"
          placeholder="Optional pre-shared key for additional security"
        />
      </el-form-item>

      <el-form-item label="Endpoint IP">
        <el-input v-model="form.endpointIp" placeholder="Peer's endpoint IP (optional)" />
      </el-form-item>

      <el-form-item label="Endpoint Port">
        <el-input-number v-model="form.endpointPort" :min="1" :max="65535" />
      </el-form-item>

      <el-form-item label="Keepalive">
        <el-input-number v-model="form.persistentKeepalive" :min="0" :max="120" />
        <span class="form-hint">seconds (0 = disabled)</span>
      </el-form-item>

      <el-form-item label="Device Type">
        <el-select v-model="form.deviceType" placeholder="Select device type" clearable>
          <el-option label="Android" value="android" />
          <el-option label="iOS" value="ios" />
          <el-option label="Windows" value="windows" />
          <el-option label="macOS" value="macos" />
          <el-option label="Linux" value="linux" />
          <el-option label="Router" value="router" />
          <el-option label="Other" value="other" />
        </el-select>
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
import type { Peer } from '@/store/types'

interface PeerFormData {
  publicKey: string
  presharedKey: string | null
  allowedIps: string
  name: string
  deviceType: string | null
  endpointIp: string | null
  endpointPort: number | null
  persistentKeepalive: number
  enabled: boolean
}

const props = defineProps<{
  modelValue: boolean
  peer?: Peer | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [peer: PeerFormData]
}>()

const formRef = ref()
const loading = ref(false)
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const isEdit = computed(() => !!props.peer?.id)

const form = ref<PeerFormData>({
  publicKey: '',
  presharedKey: null,
  allowedIps: '10.0.0.2/32',
  name: '',
  deviceType: null,
  endpointIp: null,
  endpointPort: null,
  persistentKeepalive: 25,
  enabled: true
})

const rules = {
  name: [{ required: true, message: 'Name is required', trigger: 'blur' }],
  publicKey: [{ required: true, message: 'Public key is required', trigger: 'blur' }],
  allowedIps: [{ required: true, message: 'Allowed IPs is required', trigger: 'blur' }]
}

watch(
  () => props.peer,
  (peer) => {
    if (peer) {
      form.value = {
        publicKey: peer.publicKey,
        presharedKey: peer.presharedKey,
        allowedIps: peer.allowedIps,
        name: peer.name,
        deviceType: peer.deviceType,
        endpointIp: peer.endpointIp,
        endpointPort: peer.endpointPort,
        persistentKeepalive: peer.persistentKeepalive,
        enabled: peer.enabled
      }
    } else {
      form.value = {
        publicKey: '',
        presharedKey: null,
        allowedIps: '10.0.0.2/32',
        name: '',
        deviceType: null,
        endpointIp: null,
        endpointPort: null,
        persistentKeepalive: 25,
        enabled: true
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
