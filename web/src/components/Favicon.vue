<template>
  <!-- Fallback: 首字母徽标 -->
  <div v-if="!hasValidSource || !hasIcon" class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-semibold"
       style="flex:none;transition: all 0.2s ease; background: linear-gradient(135deg, #1D9BF0 0%, #2B6BFF 50%, #37B4FF 100%); color: #FFFFFF">
    {{ initial }}
  </div>
  <!-- Favicon with multi-source fallback -->
  <img v-else :src="src"
       alt="icon"
       class="w-5 h-5 rounded"
       referrerpolicy="no-referrer"
       style="flex:none;transition: all 0.2s ease"
       @error="showFallback" />
</template>

<script setup>
import { ref, computed } from 'vue'

// 接收任意 URL（长链或短链拼接后的绝对地址）
const props = defineProps({ longUrl: { type: String, default: '' } })

const hasIcon = ref(true)
const current = ref(0)

const parsed = computed(() => {
  try { return props.longUrl ? new URL(props.longUrl) : null } catch { return null }
})
const domain = computed(() => (parsed.value?.hostname || '').replace(/^www\./, ''))
const origin = computed(() => parsed.value?.origin || '')

// 更“国内友好”的回退顺序：站点本身 → DuckDuckGo → iowen → Clearbit
const sources = computed(() => {
  const d = domain.value
  if (!d) return []
  const arr = []
  if (origin.value) arr.push(() => `${origin.value}/favicon.ico`)
  arr.push(
    () => `https://icons.duckduckgo.com/ip3/${d}.ico`,
    () => `https://api.iowen.cn/favicon/${d}.png`,
    () => `https://logo.clearbit.com/${d}`,
  )
  return arr
})

const hasValidSource = computed(() => sources.value.length > 0)
const src = computed(() => {
  if (!hasValidSource.value) return ''
  const fn = sources.value[current.value]
  return fn ? fn() : ''
})
const initial = computed(() => (domain.value[0] || '?').toUpperCase())

function showFallback(){
  if (current.value < sources.value.length - 1) current.value++
  else hasIcon.value = false
}
</script>

<style scoped>
</style>