<template>
  <!-- Fallback: 首字母徽标 -->
  <div v-if="!hasValidSource || !hasIcon" class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-semibold"
       style="flex:none;transition: all 0.2s ease; background: #3b82f6; color: #FFFFFF">
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

// 更"国内友好"的回退顺序：站点本身 → DuckDuckGo → 直接显示首字母
const sources = computed(() => {
  const d = domain.value
  if (!d) return []
  const arr = []
  // 只尝试两个来源：站点本身和 DuckDuckGo
  if (origin.value) arr.push(() => `${origin.value}/favicon.ico`)
  arr.push(() => `https://icons.duckduckgo.com/ip3/${d}.ico`)
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
  if (current.value < sources.value.length - 1) {
    current.value++
  } else {
    // 所有源都失败，显示首字母徽标
    hasIcon.value = false
  }
}
</script>

<style scoped>
</style>