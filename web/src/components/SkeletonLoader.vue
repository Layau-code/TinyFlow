<template>
  <div :class="wrapperClass">
    <div v-for="n in count" :key="n" :class="skeletonClass" :style="skeletonStyle"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ 
  count: { type: Number, default: 1 }, 
  height: { type: Number, default: 160 }, 
  width: { type: String, default: '100%' },
  vertical: { type: Boolean, default: true },
  variant: { type: String, default: 'default' } // 'default', 'card', 'text', 'circle'
})

const wrapperClass = computed(() => props.vertical ? 'space-y-2' : 'flex gap-2')

const skeletonClass = computed(() => {
  const baseClass = 'animate-pulse'
  const variantClass = {
    'default': 'bg-gray-200 rounded',
    'card': 'bg-gradient-to-r from-gray-200 via-gray-100 to-gray-200 rounded-lg shadow-sm',
    'text': 'bg-gray-200 rounded',
    'circle': 'bg-gray-200 rounded-full'
  }
  return `${baseClass} ${variantClass[props.variant] || variantClass.default}`
})

const skeletonStyle = computed(() => ({
  height: props.variant === 'circle' ? props.height + 'px' : props.height + 'px',
  width: props.variant === 'circle' ? props.height + 'px' : props.width
}))
</script>

<style scoped>
@keyframes shimmer {
  0% { background-position: -468px 0; }
  100% { background-position: 468px 0; }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>