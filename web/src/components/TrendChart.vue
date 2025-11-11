<template>
  <div class="w-full" ref="container">
    <svg :width="svgW" :height="svgH" class="overflow-visible">
      <!-- axes -->
      <line :x1="padding" :y1="svgH - padding" :x2="svgW - padding" :y2="svgH - padding" :stroke="axisColor" stroke-width="1" />
      <line :x1="padding" :y1="padding" :x2="padding" :y2="svgH - padding" :stroke="axisColor" stroke-width="1" />
      <!-- horizontal gridlines & y-axis ticks -->
      <g>
        <g v-for="(tk, ti) in yTicks" :key="'ytk-'+ti">
          <line :x1="padding" :y1="tk.y" :x2="svgW - padding" :y2="tk.y" stroke="var(--divider)" stroke-width="0.5" stroke-dasharray="2,2" />
          <text :x="padding - 6" :y="tk.y + 3" text-anchor="end" :fill="secondary" font-size="10">{{ tk.value }}</text>
        </g>
      </g>
      <!-- trend series -->
      <g v-for="(s, si) in seriesData" :key="si">
        <polyline :points="pointsAttr(si)" :stroke="s.color || lineColor" fill="none" stroke-width="1.5" />
        <g>
          <g v-for="(p,i) in s.series" :key="i">
            <circle :cx="ptX(i)" :cy="ptY(p.value)" r="2" :fill="s.color || lineColor" class="pt">
              <title>{{ p.label }}: {{ p.value }}</title>
            </circle>
            <text v-if="!multi && showValues" :x="ptX(i)" :y="ptY(p.value) - 6" text-anchor="middle" font-size="10" :fill="s.color || lineColor">{{ p.value }}</text>
          </g>
        </g>
      </g>
      <!-- x axis labels aligned to points -->
      <g v-for="(l, i) in labels" :key="'lab-'+i">
        <text :x="ptX(i)" :y="svgH - padding + 14" text-anchor="middle" :fill="secondary" font-size="10">{{ l }}</text>
      </g>
    </svg>
    <!-- legend for multi-series -->
    <div v-if="multi" class="flex flex-wrap gap-3 text-xs mt-2" :style="{ color: secondary }">
      <div v-for="(s, si) in seriesData" :key="'legend-'+si" class="flex items-center gap-1">
        <span class="inline-block w-2.5 h-2.5 rounded" :style="{ background: s.color || lineColor }"></span>
        <span>{{ s.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
const props = defineProps({
  multi: { type: Boolean, default: false },
  values: { type: Array, default: () => [] },
  labels: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] }, // [{ name, color, series:[{label,value}]}]
  width: { type: Number, default: 640 },
  height: { type: Number, default: 240 },
  showValues: { type: Boolean, default: false },
})

const padding = 24
const container = ref(null)
const svgW = ref(props.width)
const svgH = props.height

// Resize observer to make chart width responsive to its container
let ro
onMounted(() => {
  const update = () => {
    const w = container.value?.clientWidth || props.width
    svgW.value = Math.max(200, Math.floor(w))
  }
  update()
  try {
    ro = new ResizeObserver(update)
    if (container.value) ro.observe(container.value)
  } catch {}
})
onBeforeUnmount(() => { try { ro && ro.disconnect() } catch {} })

const seriesData = computed(() => {
  if (props.multi) return props.series
  return [{ name: 'trend', color: lineColor, series: (props.labels||[]).map((l,i)=>({ label:l, value: props.values[i]||0 })) }]
})

const maxVal = computed(() => {
  const vals = []
  seriesData.value.forEach(s => s.series.forEach(p => vals.push(p.value||0)))
  return Math.max(1, ...vals)
})

const stepX = computed(() => (svgW.value - padding * 2) / Math.max(1, (props.labels||[]).length - 1))
function pointsAttr(si){
  const s = seriesData.value[si]
  const pts = (s.series||[]).map((p,i)=>{
    const x = padding + i * stepX.value
    const y = padding + (svgH - padding*2) * (1 - (p.value||0)/maxVal.value)
    return `${x},${y}`
  })
  return pts.join(' ')
}

function ptX(i){ return padding + i * stepX.value }
function ptY(v){ return padding + (svgH - padding*2) * (1 - (v||0)/maxVal.value) }
const lineColor = 'var(--tf-blue-1)'
const axisColor = 'var(--tf-border)'
const secondary = 'var(--text-secondary)'

// Y 轴刻度与辅助网格线（提升数值可读性）
const yTicks = computed(() => {
  const ticks = []
  const tickCount = 4
  for (let i = 0; i <= tickCount; i++) {
    const v = Math.round((maxVal.value * i) / tickCount)
    ticks.push({ value: v, y: ptY(v) })
  }
  return ticks
})
</script>

<style scoped>
svg .pt { opacity: 0; transition: opacity .15s ease; }
svg:hover .pt { opacity: 1; }
</style>