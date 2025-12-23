<template>
  <main class="min-h-screen pt-14" style="background-color:var(--tf-bg-page)">
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
          <!-- 历史记录项 -->
          <div v-for="item in filteredHistory" :key="item.id || extractCode(item) || item.shortUrl" class="fs-card p-4">
            <div class="flex items-center justify-between gap-3">
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
            <div v-if="editingId===item.id" class="mt-3 flex items-center gap-2">
              <input v-model="editAlias" type="text" class="fs-input h-9 px-3 flex-1" :placeholder="$t('actions.newAlias')" @keydown.enter.prevent @keyup.enter="!editingComposing && saveEdit(item)" @compositionstart="editingComposing = true" @compositionend="editingComposing = false" />
              <button @click="saveEdit(item)" :disabled="updatingIds.has(item.id)" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.save') }}</button>
              <button @click="cancelEdit" class="fs-btn-secondary px-3 py-1 text-[13px]">{{ $t('actions.cancel') }}</button>
            </div>
          </div>
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

    <!-- 特性展示 -->
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
import LoadingSpinner from '../components/LoadingSpinner.vue'
import QrcodeVue from 'qrcode.vue'
import Favicon from '../components/Favicon.vue'
import { SHORT_BASE } from '/src/composables/shortBase'

const api = axios

export default {
  name: 'HomePage',
  components: { LoadingSpinner, QrcodeVue, Favicon },
  data() {
    return {
      inputUrl: '',
      customAlias: '',
      shortUrl: '',
      copyLabel: '复制链接',
      downloadTip: false,
      clearTip: false,
      generating: false,
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
    }
  },
  computed: {
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
    async refreshClickStats() {
      try {
        const res = await api.get('/api/urls/click-stats')
        const payload = res?.data ?? null
        const list = Array.isArray(payload) ? payload : Array.isArray(payload?.data) ? payload.data : []
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
        this.shortUrl = this.buildDisplayShortUrl(code)
        const newItem = {
          id: Date.now(),
          shortUrl: this.shortUrl,
          longUrl: url,
          code,
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
    confirmClearAll() {
      this.inputUrl = ''
      this.customAlias = ''
      this.shortUrl = ''
      this.copyLabel = '复制链接'
      this.clearTip = true
      setTimeout(() => { this.clearTip = false }, 2000)
    },
    downloadQrPng() {
      try {
        const el = this.$refs.qrRef
        if (!el) return
        const canvas = el.$el?.querySelector?.('canvas') || el.$el
        const url = canvas?.toDataURL?.('image/png')
        if (!url) return
        const a = document.createElement('a')
        a.href = url
        a.download = 'short-url-qr.png'
        a.click()
        this.downloadTip = true
        setTimeout(() => { this.downloadTip = false }, 2000)
      } catch (e) {
        console.error('下载二维码失败:', e)
      }
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
    buildDisplayShortUrl(code) {
      if (!code) return ''
      try {
        return new URL('/' + encodeURIComponent(String(code)), SHORT_BASE).href
      } catch {
        return SHORT_BASE + '/' + encodeURIComponent(String(code))
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
              : Array.isArray(payload?.data?.content)
                ? payload.data.content
                : []
        const total = payload?.data?.totalElements ?? payload?.totalElements ?? payload?.total ?? listRaw.length
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
        await api.put('/api/' + encodeURIComponent(oldCode), {
          shortCode: oldCode,
          customAlias: newAlias,
        }, { headers: { 'Content-Type': 'application/json;charset=utf-8' } })
        const updated = { ...item }
        updated.code = newAlias
        updated.shortUrl = this.buildDisplayShortUrl(newAlias)
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
        const res = await api.delete('/api/' + encodeURIComponent(code))
        if (res?.status === 204 || res?.status === 200) {
          this.history = (this.history || []).filter(x => this.extractCode(x) !== code)
        }
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('History delete error:', err)
      } finally {
        if (id) this.deletingIds.delete(id)
      }
    },
    normalizeUrl(u) {
      const s = (u || '').trim()
      if (!s) return ''
      if (/^https?:\/\//i.test(s)) return s
      const isDomain = /^[a-z0-9][a-z0-9.-]*\.[a-z]{2,}(?::\d+)?(?:\/.*)?$/i.test(s)
      const isLocalhost = /^localhost(?::\d+)?(?:\/.*)?$/i.test(s)
      const isIPv4 = /^(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:\/.*)?$/.test(s)
      if (isDomain || isLocalhost || isIPv4) return 'http://' + s
      return ''
    },
    resolveLongUrl(item) {
      return item?.longUrl || item?.url || item?.originalUrl || item?.destinationUrl || item?.targetUrl || ''
    },
  },
  mounted() {
    this.refreshHistory()
    this.refreshClickStats()
  }
}
</script>
