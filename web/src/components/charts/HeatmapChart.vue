<template>
  <div class="heatmap-chart">
    <div class="heatmap-header">
      <div class="header-title">24小时访问热力图</div>
      <div class="header-legend">
        <span class="legend-label">访问量:</span>
        <div class="legend-gradient">
          <span class="legend-text">低</span>
          <div class="gradient-bar"></div>
          <span class="legend-text">高</span>
        </div>
      </div>
    </div>
    <div class="heatmap-body">
      <div v-if="loading" class="heatmap-loading">
        <div class="loading-spinner"></div>
      </div>
      <div v-else-if="hours.length === 0" class="heatmap-empty">
        暂无时段数据
      </div>
      <div v-else class="heatmap-grid">
        <div v-for="hour in hours" :key="hour.hour" class="hour-cell" :style="getCellStyle(hour.value)">
          <div class="hour-time">{{ formatHour(hour.hour) }}</div>
          <div class="hour-value">{{ hour.value }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 从事件数据中提取小时分布
const hours = computed(() => {
  const hourMap = new Map()
  
  // 初始化24小时
  for (let i = 0; i < 24; i++) {
    hourMap.set(i, 0)
  }
  
  // 统计每个小时的访问量
  props.data.forEach(event => {
    if (event.ts) {
      try {
        const date = new Date(event.ts)
        const hour = date.getHours()
        hourMap.set(hour, (hourMap.get(hour) || 0) + 1)
      } catch (e) {
        // 忽略无效日期
      }
    }
  })
  
  return Array.from(hourMap.entries())
    .map(([hour, value]) => ({ hour, value }))
    .sort((a, b) => a.hour - b.hour)
})

const maxValue = computed(() => {
  return Math.max(...hours.value.map(h => h.value), 1)
})

function formatHour(hour) {
  return `${String(hour).padStart(2, '0')}:00`
}

function getCellStyle(value) {
  const intensity = value / maxValue.value
  const opacity = 0.2 + intensity * 0.8
  
  // 根据强度选择不同的颜色
  let color
  if (intensity < 0.25) {
    color = `rgba(59, 130, 246, ${opacity})` // 浅蓝
  } else if (intensity < 0.5) {
    color = `rgba(37, 99, 235, ${opacity})` // 中蓝
  } else if (intensity < 0.75) {
    color = `rgba(147, 51, 234, ${opacity})` // 紫色
  } else {
    color = `rgba(239, 68, 68, ${opacity})` // 红色
  }
  
  return {
    background: color,
    borderColor: intensity > 0.5 ? 'rgba(37, 99, 235, 0.4)' : 'rgba(226, 232, 240, 0.6)'
  }
}
</script>

<style scoped>
.heatmap-chart {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.heatmap-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.header-legend {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.legend-gradient {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-text {
  font-size: 11px;
  color: #94a3b8;
}

.gradient-bar {
  width: 80px;
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, 
    rgba(59, 130, 246, 0.3) 0%, 
    rgba(37, 99, 235, 0.6) 25%, 
    rgba(147, 51, 234, 0.8) 75%, 
    rgba(239, 68, 68, 1) 100%
  );
}

.heatmap-body {
  min-height: 200px;
}

.heatmap-loading,
.heatmap-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #64748b;
  font-size: 14px;
  gap: 12px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #e2e8f0;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
}

.hour-cell {
  aspect-ratio: 1;
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.hour-cell:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
  z-index: 1;
}

.hour-time {
  font-size: 11px;
  font-weight: 600;
  color: #1e293b;
}

.hour-value {
  font-size: 16px;
  font-weight: 700;
  color: #2563eb;
}

@media (max-width: 768px) {
  .heatmap-grid {
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    gap: 6px;
  }
  
  .hour-time {
    font-size: 10px;
  }
  
  .hour-value {
    font-size: 14px;
  }
}
</style>
