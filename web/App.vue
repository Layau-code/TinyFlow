<template>
  <!-- Top Navbar: black with gradient brand text and accent line -->
  <header class="fixed top-0 left-0 right-0 relative" :style="navStyle">
    <div class="flex items-center justify-between py-4 px-6 md:px-[10%]">
      <div class="font-bold tf-brand" aria-label="TinyFlow"><span class="tf-letter">T</span><span class="tf-letter">i</span><span class="tf-letter">n</span><span class="tf-letter">y</span><span class="tf-letter">F</span><span class="tf-letter">l</span><span class="tf-letter">o</span><span class="tf-letter">w</span></div>
      <div class="flex items-center gap-4">
        <a
          href="#"
          @click.prevent="goToDashboard"
          class="text-[14px] text-[#6B7280] hover:text-black"
          title="查看统计看板"
        >数据统计</a>
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
  <router-view v-if="isStatsOrDashboard" />
  <main class="bg-white min-h-screen" v-show="!isStatsOrDashboard">
    <section class="hero pt-12 pb-16">
      <div class="hero-inner flex flex-col items-center justify-center px-6">
        <h1 class="text-center font-semibold mb-10" style="color:#1F2329;font-size:clamp(36px,6vw,48px);line-height:1.2">让链接变短，让分享更轻松</h1>
        <p class="text-center mb-10" style="color:#4B5563;font-size:18px;line-height:1.6">一键生成短链，支持自定义别名、访问统计与 API 调用</p>

        <!-- Input + Button（按钮在右侧） - 卡片化包裹 -->
        <div class="w-full max-w-[720px] flex flex-col gap-3 p-5 md:p-6 rounded-[16px] border" :style="formCardStyle">
          <div class="flex items-stretch gap-3">
            <input
              v-model="inputUrl"
              type="text"
              placeholder="粘贴长链接，例如 https://example.com"
              class="flex-1 rounded-[12px] h-14 px-6 outline-none border"
              :style="inputStyle(focusInput)"
              @focus="focusInput = true"
              @blur="focusInput = false"
              @keydown.enter="shortenUrl"
            />
            <button
              @click="shortenUrl"
              class="rounded-[12px] h-14 px-6 text-white font-medium whitespace-nowrap"
              :style="buttonStyle"
              @mouseenter="hoverGen=true"
              @mouseleave="hoverGen=false"
            >
              生成短链
            </button>
          </div>
          <input
            v-model="customAlias"
            type="text"
            placeholder="可自定义别名，留空则自动生成"
            class="w-full rounded-[12px] h-12 px-6 outline-none border bg-white"
            :style="inputStyle(focusAlias)"
            @focus="focusAlias = true"
            @blur="focusAlias = false"
            @keydown.enter="shortenUrl"
          />
          <LoadingSpinner v-if="generating" />
        </div>

        <!-- Inline Result: show under input -->
        <div v-if="shortUrl" class="w-full max-w-[720px] mt-4 p-5 md:p-6 rounded-[16px] border bg-white" :style="resultCardStyle">
          <div class="flex flex-col md:flex-row items-start md:items-center gap-6">
            <div class="min-w-0 flex-1">
              <div class="text-sm mb-1" style="color:#4B5563">已生成短链</div>
              <a :href="shortUrl" @click.prevent="redirectViaApi({ shortUrl })" class="underline break-all" style="color:#2B6CEF">{{ decodeUrlText(shortUrl) }}</a>
              <div class="mt-3 flex gap-3">
                <button @click="copyShortUrl" class="text-[14px] px-3 py-1.5 rounded-md border" :style="minorBtnStyle">复制短链</button>
                <button @click="downloadQrPng" class="text-[14px] px-3 py-1.5 rounded-md border" :style="minorBtnStyle">下载二维码</button>
              </div>
              <div v-if="copyLabel==='已复制'" class="mt-2 text-[14px]" style="color:#10B981">已复制到剪贴板</div>
            </div>
            <div class="shrink-0">
              <QrcodeVue ref="qrRef" :value="shortUrl" :size="140" level="M" :foreground="'#2B6CEF'" :background="'#ffffff'" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- History list and controls -->
    <section class="px-6 mt-6">
      <div class="max-w-[1200px] mx-auto">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-semibold" style="color:#1F2329;font-size:24px;margin-top:48px">历史记录</h2>
          <div class="flex items-center gap-3">
            <input v-model="historyQuery" type="text" class="h-9 px-3 rounded-md border outline-none" placeholder="按短码或域名过滤" style="border-color:#E5E7EB" />
            <div class="tf-refresh-container" :class="{ 'is-disabled': refreshing }">
              <div class="hover bt-1"></div>
              <div class="hover bt-2"></div>
              <div class="hover bt-3"></div>
              <div class="hover bt-4"></div>
              <div class="hover bt-5"></div>
              <div class="hover bt-6"></div>
              <button
                class="tf-refresh-btn text-white font-medium"
                :style="refreshButtonStyle"
                :disabled="refreshing"
                @click="refreshHistory"
              >
                {{ refreshing ? '刷新中…' : '刷新历史' }}
              </button>
            </div>
          </div>
        </div>
        <div v-if="filteredHistory.length === 0" class="text-[14px]" style="color:#9CA3AF">暂无历史记录</div>
        <div v-else class="space-y-2">
          <!-- History list item: refined layout to match modern sites -->
          <div
            v-for="item in filteredHistory"
            :key="item.id || extractCode(item) || item.shortUrl"
            class="tf-hist-item p-3 rounded-[12px] border bg-white"
            style="border-color:#F3F4F6"
          >
            <div class="flex items-center justify-between gap-3">
              <!-- Left: favicon + domains + code badge -->
              <div class="flex items-center gap-3 min-w-0">
                <img
                  :src="getFavicon(item)"
                  @error="onFaviconError($event, item)"
                  alt="icon"
                  class="w-5 h-5 rounded"
                  style="flex:none;transition: all 0.2s ease"
                />
                <div class="min-w-0">
                  <div class="flex items-center gap-2 min-w-0">
                    <a :href="displayShortUrl(item)" @click.prevent="redirectViaApi(item)" class="truncate text-[13px]" style="color:#2B6CEF;max-width:52vw">{{ displayShortUrlText(item) }}</a>
                    <span class="tf-code-badge">{{ extractCode(item) }}</span>
                  </div>
                  <div class="mt-0.5 truncate text-[12px]" style="color:#6B7280;max-width:60vw">{{ item.longUrl }}</div>
                </div>
              </div>
              <!-- Right: actions -->
              <div class="flex items-center gap-2 shrink-0">
                <button @click="copyLink(displayShortUrl(item), item.id)" class="tf-copy-btn">复制</button>
                <button
                  @click="deleteHistoryItem(item)"
                  :disabled="deletingIds.has(item.id)"
                  @mouseenter="hoverDeleteId = item.id"
                  @mouseleave="hoverDeleteId = null"
                  class="tf-del-btn"
                  :class="{ 'is-loading': deletingIds.has(item.id) }"
                  aria-label="删除"
                  title="删除"
                >
                  <svg viewBox="0 0 448 512" class="tf-del-icon" aria-hidden="true">
                    <path d="M135.2 17.7L128 32H32C14.3 32 0 46.3 0 64S14.3 96 32 96H416c17.7 0 32-14.3 32-32s-14.3-32-32-32H320l-7.2-14.3C307.4 6.8 296.3 0 284.2 0H163.8c-12.1 0-23.2 6.8-28.6 17.7zM416 128H32L53.2 467c1.6 25.3 22.6 45 47.9 45H346.9c25.3 0 46.3-19.7 47.9-45L416 128z" />
                  </svg>
                </button>
              </div>
            </div>
            <div v-if="copiedId===item.id" class="mt-1 text-[12px]" style="color:#10B981">已复制</div>
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
import QrcodeVue from 'qrcode.vue'

