// 简单的 SSE 订阅工具，支持自动重连和心跳
export function subscribeRealtime(shortCode, onMessage, onOpen, opts = {}) {
  if (!shortCode) throw new Error('subscribeRealtime requires shortCode')
  const base = opts.base || ''
  const channels = opts.channels || ['trend','events']
  const query = `channels=${channels.join(',')}`
  const url = `${base}/api/stats/realtime/${encodeURIComponent(shortCode)}?${query}`

  let es = null
  let retries = 0
  let stopped = false

  function connect() {
    if (stopped) return
    try {
      es = new EventSource(url)
    } catch (e) {
      // EventSource unsupported
      onOpen && onOpen({ ok: false, reason: 'EventSource not supported' })
      return
    }

    es.onopen = (ev) => {
      retries = 0
      onOpen && onOpen({ ok: true, ev })
    }

    es.onerror = (err) => {
      // 尝试重连（后端返回 204/204 等时，EventSource 会触发 error）
      onMessage && onMessage({ type: 'error', error: err })
      // 关闭并重连
      if (es) { try { es.close() } catch (e) {} }
      es = null
      if (stopped) return
      const delay = Math.min(30000, 1000 * Math.pow(1.5, retries))
      retries++
      setTimeout(() => { if (!stopped) connect() }, delay)
    }

    es.onmessage = (e) => {
      try {
        const payload = JSON.parse(e.data)
        onMessage && onMessage(payload)
      } catch (err) {
        onMessage && onMessage({ type: 'raw', data: e.data })
      }
    }
  }

  connect()

  return {
    stop() { stopped = true; if (es) try { es.close() } catch (e) {} es = null },
  }
}

