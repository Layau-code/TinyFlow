<template>
  <div class="w-full max-w-[720px] mx-auto">
    <!-- 表单卡片 -->
    <div class="p-6 md:p-8 rounded-2xl border" :style="cardStyle">
      <form class="flex flex-col gap-4" @submit.prevent="submit">
        <!-- 长链接输入 -->
        <input
          v-model="longUrl"
          type="text"
          class="w-full h-12 rounded-xl px-4 outline-none border bg-white"
          :style="inputStyle"
          :placeholder="$t('form.urlPlaceholder')"
          @keydown.enter="submit"
        />

        <!-- 自定义别名输入（可选，支持中文） -->
        <input
          v-model="customAlias"
          type="text"
          class="w-full h-12 rounded-xl px-4 outline-none border bg-white"
          :style="inputStyle"
          :placeholder="$t('form.aliasPlaceholder')"
          @keydown.enter="submit"
          @keyup.enter="submit"
        />

        <div class="text-sm" style="color:#4B5563">{{ $t('form.aliasPlaceholder') }}</div>

        <!-- 提交按钮 -->
        <button
          class="h-12 rounded-xl font-medium w-full md:w-auto px-6"
          :style="buttonStyle"
          :disabled="loading"
          type="submit"
        >{{ loading ? $t('common.loading') : $t('form.generate') }}</button>
      </form>
    </div>

    <!-- 结果区 -->
    <div v-if="shortUrl" class="mt-6 p-6 md:p-8 rounded-2xl border bg-white" :style="resultCardStyle">
      <div class="flex flex-col md:flex-row items-start md:items-center gap-6">
        <div class="min-w-0">
          <div class="text-sm mb-1" style="color:#4B5563">{{ $t('result.created') }}</div>
          <a :href="redirectUrl" target="_blank" rel="noopener" class="underline break-all" :style="linkStyle">{{ decodedLinkText }}</a>
        </div>
        <div class="flex-1"></div>
        <!-- 二维码 -->
        <QrcodeVue :value="redirectUrl" :size="140" level="M" :foreground="'#2B6CEF'" :background="'#ffffff'"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// 中文注释：自定义别名短链表单（Vue3 + TS）
import { ref, computed } from 'vue'
import axios from 'axios'
import QrcodeVue from 'qrcode.vue'
import { useI18n } from 'vue-i18n'
import { SHORT_BASE } from '../composables/shortBase'

// UI 风格：通义千问蓝紫
const BLUE = '#2B6CEF'
const PURPLE = '#8B7DFF'

const longUrl = ref<string>('')
const customAlias = ref<string>('')
const shortUrl = ref<string>('')
const loading = ref<boolean>(false)
const { t } = useI18n()

const inputStyle = computed(() => ({
  borderColor: '#E5E7EB',
  boxShadow: 'none'
}))

const hasInput = computed(() => !!(longUrl.value && longUrl.value.trim()))
const buttonStyle = computed(() => ({
  background: hasInput.value ? `linear-gradient(135deg, ${BLUE} 0%, ${PURPLE} 100%)` : '#EAE8FF',
  color: hasInput.value ? '#FFFFFF' : '#6B72FF',
  opacity: loading.value ? 0.8 : 1,
  transform: hasInput.value ? 'translateY(-2px) scale(1.02)' : 'none'
}))

const cardStyle = computed(() => ({
  borderColor: '#E5E7EB',
  background: 'rgba(255,255,255,0.9)'
}))

const resultCardStyle = computed(() => ({
  borderColor: '#E5E7EB'
}))

const linkStyle = computed(() => ({ color: '#2B6CEF' }))
// 展示使用统一基域短链（SHORT_BASE），不包含 /api/redirect
const redirectUrl = computed(() => {
  const url = shortUrl.value || ''
  const last = (url.split('/').pop() || '').split('?')[0]
  const code = last || url
  return code ? `${SHORT_BASE}/${encodeURIComponent(code)}` : ''
})
const decodedLinkText = computed(() => {
  try { return decodeURI(redirectUrl.value || '') } catch { return redirectUrl.value || '' }
})

// 提交短链生成请求（支持中文别名）
async function submit() {
  const url = longUrl.value.trim()
  const alias = customAlias.value.trim()
  if (!/^https?:\/\/\S+/i.test(url)) {
    alert(t('errors.invalidUrl'))
    return
  }
  loading.value = true
  shortUrl.value = ''
  try {
    const res = await axios.post('/api/shorten', {
      longUrl: url,
      customAlias: alias || undefined
    }, { headers: { 'Content-Type': 'application/json;charset=utf-8' } })
    const ok = res?.data?.code === 0
    const dataUrl = res?.data?.data?.shortUrl || ''
    if (!ok || !dataUrl) throw new Error('GENERATE_FAILED')
    shortUrl.value = dataUrl
  } catch (e) {
    alert(t('errors.generateFailed'))
    console.error('shorten error', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 让内容在深色粒子背景上对比清晰 */
</style>


