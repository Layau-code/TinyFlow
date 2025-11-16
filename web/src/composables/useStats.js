import { ref } from 'vue'
import axios from 'axios'

function createApiState() {
  return { data: ref(null), loading: ref(false), error: ref(null) }
}

export function useFetchList() {
  const state = createApiState()
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const res = await axios.get('/api/urls')
      const payload = res?.data ?? null
      // 兼容后端分页返回结构：PageResponseDTO { content, totalElements, ... }
      const list = Array.isArray(payload)
        ? payload
        : Array.isArray(payload?.content)
          ? payload.content
          : Array.isArray(payload?.items)
            ? payload.items
            : Array.isArray(payload?.data?.content)
              ? payload.data.content
              : []
      state.data.value = list
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

export function useFetchOverview(shortCode) {
  const state = createApiState()
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const res = await axios.get(`/api/stats/overview/${encodeURIComponent(shortCode)}`)
      const payload = res?.data ?? null
      // 兼容后端 envelope：{ data: {...} } / { result: {...} }
      const overview = payload?.data ?? payload?.result ?? payload
      state.data.value = overview || null
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

export function useFetchTrend(shortCode, days = 7) {
  const state = createApiState()
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const res = await axios.get(`/api/stats/trend/${encodeURIComponent(shortCode)}`, { params: { days } })
      const payload = res?.data ?? null
      // 兼容多种列表结构：数组 / { items } / { data } / { content }
      const list = Array.isArray(payload)
        ? payload
        : Array.isArray(payload?.items)
          ? payload.items
          : Array.isArray(payload?.data)
            ? payload.data
            : Array.isArray(payload?.content)
              ? payload.content
              : []
      state.data.value = list
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

export function useFetchDistribution(shortCode, filtersRef) {
  const state = createApiState()
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const body = { code: shortCode, ...(filtersRef && filtersRef.value ? filtersRef.value : {}) }
      const res = await axios.post('/api/stats/distribution', body, { headers: { 'Content-Type': 'application/json;charset=utf-8' } })
      const payload = res?.data ?? null
      const dist = payload?.data ?? payload?.result ?? payload ?? {}
      state.data.value = {
        device: Array.isArray(dist?.device) ? dist.device : Array.isArray(dist?.devices) ? dist.devices : [],
        city: Array.isArray(dist?.city) ? dist.city : Array.isArray(dist?.cities) ? dist.cities : [],
        referer: Array.isArray(dist?.referer) ? dist.referer : Array.isArray(dist?.referrers) ? dist.referrers : []
      }
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

export function useCompareTrend() {
  const state = createApiState()
  const refresh = async (codes, days = 7) => {
    state.loading.value = true
    state.error.value = null
    try {
      const res = await axios.get('/api/stats/compare', { params: { trends: codes.join(','), days } })
      state.data.value = res?.data || {}
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

export function useClipboard() {
  const copied = ref(false)
  async function copy(text) {
    try {
      await navigator.clipboard.writeText(text)
      copied.value = true
      setTimeout(() => (copied.value = false), 2000)
      return true
    } catch (e) {
      return false
    }
  }
  return { copied, copy }
}

// 新增：点击统计接口 /api/urls/click-stats
export function useFetchClickStats() {
  const state = createApiState()
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const res = await axios.get('/api/urls/click-stats')
      const payload = res?.data ?? null
      // 兼容包裹结构：数组 / { data } / { items } / { content }
      const list = Array.isArray(payload)
        ? payload
        : Array.isArray(payload?.data)
          ? payload.data
          : Array.isArray(payload?.items)
            ? payload.items
            : Array.isArray(payload?.content)
              ? payload.content
              : []
      // 仅保留我们需要的字段，避免类型不一致
      state.data.value = list.map(x => ({
        shortCode: x?.shortCode,
        totalVisits: Number(x?.totalVisits ?? 0),
        todayVisits: Number(x?.todayVisits ?? 0)
      }))
    } catch (e) {
      state.error.value = e
    } finally {
      state.loading.value = false
    }
  }
  return { ...state, refresh }
}

// 导出 CSV/JSON（直接触发浏览器下载）
export async function exportStats(shortCode, params = {}, format = 'csv') {
  const url = `/api/stats/export?format=${encodeURIComponent(format)}`
  const body = { code: shortCode, ...(params || {}) }
  const res = await axios.post(url, body, { responseType: 'blob', headers: { 'Content-Type': 'application/json;charset=utf-8' } })
  const blob = res.data
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `stats-${shortCode}-${format}.` + (format === 'csv' ? 'csv' : 'json')
  document.body.appendChild(a)
  a.click()
  setTimeout(() => {
    URL.revokeObjectURL(a.href)
    document.body.removeChild(a)
  }, 100)
}
