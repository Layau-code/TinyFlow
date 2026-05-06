<template>
  <div class="data-filter">
    <!-- 筛选条件展示 -->
    <div class="filter-display" v-if="hasActiveFilters">
      <div class="active-filters">
        <span class="filter-label">筛选条件：</span>
        <div class="filter-tags">
          <span
            v-for="filter in activeFilters"
            :key="filter.key"
            class="filter-tag"
          >
            {{ filter.label }}: {{ filter.value }}
            <button
              class="filter-remove"
              @click="removeFilter(filter.key)"
            >
              ×
            </button>
          </span>
        </div>
        <button class="clear-all" @click="clearAllFilters">清除所有</button>
      </div>
    </div>

    <!-- 筛选表单 -->
    <div class="filter-form">
      <!-- 时间范围 -->
      <div class="filter-group">
        <label class="filter-label">时间范围</label>
        <div class="date-range">
          <input
            type="date"
            v-model="filters.startDate"
            class="date-input"
            @change="applyFilters"
          >
          <span class="date-separator">至</span>
          <input
            type="date"
            v-model="filters.endDate"
            class="date-input"
            @change="applyFilters"
          >
        </div>
      </div>

      <!-- 快速选择 -->
      <div class="filter-group">
        <label class="filter-label">快速选择</label>
        <div class="quick-select">
          <button
            v-for="preset in timePresets"
            :key="preset.value"
            class="preset-btn"
            :class="{ active: activePreset === preset.value }"
            @click="applyTimePreset(preset.value)"
          >
            {{ preset.label }}
          </button>
        </div>
      </div>

      <!-- 设备类型 -->
      <div class="filter-group">
        <label class="filter-label">设备类型</label>
        <div class="device-select">
          <label
            v-for="device in deviceTypes"
            :key="device.value"
            class="device-option"
          >
            <input
              type="checkbox"
              :value="device.value"
              v-model="filters.devices"
              @change="applyFilters"
              class="checkbox-input"
            >
            <span class="device-label">{{ device.label }}</span>
          </label>
        </div>
      </div>

      <!-- 操作系统 -->
      <div class="filter-group">
        <label class="filter-label">操作系统</label>
        <select
          v-model="filters.os"
          @change="applyFilters"
          class="select-input"
        >
          <option value="">全部系统</option>
          <option
            v-for="osItem in osOptions"
            :key="osItem.value"
            :value="osItem.value"
          >
            {{ osItem.label }}
          </option>
        </select>
      </div>

      <!-- 浏览器 -->
      <div class="filter-group">
        <label class="filter-label">浏览器</label>
        <select
          v-model="filters.browser"
          @change="applyFilters"
          class="select-input"
        >
          <option value="">全部浏览器</option>
          <option
            v-for="browserItem in browserOptions"
            :key="browserItem.value"
            :value="browserItem.value"
          >
            {{ browserItem.label }}
          </option>
        </select>
      </div>

      <!-- 地区 -->
      <div class="filter-group">
        <label class="filter-label">地区</label>
        <select
          v-model="filters.region"
          @change="applyFilters"
          class="select-input"
        >
          <option value="">全部地区</option>
          <option
            v-for="regionItem in regionOptions"
            :key="regionItem.value"
            :value="regionItem.value"
          >
            {{ regionItem.label }}
          </option>
        </select>
      </div>

      <!-- 搜索关键词 -->
      <div class="filter-group">
        <label class="filter-label">关键词搜索</label>
        <div class="search-input-container">
          <input
            type="text"
            v-model="filters.keyword"
            placeholder="输入关键词..."
            class="search-input"
            @input="debounceApplyFilters"
          >
          <button
            class="search-clear"
            v-if="filters.keyword"
            @click="clearKeyword"
          >
            ×
          </button>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="filter-actions">
        <button class="btn btn-secondary" @click="resetFilters">重置</button>
        <button class="btn btn-primary" @click="applyFilters">应用筛选</button>
        <button class="btn btn-ghost" @click="toggleAdvanced" v-if="!showAdvanced">
          高级筛选
        </button>
        <button class="btn btn-ghost" @click="toggleAdvanced" v-else>
          收起高级
        </button>
      </div>

      <!-- 高级筛选 -->
      <div class="advanced-filters" v-if="showAdvanced">
        <div class="filter-group">
          <label class="filter-label">最小访问量</label>
          <input
            type="number"
            v-model.number="filters.minVisits"
            min="0"
            class="number-input"
            @change="applyFilters"
          >
        </div>

        <div class="filter-group">
          <label class="filter-label">最大访问量</label>
          <input
            type="number"
            v-model.number="filters.maxVisits"
            min="0"
            class="number-input"
            @change="applyFilters"
          >
        </div>

        <div class="filter-group">
          <label class="filter-label">来源域名</label>
          <input
            type="text"
            v-model="filters.sourceDomain"
            placeholder="输入域名..."
            class="text-input"
            @input="debounceApplyFilters"
          >
        </div>

        <div class="filter-group">
          <label class="filter-label">排序方式</label>
          <select
            v-model="filters.sortBy"
            @change="applyFilters"
            class="select-input"
          >
            <option value="visits_desc">访问量降序</option>
            <option value="visits_asc">访问量升序</option>
            <option value="date_desc">最新创建</option>
            <option value="date_asc">最早创建</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const emit = defineEmits(['filterChange'])

