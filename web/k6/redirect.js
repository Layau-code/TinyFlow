import http from 'k6/http'
import { check, sleep } from 'k6'
function htmlReportLocal(data) {
  const m = data.metrics || {}
  const g = (name, stat) => {
    const mm = m[name];
    if (!mm) return ''
    const s = mm.values || mm
    const val = stat ? s[stat] : s.avg
    return typeof val === 'number' ? Number(val).toFixed(2) : String(val || '')
  }
  const rows = [
    ['Iterations', String(data.root_group?.checks ? data.root_group.checks.length : data.metrics?.iterations?.count || '')],
    ['Requests', g('http_reqs','count')],
    ['Fail rate', g('http_req_failed','rate')],
    ['RPS (mean)', g('http_reqs','rate')],
    ['Duration avg (ms)', g('http_req_duration','avg')],
    ['Duration p95 (ms)', g('http_req_duration','p(95)')],
    ['TTFB p95 (ms)', g('http_req_waiting','p(95)')],
  ]
  const tr = rows.map(([k,v])=>`<tr><td>${k}</td><td>${v}</td></tr>`).join('')
  return `<!doctype html><html><head><meta charset="utf-8"><title>K6 Report</title>
  <style>body{font-family:system-ui,Segoe UI,Arial;padding:20px;background:#fafafa;color:#222}h1{margin:0 0 12px}table{border-collapse:collapse;width:600px;max-width:100%}td{border:1px solid #ddd;padding:8px}td:first-child{font-weight:600;background:#f4f4f4}</style></head>
  <body><h1>K6 Load Test Summary</h1><table>${tr}</table></body></html>`
}

export const options = {
  stages: [
    { duration: '30s', target: Number(__ENV.VUS1 || 100) },
    { duration: '2m', target: Number(__ENV.VUS2 || 500) },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'],
    checks: ['rate>0.99'],
  },
}

const BASE = __ENV.BASE || 'http://localhost:8080'
let codes = (__ENV.CODES || '千问,腾讯技术').split(',').map(s => s.trim()).filter(Boolean)
if (codes.length === 0) codes = ['test']

export default function () {
  const code = codes[Math.floor(Math.random() * codes.length)]
  const url = `${BASE}/${encodeURIComponent(code)}`
  const res = http.get(url, { redirects: 0 })
  check(res, { '302': r => r.status === 302 })
  sleep(Number(__ENV.SLEEP || 0.3))
}

export function handleSummary(data) {
  return {
    'summary.json': JSON.stringify(data),
    'summary.html': htmlReportLocal(data),
  }
}
