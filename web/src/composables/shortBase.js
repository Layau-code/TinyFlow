// 统一短链显示基域：优先使用环境变量 VITE_SHORT_BASE，其次回退到后端默认端口
export const SHORT_BASE = (import.meta?.env?.VITE_SHORT_BASE) || 'http://localhost:8080'