// 筛选状态
const filters = ref({
  startDate: '',
  endDate: '',
  devices: [],
  os: '',
  browser: '',
  region: '',
  keyword: '',
  minVisits: null,
  maxVisits: null,
  sourceDomain: '',
  sortBy: 'visits_desc'
})

const showAdvanced = ref(false)
const activePreset = ref('7d')

// 配置选项
const timePresets = [
  { label: '今天', value: 'today' },
  { label: '昨天', value: 'yesterday' },
  { label: '最近7天', value: '7d' },
  { label: '最近30天', value: '30d' },
  { label: '本月', value: 'month' },
  { label: '上月', value: 'lastMonth' }
]

const deviceTypes = [
  { label: '桌面设备', value: 'desktop' },
  { label: '移动设备', value: 'mobile' },
  { label: '平板设备', value: 'tablet' }
]

const osOptions = [
  { label: 'Windows', value: 'windows' },
  { label: 'macOS', value: 'macos' },
  { label: 'Linux', value: 'linux' },
  { label: 'Android', value: 'android' },
  { label: 'iOS', value: 'ios' }
]

const browserOptions = [
  { label: 'Chrome', value: 'chrome' },
  { label: 'Firefox', value: 'firefox' },
  { label: 'Safari', value: 'safari' },
  { label: 'Edge', value: 'edge' },
  { label: 'Opera', value: 'opera' }
]

const regionOptions = [
  { label: '中国', value: 'cn' },
  { label: '美国', value: 'us' },
  { label: '欧洲', value: 'eu' },
  { label: '日本', value: 'jp' },
  { label: '其他', value: 'other' }
]

// 计算属性
const activeFilters = computed(() => {
  const active = []

  if (filters.value.startDate && filters.value.endDate) {
    active.push({
      key: 'date',
      label: '时间',
      value: `${filters.value.startDate} 至 ${filters.value.endDate}`
    })
  }

  if (filters.value.devices.length > 0) {
    active.push({
      key: 'devices',
      label: '设备',
      value: filters.value.devices.map(d =>
        deviceTypes.find(dt => dt.value === d)?.label || d
      ).join(', ')
    })
  }

  if (filters.value.os) {
    active.push({
      key: 'os',
      label: '系统',
      value: osOptions.find(o => o.value === filters.value.os)?.label || filters.value.os
    })
  }

  if (filters.value.browser) {
    active.push({
      key: 'browser',
      label: '浏览器',
      value: browserOptions.find(b => b.value === filters.value.browser)?.label || filters.value.browser
    })
  }

  if (filters.value.region) {
    active.push({
      key: 'region',
      label: '地区',
      value: regionOptions.find(r => r.value === filters.value.region)?.label || filters.value.region
    })
  }

  if (filters.value.keyword) {
    active.push({
      key: 'keyword',
      label: '关键词',
      value: filters.value.keyword
    })
  }

  if (filters.value.minVisits !== null) {
    active.push({
      key: 'minVisits',
      label: '最小访问',
      value: filters.value.minVisits
    })
  }

  if (filters.value.maxVisits !== null) {
    active.push({
      key: 'maxVisits',
      label: '最大访问',
      value: filters.value.maxVisits
    })
  }

  if (filters.value.sourceDomain) {
    active.push({
      key: 'sourceDomain',
      label: '来源域名',
      value: filters.value.sourceDomain
    })
  }

  return active
})

