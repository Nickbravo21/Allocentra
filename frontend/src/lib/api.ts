import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const api = {
  // Cycles
  getCycles: async () => {
    const { data } = await client.get('/cycles')
    return data
  },

  getCycle: async (id: string) => {
    const { data } = await client.get(`/cycles/${id}`)
    return data
  },

  createCycle: async (cycle: any) => {
    const { data } = await client.post('/cycles', cycle)
    return data
  },

  // Requests
  getRequests: async (cycleId: string, params?: any) => {
    const { data } = await client.get('/requests', { params: { cycleId, ...params } })
    return data
  },

  getRequest: async (id: string) => {
    const { data } = await client.get(`/requests/${id}`)
    return data
  },

  createRequest: async (request: any) => {
    const { data } = await client.post('/requests', request)
    return data
  },

  // Allocation Runs
  runAllocation: async (payload: any) => {
    const { data } = await client.post('/runs', payload)
    return data
  },

  getRunStatus: async (runId: string) => {
    const { data } = await client.get(`/runs/${runId}`)
    return data
  },

  getRuns: async (params?: any) => {
    const { data } = await client.get('/runs', { params })
    return data
  },

  // Health
  health: async () => {
    const { data } = await client.get('/health')
    return data
  },
}
