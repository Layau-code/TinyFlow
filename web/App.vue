<template>
  <!-- Top Navbar: Feishu style minimal header -->
  <header class="fixed top-0 left-0 right-0 z-50" :style="navStyle">
    <div class="flex items-center justify-between h-14 px-6 md:px-[10%]">
      <div class="flex items-center gap-8">
        <div class="font-semibold text-[20px]" style="color:#1F2329">TinyFlow</div>
        <nav class="hidden md:flex items-center gap-6">
          <a href="#" @click.prevent="$router.push('/')" class="text-[14px] hover:text-[#3370FF] transition" style="color:#646A73">{{ $t('common.home') }}</a>
          <a href="#" @click.prevent="goToDashboard" class="text-[14px] hover:text-[#3370FF] transition" style="color:#646A73">{{ $t('nav.dashboard') }}</a>
        </nav>
      </div>
      <div class="flex items-center gap-4">
        <div v-if="isAuthenticated" class="flex items-center gap-3">
          <span class="text-[14px]" style="color:#8F959E">{{ currentUsername }}</span>
          <button @click="handleLogout" class="text-[14px] text-[#646A73] hover:text-[#F54A45] transition">退出登录</button>
        </div>
        <a v-else href="#" @click.prevent="$router.push('/login')" class="text-[14px] font-medium hover:text-[#2B5FE6] transition" style="color:#3370FF">登录</a>
        
        <a
          href="https://github.com/Layau-code/TinyFlow"
          target="_blank"
          class="flex overflow-hidden items-center text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white disabled:pointer-events-none disabled:opacity-50 text-white shadow hover:opacity-95 h-9 px-4 py-2 max-w-52 whitespace-pre md:flex group relative justify-center gap-2 rounded-md transition-all duration-300 ease-out hover:ring-2 hover:ring-white hover:ring-offset-2"
          :style="githubBtnStyle"
        >
          <span class="absolute right-0 -mt-12 h-32 w-8 translate-x-12 rotate-12 bg-white opacity-10 transition-all duration-1000 ease-out group-hover:-translate-x-40"></span>
          <div class="flex items-center">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 438.549 438.549">
              <path d="M409.132 114.573c-19.608-33.596-46.205-60.194-79.798-79.8-33.598-19.607-70.277-29.408-110.063-29.408-39.781 0-76.472 9.804-110.063 29.408-33.596 19.605-60.192 46.204-79.8 79.8C9.803 148.168 0 184.854 0 224.63c0 47.78 13.94 90.745 41.827 128.906 27.884 38.164 63.906 64.572 108.063 79.227 5.14.954 8.945.283 11.419-1.996 2.475-2.282 3.711-5.14 3.711-8.562 0-.571-.049-5.708-.144-15.417a2549.81 2549.81 0 01-.144-25.406l-6.567 1.136c-4.187.767-9.469 1.092-15.846 1-6.374-.089-12.991-.757-19.842-1.999-6.854-1.231-13.229-4.086-19.13-8.559-5.898-4.473-10.085-10.328-12.56-17.556l-2.855-6.57c-1.903-4.374-4.899-9.233-8.992-14.559-4.093-5.331-8.232-8.945-12.419-10.848l-1.999-1.431c-1.332-.951-2.568-2.098-3.711-3.429-1.142-1.331-1.997-2.663-2.568-3.997-.572-1.335-.098-2.43 1.427-3.289 1.525-.859 4.281-1.276 8.28-1.276l5.708.853c3.807.763 8.516 3.042 14.133 6.851 5.614 3.806 10.229 8.754 13.846 14.842 4.38 7.806 9.657 13.754 15.846 17.847 6.184 4.093 12.419 6.136 18.699 6.136 6.28 0 11.704-.476 16.274-1.423 4.565-.952 8.848-2.383 12.847-4.285 1.713-12.758 6.377-22.559 13.988-29.41-10.848-1.14-20.601-2.857-29.264-5.14-8.658-2.286-17.605-5.996-26.835-11.14-9.235-5.137-16.896-11.516-22.985-19.126-6.09-7.614-11.088-17.61-14.987-29.979-3.901-12.374-5.852-26.648-5.852-42.826 0-23.035 7.52-42.637 22.557-58.817-7.044-17.318-6.379-36.732 1.997-58.24 5.52-1.715 13.706-.428 24.554 3.853 10.85 4.283 18.794 7.952 23.84 10.994 5.046 3.041 9.089 5.618 12.135 7.708 17.705-4.947 35.976-7.421 54.818-7.421s37.117 2.474 54.823 7.421l10.849-6.849c7.419-4.57 16.18-8.758 26.262-12.565 10.088-3.805 17.802-4.853 23.134-3.138 8.562 21.509 9.325 40.922 2.279 58.24 15.036 16.18 22.559 35.787 22.559 58.817 0 16.178-1.958 30.497-5.853 42.966-3.9 12.471-8.941 22.457-15.125 29.979-6.191 7.521-13.901 13.85-23.131 18.986-9.232 5.14-18.182 8.85-26.84 11.136-8.662 2.286-18.415 4.004-29.263 5.146 9.894 8.562 14.842 22.077 14.842 40.539v60.237c0 3.422 1.19 6.279 3.572 8.562 2.379 2.279 6.136 2.95 11.276 1.995 44.163-14.653 80.185-41.062 108.068-79.226 27.88-38.161 41.825-81.126 41.825-128.906-.01-39.771-9.818-76.454-29.414-110.049z" />
            </svg>
            <span class="ml-1 text-white">Star on GitHub</span>
          </div>
          <div class="ml-2 hidden md:flex items-center gap-1 text-sm">
            <svg class="w-4 h-4 text-gray-200 transition-all duration-300 group-hover:text-yellow-300" aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
              <path clip-rule="evenodd" fill-rule="evenodd" d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.006 5.404.434c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.434 2.082-5.005Z" />
            </svg>
            <span class="inline-block tabular-nums tracking-wider font-display font-medium text-white">Star</span>
          </div>
        </a>
        <LanguageSwitcher />
      </div>
    </div>
  </header>

  <!-- Rest of page -->
  <router-view v-if="isStatsOrDashboard" class="pt-14" />
  <main class="min-h-screen pt-14" v-show="!isStatsOrDashboard" style="background-color:var(--tf-bg-page)">
    <section class="hero pt-24 pb-16">
      <div class="hero-inner max-w-5xl mx-auto px-6 flex flex-col items-center text-center gap-8">
        <!-- 标题区 -->
        <div class="space-y-3">
          <h1 class="font-semibold" style="color:#1F2329;font-size:clamp(40px,6vw,56px);line-height:1.2">
            {{ $t('hero.title') }}
          </h1>
          <p class="text-[16px] md:text-[18px]" style="color:#646A73;line-height:1.7">
            {{ $t('hero.subtitle') }}
          </p>
        </div>

        <!-- 表单卡片（飞书风格）-->
        <div class="w-full max-w-3xl fs-card p-6 md:p-8">
          <div class="flex flex-col gap-4">
            <!-- 主输入区 -->
            <div class="flex items-stretch gap-3">
              <input
                v-model="inputUrl"
                type="text"
                :placeholder="$t('form.urlPlaceholder')"
                class="fs-input flex-1 h-12"
                @focus="focusInput = true"
                @blur="focusInput = false"
                @keydown.enter="shortenUrl"
              />
              <button @click="shortenUrl" class="fs-btn-primary px-8 h-12">
                {{ $t('form.generate') }}
              </button>
            </div>
            <!-- 自定义别名 -->
            <input
              v-model="customAlias"
              type="text"
              :placeholder="$t('form.aliasPlaceholder')"
              class="fs-input h-10"
              @focus="focusAlias = true"
              @blur="focusAlias = false"
              @keydown.enter="shortenUrl"
            />
            <LoadingSpinner v-if="generating" />
            <div v-if="clearTip" class="text-[14px]" style="color:var(--tf-success)">已清空</div>
          </div>
        </div>

        <!-- 结果卡片 -->
        <div v-if="shortUrl" class="w-full max-w-3xl fs-card p-6 md:p-8">
          <div class="flex flex-col md:flex-row items-start md:items-center gap-6">
            <div class="min-w-0 flex-1 text-left">
              <div class="text-sm mb-2" style="color:var(--tf-text-muted)">{{ $t('result.created') }}</div>
              <button @click="redirectViaApi({ shortUrl })" class="underline break-all text-[16px] font-medium" style="color:var(--tf-brand-primary);background:none;border:none;padding:0;cursor:pointer">
                {{ decodeUrlText(shortUrl) }}
              </button>
              <div class="mt-4 flex gap-3">
                <button @click="copyShortUrl" class="fs-btn-secondary px-4 py-2">
                  {{ $t('result.copy') }}
                </button>
                <button @click="downloadQrPng" class="fs-btn-secondary px-4 py-2">
                  {{ $t('result.downloadQr') }}
                </button>
                <button @click="confirmClearAll" class="fs-btn-secondary px-4 py-2">
                  确认
                </button>
              </div>
              <div v-if="copyLabel==='已复制'" class="mt-2 text-[14px]" style="color:var(--tf-success)">
                {{ $t('result.copied') }}
              </div>
              <div v-if="downloadTip" class="mt-2 text-[14px]" style="color:var(--tf-success)">二维码已保存</div>
            </div>
            <div class="shrink-0">
              <QrcodeVue ref="qrRef" :value="shortUrl" :size="140" level="M" :foreground="'#3370FF'" :background="'#ffffff'" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 历史记录区域 -->
    <section class="px-6 py-12">
      <div class="max-w-5xl mx-auto">
        <div class="flex items-center justify-between mb-6">
          <h2 class="font-semibold text-[24px]" style="color:var(--tf-text-title)">{{ $t('history.title') }}</h2>
          <div class="flex items-center gap-3">
            <input v-model="historyQuery" type="text" class="fs-input h-9 px-3" :placeholder="$t('history.filterPlaceholder')" />
            <button @click="refreshHistory" :disabled="refreshing" class="fs-btn-primary px-4 py-2 h-9">
              {{ refreshing ? $t('history.refreshing') : $t('history.refresh') }}
            </button>
          </div>
        </div>
        <div v-if="filteredHistory.length === 0" class="text-[14px]" style="color:var(--tf-text-muted)">{{ $t('history.empty') }}</div>
        <div v-else class="space-y-3">
          <!-- 历史记录项（飞书风格卡片）-->
          <div v-for="item in filteredHistory" :key="item.id || extractCode(item) || item.shortUrl" class="fs-card p-4">
            <div class="flex items-center justify-between gap-3">
              <!-- 左侧：favicon + 域名 + code徽章 -->
              <div class="flex items-center gap-3 min-w-0">
                <Favicon :long-url="normalizeUrl(resolveLongUrl(item)) || (location.origin + (item.shortUrl || ''))" />
                <div class="min-w-0">
                  <div class="flex items-center gap-2 min-w-0">
                    <button @click="redirectViaApi(item)" class="truncate text-[14px] font-medium hover:underline" style="color:var(--tf-brand-primary);max-width:52vw;background:none;border:none;padding:0;cursor:pointer">{{ displayShortUrlText(item) }}</button>
                    <span class="px-2 py-0.5 text-[12px] rounded" style="background:var(--tf-brand-lighter);color:var(--tf-brand-primary)">{{ extractCode(item) }}</span>
                  </div>
                  <div class="mt-1 truncate text-[13px]" style="color:var(--tf-text-muted);max-width:60vw">{{ item.longUrl }}</div>
                </div>
              </div>
              <!-- 右侧：操作按钮 -->
              <div class="flex items-center gap-2 shrink-0">
                <button @click="copyLink(displayShortUrl(item), item.id)" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.copy') }}</button>
                <button @click="startEdit(item)" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.edit') }}</button>
                <button @click="deleteHistoryItem(item)" :disabled="deletingIds.has(item.id)" class="fs-btn-secondary px-2 py-1 text-[13px] hover:border-red-500 hover:text-red-500" :title="$t('actions.delete')">
                  <svg viewBox="0 0 448 512" class="w-4 h-4" fill="currentColor">
                    <path d="M135.2 17.7L128 32H32C14.3 32 0 46.3 0 64S14.3 96 32 96H416c17.7 0 32-14.3 32-32s-14.3-32-32-32H320l-7.2-14.3C307.4 6.8 296.3 0 284.2 0H163.8c-12.1 0-23.2 6.8-28.6 17.7zM416 128H32L53.2 467c1.6 25.3 22.6 45 47.9 45H346.9c25.3 0 46.3-19.7 47.9-45L416 128z" />
                  </svg>
                </button>
              </div>
            </div>
            <div v-if="copiedId===item.id" class="mt-2 text-[13px]" style="color:var(--tf-success)">{{ $t('actions.copied') }}</div>
            <!-- 编辑行 -->
            <div v-if="editingId===item.id" class="mt-3 flex items-center gap-2">
              <input v-model="editAlias" type="text" class="fs-input h-9 px-3 flex-1" :placeholder="$t('actions.newAlias')" @keydown.enter.prevent @keyup.enter="!editingComposing && saveEdit(item)" @compositionstart="editingComposing = true" @compositionend="editingComposing = false" />
              <button @click="saveEdit(item)" :disabled="updatingIds.has(item.id)" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.save') }}</button>
              <button @click="cancelEdit" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.cancel') }}</button>
            </div>
          </div>
          <!-- 分页 -->
          <div class="flex items-center justify-between mt-4 pt-4" style="border-top:1px solid var(--tf-divider)">
            <div class="text-[13px]" style="color:var(--tf-text-muted)">
              {{ $t('historyPagination.total', { count: histTotal, page: histPage, pages: Math.max(1, Math.ceil(histTotal / histPageSize)) }) }}
            </div>
            <div class="flex items-center gap-3">
              <button @click="prevHist" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('historyPagination.prev') }}</button>
              <button @click="nextHist" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('historyPagination.next') }}</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特性展示（飞书风格卡片网格）-->
    <section class="py-16 px-6">
      <div class="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="fs-card p-6">
          <div class="font-semibold mb-2 text-[16px]" style="color:var(--tf-text-title)">自定义别名</div>
          <div class="text-[14px]" style="color:var(--tf-text-body)">可自定义短链后缀，增强品牌一致性</div>
        </div>
        <div class="fs-card p-6">
          <div class="font-semibold mb-2 text-[16px]" style="color:var(--tf-text-title)">访问统计</div>
          <div class="text-[14px]" style="color:var(--tf-text-body)">统计点击与来源，数据驱动优化分享</div>
        </div>
        <div class="fs-card p-6">
          <div class="font-semibold mb-2 text-[16px]" style="color:var(--tf-text-title)">实时同步</div>
          <div class="text-[14px]" style="color:var(--tf-text-body)">通过 API 刷新/删除历史记录，轻松管理与访问</div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="py-8 px-6" style="border-top:1px solid var(--tf-divider)">
      <div class="text-center text-[14px]" style="color:var(--tf-text-muted)">
        {{ $t('footer.suffix') }}
        <a href="mailto:support@tinyflow.local" class="hover:underline" style="color:var(--tf-brand-primary)">{{ $t('footer.contactUs') }}</a>
      </div>
    </footer>
  </main>
