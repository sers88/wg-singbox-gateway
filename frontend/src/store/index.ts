import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

export * from './modules/auth'
export * from './modules/wireguard'
export * from './modules/proxy'
export * from './modules/system'
