<template>
  <div ref="root" class="async-chart-loader">
    <component v-if="isLoaded && ChartComp" :is="ChartComp" ref="innerComp" v-bind="passThroughProps" v-on="attrs" @error="onError" />
    <div v-else class="loader-placeholder">
      <slot name="fallback">
        <div class="skeleton" :style="{ height: height + 'px' }"></div>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed, defineExpose, useAttrs } from 'vue'

const attrs = useAttrs()
const props = defineProps({
  loader: { type: Function, required: true },
  height: { type: Number, default: 280 },
  rootMargin: { type: String, default: '200px 0px' },
  threshold: { type: Number, default: 0.1 },
  passThroughProps: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['loaded', 'error'])

const root = ref(null)
const isLoaded = ref(false)
const ChartComp = ref(null)
const innerComp = ref(null)
let observer = null

async function load() {
  try {
    const mod = await props.loader()
    ChartComp.value = mod.default || mod
    isLoaded.value = true
    emit('loaded')
  } catch (e) {
    console.error('AsyncChartLoader load error', e)
    emit('error', e)
  }
}

function onIntersect(entries) {
  for (const entry of entries) {
    if (entry.isIntersecting) {
      if (!isLoaded.value) load()
    }
  }
}

onMounted(() => {
  try {
    observer = new IntersectionObserver(onIntersect, { root: null, rootMargin: props.rootMargin, threshold: props.threshold })
    if (root.value) observer.observe(root.value)
  } catch (e) {
    // 如果浏览器不支持 IntersectionObserver，直接加载
    load()
  }
})

onBeforeUnmount(() => { if (observer && root.value) observer.unobserve(root.value) })

function onError(e) { emit('error', e) }

// 暴露 inner component 的实例访问器，供父组件调用子组件方法（如 getDataURL）
function getInnerInstance() { return innerComp.value }

defineExpose({ getInnerInstance })
</script>

<style scoped>
.async-chart-loader { width: 100%; }
.loader-placeholder { display: flex; align-items: center; justify-content: center; }
.skeleton { width: 100%; background: linear-gradient(90deg, #f3f4f6 0%, #e5e7eb 50%, #f3f4f6 100%); border-radius: 8px; animation: shimmer 1.2s linear infinite; }
@keyframes shimmer { 0% { background-position: -200% 0 } 100% { background-position: 200% 0 } }
</style>
