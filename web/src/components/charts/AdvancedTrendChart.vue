<template>
  <div class="advanced-trend-chart">
    <!-- 图表顶部控制栏 -->
    <div class="chart-controls">
      <div class="control-group">
        <div class="control-label">时间范围：</div>
        <div class="preset-buttons">
          <button
            v-for="preset in timePresets"
            :key="preset.value"
            class="preset-btn"
            :class="{ active: selectedPreset === preset.value }"
            @click="setPreset(preset.value)"
          >
            {{ preset.label }}
          </button>
        </div>
      </div>

      <div class="control-group">
        <div class="control-label">数据类型：</div>
        <div class="type-buttons">
          <button
            v-for="type in chartTypes"
            :key="type.value"
            class="type-btn"
            :class="{ active: chartType === type.value }"
            @click="setChartType(type.value)"
          >
            {{ type.label }}
          </button>
        </div>
      </div>

      <div class="chart-actions">
        <button class="action-btn" @click="exportChart" title="导出图表">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
          </svg>
        </button>
        <button class="action-btn" @click="refreshData" title="刷新数据">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-container">
      <div v-if="loading" class="chart-loading">
        <div class="loading-spinner"></div>
        <div class="loading-text">加载中...</div>
      </div>

      <div v-else-if="filteredData.length === 0" class="chart-empty">
        <div class="empty-icon">📊</div>
        <div class="empty-text">暂无数据</div>
        <div class="empty-desc">请选择其他时间范围或检查数据</div>
      </div>

      <div v-else ref="chartRef" class="chart-canvas"></div>
    </div>

    <!-- 统计信息 -->
    <div class="chart-stats">
      <div class="stat-item">
        <div class="stat-label">平均值</div>
        <div class="stat-value">{{ avgValue }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">峰值</div>
        <div class="stat-value">{{ peakValue }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">增长率</div>
        <div class="stat-value" :class="growthRateClass">
          {{ growthRate }}%
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-label">总访问</div>
        <div class="stat-value">{{ totalValue }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { Chart } from '@antv/g2'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  xField: {
    type: String,
    default: 'date'
  },
  yField: {
    type: String,
    default: 'value'
  },
  height: {
    type: Number,
    default: 320
  },
  showStats: {
    type: Boolean,
    default: true
  }
})

// 响应式数据
const selectedPreset = ref(7)
const chartType = ref('line')
const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

// 配置选项
const timePresets = [
  { label: '7天', value: 7 },
  { label: '14天', value: 14 },
  { label: '30天', value: 30 },
  { label: '90天', value: 90 }
]

const chartTypes = [
  { label: '折线图', value: 'line' },
  { label: '面积图', value: 'area' },
  { label: '柱状图', value: 'column' }
]

// 计算属性
const filteredData = computed(() => {
  if (!props.data || props.data.length === 0) return []

  const days = selectedPreset.value
  const today = new Date()
  const cutoffDate = new Date(today.getTime() - days * 24 * 60 * 60 * 1000)

  return props.data.filter(item => {
    const itemDate = new Date(item[props.xField])
    return itemDate >= cutoffDate
  })
})

const avgValue = computed(() => {
  if (filteredData.value.length === 0) return 0
  const total = filteredData.value.reduce((sum, item) => sum + item[props.yField], 0)
  return Math.round(total / filteredData.value.length)
})

const peakValue = computed(() => {
  if (filteredData.value.length === 0) return 0
  return Math.max(...filteredData.value.map(item => item[props.yField]))
})

const totalValue = computed(() => {
  if (filteredData.value.length === 0) return 0
  return filteredData.value.reduce((sum, item) => sum + item[props.yField], 0)
})

const growthRate = computed(() => {
  if (filteredData.value.length < 2) return 0

  const values = filteredData.value.map(item => item[props.yField])
  const latest = values[values.length - 1]
  const previous = values[values.length - 2]

  if (previous === 0) return latest > 0 ? 100 : 0

  return Math.round(((latest - previous) / previous) * 100)
})

const growthRateClass = computed(() => {
  if (growthRate.value > 0) return 'positive'
  if (growthRate.value < 0) return 'negative'
  return 'neutral'
})

// 方法
function setPreset(preset) {
  selectedPreset.value = preset
  updateChart()
}

function setChartType(type) {
  chartType.value = type
  updateChart()
}

function refreshData() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    updateChart()
  }, 500)
}

function exportChart() {
  if (!chartInstance) return

  const dataURL = chartInstance.toDataURL()
  const link = document.createElement('a')
  link.download = `趋势图-${new Date().toISOString().split('T')[0]}.png`
  link.href = dataURL
  link.click()
}

function initChart() {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.destroy()
  }

  chartInstance = new Chart({
    container: chartRef.value,
    autoFit: true,
    height: props.height,
  })

  updateChart()
}

