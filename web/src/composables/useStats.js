import { ref } from 'vue'
import axios from 'axios'
import { API_BASE } from './shortBase'
// 只有在 API_BASE 有值时才设置 baseURL
try { if (API_BASE && API_BASE.trim()) { axios.defaults.baseURL = API_BASE } } catch {}

function createApiState() {
  return { data: ref(null), loading: ref(false), error: ref(null) }
}

export function useFetchList(pageRef, sizeRef) {
  const state = createApiState()
  state.meta = ref({ totalElements: 0, totalPages: 1, size: 10, number: 0 })
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const p = (pageRef && typeof pageRef === 'object' && 'value' in pageRef) ? Number(pageRef.value) : Number(pageRef || 1)
      const s = (sizeRef && typeof sizeRef === 'object' && 'value' in sizeRef) ? Number(sizeRef.value) : Number(sizeRef || 10)
      const res = await axios.get('/api/urls', { params: { page: Math.max(0, p - 1), size: Math.max(1, s) } })
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
      try {
        const { SHORT_BASE } = await import('../composables/shortBase')
        state.data.value = (list || []).map((it) => {
          const raw = String(it?.shortUrl || '')
          const last = (raw.split('/').pop() || '').split('?')[0]
          const code = String(it?.shortCode || last || '')
          const fixed = code ? `${SHORT_BASE}/${encodeURIComponent(code)}` : raw
          return { ...it, shortUrl: fixed }
        })
        state.meta.value = {
          totalElements: Number(payload?.data?.totalElements ?? payload?.totalElements ?? (Array.isArray(list) ? list.length : 0)),
          totalPages: Number(payload?.data?.totalPages ?? payload?.totalPages ?? 1),
          size: Number(payload?.data?.size ?? payload?.size ?? s),
          number: Number(payload?.data?.number ?? payload?.number ?? Math.max(0, p - 1))
        }
      } catch {
        state.data.value = list
        state.meta.value = {
          totalElements: Number(payload?.data?.totalElements ?? payload?.totalElements ?? (Array.isArray(list) ? list.length : 0)),
          totalPages: Number(payload?.data?.totalPages ?? payload?.totalPages ?? 1),
          size: Number(payload?.data?.size ?? payload?.size ?? s),
          number: Number(payload?.data?.number ?? payload?.number ?? Math.max(0, p - 1))
        }
      }
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

export function useFetchTrend(shortCode, daysSource = 7) {
  const state = createApiState()
  const getDays = () => (typeof daysSource === 'object' && daysSource !== null && 'value' in daysSource)
    ? (daysSource.value ?? 7)
    : daysSource
  const refresh = async () => {
    state.loading.value = true
    state.error.value = null
    try {
      const d = getDays()
      console.debug('[trend] GET /api/stats/trend', { shortCode, days: d })
      const res = await axios.get(`/api/stats/trend/${encodeURIComponent(shortCode)}`, { params: { days: d } })
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
      console.error('[trend] request failed', e)
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
      const raw = (filtersRef && filtersRef.value) ? { ...filtersRef.value } : {}
      const body = { code: shortCode, ...Object.fromEntries(Object.entries(raw).filter(([k,v]) => v !== undefined && v !== null && String(v).trim() !== '')) }
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
  const body = { code: shortCode, ...Object.fromEntries(Object.entries(params||{}).filter(([k,v]) => v !== undefined && v !== null && String(v).trim() !== '')) }
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