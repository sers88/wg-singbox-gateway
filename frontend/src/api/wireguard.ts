import client from './client'
import type { WireGuardConfig, Peer } from '@/store/types'

export async function getWireGuardConfigApi(): Promise<WireGuardConfig> {
  const response = await client.get('/wireguard/config')
  return response.data
}

export async function updateWireGuardConfigApi(config: any): Promise<WireGuardConfig> {
  const response = await client.put('/wireguard/config', config)
  return response.data
}

export async function getWireGuardStatusApi(): Promise<any> {
  const response = await client.get('/wireguard/status')
  return response.data
}

export async function startWireGuardApi(): Promise<any> {
  const response = await client.post('/wireguard/start')
  return response.data
}

export async function stopWireGuardApi(): Promise<any> {
  const response = await client.post('/wireguard/stop')
  return response.data
}

export async function getWireGuardPeersApi(): Promise<Peer[]> {
  const response = await client.get('/wireguard/peers')
  return response.data
}

export async function createPeerApi(peer: any): Promise<Peer> {
  const response = await client.post('/wireguard/peers', peer)
  return response.data
}

export async function updatePeerApi(id: number, peer: any): Promise<Peer> {
  const response = await client.put(`/wireguard/peers/${id}`, peer)
  return response.data
}

export async function deletePeerApi(id: number): Promise<void> {
  await client.delete(`/wireguard/peers/${id}`)
}

export async function togglePeerApi(id: number): Promise<Peer> {
  const response = await client.post(`/wireguard/peers/${id}/toggle`)
  return response.data
}

export async function getPeerQRCodeApi(id: number, serverEndpoint?: string): Promise<string> {
  const params = serverEndpoint ? { serverEndpoint } : undefined
  const response = await client.get(`/wireguard/peers/${id}/qrcode`, {
    params,
    responseType: 'text'
  })
  return response.data
}
