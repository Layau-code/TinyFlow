// 轻量的按需 echarts 加载器
export async function loadECharts() {
  // 这里使用动态 import，Vite 会把 echarts 拆分成单独 chunk
  const echarts = await import('echarts')
  return echarts
}