</template>

<script>
import axios from 'axios'
import LoadingSpinner from '/src/components/LoadingSpinner.vue'
import QrcodeVue from 'qrcode.vue'
import LanguageSwitcher from '/src/components/LanguageSwitcher.vue'
import Favicon from '/src/components/Favicon.vue'
import { useAuth } from '/src/composables/useAuth'
import { computed } from 'vue'
import { SHORT_BASE } from '/src/composables/shortBase'

const API_BASE = (typeof window !== 'undefined' && window.location && window.location.origin)
  ? window.location.origin
  : 'http://localhost:8080' // 用于顶部“历史记录”跳转链接
// 使用全局 axios 实例（已在 useAuth 中配置拦截器）
const api = axios

export default {
  name: 'App',
  components: { LoadingSpinner, QrcodeVue, LanguageSwitcher, Favicon },
  setup() {
    const { username, isAuthenticated, logout } = useAuth()
    return {
      currentUsername: username,
      isAuthenticated,
      handleLogout: () => {
        logout()
        window.location.href = '/login'
      }
    }
  },
  data() {
    return {
      apiBase: API_BASE,
      inputUrl: '',
      focusAlias: false,
      customAlias: '',
      longUrl: '',
      shortUrl: '',
      copyLabel: '复制链接',
      downloadTip: false,
      clearTip: false,
      focusInput: false,
      hoverGen: false,
      generating: false,
      scrolled: false,
      copyTimer: null,
      history: [],
      clickStats: [],
      historyQuery: '',
      histPage: 1,
      histPageSize: 10,
      histTotal: 0,
      refreshing: false,
      deletingIds: new Set(),
      updatingIds: new Set(),
      copiedId: null,
      copyItemTimer: null,
      editingId: null,
      editAlias: '',
      editingComposing: false,
      FAVICONS_ENABLED: true,  // 是否启用外部 favicon 请求
    }
  },
  computed: {
    navStyle() {
      return {
        backgroundColor: 'rgba(255,255,255,0.95)',
        backdropFilter: 'saturate(180%) blur(12px)',
        WebkitBackdropFilter: 'saturate(180%) blur(12px)',
        borderBottom: '1px solid var(--tf-border)',
        boxShadow: 'var(--tf-shadow-card)'
      }
    },
    githubBtnStyle() {
      return {
        background: 'linear-gradient(135deg, #3370FF 0%, #2B5FE6 45%, #38BDF8 100%)'
      }
    },
    isStatsOrDashboard() {
      const p = this.$route?.path || ''
      return p.startsWith('/stats/') || p === '/dashboard' || p === '/login'
    },
    statsMap() {
      const map = {}
      ;(this.clickStats || []).forEach(s => {
        const k = s?.shortCode
        if (!k) return
        map[k] = {
          totalVisits: Number(s?.totalVisits || 0),
          todayVisits: Number(s?.todayVisits || 0)
        }
      })
      return map
    },
    filteredHistory() {
      const q = (this.historyQuery || '').trim().toLowerCase()
      const source = this.history || []
      const base = q
        ? source.filter((it) => {
            const code = (this.extractCode(it) || '').toLowerCase()
            const host = (() => { try { return new URL(this.resolveLongUrl(it) || it.shortUrl || '').hostname.toLowerCase() } catch { return '' } })()
            return code.includes(q) || host.includes(q)
          })
        : [...source]
      const merged = base.map(it => {
        const code = this.extractCode(it)
        const stat = this.statsMap[code] || {}
        return {
          ...it,
          totalVisits: stat.totalVisits ?? Number(it.totalVisits || 0),
          todayVisits: stat.todayVisits ?? Number(it.todayVisits || 0)
        }
      })
      return merged.sort((a,b) => Number(b.totalVisits||0) - Number(a.totalVisits||0))
    },
  },
  methods: {
    inputStyle(active) {
      return {
        borderColor: active ? '#2563EB' : '#E5E7EB',
        boxShadow: active ? '0 0 0 4px rgba(37,99,235,0.14)' : 'none',
      }
    },
    async refreshClickStats() {
      try {
        const res = await api.get('/api/urls/click-stats')
        const payload = res?.data ?? null
        const list = Array.isArray(payload)
          ? payload
          : Array.isArray(payload?.data)
            ? payload.data
            : Array.isArray(payload?.items)
              ? payload.items
              : Array.isArray(payload?.content)
                ? payload.content
                : []
        this.clickStats = list.map(x => ({
          shortCode: x?.shortCode,
          totalVisits: Number(x?.totalVisits ?? 0),
          todayVisits: Number(x?.todayVisits ?? 0)
        }))
      } catch (err) {
        console.error('Click stats refresh error:', err)
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
        // 统一展示域名为 http://localhost:8080（外部短链形式）
        this.shortUrl = this.buildDisplayShortUrl(code)
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
        // 动效省略，保持首页简洁

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
        this.pressedCopy = true
        setTimeout(() => { this.pressedCopy = false }, 180)
      } catch (e) {
        console.error('复制失败:', e)
      }
    },
    confirmClearAll() {
      try {
        this.inputUrl = ''
        this.customAlias = ''
        this.longUrl = ''
        this.shortUrl = ''
        this.copyLabel = '复制链接'
        this.focusInput = false
        this.focusAlias = false
        clearTimeout(this.copyTimer)
        this.copyTimer = null
        this.copiedId = null
        clearTimeout(this.copyItemTimer)
        this.copyItemTimer = null
        this.editingId = null
        this.editAlias = ''
        this.clearTip = true
        this.pressedConfirm = true
        setTimeout(() => { this.pressedConfirm = false }, 180)
        setTimeout(() => { this.clearTip = false }, 2000)
      } catch (e) {
        console.error('清空失败:', e)
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
        this.pressedDownload = true
        setTimeout(() => { this.pressedDownload = false }, 180)
        this.downloadTip = true
        setTimeout(() => { this.downloadTip = false }, 2000)
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
        const url = this.buildDisplayShortUrl(code)
        window.open(url, '_blank')
      } catch (err) {
        alert('跳转失败，请稍后再试')
        console.error('Redirect error:', err)
      }
    },
    extractCode(item) {
      const direct = item?.code || item?.shortCode || item?.shortId || item?.alias || item?.slug || item?.key
      if (direct) return String(direct)
      const url = item?.shortUrl || ''
      try {
        const u = new URL(url)
        const raw = u.pathname.replace(/^\//, '')
        return raw ? decodeURIComponent(raw) : ''
      } catch {
        const parts = url.split('/')
        const last = parts[parts.length - 1] || ''
        try { return decodeURIComponent(last) } catch { return last }
      }
    },
    buildShortUrl(code) {
      if (!code) return ''
      try {
        return new URL('/api/redirect/' + encodeURIComponent(String(code)), 'http://localhost:8080').href
      } catch {
        return 'http://localhost:8080/api/redirect/' + encodeURIComponent(String(code))
      }
    },
    buildDisplayShortUrl(code) {
      if (!code) return ''
      try {
        return new URL('/' + encodeURIComponent(String(code)), 'http://localhost:8080').href
      } catch {
        return 'http://localhost:8080/' + encodeURIComponent(String(code))
      }
    },
    displayShortUrl(item) {
      const code = this.extractCode(item)
      return this.buildDisplayShortUrl(code)
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
        const params = {
          page: Math.max(0, (this.histPage || 1) - 1),
          size: this.histPageSize
        }
        const res = await api.get('/api/urls', { params })
        const payload = res?.data ?? null
        const listRaw = Array.isArray(payload)
          ? payload
          : Array.isArray(payload?.items)
            ? payload.items
            : Array.isArray(payload?.content)
              ? payload.content
              : Array.isArray(payload?.data?.items)
                ? payload.data.items
                : Array.isArray(payload?.data?.content)
                  ? payload.data.content
                  : []
        const total = (
          payload?.total ??
          payload?.totalElements ??
          payload?.data?.total ??
          payload?.data?.totalElements ??
          listRaw.length
        ) || 0
        this.histTotal = Number(total) || 0
        this.history = (listRaw || []).map((it) => {
          const code = it?.shortCode || this.extractCode(it)
          const shortUrl = this.buildDisplayShortUrl(code)
          const longUrl = it?.longUrl || this.resolveLongUrl(it) || ''
          const id = it?.id ?? code ?? (Date.now() + Math.random())
          const createdAt = it?.createdAt
          return { ...it, id, code, shortUrl, longUrl, createdAt }
        })
      } catch (err) {
        alert('获取历史记录失败，请稍后重试')
        console.error('History refresh error:', err)
      } finally {
        this.refreshing = false
      }
    },
    prevHist() {
      this.histPage = Math.max(1, this.histPage - 1)
      this.refreshHistory()
    },
    nextHist() {
      const pages = Math.max(1, Math.ceil(this.histTotal / this.histPageSize))
      this.histPage = Math.min(pages, this.histPage + 1)
      this.refreshHistory()
    },
    startEdit(item) {
      this.editingId = item?.id || null
      const code = this.extractCode(item)
      this.editAlias = code || ''
    },
    cancelEdit() {
      this.editingId = null
      this.editAlias = ''
    },
    async saveEdit(item) {
      const id = item?.id
      const oldCode = this.extractCode(item)
      const newAlias = (this.editAlias || '').trim()
      if (!id || !oldCode) return
      if (!newAlias) { alert('请输入新的短码或别名'); return }
      this.updatingIds.add(id)
      try {
        // 调用后端更新接口：PUT /api/{shortCode}
        await api.put('/api/' + encodeURIComponent(oldCode), {
          shortCode: oldCode,
          customAlias: newAlias,
        }, { headers: { 'Content-Type': 'application/json;charset=utf-8' } })
        // 本地更新：将展示短码替换为新别名
        const updated = { ...item }
        updated.code = newAlias
        updated.shortUrl = this.buildShortUrl(newAlias)
        this.history = (this.history || []).map(x => x.id === id ? updated : x)
        this.cancelEdit()
      } catch (err) {
        alert('更新失败，请稍后重试')
        console.error('Update short url error:', err)
      } finally {
        this.updatingIds.delete(id)
      }
    },
    async deleteHistoryItem(item) {
      const id = item?.id
      const code = this.extractCode(item)
      if (!code) return
      if (id) this.deletingIds.add(id)
      try {
        // 改为按短码删除：DELETE /api/{shortCode}
        const res = await api.delete('/api/' + encodeURIComponent(code))
        if (res?.status === 204 || res?.status === 200) {
          // 成功后本地移除相同短码的项，无需刷新
          this.history = (this.history || []).filter(x => this.extractCode(x) !== code)
        }
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('History delete error:', err)
      } finally {
        if (id) this.deletingIds.delete(id)
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
    faviconSrc(item) {
      // 全局开关：关闭则不发起任何跨站图标请求
      if (!this.FAVICONS_ENABLED) return ''
      const raw = this.resolveLongUrl(item)
      const url = this.normalizeUrl(raw)
      if (!url) return ''
      try {
        const u = new URL(url)
        // 首选站点自身的 favicon
        return `${u.origin}/favicon.ico`
      } catch { return '' }
    },
    onFaviconError(ev, item) {
      const hide = () => { try { ev.target.style.display = 'none' } catch {} ev.target.onerror = null; try { item.__iconFailed = true } catch {} }
      try {
        const u = new URL(this.normalizeUrl(this.resolveLongUrl(item)) || item?.shortUrl)
        const host = u.hostname
        // 逐级回退：DuckDuckGo -> Google S2 -> Clearbit -> iowen -> 隐藏
        ev.target.onerror = () => {
          ev.target.onerror = () => {
            ev.target.onerror = () => {
              ev.target.onerror = () => {
                // 最终回退到本地生成的占位图，以保证视觉一致
                ev.target.onerror = hide
                ev.target.src = this.placeholderIcon(host)
              }
              ev.target.src = `https://api.iowen.cn/favicon/${host}.png`
            }
            ev.target.src = `https://logo.clearbit.com/${host}`
          }
          ev.target.src = `https://www.google.com/s2/favicons?domain=${host}&sz=64`
        }
        ev.target.src = `https://icons.duckduckgo.com/ip3/${host}.ico`
      } catch {
        hide()
      }
    },
    getHostInitial(item) {
      try {
        const url = this.normalizeUrl(this.resolveLongUrl(item)) || item?.shortUrl || ''
        const host = new URL(url).hostname || ''
        return (host[0] || '?').toUpperCase()
      } catch { return '?' }
    },
    normalizeUrl(u) {
      const s = (u || '').trim()
      if (!s) return ''
      // 已有协议直接返回
      if (/^https?:\/\//i.test(s)) return s
      // 支持以下裸域/主机形式：
      // - 常规域名：example.com、sub.example.co.uk
      // - localhost：localhost、localhost:3000
      // - IPv4：127.0.0.1、192.168.1.10:8080
      // - IPv6：[fe80::1]:8080
      const isDomain = /^[a-z0-9][a-z0-9.-]*\.[a-z]{2,}(?::\d+)?(?:\/.*)?$/i.test(s)
      const isLocalhost = /^localhost(?::\d+)?(?:\/.*)?$/i.test(s)
      const isIPv4 = /^(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:\/.*)?$/.test(s)
      const isIPv6 = /^\[[0-9a-fA-F:]+\](?::\d+)?(?:\/.*)?$/.test(s)
      if (isDomain || isLocalhost || isIPv4 || isIPv6) return 'http://' + s
      return ''
    },
    placeholderIcon(host) {
      try {
        const size = 64
        const canvas = document.createElement('canvas')
        canvas.width = size; canvas.height = size
        const ctx = canvas.getContext('2d')
        // 颜色取自 host hash
        let hash = 0
        for (let i = 0; i < host.length; i++) hash = (hash * 31 + host.charCodeAt(i)) >>> 0
        const hue = hash % 360
        ctx.fillStyle = `hsl(${hue}, 60%, 80%)`
        ctx.fillRect(0, 0, size, size)
        ctx.fillStyle = `hsl(${hue}, 70%, 35%)`
        ctx.font = 'bold 32px sans-serif'
        ctx.textAlign = 'center'
        ctx.textBaseline = 'middle'
        const initial = (host[0] || '?').toUpperCase()
        ctx.fillText(initial, size / 2, size / 2)
        return canvas.toDataURL('image/png')
      } catch { return '' }
    },
    resolveLongUrl(item) {
      // 兼容后端不同字段命名：longUrl/url/originalUrl/destinationUrl/targetUrl/target
      return (
        item?.longUrl ||
        item?.url ||
        item?.originalUrl ||
        item?.destinationUrl ||
        item?.destUrl ||
        item?.targetUrl ||
        item?.target ||
        item?.long ||
        ''
      )
    },
    async hydrateLongUrls() {
      const items = (this.history || []).filter(it => !it.longUrl)
      if (items.length === 0) return
      const tasks = items.map(async (it) => {
        const code = this.extractCode(it)
        if (!code) return
        try {
          const res = await api.get('/api/' + encodeURIComponent(code))
          const payload = res?.data
          let text = typeof payload === 'string' ? payload : (payload?.data ?? payload?.message ?? payload?.msg)
          let longUrl = ''
          if (typeof text === 'string') {
            const m = text.match(/Redirect to:\s*(.+)$/i)
            longUrl = (m ? m[1] : text).trim()
          }
          if (!longUrl) longUrl = payload?.longUrl || payload?.data?.longUrl || ''
          if (longUrl) it.longUrl = longUrl
        } catch (e) {
          // 静默失败，保留占位首字母
          console.debug('hydrate longUrl failed for', code, e?.message || e)
        }
      })
      await Promise.allSettled(tasks)
    },
  },
  mounted() {
    this.refreshHistory()
    this.refreshClickStats()
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
  /* 允许点击透传到按钮，避免覆盖层拦截点击 */
  pointer-events: none;
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
.tf-action-btn {
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.tf-action-btn.is-pressed {
  transform: scale(0.97);
  box-shadow: 0 0 0 3px rgba(43,108,239,0.15);
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
  background: linear-gradient(-45deg, #2563EB 0%, #38BDF8 100%);
  z-index: -10;
  pointer-events: none;
  transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.tf-brand-button::after {
  content: '';
  z-index: -1;
  position: absolute;
  inset: 0;
  background: linear-gradient(-45deg, #2563EB 0%, #38BDF8 100%);
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
  background-image: linear-gradient(135deg, #2563EB 0%, #1D4ED8 45%, #38BDF8 100%);
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
  background-image: linear-gradient(135deg, #2563EB 0%, #1D4ED8 45%, #38BDF8 100%);
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
.tf-del-btn .tf-del-icon path { fill: #2563EB; }

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
.tf-del-btn:hover { background: linear-gradient(135deg, #2563EB 0%, #38BDF8 100%); border-color: transparent; box-shadow: 0 6px 20px rgba(37,99,235,0.3); }
.tf-del-btn:hover .tf-del-icon path { fill: #ffffff; }
.tf-del-btn:hover .tf-del-icon { transform: translateY(1px) scale(1.05); }
.tf-del-btn:hover::after { opacity: .95; transform: translateX(-50%) translateY(26px); }

/* 禁用态 */
.tf-del-btn.is-loading,
.tf-del-btn:disabled { background: linear-gradient(135deg, #93C5FD 0%, #BFDBFE 100%); cursor: not-allowed; box-shadow: none; }
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
  background: rgba(37,99,235,0.12);
  color: #2563EB;
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
