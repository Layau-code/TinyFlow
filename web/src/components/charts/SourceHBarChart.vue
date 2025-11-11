<template>
  <div class="space-y-2">
    <div v-for="(item, i) in data" :key="i" class="w-full">
      <div class="flex items-center justify-between text-sm mb-1">
        <span class="text-gray-600">{{ item.source }}</span>
        <span class="text-gray-900">{{ item.count }}</span>
      </div>
      <div class="w-full bg-gray-200 rounded">
        <div class="bg-indigo-600 h-3 rounded" :style="{ width: barWidth(item.count) }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ data: { type: Array, default: () => [] } })
const maxVal = computed(() => Math.max(1, ...props.data.map(d => d.count || 0)))
const barWidth = (v) => `${Math.round((v / maxVal.value) * 100)}%`
</script>

<style scoped>
</style>