import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getWireGuardConfigApi,
  updateWireGuardConfigApi,
  getWireGuardStatusApi,
  startWireGuardApi,
  stopWireGuardApi,
  getWireGuardPeersApi,
  createPeerApi,
  updatePeerApi,
  deletePeerApi,
  togglePeerApi
} from '@/api/wireguard'
import type { WireGuardConfig, Peer } from '@/store/types'

export const useWireGuardStore = defineStore('wireguard', () => {
  const config = ref<WireGuardConfig | null>(null)
  const status = ref<any>(null)
  const peers = ref<Peer[]>([])
  const loading = ref(false)

  async function loadConfig() {
    loading.value = true
    try {
      const response = await getWireGuardConfigApi()
      config.value = response
    } finally {
      loading.value = false
    }
  }

  async function loadStatus() {
    const response = await getWireGuardStatusApi()
    status.value = response
  }

  async function loadPeers() {
    loading.value = true
    try {
      const response = await getWireGuardPeersApi()
      peers.value = response
    } finally {
      loading.value = false
    }
  }

  async function updateConfig(newConfig: Partial<WireGuardConfig>) {
    const request: any = {
      privateKey: newConfig.privateKey || '',
      publicKey: newConfig.publicKey || '',
      listenPort: newConfig.listenPort || 51820,
      address: newConfig.address || '10.0.0.1/24',
      mtu: newConfig.mtu || 1280,
      postUp: newConfig.postUp,
      postDown: newConfig.postDown,
      enabled: newConfig.enabled ?? true,
      interfaceName: newConfig.interfaceName || 'wg0'
    }
    config.value = await updateWireGuardConfigApi(request)
  }

  async function startInterface() {
    status.value = await startWireGuardApi()
  }

  async function stopInterface() {
    status.value = await stopWireGuardApi()
  }

  async function createPeer(peer: Partial<Peer>) {
    const request: any = {
      publicKey: peer.publicKey || '',
      presharedKey: peer.presharedKey,
      allowedIps: peer.allowedIps || '',
      name: peer.name || '',
      deviceType: peer.deviceType,
      endpointIp: peer.endpointIp,
      endpointPort: peer.endpointPort,
      persistentKeepalive: peer.persistentKeepalive || 25,
      enabled: peer.enabled ?? true
    }
    const created = await createPeerApi(request)
    peers.value.push(created)
    return created
  }

  async function updatePeer(id: number, peer: Partial<Peer>) {
    const request: any = {
      publicKey: peer.publicKey || '',
      presharedKey: peer.presharedKey,
      allowedIps: peer.allowedIps || '',
      name: peer.name || '',
      deviceType: peer.deviceType,
      endpointIp: peer.endpointIp,
      endpointPort: peer.endpointPort,
      persistentKeepalive: peer.persistentKeepalive || 25,
      enabled: peer.enabled ?? true
    }
    const updated = await updatePeerApi(id, request)
    const index = peers.value.findIndex(p => p.id === id)
    if (index !== -1) {
      peers.value[index] = updated
    }
    return updated
  }

  async function deletePeer(id: number) {
    await deletePeerApi(id)
    peers.value = peers.value.filter(p => p.id !== id)
  }

  async function togglePeer(id: number) {
    const toggled = await togglePeerApi(id)
    const index = peers.value.findIndex(p => p.id === id)
    if (index !== -1) {
      peers.value[index] = toggled
    }
    return toggled
  }

  return {
    config,
    status,
    peers,
    loading,
    loadConfig,
    loadStatus,
    loadPeers,
    updateConfig,
    startInterface,
    stopInterface,
    createPeer,
    updatePeer,
    deletePeer,
    togglePeer
  }
})
