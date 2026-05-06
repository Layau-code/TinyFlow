export interface Range { from?: string | number; to?: string | number }
export interface SummaryParams { range?: Range; metric?: string; groupBy?: string; page?: number; pageSize?: number }
export interface DetailParams { id: string; page?: number; pageSize?: number }

// Custom error types for clearer handling
export class ApiError extends Error {
  status?: number
  body?: string
  url?: string
  constructor(message: string, opts?: { status?: number; body?: string; url?: string }) {
    super(message)
    this.name = 'ApiError'
    if (opts) {
      this.status = opts.status
      this.body = opts.body
      this.url = opts.url
    }
  }
}

export class TimeoutError extends Error {
  constructor(message = 'Request timed out') { super(message); this.name = 'TimeoutError' }
}

async function fetchWithTimeout(url: string, init: RequestInit = {}, timeoutMs = 15000, externalSignal?: AbortSignal) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)
  let onExternalAbort: (() => void) | null = null
  try {
    if (externalSignal) {
      if (externalSignal.aborted) controller.abort()
      else {
        onExternalAbort = () => controller.abort()
        externalSignal.addEventListener('abort', onExternalAbort)
      }
    }
    return await fetch(url, { ...init, signal: controller.signal })
  } finally {
    clearTimeout(timer)
    if (externalSignal && onExternalAbort) externalSignal.removeEventListener('abort', onExternalAbort)
  }
}

// Helper: retry fetch with exponential backoff for transient errors
async function retryFetch(url: string, init: RequestInit = {}, timeoutMs = 15000, retries = 0, externalSignal?: AbortSignal) {
  let attempt = 0
  const baseDelay = 300
  while (true) {
    try {
      const res = await fetchWithTimeout(url, init, timeoutMs, externalSignal)
      // if server error (5xx) and we still have retries, throw to retry
      if (res.status >= 500 && attempt < retries) {
        throw new Error(`Server error ${res.status}`)
      }
      return res
    } catch (err) {
      // if aborted by external signal, rethrow immediately
      if (externalSignal && externalSignal.aborted) throw err
      if (attempt >= retries) throw err
      const delay = baseDelay * Math.pow(2, attempt) + Math.floor(Math.random() * 100)
      await sleepThenable(delay)
      attempt++
    }
  }
}

// Simple in-memory cache for summaries (use plain object to avoid relying on Map support in older lib settings)
const summaryCache: Record<string, { expiresAt: number; data: any } | undefined> = {}

// lightweight thenable sleep to avoid using `new Promise` (some TS lib configs lack Promise type at analysis time)
function sleepThenable(ms: number) {
  return { then: (resolve: () => void) => setTimeout(resolve, ms) }
}

export async function getSummary(
  params: SummaryParams | any = {},
  options: { timeoutMs?: number; retries?: number; cacheTTL?: number; force?: boolean; signal?: AbortSignal } = {}
) {
  // params: { range, metric, groupBy, page, pageSize }
  const query = new URLSearchParams()

  const page = typeof params.page === 'number' ? params.page : 0
  const pageSize = typeof params.pageSize === 'number' ? params.pageSize : 100

  if (params.metric) query.set('metric', String(params.metric))
  if (params.groupBy) query.set('groupBy', String(params.groupBy))
  query.set('page', String(page))
  query.set('pageSize', String(pageSize))
  if (params.range?.from !== undefined && params.range?.from !== null) query.set('from', String(params.range.from))
  if (params.range?.to !== undefined && params.range?.to !== null) query.set('to', String(params.range.to))

  const url = `/api/stats/summary?${query.toString()}`

  const cacheTTL = typeof options.cacheTTL === 'number' ? options.cacheTTL : 30 * 1000 // default 30s
  const cacheKey = url
  if (!options.force) {
    const cached = summaryCache[cacheKey]
    if (cached && Date.now() < cached.expiresAt) return cached.data
  }

  try {
    const res = await retryFetch(url, undefined, options.timeoutMs ?? 15000, options.retries ?? 0, options.signal)
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new ApiError(`API error ${res.status} ${res.statusText}`, { status: res.status, body: text, url })
    }
    const json = await res.json()
    // cache the result
    summaryCache[cacheKey] = { expiresAt: Date.now() + cacheTTL, data: json }
    return json
  } catch (err: any) {
    if (err && err.name === 'AbortError') throw new TimeoutError()
    throw err
  }
}

export async function getDetail(
  params: DetailParams | any,
  options: { timeoutMs?: number; retries?: number; signal?: AbortSignal } = {}
) {
  // params: { id, page, pageSize }
  if (!params || (!params.id && params.id !== 0)) throw new Error('getDetail: missing required parameter `id`')

  const query = new URLSearchParams()
  const page = typeof params.page === 'number' ? params.page : 0
  const pageSize = typeof params.pageSize === 'number' ? params.pageSize : 100
  query.set('page', String(page))
  query.set('pageSize', String(pageSize))

  const encodedId = encodeURIComponent(String(params.id))
  const url = `/api/stats/detail/${encodedId}?${query.toString()}`

  try {
    const res = await retryFetch(url, undefined, options.timeoutMs ?? 15000, options.retries ?? 0, options.signal)
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new ApiError(`API error ${res.status} ${res.statusText}`, { status: res.status, body: text, url })
    }
    return await res.json()
  } catch (err: any) {
    if (err && err.name === 'AbortError') throw new TimeoutError()
    throw err
  }
}

// Utility: allow manual cache invalidation
export function clearSummaryCache() { Object.keys(summaryCache).forEach(k => delete summaryCache[k]) }