const API_BASE = 'http://localhost:8080' // 用于顶部“历史记录”跳转链接
const api = axios.create({ baseURL: '' }) // 使用 Vite dev 代理转发 /api 与 /shorten

export default {
  name: 'App',
  components: { LoadingSpinner, QrcodeVue },
  data() {
    return {
      apiBase: API_BASE,
      inputUrl: '',
      focusAlias: false,
      customAlias: '',
      longUrl: '',
      shortUrl: '',
      copyLabel: '复制链接',
      focusInput: false,
      hoverGen: false,
      generating: false,
      scrolled: false,
      copyTimer: null,
      history: [],
      historyQuery: '',
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
        backgroundColor: 'rgba(255,255,255,0.85)',
        backdropFilter: 'saturate(180%) blur(8px)',
        WebkitBackdropFilter: 'saturate(180%) blur(8px)',
        transition: 'background-color 300ms ease, backdrop-filter 300ms ease, box-shadow 300ms ease',
        boxShadow: '0 1px 0 rgba(0,0,0,0.04)'
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
      return { display: 'none' }
    },
    githubBtnStyle() {
      return {
        background: 'linear-gradient(135deg, #6B72FF 0%, #8A6BFF 50%, #A66BFF 100%)'
      }
    },
    formCardStyle() {
      return {
        borderColor: '#E5E7EB',
        background: '#FFFFFF',
        boxShadow: 'none'
      }
    },
    resultCardStyle() {
      return { borderColor: '#E5E7EB' }
    },
    minorBtnStyle() {
      return { borderColor: '#E5E7EB', color: '#2B6CEF', background: '#fff' }
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
    isStatsOrDashboard() {
      const p = this.$route?.path || ''
      return p.startsWith('/stats/') || p === '/dashboard'
    },
    filteredHistory() {
      const q = (this.historyQuery || '').trim().toLowerCase()
      if (!q) return this.history
      return (this.history || []).filter((it) => {
        const code = (this.extractCode(it) || '').toLowerCase()
        const host = (() => { try { return new URL(it.longUrl || it.shortUrl || '').hostname.toLowerCase() } catch { return '' } })()
        return code.includes(q) || host.includes(q)
      })
    },
  },
  methods: {
    inputStyle(active) {
      return {
        borderColor: active ? '#8A6BFF' : '#E5E7EB',
        boxShadow: active ? '0 0 0 4px rgba(138,107,255,0.1)' : 'none',
      }
    },
    
    async shortenUrl() {
      const url = this.inputUrl?.trim()
      const alias = this.customAlias?.trim()
      if (!url) return
      const isValid = /^https?:\/\/\S+/i.test(url)
      if (!isValid) {
        alert('请输入有效的 URL（以 http:// 或 https:// 开头）')
        return
      }
      this.generating = true
      try {
        // 优先尝试纯文本接口 /shorten，失败则回退到 JSON 接口 /api/shorten
        let shortRaw = ''
        try {
          const qp = alias ? `?alias=${encodeURIComponent(alias)}` : ''
          const res1 = await api.post(`/shorten${qp}`, url, { headers: { 'Content-Type': 'text/plain' } })
          shortRaw = res1?.data?.shortUrl ?? res1?.data ?? ''
          if (!shortRaw) throw new Error('EMPTY_SHORT_URL')
        } catch (e1) {
          const res2 = await api.post('/api/shorten', { longUrl: url, customAlias: alias || undefined }, { headers: { 'Content-Type': 'application/json;charset=utf-8' } })
          const ok = res2?.data?.code === 0
          shortRaw = res2?.data?.data?.shortUrl ?? res2?.data?.shortUrl ?? ''
          if (!ok || !shortRaw) throw new Error('JSON_SHORTEN_FAILED')
        }

        const code = this.extractCode({ shortUrl: shortRaw })
        // 统一展示域名为 http://localhost:8080
        this.shortUrl = this.buildShortUrl(code)
        this.longUrl = url
        // 本地插入至历史顶部，无需整页刷新
        const newItem = {
          id: Date.now(),
          shortUrl: this.shortUrl,
          longUrl: url,
          code,
          createdAt: new Date().toISOString(),
        }
        this.history = [newItem, ...(this.history || [])]
        // 触发全屏粒子压缩动画（若已挂载背景组件）
        this.$refs?.qwenBg?.playCompression(this.shortUrl)
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
    downloadQrPng() {
      try {
        const el = this.$refs.qrRef
        if (!el) return
        // qrcode.vue 默认渲染 canvas
        const canvas = el.$el?.querySelector?.('canvas') || el.$el
        const url = canvas?.toDataURL?.('image/png')
        if (!url) return
        const a = document.createElement('a')
        a.href = url
        a.download = 'short-url-qr.png'
        a.click()
      } catch (e) {
        console.error('下载二维码失败:', e)
      }
    },
    goToStatsNav() {
      // 优先使用最近生成的短码，没有则使用历史第一条
      let code = ''
      if (this.shortUrl) {
        code = this.extractCode({ shortUrl: this.shortUrl }) || ''
      }
      if (!code && (this.history && this.history.length)) {
        code = this.extractCode(this.history[0]) || ''
      }
      if (!code) {
        alert('暂无可统计的短码，请先生成或刷新历史')
        return
      }
      const url = `/stats/${encodeURIComponent(code)}`
      window.location.href = url
    },
    goToDashboard() {
      if (this.$router) this.$router.push('/dashboard')
      else window.location.href = '/dashboard'
    },
    visitOriginal() {
      if (!this.longUrl) return
      window.open(this.longUrl, '_blank')
    },
    async redirectViaApi(item) {
      try {
        const code = this.extractCode(item)
        if (!code) return
        const res = await api.get('/api/' + encodeURIComponent(code))
        const payload = res?.data
        let text = typeof payload === 'string' ? payload : (payload?.data ?? payload?.message ?? payload?.msg)
        let longUrl = ''
        if (typeof text === 'string') {
          const m = text.match(/Redirect to:\s*(.+)$/i)
          longUrl = (m ? m[1] : text).trim()
        }
        // 兼容可能的结构字段
        if (!longUrl) longUrl = payload?.longUrl || payload?.data?.longUrl || ''
        if (!longUrl) throw new Error('NO_LONG_URL')
        window.open(longUrl, '_blank')
      } catch (err) {
        const code = err?.response?.data?.code
        if (code === 1004) alert('短链不存在')
        else alert('跳转失败，请稍后再试')
        console.error('Redirect error:', err)
      }
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
        return new URL('/' + String(code), API_BASE).href
      } catch {
        return (API_BASE || '') + '/' + String(code)
      }
    },
    displayShortUrl(item) {
      return item?.shortUrl || this.buildShortUrl(this.extractCode(item))
    },
    displayShortUrlText(item) {
      try {
        return decodeURI(this.displayShortUrl(item))
      } catch {
        return this.displayShortUrl(item)
      }
    },
    decodeUrlText(u) {
      try { return decodeURI(String(u)) } catch { return String(u) }
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

<style>
.tf-brand-button {
  position: relative;
  width: 120px;
  height: 40px;
  background-color: #000;
  display: flex;
  align-items: center;
  color: white;
  flex-direction: column;
  justify-content: center;
  border: none;
  padding: 12px;
  gap: 12px;
  border-radius: 8px;
  cursor: pointer;
}

/* 强化“刷新历史”按钮的悬停反馈，仅参考结构，保持原配色 */
.tf-refresh-container {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  grid-template-areas: "bt-1 bt-2 bt-3" "bt-4 bt-5 bt-6";
  width: 120px;
  height: 36px;
  perspective: 800px;
}
.tf-refresh-container.is-disabled {
  pointer-events: none;
}
.tf-refresh-container .hover {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 2;
}
.tf-refresh-container .bt-1 { grid-area: bt-1; }
.tf-refresh-container .bt-2 { grid-area: bt-2; }
.tf-refresh-container .bt-3 { grid-area: bt-3; }
.tf-refresh-container .bt-4 { grid-area: bt-4; }
.tf-refresh-container .bt-5 { grid-area: bt-5; }
.tf-refresh-container .bt-6 { grid-area: bt-6; }

.tf-refresh-btn {
  position: absolute;
  inset: 0;
  width: 120px;
  height: 36px;
  border: 2px solid #6B72FF;
  border-radius: 12px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, opacity 0.25s ease;
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.tf-refresh-btn:active { transform: scale(0.97); }
.tf-refresh-container.is-disabled .tf-refresh-btn { opacity: 0.7; }

.tf-refresh-container .bt-1:hover ~ .tf-refresh-btn {
  transform: rotateX(10deg) rotateY(-10deg);
  box-shadow: -2px -2px 0 rgba(30, 30, 60, 0.3);
}
.tf-refresh-container .bt-3:hover ~ .tf-refresh-btn {
  transform: rotateX(10deg) rotateY(10deg);
  box-shadow: 2px -2px 0 rgba(30, 30, 60, 0.3);
}
.tf-refresh-container .bt-4:hover ~ .tf-refresh-btn {
  transform: rotateX(-10deg) rotateY(-10deg);
  box-shadow: -2px 2px 0 rgba(30, 30, 60, 0.3);
}
.tf-refresh-container .bt-6:hover ~ .tf-refresh-btn {
  transform: rotateX(-10deg) rotateY(10deg);
  box-shadow: 2px 2px 0 rgba(30, 30, 60, 0.3);
}
.tf-brand-button::before {
  content: '';
  position: absolute;
  inset: 0;
  left: -4px;
  top: -1px;
  margin: auto;
  width: 128px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(-45deg, #6B72FF 0%, #A66BFF 100%);
  z-index: -10;
  pointer-events: none;
  transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.tf-brand-button::after {
  content: '';
  z-index: -1;
  position: absolute;
  inset: 0;
  background: linear-gradient(-45deg, #7E6BFF 0%, #B66BFF 100%);
  transform: translate3d(0, 0, 0) scale(0.95);
  filter: blur(20px);
}
.tf-brand-button:hover::after {
  filter: blur(30px);
}
.tf-brand-button:hover::before {
  transform: rotate(-180deg);
}
.tf-brand-button:active::before {
  scale: 0.7;
}

/* Brand text gradient + hover animation */
.tf-brand-text {
  display: inline-block;
  background-image: linear-gradient(135deg, #6B72FF 0%, #8A6BFF 60%, #A66BFF 100%);
  background-size: 200% 200%;
  background-position: 0% 50%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  transition: transform 180ms ease, letter-spacing 180ms ease;
}
.tf-brand-button:hover .tf-brand-text {
  animation: tfBrandShimmer 2s ease-in-out infinite;
  transform: translateY(-1px);
  letter-spacing: 0.5px;
}
@keyframes tfBrandShimmer {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>

<style>
/* 品牌逐字动画 */
.tf-brand {
  display: inline-flex;
  align-items: baseline;
  gap: 0;
  line-height: 1;
  font-size: 24px;
}
.tf-letter {
  display: inline-block;
  background-image: linear-gradient(135deg, #6B72FF 0%, #8A6BFF 60%, #A66BFF 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  transform-origin: bottom;
  will-change: transform, background-position;
  backface-visibility: hidden;
  transform-style: preserve-3d;
  --jump-peak: 8px;
  cursor: pointer;
  animation: tfWaterFlow 4800ms ease-in-out infinite;
 }
/* 邻近字母波浪联动（:has 选择器） */
/* 悬停当前字母立即跳动，持续更短以提升响应感 */
.tf-letter:hover {
  animation: tfLetterJump 440ms cubic-bezier(0.22, 1, 0.36, 1) 1 both;
 }

/* i=1: 右侧 2/3/4 依次延迟 */
.tf-brand:has(.tf-letter:nth-child(1):hover) .tf-letter:nth-child(2) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(1):hover) .tf-letter:nth-child(3) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(1):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=2: 左 1 右 3/4/5 */
.tf-brand:has(.tf-letter:nth-child(2):hover) .tf-letter:nth-child(1) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(2):hover) .tf-letter:nth-child(3) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(2):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(2):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=3: 左 2/1 右 4/5/6 */
.tf-brand:has(.tf-letter:nth-child(3):hover) .tf-letter:nth-child(2) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(3):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(3):hover) .tf-letter:nth-child(1) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(3):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(3):hover) .tf-letter:nth-child(6) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=4: 左 3/2/1 右 5/6/7 */
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(3) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(2) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(6) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(1) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }
.tf-brand:has(.tf-letter:nth-child(4):hover) .tf-letter:nth-child(7) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=5: 左 4/3/2 右 6/7/8 */
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(6) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(3) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(7) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(2) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }
.tf-brand:has(.tf-letter:nth-child(5):hover) .tf-letter:nth-child(8) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=6: 左 5/4/3 右 7/8 */
.tf-brand:has(.tf-letter:nth-child(6):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(6):hover) .tf-letter:nth-child(7) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(6):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(6):hover) .tf-letter:nth-child(8) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(6):hover) .tf-letter:nth-child(3) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=7: 左 6/5/4 右 8 */
.tf-brand:has(.tf-letter:nth-child(7):hover) .tf-letter:nth-child(6) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(7):hover) .tf-letter:nth-child(8) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(7):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(7):hover) .tf-letter:nth-child(4) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }

/* i=8: 左 7/6/5 */
.tf-brand:has(.tf-letter:nth-child(8):hover) .tf-letter:nth-child(7) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 50ms; }
.tf-brand:has(.tf-letter:nth-child(8):hover) .tf-letter:nth-child(6) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 100ms; }
.tf-brand:has(.tf-letter:nth-child(8):hover) .tf-letter:nth-child(5) { animation: tfLetterJump 360ms ease-out 1 both; animation-delay: 150ms; }
/* Idle water-flow across letters: phased delays create traveling wave */
.tf-brand .tf-letter:nth-child(1) { animation-delay: 0ms; }
.tf-brand .tf-letter:nth-child(2) { animation-delay: 120ms; }
.tf-brand .tf-letter:nth-child(3) { animation-delay: 240ms; }
.tf-brand .tf-letter:nth-child(4) { animation-delay: 360ms; }
.tf-brand .tf-letter:nth-child(5) { animation-delay: 480ms; }
.tf-brand .tf-letter:nth-child(6) { animation-delay: 600ms; }
.tf-brand .tf-letter:nth-child(7) { animation-delay: 720ms; }
.tf-brand .tf-letter:nth-child(8) { animation-delay: 840ms; }

