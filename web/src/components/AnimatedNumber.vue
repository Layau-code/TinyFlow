<template>
  <span ref="numberRef" class="animated-number">{{ displayValue }}</span>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 1000
  },
  decimals: {
    type: Number,
    default: 0
  }
})

const displayValue = ref(0)
const numberRef = ref(null)

function animateNumber(start, end, duration) {
  const startTime = Date.now()
  const range = end - start

  const step = () => {
    const now = Date.now()
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用缓动函数（easeOutQuad）
    const easeProgress = progress * (2 - progress)
    
    const current = start + range * easeProgress
    displayValue.value = parseFloat(current.toFixed(props.decimals))

    if (progress < 1) {
      requestAnimationFrame(step)
    } else {
      displayValue.value = parseFloat(end.toFixed(props.decimals))
    }
  }

  requestAnimationFrame(step)
}

watch(() => props.value, (newVal, oldVal) => {
  animateNumber(oldVal || 0, newVal, props.duration)
}, { immediate: true })

onMounted(() => {
  animateNumber(0, props.value, props.duration)
})
</script>

<style scoped>
.animated-number {
  font-variant-numeric: tabular-nums;
}
</style>
