<template>
  <div class="event-stream">
    <div class="stream-header">
      <div class="stream-title">实时事件流</div>
      <div class="stream-badge">{{ events.length }} 条记录</div>
    </div>
    <div class="stream-container">
      <div v-if="loading" class="stream-loading">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else-if="events.length === 0" class="stream-empty">
        暂无访问记录
      </div>
      <div v-else class="stream-list">
        <div v-for="(event, idx) in events" :key="idx" class="event-item" :style="{ animationDelay: `${idx * 0.05}s` }">
          <div class="event-time">
            <svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke-width="2"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6l4 2"/>
            </svg>
            <span>{{ formatTime(event.ts) }}</span>
          </div>
          <div class="event-details">
            <div class="detail-row">
              <span class="detail-label">IP</span>
              <span class="detail-value">{{ event.ip || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">设备</span>
              <span class="detail-value device-tag" :class="getDeviceClass(event.deviceType)">
                {{ event.deviceType || '未知' }}
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">地区</span>
              <span class="detail-value">{{ formatLocation(event.city, event.country) }}</span>
            </div>
            <div v-if="event.referer" class="detail-row">
              <span class="detail-label">来源</span>
              <span class="detail-value referer-link">{{ event.referer }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue'

const props = defineProps({
  events: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

function formatTime(ts) {
  if (!ts) return '-'
  try {
    const date = new Date(ts)
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
    
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return String(ts)
  }
}

function formatLocation(city, country) {
  if (city && country) return `${country} · ${city}`
  if (city) return city
  if (country) return country
  return '未知'
}

function getDeviceClass(deviceType) {
  const type = String(deviceType || '').toLowerCase()
  if (type.includes('mobile') || type.includes('phone')) return 'device-mobile'
  if (type.includes('desktop') || type.includes('pc')) return 'device-desktop'
  if (type.includes('tablet') || type.includes('pad')) return 'device-tablet'
  return 'device-unknown'
}
</script>

<style scoped>
.event-stream {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stream-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stream-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stream-badge {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.stream-container {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.stream-container::-webkit-scrollbar {
  width: 6px;
}

.stream-container::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.stream-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.stream-container::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.stream-loading,
.stream-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
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

.stream-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-item {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  animation: slideIn 0.3s ease-out backwards;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.event-item:hover {
  border-color: #2563eb;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
  transform: translateX(4px);
}

.event-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #2563eb;
  font-weight: 500;
}

.event-time svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.event-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.detail-label {
  min-width: 50px;
  color: #64748b;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  color: #334155;
  flex: 1;
  word-break: break-all;
}

.device-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.device-mobile {
  background: #dbeafe;
  color: #1e40af;
}

.device-desktop {
  background: #f3e8ff;
  color: #6b21a8;
}

.device-tablet {
  background: #fef3c7;
  color: #92400e;
}

.device-unknown {
  background: #f1f5f9;
  color: #64748b;
}

.referer-link {
  font-size: 12px;
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px dashed #2563eb;
}
</style>
