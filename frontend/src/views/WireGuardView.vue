<template>
  <div class="wireguard-view">
    <h2 class="page-title">WireGuard Configuration</h2>

    <WGStatusCard
      :config="wireGuardStore.config"
      :status="wireGuardStore.status?.status || 'STOPPED'"
      @start="handleStart"
      @stop="handleStop"
      @refresh="handleRefresh"
    />

    <PeerList
      :peers="wireGuardStore.peers"
      :loading="wireGuardStore.loading"
      @add="showPeerForm = true"
      @edit="handleEditPeer"
      @delete="handleDeletePeer"
      @toggle="handleTogglePeer"
      @qrcode="handleShowQRCode"
    />

    <PeerForm
      v-model="showPeerForm"
      :peer="editingPeer"
      @save="handleSavePeer"
    />

    <PeerQRCode
      v-model="showQRCode"
      :config-text="qrCodeText"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WGStatusCard from '@/components/wireguard/WGStatusCard.vue'
import PeerList from '@/components/wireguard/PeerList.vue'
import PeerForm from '@/components/wireguard/PeerForm.vue'
import PeerQRCode from '@/components/wireguard/PeerQRCode.vue'
import { useWireGuardStore } from '@/store/modules/wireguard'
import { getPeerQRCodeApi } from '@/api/wireguard'
import type { Peer } from '@/store/types'

const wireGuardStore = useWireGuardStore()

const showPeerForm = ref(false)
const showQRCode = ref(false)
const editingPeer = ref<Peer | null>(null)
const qrCodeText = ref('')

onMounted(async () => {
  await wireGuardStore.loadConfig()
  await wireGuardStore.loadStatus()
  await wireGuardStore.loadPeers()
})

async function handleStart() {
  await wireGuardStore.startInterface()
  ElMessage.success('WireGuard started')
}

async function handleStop() {
  await wireGuardStore.stopInterface()
  ElMessage.success('WireGuard stopped')
}

async function handleRefresh() {
  await wireGuardStore.loadStatus()
  await wireGuardStore.loadConfig()
}

function handleEditPeer(peer: Peer) {
  editingPeer.value = peer
  showPeerForm.value = true
}

async function handleSavePeer(peer: any) {
  try {
    if (editingPeer.value?.id) {
      await wireGuardStore.updatePeer(editingPeer.value.id!, peer)
      ElMessage.success('Peer updated')
    } else {
      await wireGuardStore.createPeer(peer)
      ElMessage.success('Peer created')
    }
    editingPeer.value = null
    await wireGuardStore.loadPeers()
  } catch (error) {
    ElMessage.error('Failed to save peer')
  }
}

async function handleDeletePeer(id: number) {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this peer?', 'Warning', {
      type: 'warning'
    })
    await wireGuardStore.deletePeer(id)
    ElMessage.success('Peer deleted')
    await wireGuardStore.loadPeers()
  } catch {
    // User cancelled
  }
}

async function handleTogglePeer(id: number) {
  try {
    await wireGuardStore.togglePeer(id)
    ElMessage.success('Peer toggled')
  } catch (error) {
    ElMessage.error('Failed to toggle peer')
  }
}

async function handleShowQRCode(id: number) {
  try {
    qrCodeText.value = await getPeerQRCodeApi(id)
    showQRCode.value = true
  } catch (error) {
    ElMessage.error('Failed to generate QR code')
  }
}
</script>

<style scoped>
.wireguard-view {
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}
</style>