@keyframes tfWaterFlow {
  0%   { transform: translate3d(0, 0px, 0); background-position: 0% 50%; }
  12%  { transform: translate3d(0, -1.6px, 0); background-position: 25% 50%; }
  25%  { transform: translate3d(0, 0px, 0); background-position: 50% 50%; }
  37%  { transform: translate3d(0, 1.2px, 0); background-position: 65% 50%; }
  50%  { transform: translate3d(0, 0px, 0); background-position: 75% 50%; }
  62%  { transform: translate3d(0, -1px, 0); background-position: 85% 50%; }
  75%  { transform: translate3d(0, 0px, 0); background-position: 95% 50%; }
  87%  { transform: translate3d(0, 0.8px, 0); background-position: 100% 50%; }
  100% { transform: translate3d(0, 0px, 0); background-position: 0% 50%; }
}

@keyframes tfLetterJump {
  0%   { transform: translate3d(0, 0, 0); }
  28%  { transform: translate3d(0, calc(-1 * var(--jump-peak, 8px)), 0); }
  52%  { transform: translate3d(0, calc(0.46 * var(--jump-peak, 8px)), 0); }
  74%  { transform: translate3d(0, calc(-0.18 * var(--jump-peak, 8px)), 0); }
  100% { transform: translate3d(0, 0, 0); }
}
</style>

