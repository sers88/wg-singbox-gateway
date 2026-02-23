import client from './client'
import type { ProxyConfig, RoutingRule } from '@/store/types'

export async function getProxyConfigsApi(): Promise<ProxyConfig[]> {
  const response = await client.get('/proxy/configs')
  return response.data
}

export async function getActiveProxyConfigApi(): Promise<ProxyConfig | null> {
  const response = await client.get('/proxy/configs/active')
  return response.data
}

export async function createProxyConfigApi(config: any): Promise<ProxyConfig> {
  const response = await client.post('/proxy/configs', config)
  return response.data
}

export async function updateProxyConfigApi(id: number, config: any): Promise<ProxyConfig> {
  const response = await client.put(`/proxy/configs/${id}`, config)
  return response.data
}

export async function deleteProxyConfigApi(id: number): Promise<void> {
  await client.delete(`/proxy/configs/${id}`)
}

export async function toggleProxyConfigApi(id: number): Promise<ProxyConfig> {
  const response = await client.post(`/proxy/configs/${id}/toggle`)
  return response.data
}

export async function setActiveProxyConfigApi(id: number): Promise<ProxyConfig> {
  const response = await client.post(`/proxy/configs/${id}/active`)
  return response.data
}

export async function testProxyConnectionApi(id: number): Promise<any> {
  const response = await client.post(`/proxy/configs/${id}/test`)
  return response.data
}

export async function getRoutingRulesApi(): Promise<RoutingRule[]> {
  const response = await client.get('/routing/rules')
  return response.data
}

export async function createRoutingRuleApi(rule: any): Promise<RoutingRule> {
  const response = await client.post('/routing/rules', rule)
  return response.data
}

export async function updateRoutingRuleApi(id: number, rule: any): Promise<RoutingRule> {
  const response = await client.put(`/routing/rules/${id}`, rule)
  return response.data
}

export async function deleteRoutingRuleApi(id: number): Promise<void> {
  await client.delete(`/routing/rules/${id}`)
}

export async function toggleRoutingRuleApi(id: number): Promise<RoutingRule> {
  const response = await client.post(`/routing/rules/${id}/toggle`)
  return response.data
}

export async function getRoutingRuleTypesApi(): Promise<any[]> {
  const response = await client.get('/routing/rules/types')
  return response.data
}

export async function getGeoSiteCategoriesApi(): Promise<string[]> {
  const response = await client.get('/routing/rules/geosite-categories')
  return response.data
}
