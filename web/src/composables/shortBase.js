const PUBLIC_IP = 'http://47.97.110.128'
export const SHORT_BASE = PUBLIC_IP
export const API_BASE = (import.meta?.env?.VITE_API_BASE) || ''
export function cleanShortUrl(url) {
  if (!url) return ''
  return url.replace(/^http:\/\/localhost:8080/i, PUBLIC_IP)
}
