<template>
  <div class="w-full">
    <svg :width="width" :height="height" class="overflow-visible">
      <polyline :points="pointsAttr" fill="none" :stroke="color" stroke-width="2" />
      <g v-for="(pt, i) in scaled" :key="i">
        <circle :cx="pt.x" :cy="pt.y" :r="3" :fill="color" />
      </g>
    </svg>
    <div class="flex justify-between text-xs text-gray-500 mt-2">
      <span v-for="(l, i) in labels" :key="i">{{ l }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  values: { type: Array, default: () => [] },
  labels: { type: Array, default: () => [] },
  width: { type: Number, default: 600 },
  height: { type: Number, default: 220 },
  color: { type: String, default: '#4F46E5' },
})

const padding = 20
const maxVal = computed(() => Math.max(1, ...props.values))
const stepX = computed(() => (props.width - padding * 2) / Math.max(1, props.values.length - 1))
const scaled = computed(() => props.values.map((v, i) => {
  const x = padding + i * stepX.value
  const y = padding + (props.height - padding * 2) * (1 - v / maxVal.value)
  return { x, y }
}))
const pointsAttr = computed(() => scaled.value.map(p => `${p.x},${p.y}`).join(' '))
</script>

<style scoped>
</style>