<style scoped>
/* 删除按钮：固定尺寸，不挤压布局；悬停显示“删除”标签 */
.tf-del-btn {
  width: 36px;
  height: 36px;
  border-radius: 9999px;
  background: #ffffff;
  border: 1px solid #E5E7EB;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: background .25s ease, box-shadow .25s ease, transform .2s ease;
  box-shadow: none;
}
.tf-del-btn .tf-del-icon { width: 14px; height: 14px; transition: transform .25s ease, opacity .2s ease, fill .25s ease; }
.tf-del-btn .tf-del-icon path { fill: #6B72FF; }

/* 悬停态：红色高亮且展示文字标签，但不改变按钮本体尺寸，避免布局抖动 */
.tf-del-btn::after {
  content: '删除';
  position: absolute;
  left: 50%;
  transform: translateX(-50%) translateY(22px);
  bottom: -6px;
  font-size: 11px;
  line-height: 1;
  color: #ffffff;
  opacity: 0;
  pointer-events: none;
  transition: opacity .25s ease, transform .25s ease;
}
.tf-del-btn:hover { background: linear-gradient(135deg, #4F9DFF 0%, #7A6BFF 100%); border-color: transparent; box-shadow: 0 6px 20px rgba(79,157,255,0.35); }
.tf-del-btn:hover .tf-del-icon path { fill: #ffffff; }
.tf-del-btn:hover .tf-del-icon { transform: translateY(1px) scale(1.05); }
.tf-del-btn:hover::after { opacity: .95; transform: translateX(-50%) translateY(26px); }

/* 禁用态 */
.tf-del-btn.is-loading,
.tf-del-btn:disabled { background: linear-gradient(135deg, #98B9FF 0%, #C1B9FF 100%); cursor: not-allowed; box-shadow: none; }
.tf-del-btn.is-loading .tf-del-icon,
.tf-del-btn:disabled .tf-del-icon { opacity: .6; }

/* History item modern style */
.tf-hist-item { transition: box-shadow .25s ease, border-color .25s ease, transform .12s ease; }
.tf-hist-item:hover { box-shadow: 0 10px 28px rgba(43,108,239,0.08), 0 3px 10px rgba(122,109,255,0.06); border-color: #E5E7EB; }

.tf-code-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  font-size: 11px;
  border-radius: 9999px;
  background: rgba(107,114,255,0.12);
  color: #6B72FF;
}

.tf-copy-btn {
  font-size: 12px;
  color: #2B6CEF;
  padding: 4px 8px;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
  background: #fff;
  transition: background-color .2s ease, box-shadow .2s ease, border-color .2s ease;
}
.tf-copy-btn:hover { background: #F3F6FF; border-color: #DBE5FF; box-shadow: 0 1px 6px rgba(43,108,239,0.15); }
</style>