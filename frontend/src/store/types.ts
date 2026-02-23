export interface User {
  id: number | null
  username: string
  email: string | null
  role: string
  lastLogin: string | null
}

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  refreshToken: string | null
  user: User
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  message: string | null
  errors: string[] | null
}

export interface WireGuardConfig {
  id: number | null
  privateKey: string
  publicKey: string
  listenPort: number
  address: string
  mtu: number
  postUp: string | null
  postDown: string | null
  enabled: boolean
  interfaceName: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface Peer {
  id: number | null
  publicKey: string
  presharedKey: string | null
  allowedIps: string
  name: string
  deviceType: string | null
  endpointIp: string | null
  endpointPort: number | null
  persistentKeepalive: number
  lastHandshake: string | null
  transferRx: number
  transferTx: number
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface ProxyConfig {
  id: number | null
  type: string
  server: string
  serverPort: number
  password: string | null
  uuid: string | null
  serverName: string | null
  insecure: boolean
  network: string
  flow: string | null
  alterId: number
  security: string
  method: string
  enabled: boolean
  priority: number
  createdAt: string
  updatedAt: string
}

export interface RoutingRule {
  id: number | null
  type: string
  value: string
  outboundTag: string
  enabled: boolean
  priority: number
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface SystemStatus {
  wireGuardStatus: string
  singBoxStatus: string
  connectedPeers: number
  totalTransferRx: number
  totalTransferTx: number
  uptime: number
  cpuUsage: number | null
  memoryUsage: number | null
  diskUsage: number | null
  timestamp: string
}

export const ServiceStatus = {
  RUNNING: 'RUNNING',
  STOPPED: 'STOPPED',
  ERROR: 'ERROR',
  RESTARTING: 'RESTARTING'
} as const

export type ServiceStatusValue = typeof ServiceStatus[keyof typeof ServiceStatus]