function updateChart() {
  if (!chartInstance || filteredData.value.length === 0) return

  chartInstance.clear()

  // 配置图表
  chartInstance.data(filteredData.value)

  // 根据图表类型配置编码
  const encode = {
    x: props.xField,
    y: props.yField
  }

  switch (chartType.value) {
    case 'line':
      chartInstance
        .line()
        .encode(encode)
        .style('stroke', '#3b82f6')
        .style('lineWidth', 3)
        .style('smooth', true)
      break
    case 'area':
      chartInstance
        .area()
        .encode(encode)
        .style('fill', 'l(90) 0:#3b82f6 1:#ffffff')
        .style('smooth', true)
      break
    case 'column':
      chartInstance
        .interval()
        .encode(encode)
        .style('fill', '#3b82f6')
      break
  }

  // 添加数据点
  chartInstance
    .point()
    .encode(encode)
    .style('fill', '#ffffff')
    .style('stroke', '#3b82f6')
    .style('lineWidth', 2)

  // 配置坐标轴
  chartInstance.axis('x', {
    title: false,
    labelFormatter: (value) => {
      const date = new Date(value)
      return `${date.getMonth() + 1}/${date.getDate()}`
    }
  })

  chartInstance.axis('y', {
    title: false,
    labelFormatter: (value) => {
      if (value >= 1000) return (value / 1000).toFixed(1) + 'k'
      return value
    }
  })

  // 添加交互
  chartInstance.interaction('elementHighlight', true)
  chartInstance.interaction('tooltip', {
    shared: true
  })

  chartInstance.render()
}

// 生命周期
onMounted(() => {
  setTimeout(initChart, 100)
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.destroy()
  }
})

watch(() => props.data, () => {
  setTimeout(updateChart, 50)
}, { deep: true })

watch(() => props.height, updateChart)
</script>

<style scoped>
.advanced-trend-chart {
  background: var(--tf-surface);
  border-radius: var(--tf-radius-lg);
  border: 1px solid var(--tf-border);
  overflow: hidden;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: var(--tf-spacing-4);
  padding: var(--tf-spacing-4);
  border-bottom: 1px solid var(--tf-border);
  background: var(--tf-surface-2);
  flex-wrap: wrap;
}

.control-group {
  display: flex;
  align-items: center;
  gap: var(--tf-spacing-3);
}

.control-label {
  font-size: var(--tf-text-sm);
  color: var(--tf-text-secondary);
  font-weight: 500;
}

.preset-buttons,
.type-buttons {
  display: flex;
  gap: var(--tf-spacing-2);
}

.preset-btn,
.type-btn {
  padding: var(--tf-spacing-2) var(--tf-spacing-3);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  background: var(--tf-surface);
  color: var(--tf-text-secondary);
  font-size: var(--tf-text-sm);
  cursor: pointer;
  transition: all var(--tf-transition);
}

.preset-btn:hover,
.type-btn:hover {
  border-color: var(--tf-primary);
  color: var(--tf-primary);
}

.preset-btn.active,
.type-btn.active {
  background: var(--tf-primary);
  border-color: var(--tf-primary);
  color: white;
}

.chart-actions {
  display: flex;
  gap: var(--tf-spacing-2);
  margin-left: auto;
}

.action-btn {
  padding: var(--tf-spacing-2);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  background: var(--tf-surface);
  color: var(--tf-text-secondary);
  cursor: pointer;
  transition: all var(--tf-transition);
}

.action-btn:hover {
  background: var(--tf-primary);
  color: white;
  border-color: var(--tf-primary);
}

.chart-container {
  position: relative;
  min-height: v-bind(height + 'px');
  padding: var(--tf-spacing-4);
}

.chart-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: v-bind(height + 'px');
  color: var(--tf-text-secondary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--tf-border);
  border-top: 3px solid var(--tf-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--tf-spacing-2);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: v-bind(height + 'px');
  color: var(--tf-text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--tf-spacing-3);
}

.empty-text {
  font-size: var(--tf-text-lg);
  font-weight: 500;
  margin-bottom: var(--tf-spacing-1);
}

.empty-desc {
  font-size: var(--tf-text-sm);
  color: var(--tf-text-tertiary);
}

.chart-canvas {
  width: 100%;
  height: v-bind(height + 'px');
}

.chart-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--tf-spacing-4);
  padding: var(--tf-spacing-4);
  border-top: 1px solid var(--tf-border);
  background: var(--tf-surface-2);
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: var(--tf-text-xs);
  color: var(--tf-text-tertiary);
  margin-bottom: var(--tf-spacing-1);
  text-transform: uppercase;
  font-weight: 500;
}

.stat-value {
  font-size: var(--tf-text-xl);
  font-weight: 600;
  color: var(--tf-text);
}

.stat-value.positive {
  color: var(--tf-success);
}

.stat-value.negative {
  color: var(--tf-error);
}

.stat-value.neutral {
  color: var(--tf-warning);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-controls {
    flex-direction: column;
    align-items: stretch;
    gap: var(--tf-spacing-3);
  }

  .chart-actions {
    margin-left: 0;
    justify-content: center;
  }

  .chart-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .control-group {
    justify-content: space-between;
  }
}
</style>