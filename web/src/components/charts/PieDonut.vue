<template>
  <div class="w-full">
    <div class="flex flex-wrap items-center justify-center gap-3 mb-3">
      <div class="flex items-center gap-2" v-for="(item,i) in data" :key="i"
           @mouseenter="setHover(i)" @mouseleave="clearHover()">
        <span class="inline-block w-3 h-3 rounded" :style="{background:colorsComputed[i%colorsComputed.length]}"></span>
        <span class="text-[var(--text-secondary)] text-sm">{{item.label}}</span>
      </div>
    </div>
    <div class="flex items-center justify-center">
      <div class="relative" style="width:240px;height:240px">
        <svg width="240" height="240" viewBox="0 0 100 100" class="absolute inset-0">
          <circle cx="50" cy="50" r="40" :stroke="divider" stroke-width="20" fill="none"/>
          <g v-if="hasData">
            <path v-for="(seg,i) in segments" :key="i" :d="seg.path" :fill="colorsComputed[i%colorsComputed.length]" fill-rule="evenodd" />
          </g>
        </svg>
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="text-center">
            <div class="font-bold" :style="{ fontSize: hoverItem? '28px' : '20px', color: hoverColor }">
              {{ !hasData ? '暂无数据' : (hoverItem ? hoverItem.label : '总点击') }}
            </div>
            <div class="mt-1" :style="{ fontSize: hoverItem? '18px' : '14px', color: 'var(--text-secondary)' }">
              {{ !hasData ? '' : (hoverItem ? precisePercent(hoverItem.value) + ' · ' + hoverItem.value : total) }}
            </div>
          </div>
        </div>
      </div>
      <div class="ml-4 text-sm">
        <div v-if="hasData" class="flex items-center gap-2" v-for="(item,i) in data" :key="i"
             @mouseenter="setHover(i)" @mouseleave="clearHover()">
          <span class="inline-block w-3 h-3 rounded" :style="{background:colorsComputed[i%colorsComputed.length]}"></span>
          <span class="text-[var(--text-secondary)]">{{item.label}}</span>
          <span class="font-medium text-[var(--text-primary)]">{{item.value}}</span>
          <span class="text-[var(--text-secondary)] ml-1">({{ precisePercent(item.value) }})</span>
        </div>
        <div v-else class="q-help">暂无数据或接口未返回</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ data: { type: Array, default: () => [] }, colors: { type: Array, default: () => [] } })

const totalRaw = computed(()=> (props.data||[]).reduce((a,b)=> a + Number(b.value||0), 0))
const total = computed(()=> totalRaw.value > 0 ? totalRaw.value : 1)
const colorsComputed = computed(() => (props.colors && props.colors.length ? props.colors : ['#6E44FF','#8A7CFD','#7A5BFF','#9D8BFF','#B0A4FF','#C7BEFF','#D6D1FF','#E0DCFF','#EAE8FF']))
const divider = 'var(--divider)'

const toRad = (deg)=> (deg * Math.PI) / 180
const pointOnCircle = (cx, cy, r, ang)=> ({ x: cx + r * Math.cos(ang), y: cy + r * Math.sin(ang) })
const arcPath = (cx, cy, rInner, rOuter, startAng, endAng) => {
  const largeArc = (endAng - startAng) > Math.PI ? 1 : 0
  const p1 = pointOnCircle(cx, cy, rOuter, startAng)
  const p2 = pointOnCircle(cx, cy, rOuter, endAng)
  const p3 = pointOnCircle(cx, cy, rInner, endAng)
  const p4 = pointOnCircle(cx, cy, rInner, startAng)
  return `M ${p1.x} ${p1.y} A ${rOuter} ${rOuter} 0 ${largeArc} 1 ${p2.x} ${p2.y} L ${p3.x} ${p3.y} A ${rInner} ${rInner} 0 ${largeArc} 0 ${p4.x} ${p4.y} Z`
}
const segments = computed(() => {
  const rOuter = 50 - 10
  const rInner = 50 - 30
  let acc = -Math.PI/2
  const arr = (props.data||[]).filter(d=> Number(d.value||0) > 0)
  return arr.map(d => {
    const portion = Number(d.value||0) / total.value
    const start = acc
    const end = acc + portion * 2 * Math.PI
    acc = end
    return { path: arcPath(50, 50, rInner, rOuter, start, end) }
  })
})

const precisePercent = (v)=> `${((v/total.value)*100).toFixed(2)}%`
const hoverIndex = ref(null)
const setHover = (i)=> { hoverIndex.value = i }
const clearHover = ()=> { hoverIndex.value = null }
const hoverItem = computed(()=> hoverIndex.value==null ? null : props.data[hoverIndex.value])
const hoverColor = computed(()=> hoverIndex.value==null ? 'var(--text-secondary)' : colorsComputed.value[hoverIndex.value % colorsComputed.value.length])
const hasData = computed(()=> (props.data||[]).some(d => Number(d.value||0) > 0))

</script>

<style scoped>
svg { transform: rotate(-90deg); }
</style>