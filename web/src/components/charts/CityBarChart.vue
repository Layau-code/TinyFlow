<template>
  <div class="w-full">
    <div class="grid grid-cols-5 gap-4 items-end">
      <div v-for="(item, i) in data" :key="i" class="flex flex-col items-center">
        <div class="w-full bg-indigo-200" :style="{ height: barHeight(item.count) }"></div>
        <div class="text-xs text-gray-600 mt-1 truncate w-full text-center">{{ item.city }}</div>
        <div class="text-xs text-gray-900 mt-0.5">{{ item.count }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ data: { type: Array, default: () => [] } })
const maxVal = computed(() => Math.max(1, ...props.data.map(d => d.count || 0)))
const barHeight = (v) => `${Math.round((v / maxVal.value) * 160) + 20}px`
</script>

<style scoped>
</style>