<template>
  <!-- Top Navbar: black with gradient brand text and accent line -->
  <header class="fixed top-0 left-0 right-0 relative" :style="navStyle">
    <div class="flex items-center justify-between py-4 px-6 md:px-[10%]">
      <div class="font-bold" :style="brandTextStyle">TinyFlow</div>
      <div class="flex items-center gap-4">
        <a :href="apiBase + '/api/history'" target="_blank" class="text-[14px]" style="color:#FFFFFF">历史记录</a>
        <a
          href="https://github.com/Layau-code/TinyFlow"
          target="_blank"
          class="flex overflow-hidden items-center text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white disabled:pointer-events-none disabled:opacity-50 text-white shadow hover:opacity-95 h-9 px-4 py-2 max-w-52 whitespace-pre md:flex group relative justify-center gap-2 rounded-md transition-all duration-300 ease-out hover:ring-2 hover:ring-white hover:ring-offset-2"
          :style="githubBtnStyle"
        >
          <span class="absolute right-0 -mt-12 h-32 w-8 translate-x-12 rotate-12 bg-white opacity-10 transition-all duration-1000 ease-out group-hover:-translate-x-40"></span>
          <div class="flex items-center">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 438.549 438.549" xmlns="http://www.w3.org/2000/svg">
              <path d="M409.132 114.573c-19.608-33.596-46.205-60.194-79.798-79.8-33.598-19.607-70.277-29.408-110.063-29.408-39.781 0-76.472 9.804-110.063 29.408-33.596 19.605-60.192 46.204-79.8 79.8C9.803 148.168 0 184.854 0 224.63c0 47.78 13.94 90.745 41.827 128.906 27.884 38.164 63.906 64.572 108.063 79.227 5.14.954 8.945.283 11.419-1.996 2.475-2.282 3.711-5.14 3.711-8.562 0-.571-.049-5.708-.144-15.417a2549.81 2549.81 0 01-.144-25.406l-6.567 1.136c-4.187.767-9.469 1.092-15.846 1-6.374-.089-12.991-.757-19.842-1.999-6.854-1.231-13.229-4.086-19.13-8.559-5.898-4.473-10.085-10.328-12.56-17.556l-2.855-6.57c-1.903-4.374-4.899-9.233-8.992-14.559-4.093-5.331-8.232-8.945-12.419-10.848l-1.999-1.431c-1.332-.951-2.568-2.098-3.711-3.429-1.142-1.331-1.997-2.663-2.568-3.997-.572-1.335-.098-2.43 1.427-3.289 1.525-.859 4.281-1.276 8.28-1.276l5.708.853c3.807.763 8.516 3.042 14.133 6.851 5.614 3.806 10.229 8.754 13.846 14.842 4.38 7.806 9.657 13.754 15.846 17.847 6.184 4.093 12.419 6.136 18.699 6.136 6.28 0 11.704-.476 16.274-1.423 4.565-.952 8.848-2.383 12.847-4.285 1.713-12.758 6.377-22.559 13.988-29.41-10.848-1.14-20.601-2.857-29.264-5.14-8.658-2.286-17.605-5.996-26.835-11.14-9.235-5.137-16.896-11.516-22.985-19.126-6.09-7.614-11.088-17.61-14.987-29.979-3.901-12.374-5.852-26.648-5.852-42.826 0-23.035 7.52-42.637 22.557-58.817-7.044-17.318-6.379-36.732 1.997-58.24 5.52-1.715 13.706-.428 24.554 3.853 10.85 4.283 18.794 7.952 23.84 10.994 5.046 3.041 9.089 5.618 12.135 7.708 17.705-4.947 35.976-7.421 54.818-7.421s37.117 2.474 54.823 7.421l10.849-6.849c7.419-4.57 16.18-8.758 26.262-12.565 10.088-3.805 17.802-4.853 23.134-3.138 8.562 21.509 9.325 40.922 2.279 58.24 15.036 16.18 22.559 35.787 22.559 58.817 0 16.178-1.958 30.497-5.853 42.966-3.9 12.471-8.941 22.457-15.125 29.979-6.191 7.521-13.901 13.85-23.131 18.986-9.232 5.14-18.182 8.85-26.84 11.136-8.662 2.286-18.415 4.004-29.263 5.146 9.894 8.562 14.842 22.077 14.842 40.539v60.237c0 3.422 1.19 6.279 3.572 8.562 2.379 2.279 6.136 2.95 11.276 1.995 44.163-14.653 80.185-41.062 108.068-79.226 27.88-38.161 41.825-81.126 41.825-128.906-.01-39.771-9.818-76.454-29.414-110.049z" />
            </svg>
            <span class="ml-1 text-white">Star on GitHub</span>
          </div>
          <div class="ml-2 hidden md:flex items-center gap-1 text-sm">
            <svg class="w-4 h-4 text-gray-200 transition-all duration-300 group-hover:text-yellow-300" aria-hidden="true" fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path clip-rule="evenodd" fill-rule="evenodd" d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.006 5.404.434c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.434 2.082-5.005Z" />
            </svg>
            <span class="inline-block tabular-nums tracking-wider font-display font-medium text-white">Star</span>
          </div>
        </a>
      </div>
    </div>
    <div class="absolute bottom-0 left-0 right-0 h-[2px]" :style="navAccentStyle"></div>
  </header>

  <!-- Rest of page -->
  <main class="bg-white min-h-screen">
    <section class="hero pt-28 pb-16">
      <div class="hero-inner flex flex-col items-center justify-center px-6">
        <h1 class="text-center font-semibold mb-10" style="color:#1F2329;font-size:clamp(36px,6vw,48px);line-height:1.2">让链接变短，让分享更轻松</h1>
        <p class="text-center mb-10" style="color:#4B5563;font-size:18px;line-height:1.6">一键生成短链，支持自定义别名、访问统计与 API 调用</p>

        <!-- Input + Button -->
        <div class="w-full max-w-[640px] flex gap-3 flex-col md:flex-row">
          <input
            v-model="inputUrl"
            type="text"
            placeholder="粘贴长链接，例如 https://example.com"
            class="w-full rounded-[12px] h-14 px-6 outline-none border"
            :style="inputStyle"
            @focus="focusInput = true"
            @blur="focusInput = false"
            @keydown.enter="shortenUrl"
          />
          <button
            @click="shortenUrl"
            class="rounded-[12px] h-14 px-6 text-white font-medium w-full md:w-auto"
            :style="buttonStyle"
            @mouseenter="hoverGen=true"
            @mouseleave="hoverGen=false"
          >
            生成短链
          </button>
        </div>
        <LoadingSpinner v-if="generating" />
        <!-- API说明 -->
        <p class="w-full max-w-[640px] mt-3 text-[14px]" style="color:#4B5563">
          请输入需要缩短的长链接，点击生成后我们将通过 <span class="font-mono">/shorten</span> API 为您创建一个简洁的短链接。
        </p>

        <!-- Inline Result: show under input -->
        <div v-if="shortUrl" class="w-full max-w-[640px] mt-4">
          <div class="flex items-center gap-3 flex-wrap">
            <a :href="shortUrl" target="_blank" class="underline break-all" style="color:#6B72FF">{{ shortUrl }}</a>
            <button @click="copyShortUrl" class="text-[14px]" :style="copyBtnStyle">{{ copyLabel }}</button>
            <button @click="visitOriginal" class="text-[14px]" style="color:#6B72FF">访问</button>
          </div>
          <div v-if="copyLabel==='已复制'" class="mt-2 text-[14px]" style="color:#10B981">已复制到剪贴板</div>
        </div>
      </div>
    </section>

    <!-- History list and controls -->
    <section class="px-6 mt-6">
      <div class="max-w-[1200px] mx-auto">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold" style="color:#1F2329;font-size:24px;margin-top:48px">历史记录</h2>
          <button
            @click="refreshHistory"
            :disabled="refreshing"
            class="rounded-[12px] h-9 px-4 text-white font-medium"
            :style="refreshButtonStyle"
          >
            {{ refreshing ? '刷新中…' : '刷新历史' }}
          </button>
        </div>
        <div v-if="history.length === 0" class="text-[14px]" style="color:#9CA3AF">暂无历史记录</div>
        <div v-else class="space-y-3">
          <!-- History list item: add favicon at left -->
          <div
            v-for="item in history"
            :key="item.id || extractCode(item) || item.shortUrl"
            class="p-4 rounded-[16px] border"
            style="border-color:#F3F4F6;background:#fff"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0">
                <img
                  :src="getFavicon(item)"
                  @error="onFaviconError($event, item)"
                  alt="icon"
                  class="w-6 h-6 rounded"
                  style="flex:none;transition: all 0.2s ease"
                />
                <code class="text-[13px] font-mono" style="color:#6B72FF">{{ extractCode(item) }}</code>
              </div>
              <button
                @click="deleteHistoryItem(item)"
                :disabled="deletingIds.has(item.id)"
                @mouseenter="hoverDeleteId = item.id"
                @mouseleave="hoverDeleteId = null"
                class="text-[16px]"
                :style="deleteBtnStyle(item.id)"
                aria-label="删除"
                title="删除"
              >×</button>
            </div>
            <div class="mt-2 flex items-center gap-3 min-w-0">
              <a :href="displayShortUrl(item)" target="_blank" class="underline truncate" style="color:#6B72FF;max-width:70%">{{ displayShortUrl(item) }}</a>
              <button @click="copyLink(displayShortUrl(item), item.id)" class="text-[13px]" style="color:#6B72FF;transition: all 0.2s ease">复制</button>
            </div>
            <div v-if="copiedId===item.id" class="mt-1 text-[12px]" style="color:#10B981">已复制</div>
            <div class="mt-1 truncate" style="color:#4B5563">原始链接：
              <a :href="item.longUrl" target="_blank" class="underline truncate" style="color:#6B72FF;max-width:100%">{{ item.longUrl }}</a>
            </div>
            <div class="mt-1 text-[12px]" style="color:#9CA3AF">创建时间：{{ formatDate(item.createdAt || item.createTime || item.timestamp) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features highlight (optional) -->
    <section class="mt-16 bg-[#F9FAFB]">
      <div class="max-w-[1200px] mx-auto py-16 px-6 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="p-6 rounded-[16px] border" style="border-color:#F3F4F6;background:#fff">
          <div class="font-semibold mb-2" style="color:#1F2329">自定义别名</div>
          <div style="color:#4B5563">可自定义短链后缀，增强品牌一致性</div>
        </div>
        <div class="p-6 rounded-[16px] border" style="border-color:#F3F4F6;background:#fff">
          <div class="font-semibold mb-2" style="color:#1F2329">访问统计</div>
          <div style="color:#4B5563">统计点击与来源，数据驱动优化分享</div>
        </div>
        <div class="p-6 rounded-[16px] border" style="border-color:#F3F4F6;background:#fff">
          <div class="font-semibold mb-2" style="color:#1F2329">实时同步</div>
          <div style="color:#4B5563">通过 API 刷新/删除历史记录，轻松管理与访问</div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="border-t mt-16">
      <div class="text-center py-6 text-[14px]" style="color:#9CA3AF;border-color:#F3F4F6">
        © 2025 TinyFlow · 开源 · 隐私政策 · 反馈建议 ·
        <a href="mailto:support@tinyflow.local" style="color:#6B72FF">联系我们</a>
      </div>
    </footer>
  </main>
</template>

<script>
import axios from 'axios'
import LoadingSpinner from './src/components/LoadingSpinner.vue'

const API_BASE = 'http://localhost:8080' // 用于顶部“历史记录”跳转链接
const api = axios.create({ baseURL: '' }) // 使用 Vite dev 代理转发 /api 与 /shorten

export default {
  name: 'App',
  components: { LoadingSpinner },
  data() {
    return {
      apiBase: API_BASE,
      inputUrl: '',
      longUrl: '',
      shortUrl: '',
      copyLabel: '复制链接',
      focusInput: false,
      hoverGen: false,
      generating: false,
      scrolled: false,
      copyTimer: null,
      history: [],
      refreshing: false,
      deletingIds: new Set(),
      hoverDeleteId: null,
      copiedId: null,
      copyItemTimer: null,
    }
  },
  computed: {
    navStyle() {
      return {
        backgroundColor: '#0F1115'
      }
    },
    brandTextStyle() {
      return {
        fontSize: '20px',
        backgroundImage: 'linear-gradient(135deg, #6B72FF 0%, #8A6BFF 60%, #A66BFF 100%)',
        WebkitBackgroundClip: 'text',
        backgroundClip: 'text',
        color: 'transparent',
        letterSpacing: '0.3px'
      }
    },
    navAccentStyle() {
      return {
        background: 'linear-gradient(90deg, #6B72FF, #8A6BFF, #A66BFF)',
        opacity: 0.6
      }
    },
    githubBtnStyle() {
      return {
        background: 'linear-gradient(135deg, #6B72FF 0%, #8A6BFF 50%, #A66BFF 100%)'
      }
    },
    inputStyle() {
      return {
        borderColor: this.focusInput ? '#8A6BFF' : '#E5E7EB',
        boxShadow: this.focusInput ? '0 0 0 4px rgba(138,107,255,0.1)' : 'none',
      }
    },
    buttonStyle() {
      const base = 'linear-gradient(135deg, #6B72FF 0%, #8A6BFF 50%, #A66BFF 100%)'
      const hover = 'linear-gradient(135deg, #7E6BFF 0%, #9A6BFF 50%, #B66BFF 100%)'
      return { background: this.hoverGen ? hover : base, transform: this.hoverGen ? 'translateY(-2px) scale(1.02)' : 'none' }
    },
    copyBtnStyle() {
      return { color: this.copyLabel === '已复制' ? '#10B981' : '#6B72FF' }
    },
    refreshButtonStyle() {
      const base = 'linear-gradient(135deg, #6B72FF 0%, #8A6BFF 50%, #A66BFF 100%)'
      const disabled = 'linear-gradient(135deg, #7E6BFF 0%, #9A6BFF 50%, #B66BFF 100%)'
      return { background: this.refreshing ? disabled : base, opacity: this.refreshing ? 0.8 : 1 }
    },
  },
  methods: {
    async shortenUrl() {
      const payload = { url: this.inputUrl?.trim() }
      if (!payload.url) return
      const isValid = /^https?:\/\/\S+/i.test(payload.url)
      if (!isValid) {
        alert('请输入有效的 URL（以 http:// 或 https:// 开头）')
        return
      }
      this.generating = true
      try {
        // 纯文本 POST /shorten
        const res = await api.post('/shorten', payload.url, { headers: { 'Content-Type': 'text/plain' } })
        const shortUrl = res?.data?.shortUrl ?? res?.data ?? ''
        this.shortUrl = shortUrl
        this.longUrl = payload.url
        // 本地插入至历史顶部，无需整页刷新
        const newItem = {
          id: res?.data?.id || res?.data?.code || Date.now(),
          shortUrl: shortUrl,
          longUrl: payload.url,
          code: this.extractCode({ shortUrl }),
          createdAt: new Date().toISOString(),
        }
        this.history = [newItem, ...(this.history || [])]
      } catch (error) {
        const status = error?.response?.status
        if (status === 400) alert('链接格式无效或请求错误')
        else if (status === 409) alert('短码冲突，请重试')
        else alert('生成失败，请稍后再试')
        console.error('Shorten error:', error)
      } finally {
        this.generating = false
      }
    },
    async copyShortUrl() {
      if (!this.shortUrl) return
      try {
        await navigator.clipboard.writeText(this.shortUrl)
        this.copyLabel = '已复制'
        clearTimeout(this.copyTimer)
        this.copyTimer = setTimeout(() => { this.copyLabel = '复制链接' }, 2000)
      } catch (e) {
        console.error('复制失败:', e)
      }
    },
    visitOriginal() {
      if (!this.longUrl) return
      window.open(this.longUrl, '_blank')
    },
    extractCode(item) {
      // Prefer explicit code fields from backend, then derive from shortUrl
      const direct = item?.code || item?.shortCode || item?.shortId || item?.alias || item?.slug || item?.key
      if (direct) return String(direct)
      const url = item?.shortUrl || ''
      try {
        const u = new URL(url)
        const path = u.pathname.replace(/^\//, '')
        return path || ''
      } catch {
        const parts = url.split('/')
        return parts[parts.length - 1] || ''
      }
    },
    buildShortUrl(code) {
      if (!code) return ''
      try {
        return new URL('/' + String(code), window.location.origin).href
      } catch {
        return (window.location.origin || '') + '/' + String(code)
      }
    },
    displayShortUrl(item) {
      return item?.shortUrl || this.buildShortUrl(this.extractCode(item))
    },
    async copyLink(url, id) {
      try {
        await navigator.clipboard.writeText(url)
        this.copiedId = id
        clearTimeout(this.copyItemTimer)
        this.copyItemTimer = setTimeout(() => { this.copiedId = null }, 2000)
      } catch (e) {
        console.error('复制失败:', e)
      }
    },
    async refreshHistory() {
      if (this.refreshing) return
      this.refreshing = true
      try {
        const res = await api.get('/api/history/refresh')
        const list = Array.isArray(res?.data) ? res.data : (res?.data?.items || [])
        this.history = (list || []).map((it) => {
          const code = this.extractCode(it)
          const shortUrl = it?.shortUrl || this.buildShortUrl(code)
          return { ...it, code, shortUrl }
        })
      } catch (err) {
        alert('获取历史记录失败，请稍后重试')
        console.error('History refresh error:', err)
      } finally {
        this.refreshing = false
      }
    },
    async deleteHistoryItem(item) {
      const id = item?.id
      if (!id) return
      this.deletingIds.add(id)
      try {
        const res = await api.delete(`/api/history/${id}`)
        if (res?.status === 204 || res?.status === 200) {
          // 成功后本地移除项，无需刷新
          this.history = (this.history || []).filter(x => x?.id !== id)
        }
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('History delete error:', err)
      } finally {
        this.deletingIds.delete(id)
      }
    },
    deleteBtnStyle(id) {
      return {
        color: this.deletingIds.has(id) ? '#9CA3AF' : (this.hoverDeleteId === id ? '#EF4444' : '#9CA3AF'),
        transition: 'all 0.2s ease'
      }
    },
    formatDate(value) {
      if (!value) return '-'
      try {
        const date = typeof value === 'number' ? new Date(value) : new Date(String(value))
        return date.toLocaleString()
      } catch { return String(value) }
    },
    onScroll() {
      this.scrolled = window.scrollY > 8
    },
    getFavicon(item) {
      const url = item?.longUrl || item?.shortUrl || ''
      try {
        const host = new URL(url).hostname
        return `https://icons.duckduckgo.com/ip3/${host}.ico`
      } catch {
        return ''
      }
    },
    onFaviconError(ev, item) {
      try {
        const u = new URL(item?.longUrl || item?.shortUrl)
        ev.target.src = `${u.origin}/favicon.ico`
        ev.target.onerror = null
      } catch {
        ev.target.style.display = 'none'
      }
    },
  },
  mounted() {
    this.refreshHistory()
  }
}
</script>