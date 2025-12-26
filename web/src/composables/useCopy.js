/**
 * 复制到剪贴板工具函数
 * 兼容 HTTP 和 HTTPS 环境
 */
export function copyToClipboard(text) {
  return new Promise((resolve, reject) => {
    // 方法1: 现代浏览器的 Clipboard API (需要 HTTPS)
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => resolve())
        .catch(() => {
          // 降级到方法2
          fallbackCopy(text, resolve, reject)
        })
    } else {
      // 方法2: 传统方法 (兼容 HTTP)
      fallbackCopy(text, resolve, reject)
    }
  })
}

/**
 * 降级复制方法：使用 execCommand
 * 兼容老版本浏览器和 HTTP 环境
 */
function fallbackCopy(text, resolve, reject) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.top = '0'
  textarea.style.left = '0'
  textarea.style.opacity = '0'
  textarea.style.pointerEvents = 'none'
  
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  
  try {
    const successful = document.execCommand('copy')
    document.body.removeChild(textarea)
    
    if (successful) {
      resolve()
    } else {
      reject(new Error('复制命令执行失败'))
    }
  } catch (err) {
    document.body.removeChild(textarea)
    reject(err)
  }
}
