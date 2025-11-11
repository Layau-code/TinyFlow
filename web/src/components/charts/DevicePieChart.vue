<template>
  <div class="w-full flex items-center justify-center">
    <svg :width="size" :height="size" viewBox="0 0 100 100">
      <circle cx="50" cy="50" r="40" stroke="#E5E7EB" stroke-width="20" fill="none" />
      <circle cx="50" cy="50" r="40" :stroke="colors.ios" stroke-width="20" fill="none"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="offsets.ios"
              stroke-linecap="round"/>
      <circle cx="50" cy="50" r="40" :stroke="colors.android" stroke-width="20" fill="none"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="offsets.android"
              stroke-linecap="round"/>
      <circle cx="50" cy="50" r="40" :stroke="colors.pc" stroke-width="20" fill="none"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="offsets.pc"
              stroke-linecap="round"/>
    </svg>
    <div class="ml-4 text-sm">
      <div class="flex items-center gap-2" v-for="(val, key) in data" :key="key">
        <span class="inline-block w-3 h-3 rounded" :style="{ background: colors[key] }"></span>
        <span class="text-gray-600">{{ labels[key] }}：</span>
        <span class="font-medium text-gray-900">{{ val }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({ ios: 0, android: 0, pc: 0 }) },
  size: { type: Number, default: 220 },
})

const total = computed(() => Object.values(props.data).reduce((a, b) => a + b, 0) || 1)
const circumference = 2 * Math.PI * 40
const start = 0
const iosPct = computed(() => props.data.ios / total.value)
const androidPct = computed(() => props.data.android / total.value)
const pcPct = computed(() => props.data.pc / total.value)

const offsets = computed(() => ({
  ios: circumference * (1 - iosPct.value),
  android: circumference * (1 - (iosPct.value + androidPct.value)),
  pc: circumference * (1 - (iosPct.value + androidPct.value + pcPct.value)),
}))

const colors = { ios: '#4F46E5', android: '#7C3AED', pc: '#06B6D4' }
const labels = { ios: 'iOS', android: 'Android', pc: 'PC' }
</script>

<style scoped>
svg { transform: rotate(-90deg); }
</style>