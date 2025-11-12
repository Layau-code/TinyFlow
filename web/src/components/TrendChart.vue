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
      <!-- wide hover hit areas for long ranges (e.g., 30 days) -->
      <g v-if="wideHit">
        <rect v-for="(r, i) in hitRects" :key="'hit-'+i"
          :x="r.x" :y="padding" :width="r.w" :height="svgH - padding * 2"
          fill="transparent" style="cursor: crosshair" @mouseenter="setHoverByIndex(i)" @mousemove="setHoverByIndex(i)" @mouseleave="clearHover" />
      </g>
      <!-- hover vertical guide -->
      <line v-if="hoverIdx!==null" :x1="ptX(hoverIdx)" :y1="padding" :x2="ptX(hoverIdx)" :y2="svgH - padding" stroke="var(--tf-blue-1)" stroke-width="0.5" stroke-dasharray="3,3" />
      <!-- trend series -->
      <g v-for="(s, si) in seriesData" :key="si">
        <polyline :points="pointsAttr(si)" :stroke="s.color || lineColor" fill="none" stroke-width="1.5"
                  style="pointer-events: visibleStroke; cursor: crosshair"
                  @mouseenter="onLineMove(si, $event)" @mousemove="onLineMove(si, $event)" @mouseleave="clearHover" />
        <g>
          <g v-for="(p,i) in s.series" :key="i" @mouseenter="setHover(i, p.label, p.value, s.name, s.color)" @mousemove="setHover(i, p.label, p.value, s.name, s.color)" @mouseleave="clearHover">
            <circle :cx="ptX(i)" :cy="ptY(p.value)" r="2" :fill="s.color || lineColor" class="pt">
              <title>{{ p.label }}: {{ p.value }}</title>
            </circle>
            <text v-if="!multi && showValues" :x="ptX(i)" :y="ptY(p.value) - 6" text-anchor="middle" font-size="10" :fill="s.color || lineColor">{{ p.value }}</text>
          </g>
        </g>
      </g>
      <!-- in-chart tooltip -->
      <g v-if="hoverIdx!==null && hoverName">
        <rect :x="tip.x" :y="tip.y" :width="tip.w" :height="tip.h" rx="6" ry="6" fill="#ffffff" :stroke="axisColor" stroke-width="0.8" />
        <text :x="tip.x + 8" :y="tip.y + 14" :fill="secondary" font-size="11">{{ hoverLabel }}</text>
        <text :x="tip.x + 8" :y="tip.y + 28" :fill="hoverColor || lineColor" font-size="12">{{ hoverName }}：{{ hoverValue }}</text>
      </g>
      <!-- x axis labels aligned to points (for long ranges) -->
      <g v-if="showSvgXAxisLabels">
        <g v-for="(l, i) in labels" :key="'lab-'+i" v-if="i % labelEvery === 0">
          <text :x="ptX(i)" :y="svgH - padding + 14" text-anchor="middle" :fill="secondary" font-size="10">{{ l }}</text>
        </g>
      </g>
    </svg>
    <!-- bottom label row for short ranges (<=16 labels) -->
    <div v-if="showBottomLabelRow" class="flex justify-between text-xs mt-2" :style="{ color: secondary }">
      <span v-for="(l, i) in labels" :key="'bl-'+i">{{ l }}</span>
    </div>
    <!-- legend for multi-series -->
    <div v-if="multi" class="flex flex-wrap gap-3 text-xs mt-2" :style="{ color: secondary }">
      <div v-for="(s, si) in seriesData" :key="'legend-'+si" class="flex items-center gap-1">
        <span class="inline-block w-2.5 h-2.5 rounded" :style="{ background: s.color || lineColor }"></span>
        <span>{{ s.name }}</span>
      </div>
    </div>
    <!-- hover info display -->
    <div class="flex justify-center text-xs mt-2" :style="{ color: secondary }">
      <span v-if="hoverLabel && hoverName">{{ hoverLabel }} · {{ hoverName }}：{{ hoverValue }}</span>
      <span v-else-if="hoverLabel">{{ hoverLabel }}</span>
      <span v-else>&nbsp;</span>
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

