<template>
  <div class="browser-os-chart">
    <div class="chart-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="activeTab = tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
      >
        {{ tab.label }}
      </button>
    </div>
    <div class="chart-content">
      <div v-if="activeTab === 'browser'" class="chart-wrapper">
        <div v-if="browserData.length === 0" class="empty-state">
          暂无浏览器数据
        </div>
        <div v-else class="data-list">
          <div v-for="(item, idx) in browserData" :key="idx" class="data-item">
            <div class="item-header">
              <span class="item-icon" :style="{ background: getBrowserColor(item.label) }">
                {{ getBrowserIcon(item.label) }}
              </span>
              <span class="item-name">{{ item.label }}</span>
              <span class="item-value">{{ item.value }}</span>
              <span class="item-percent">{{ getPercent(item.value, browserTotal) }}%</span>
            </div>
            <div class="item-bar">
              <div 
                class="item-bar-fill" 
                :style="{ width: getPercent(item.value, browserTotal) + '%', background: getBrowserColor(item.label) }"
              ></div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="chart-wrapper">
        <div v-if="osData.length === 0" class="empty-state">
          暂无操作系统数据
        </div>
        <div v-else class="data-list">
          <div v-for="(item, idx) in osData" :key="idx" class="data-item">
            <div class="item-header">
              <span class="item-icon" :style="{ background: getOSColor(item.label) }">
                {{ getOSIcon(item.label) }}
              </span>
              <span class="item-name">{{ item.label }}</span>
              <span class="item-value">{{ item.value }}</span>
              <span class="item-percent">{{ getPercent(item.value, osTotal) }}%</span>
            </div>
            <div class="item-bar">
              <div 
                class="item-bar-fill" 
                :style="{ width: getPercent(item.value, osTotal) + '%', background: getOSColor(item.label) }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const activeTab = ref('browser')

const tabs = [
  { key: 'browser', label: '浏览器分布' },
  { key: 'os', label: '操作系统分布' }
]

// 从 UA 解析浏览器和操作系统
const browserData = computed(() => {
  const map = new Map()
  props.data.forEach(event => {
    const browser = parseBrowser(event.ua || event.deviceType || '')
    map.set(browser, (map.get(browser) || 0) + 1)
  })
  return Array.from(map.entries())
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)
})

const osData = computed(() => {
  const map = new Map()
  props.data.forEach(event => {
    const os = parseOS(event.ua || event.deviceType || '')
    map.set(os, (map.get(os) || 0) + 1)
  })
  return Array.from(map.entries())
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)
})

const browserTotal = computed(() => browserData.value.reduce((sum, item) => sum + item.value, 0))
const osTotal = computed(() => osData.value.reduce((sum, item) => sum + item.value, 0))

function parseBrowser(ua) {
  const str = String(ua).toLowerCase()
  if (str.includes('chrome') && !str.includes('edg')) return 'Chrome'
  if (str.includes('safari') && !str.includes('chrome')) return 'Safari'
  if (str.includes('firefox')) return 'Firefox'
  if (str.includes('edg')) return 'Edge'
  if (str.includes('opera') || str.includes('opr')) return 'Opera'
  if (str.includes('msie') || str.includes('trident')) return 'IE'
  return '其他'
}

function parseOS(ua) {
  const str = String(ua).toLowerCase()
  if (str.includes('windows') || str.includes('win')) return 'Windows'
  if (str.includes('mac') || str.includes('osx')) return 'macOS'
  if (str.includes('linux')) return 'Linux'
  if (str.includes('android')) return 'Android'
  if (str.includes('ios') || str.includes('iphone') || str.includes('ipad')) return 'iOS'
  return '其他'
}

function getBrowserIcon(name) {
  const icons = {
    'Chrome': '🌐',
    'Safari': '🧭',
    'Firefox': '🦊',
    'Edge': '🌊',
    'Opera': '🎭',
    'IE': '📘'
  }
  return icons[name] || '🌍'
}

function getOSIcon(name) {
  const icons = {
    'Windows': '🪟',
    'macOS': '🍎',
    'Linux': '🐧',
    'Android': '🤖',
    'iOS': '📱'
  }
  return icons[name] || '💻'
}

function getBrowserColor(name) {
  const colors = {
    'Chrome': 'linear-gradient(135deg, #4285F4, #34A853)',
    'Safari': 'linear-gradient(135deg, #007AFF, #00C3FF)',
    'Firefox': 'linear-gradient(135deg, #FF6611, #FF9500)',
    'Edge': 'linear-gradient(135deg, #0078D7, #00BCF2)',
    'Opera': 'linear-gradient(135deg, #FF1B2D, #FA4B00)',
    'IE': 'linear-gradient(135deg, #1E3A8A, #3B82F6)'
  }
  return colors[name] || 'linear-gradient(135deg, #94A3B8, #CBD5E1)'
}

function getOSColor(name) {
  const colors = {
    'Windows': 'linear-gradient(135deg, #00A4EF, #0078D4)',
    'macOS': 'linear-gradient(135deg, #000000, #333333)',
    'Linux': 'linear-gradient(135deg, #FCC624, #F57C00)',
    'Android': 'linear-gradient(135deg, #3DDC84, #07C160)',
    'iOS': 'linear-gradient(135deg, #007AFF, #5AC8FA)'
  }
  return colors[name] || 'linear-gradient(135deg, #94A3B8, #CBD5E1)'
}

function getPercent(value, total) {
  if (!total) return 0
  return ((value / total) * 100).toFixed(1)
}
</script>

<style scoped>
.browser-os-chart {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.chart-tabs {
  display: flex;
  border-bottom: 2px solid #f1f5f9;
  background: #fafbfc;
}

.tab-btn {
  flex: 1;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.tab-btn:hover {
  color: #2563eb;
  background: #f8fafc;
}

.tab-btn.active {
  color: #2563eb;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
}

.chart-content {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
  font-size: 14px;
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.data-item {
  transition: all 0.3s ease;
}

.data-item:hover {
  transform: translateX(4px);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.item-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 16px;
  flex-shrink: 0;
}

.item-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.item-value {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
  min-width: 40px;
  text-align: right;
}

.item-percent {
  font-size: 13px;
  color: #64748b;
  min-width: 50px;
  text-align: right;
}

.item-bar {
  width: 100%;
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.item-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}
</style>