const hasActiveFilters = computed(() => activeFilters.value.length > 0)

// 方法
function applyTimePreset(preset) {
  const today = new Date()
  activePreset.value = preset

  switch (preset) {
    case 'today':
      filters.value.startDate = today.toISOString().split('T')[0]
      filters.value.endDate = today.toISOString().split('T')[0]
      break
    case 'yesterday':
      const yesterday = new Date(today)
      yesterday.setDate(today.getDate() - 1)
      filters.value.startDate = yesterday.toISOString().split('T')[0]
      filters.value.endDate = yesterday.toISOString().split('T')[0]
      break
    case '7d':
      const weekAgo = new Date(today)
      weekAgo.setDate(today.getDate() - 7)
      filters.value.startDate = weekAgo.toISOString().split('T')[0]
      filters.value.endDate = today.toISOString().split('T')[0]
      break
    case '30d':
      const monthAgo = new Date(today)
      monthAgo.setDate(today.getDate() - 30)
      filters.value.startDate = monthAgo.toISOString().split('T')[0]
      filters.value.endDate = today.toISOString().split('T')[0]
      break
    case 'month':
      const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
      filters.value.startDate = firstDay.toISOString().split('T')[0]
      filters.value.endDate = today.toISOString().split('T')[0]
      break
    case 'lastMonth':
      const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1)
      const lastMonthLastDay = new Date(today.getFullYear(), today.getMonth(), 0)
      filters.value.startDate = lastMonth.toISOString().split('T')[0]
      filters.value.endDate = lastMonthLastDay.toISOString().split('T')[0]
      break
  }

  applyFilters()
}

let debounceTimer = null
function debounceApplyFilters() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(applyFilters, 300)
}

function applyFilters() {
  emit('filterChange', { ...filters.value })
}

function removeFilter(key) {
  if (key === 'date') {
    filters.value.startDate = ''
    filters.value.endDate = ''
  } else if (Array.isArray(filters.value[key])) {
    filters.value[key] = []
  } else {
    filters.value[key] = ''
  }
  applyFilters()
}

function clearAllFilters() {
  Object.keys(filters.value).forEach(key => {
    if (Array.isArray(filters.value[key])) {
      filters.value[key] = []
    } else {
      filters.value[key] = ''
    }
  })
  activePreset.value = '7d'
  applyFilters()
}

function resetFilters() {
  clearAllFilters()
  // 重置为默认时间范围（最近7天）
  applyTimePreset('7d')
}

function clearKeyword() {
  filters.value.keyword = ''
  applyFilters()
}

function toggleAdvanced() {
  showAdvanced.value = !showAdvanced.value
}

// 生命周期
onMounted(() => {
  // 默认应用最近7天的筛选
  applyTimePreset('7d')
})
</script>

<style scoped>
.data-filter {
  background: var(--tf-surface);
  border-radius: var(--tf-radius-lg);
  border: 1px solid var(--tf-border);
  overflow: hidden;
}

.filter-display {
  padding: var(--tf-spacing-4);
  border-bottom: 1px solid var(--tf-border);
  background: var(--tf-surface-2);
}

.active-filters {
  display: flex;
  align-items: center;
  gap: var(--tf-spacing-3);
  flex-wrap: wrap;
}