// reduce x-label density for long ranges
const labelEvery = computed(() => {
  const len = (props.labels||[]).length
  // 保证 7/14 天完整显示；适度稀疏 17~24；更长按约6个刻度
  if (len <= 16) return 1
  if (len <= 24) return 2
  return Math.ceil(len / 6)
})

// 控制不同范围的标签呈现位置：短范围放到图下方，长范围保留在SVG内
const showBottomLabelRow = computed(() => ((props.labels||[]).length) <= 16)
const showSvgXAxisLabels = computed(() => !showBottomLabelRow.value)

// hover info
const hoverIdx = ref(null)
const hoverLabel = ref('')
const hoverName = ref('')
const hoverValue = ref(0)
const hoverColor = ref('')
function setHover(i, label, value, seriesName, color){
  hoverIdx.value = i
  hoverLabel.value = String(label)
  hoverName.value = String(seriesName || '')
  hoverValue.value = Number(value || 0)
  hoverColor.value = String(color || '')
}
function clearHover(){
  hoverIdx.value = null
  hoverLabel.value = ''
  hoverName.value = ''
  hoverValue.value = 0
  hoverColor.value = ''
}

// whether to enable wide hit zones (for 30-day view)
const wideHit = computed(() => (props.labels || []).length >= 25)
const hitRects = computed(() => {
  const arr = []
  const len = (props.labels || []).length
  const step = stepX.value
  for (let i = 0; i < len; i++) {
    const center = ptX(i)
    let x = center - step / 2
    const minX = padding
    const maxX = (svgW.value - padding) - step
    if (x < minX) x = minX
    if (x > maxX) x = maxX
    arr.push({ x, w: step })
  }
  return arr
})

// 根据鼠标位置在折线上计算索引并设置悬停信息
function onLineMove(si, evt){
  try {
    const rect = container.value?.getBoundingClientRect()
    const xRel = (evt?.clientX || 0) - (rect?.left || 0)
    let idx = Math.round((xRel - padding) / stepX.value)
    idx = Math.max(0, Math.min(idx, (props.labels||[]).length - 1))
    const s = seriesData.value[si]
    const p = (s?.series || [])[idx] || { label: (props.labels||[])[idx], value: 0 }
    setHover(idx, p.label, p.value, s?.name, s?.color)
  } catch {}
}

// 在宽感知区域内，选取该日期点击数最大的系列进行展示
function setHoverByIndex(i){
  const labels = props.labels || []
  const arr = seriesData.value || []
  let bestSi = -1, bestVal = -1
  arr.forEach((s, si) => {
    const v = Number((s?.series || [])[i]?.value || 0)
    if (v > bestVal) { bestVal = v; bestSi = si }
  })
  const target = arr[bestSi] || arr[0] || null
  const p = target ? (target.series || [])[i] : null
  setHover(i, labels[i], p ? p.value : 0, target ? target.name : '', target ? target.color : '')
}

// 悬停浮层位置（靠近点，自动避免溢出）
const tip = computed(() => {
  if (hoverIdx.value == null) return { x: 0, y: 0, w: 132, h: 34 }
  const w = 132, h = 34
  const hx = ptX(hoverIdx.value)
  const hy = ptY(hoverValue.value)
  let x = hx + 6, y = hy - h - 6
  const right = x + w
  const maxX = svgW.value - padding
  const minY = padding
  if (right > maxX) x = hx - w - 6
  if (y < minY) y = hy + 8
  return { x, y, w, h }
})
</script>

<style scoped>
svg .pt { opacity: 0; transition: opacity .15s ease; }
svg:hover .pt { opacity: 1; }
</style>