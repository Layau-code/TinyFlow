import { ref } from 'vue'
import axios from 'axios'

export function useApi(getUrl, { immediate = true, transform } = {}) {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const exec = async () => {
    const url = typeof getUrl === 'function' ? getUrl() : getUrl
    if (!url) return
    loading.value = true
    error.value = null
    try {
      const res = await axios.get(url)
      const payload = res?.data ?? null
      data.value = transform ? transform(payload) : payload
    } catch (e) {
      error.value = e
    } finally {
      loading.value = false
    }
  }

  if (immediate) exec()

  return { data, loading, error, refresh: exec }
}