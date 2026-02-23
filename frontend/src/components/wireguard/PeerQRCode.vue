<template>
  <el-dialog v-model="visible" title="Peer QR Code" width="400px">
    <div class="qrcode-container">
      <div ref="qrcodeRef" class="qrcode"></div>
      <div class="config-text">
        <el-text type="info">{{ configText }}</el-text>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">Close</el-button>
      <el-button type="primary" @click="downloadQRCode">
        Download QR Code
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import QRCode from 'qrcode'

const props = defineProps<{
  modelValue: boolean
  configText: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const qrcodeRef = ref<HTMLDivElement>()
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

async function generateQRCode() {
  await nextTick()
  if (!qrcodeRef.value || !props.configText) return

  try {
    qrcodeRef.value.innerHTML = ''
    const canvas = await QRCode.toCanvas(props.configText, {
      width: 300,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    })
    qrcodeRef.value.appendChild(canvas)
  } catch (error) {
    console.error('Failed to generate QR code:', error)
  }
}

function downloadQRCode() {
  const canvas = qrcodeRef.value?.querySelector('canvas')
  if (!canvas) return

  const link = document.createElement('a')
  link.download = 'wireguard-peer.png'
  link.href = canvas.toDataURL('image/png')
  link.click()
}

function handleClose() {
  visible.value = false
}

watch(
  () => props.configText,
  () => {
    if (visible.value) {
      generateQRCode()
    }
  }
)

watch(
  visible,
  (v) => {
    if (v) {
      generateQRCode()
    }
  }
)
</script>

<script lang="ts">
import { computed } from 'vue'
export default {}
</script>

<style scoped>
.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.qrcode {
  padding: 10px;
  background: white;
  border-radius: 8px;
}

.config-text {
  max-width: 100%;
  word-break: break-all;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.5;
  text-align: left;
}
</style>
