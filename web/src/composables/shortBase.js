????????export const SHORT_BASE = (
  import.meta?.env?.VITE_SHORT_BASE
) || (
  typeof window !== 'undefined' && window.location && window.location.origin
    ? window.location.origin
    : 'http://47.97.110.128'
)

export const API_BASE = import.meta?.env?.VITE_API_BASE || ''

export function cleanShortUrl(url) {
  if (!url) return ''
  return url.replace(/^http:\/\/localhost:8080/i, SHORT_BASE)
}