.filter-label {
  font-size: var(--tf-text-sm);
  color: var(--tf-text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.filter-tags {
  display: flex;
  gap: var(--tf-spacing-2);
  flex-wrap: wrap;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  padding: var(--tf-spacing-1) var(--tf-spacing-2);
  background: var(--tf-primary-light);
  color: var(--tf-primary);
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  border: 1px solid var(--tf-primary);
}

.filter-remove {
  margin-left: var(--tf-spacing-1);
  background: none;
  border: none;
  color: var(--tf-primary);
  cursor: pointer;
  font-size: var(--tf-text-lg);
  line-height: 1;
  padding: 0;
}

.clear-all {
  margin-left: auto;
  background: none;
  border: none;
  color: var(--tf-primary);
  font-size: var(--tf-text-sm);
  cursor: pointer;
  text-decoration: underline;
}

.filter-form {
  padding: var(--tf-spacing-4);
}

.filter-group {
  margin-bottom: var(--tf-spacing-4);
}

.filter-group:last-child {
  margin-bottom: 0;
}

.date-range {
  display: flex;
  align-items: center;
  gap: var(--tf-spacing-2);
}

.date-input {
  flex: 1;
  padding: var(--tf-spacing-2);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  background: var(--tf-surface);
  color: var(--tf-text);
}

.date-separator {
  color: var(--tf-text-secondary);
  font-size: var(--tf-text-sm);
}

.quick-select {
  display: flex;
  gap: var(--tf-spacing-2);
  flex-wrap: wrap;
}

.preset-btn {
  padding: var(--tf-spacing-2) var(--tf-spacing-3);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  background: var(--tf-surface);
  color: var(--tf-text-secondary);
  font-size: var(--tf-text-sm);
  cursor: pointer;
  transition: all var(--tf-transition);
}

.preset-btn:hover {
  border-color: var(--tf-primary);
  color: var(--tf-primary);
}

.preset-btn.active {
  background: var(--tf-primary);
  border-color: var(--tf-primary);
  color: white;
}

.device-select {
  display: flex;
  gap: var(--tf-spacing-4);
}

.device-option {
  display: flex;
  align-items: center;
  gap: var(--tf-spacing-2);
}

.checkbox-input {
  margin: 0;
}

.device-label {
  font-size: var(--tf-text-sm);
  color: var(--tf-text);
}

.select-input {
  width: 100%;
  padding: var(--tf-spacing-2);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  background: var(--tf-surface);
  color: var(--tf-text);
}

.search-input-container {
  position: relative;
}

.search-input {
  width: 100%;
  padding: var(--tf-spacing-2) var(--tf-spacing-8) var(--tf-spacing-2) var(--tf-spacing-2);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  background: var(--tf-surface);
  color: var(--tf-text);
}

.search-clear {
  position: absolute;
  right: var(--tf-spacing-2);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--tf-text-secondary);
  cursor: pointer;
  font-size: var(--tf-text-lg);
  line-height: 1;
  padding: 0;
}

.number-input,
.text-input {
  width: 100%;
  padding: var(--tf-spacing-2);
  border: 1px solid var(--tf-border);
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  background: var(--tf-surface);
  color: var(--tf-text);
}

.filter-actions {
  display: flex;
  gap: var(--tf-spacing-3);
  margin-top: var(--tf-spacing-4);
  flex-wrap: wrap;
}

.btn {
  padding: var(--tf-spacing-2) var(--tf-spacing-4);
  border: 1px solid;
  border-radius: var(--tf-radius);
  font-size: var(--tf-text-sm);
  cursor: pointer;
  transition: all var(--tf-transition);
}

.btn-primary {
  background: var(--tf-primary);
  border-color: var(--tf-primary);
  color: white;
}

.btn-primary:hover {
  background: var(--tf-primary-dark);
  border-color: var(--tf-primary-dark);
}

.btn-secondary {
  background: var(--tf-surface);
  border-color: var(--tf-border);
  color: var(--tf-text);
}

.btn-secondary:hover {
  background: var(--tf-surface-2);
  border-color: var(--tf-border-hover);
}

.btn-ghost {
  background: transparent;
  border-color: transparent;
  color: var(--tf-primary);
}

.btn-ghost:hover {
  background: var(--tf-primary-light);
}

.advanced-filters {
  margin-top: var(--tf-spacing-4);
  padding-top: var(--tf-spacing-4);
  border-top: 1px dashed var(--tf-border);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--tf-spacing-4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-display,
  .filter-form {
    padding: var(--tf-spacing-3);
  }

  .active-filters {
    flex-direction: column;
    align-items: stretch;
    gap: var(--tf-spacing-2);
  }

  .clear-all {
    margin-left: 0;
    align-self: flex-start;
  }

  .device-select {
    flex-direction: column;
    gap: var(--tf-spacing-2);
  }

  .filter-actions {
    flex-direction: column;
  }

  .advanced-filters {
    grid-template-columns: 1fr;
  }
}
</style>