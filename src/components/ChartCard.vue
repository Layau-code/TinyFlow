<template>
  <div class="chart-card" ref="rootEl">
    <div class="card-header">
      <h3>{{ title }}</h3>
      <div class="card-actions">
        <button v-if="exportable" class="action-btn" @click="handleExportPng" title="导出 PNG">PNG</button>
        <button v-if="exportable" class="action-btn" @click="exportCSV" title="导出 CSV">CSV</button>
        <button class="action-btn" @click="toggleFullScreen" title="全屏">⤢</button>
      </div>
    </div>
    <div class="card-body">
      <div ref="chartRef" class="chart-container" :style="{ height: height + 'px' }" />
    </div>
  </div>
</template>

<script lang="ts">
// 声明模块以避免在没有安装 echarts 类型时的编译错误
declare module 'echarts';

import { onMounted, onBeforeUnmount, ref, watch } from 'vue';

export default {
  name: 'ChartCard',
  props: {
    title: { type: String, default: '' },
    chartData: { type: Object, required: true }, // { x: [], series: [{ name, data }] }
    chartType: { type: String, default: 'line' },
    lazy: { type: Boolean, default: false },
    height: { type: Number, default: 260 },
    exportable: { type: Boolean, default: true },
    engine: { type: String, default: 'echarts' }
  },
  emits: ['point-click'],
  setup(props, { emit }) {
    const chartRef = ref<HTMLElement | null>(null);
    const rootEl = ref<HTMLElement | null>(null);
    let chartInstance: any = null;
    let echarts: any = null;
    let observer: IntersectionObserver | null = null;

    const createOption = (data: any) => {
      const option: any = {
        tooltip: { trigger: 'axis' },
        legend: { data: data.series?.map((s: any) => s.name) || [] },
        grid: { left: '8%', right: '8%', bottom: '12%' },
        xAxis: { type: 'category', data: data.x || [], boundaryGap: props.chartType === 'bar' },
        yAxis: { type: 'value' },
        dataZoom: [{ type: 'slider', start: 0, end: 100 }, { type: 'inside' }],
        series: (data.series || []).map((s: any) => {
          const common: any = { name: s.name, data: s.data };
          if (props.chartType === 'bar') return { ...common, type: 'bar' };
          if (props.chartType === 'area') return { ...common, type: 'line', areaStyle: {} };
          if (props.chartType === 'pie') {
            return null;
          }
          return { ...common, type: 'line', smooth: true };
        }).filter(Boolean),
      };

      if (props.chartType === 'pie' && data.series && data.series.length) {
        option.series = [{
          name: props.title,
          type: 'pie',
          radius: '60%',
          data: data.series.map((s: any) => ({ name: s.name, value: (s.data || []).reduce((a: number,b:number)=>a+b,0) })),
        }];
        option.legend = { orient: 'vertical', left: 10 };
        delete option.xAxis; delete option.yAxis;
      }

      return option;
    };

    async function initChart(){
      try {
        if (props.engine && props.engine !== 'echarts') {
          console.warn('ChartCard: engine %s not supported yet, falling back to echarts', props.engine)
        }
        const mod = await import('echarts')
        echarts = mod.default || mod
        if (!chartRef.value) return
        chartInstance = echarts.init(chartRef.value)
        const opt = createOption(props.chartData)
        chartInstance.setOption(opt)
        chartInstance.off('click')
        chartInstance.on('click', (params: any) => {
          emit('point-click', { key: params?.seriesId || params?.seriesName, value: params?.value, dataIndex: params?.dataIndex, raw: params })
        })
        // add zoom and brush handlers if needed in future
        window.addEventListener('resize', resizeHandler)
      } catch (e) {
        console.error('ChartCard init echarts error', e)
      }
    }

    function resizeHandler(){ try { chartInstance && chartInstance.resize() } catch {} }

    function update(){ if (chartInstance) chartInstance.setOption(createOption(props.chartData)) }

    function getDataURL(options = { type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' }){
      try { if (!chartInstance) return null; return chartInstance.getDataURL(options) } catch (e) { console.error(e); return null }
    }

    function exportCSV(){
      try {
        const data = props.chartData || {}
        const x = data.x || []
        const rows: string[] = []
        const header = ['x', ...(data.series || []).map((s:any)=>s.name)]
        rows.push(header.join(','))
        for (let i=0;i<(x.length||0);i++){
          const row = [x[i]]
          for (const s of (data.series||[])) row.push((s.data && s.data[i])!=null ? s.data[i] : '')
          rows.push(row.join(','))
        }
        const csv = rows.join('\n')
        const blob = new Blob([csv], { type: 'text/csv' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `${(props.title||'chart').replace(/[^a-z0-9]/gi,'_')}.csv`
        a.click()
        URL.revokeObjectURL(url)
      } catch (e) { console.error('exportCSV error', e) }
    }

    async function handleExportPng(){
      try {
        const dataUrl = getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' })
        if (!dataUrl) { alert('图表未就绪或不支持导出'); return }
        const a = document.createElement('a')
        a.href = dataUrl
        a.download = `${(props.title||'chart').replace(/[^a-z0-9]/gi,'_')}-chart.png`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
      } catch (e) { console.error('export png error', e); alert('导出失败') }
    }

    async function toggleFullScreen(){
      try {
        const el = rootEl.value || chartRef.value
        if (!el) return
        // @ts-ignore
        if (document.fullscreenElement) {
          await document.exitFullscreen()
        } else {
          // @ts-ignore
          await (el as any).requestFullscreen()
        }
        // allow chart to resize after fullscreen change
        setTimeout(() => { try { chartInstance && chartInstance.resize() } catch {} }, 250)
      } catch (e) { console.error('toggleFullScreen error', e) }
    }

    onMounted(() => {
      if (props.lazy && chartRef.value) {
        try {
          observer = new IntersectionObserver((entries)=>{
            for(const entry of entries){ if (entry.isIntersecting) { initChart(); if (observer && chartRef.value) observer.unobserve(chartRef.value); } }
          }, { root: null, rootMargin: '200px 0px', threshold: 0.1 })
          observer.observe(chartRef.value)
        } catch (e) { initChart() }
      } else { initChart() }
    })

    onBeforeUnmount(()=>{ try{ chartInstance && chartInstance.dispose(); window.removeEventListener('resize', resizeHandler); if (observer && chartRef.value) observer.unobserve(chartRef.value) }catch{} })

    watch(()=>[props.chartData, props.chartType], ()=>{ update() }, { deep: true })

    return { chartRef, rootEl, getDataURL, exportCSV, resizeHandler, handleExportPng, toggleFullScreen }
  },
};
</script>

<style scoped>
.chart-card { background:#fff; border-radius:8px; padding:8px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.card-actions { display:flex; gap:8px; }
.action-btn { padding:6px 8px; border-radius:6px; border:1px solid #e5e7eb; background:#fff; cursor:pointer; font-size:12px }
.chart-container { width:100%; }
.card-header h3 { margin:0; font-size:14px; }
</style>
