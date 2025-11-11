import { createI18n } from 'vue-i18n'

const saved = typeof localStorage !== 'undefined' ? localStorage.getItem('locale') : null
const browser = typeof navigator !== 'undefined' ? navigator.language || 'en' : 'en'
const defaultLocale = saved || (browser.toLowerCase().startsWith('zh') ? 'zh' : 'en')

const messages = {
  zh: {
    nav: { dashboard: '数据统计' },
    hero: {
      title: '让链接变短，让分享更轻松',
      subtitle: '一键生成短链，支持自定义别名、访问统计与 API 调用'
    },
    form: {
      urlPlaceholder: '粘贴长链接，例如 https://example.com',
      aliasPlaceholder: '可自定义别名，留空则自动生成',
      generate: '生成短链'
    },
    result: {
      created: '已生成短链',
      copy: '复制短链',
      downloadQr: '下载二维码',
      copied: '已复制到剪贴板'
    },
    history: {
      title: '我的短链',
      filterPlaceholder: '按短码或域名过滤',
      refresh: '刷新',
      refreshing: '刷新中…',
      empty: '暂无历史记录'
    },
    // 首页历史分页文案
    historyPagination: {
      total: '共 {count} 条，当前第 {page} / {pages} 页',
      prev: '上一页',
      next: '下一页'
    },
    actions: {
      copy: '复制', edit: '编辑', delete: '删除', save: '保存', cancel: '取消',
      newAlias: '新的短码/别名', copied: '已复制'
    },
    footer: { contactUs: '联系我们', suffix: '© 2025 TinyFlow · 开源 · 隐私政策 · 反馈建议 ·' },
    common: { home: '首页', unknown: '未知', loading: '加载中…', noData: '暂无数据' },
    dashboard: {
      title: '我的短链统计', backHome: '返回首页', searchPlaceholder: '搜索短码或域名', refresh: '刷新',
      overview: '全局概览', toggleShowPie: '显示饼图', toggleShowTrend: '近七天趋势',
      todayDist: '今日点击分布', totalDist: '总点击分布', noDataOrNone: '暂无数据或暂无短链',
      descTop5Trend: '展示近7天点击总数前五短链的趋势并按日期对齐',
      table: { shortCode: 'Short Code', url: 'URL', total: 'Total', today: 'Today', share: '占总点击比例', createdAt: 'Created At', actions: '操作' },
      exportCsv: '导出 CSV',
      pagination: { total: '共 {count} 条，当前第 {page} / {pages} 页', prev: '上一页', next: '下一页' },
      other: '其他'
    },
    stats: {
      breadcrumb: '首页 / 统计看板 / {code}', backHome: '返回首页', backDashboard: '返回看板', copyShort: '复制短链',
      referrerShare: '来源渠道占比（饼图）', details: '详细统计数据',
      labels: { shortCode: '短码', shortUrl: '短链', totalVisits: '总访问量', todayVisits: '今日访问量', createdAt: '创建时间', topCities: 'Top 城市', devices: '设备分布' },
      noData: '暂无数据', retry: '重试', errorNotFound: '短链不存在或无权查看', unknown: '未知'
    },
    errors: {
      invalidUrl: '请输入有效的 URL（以 http:// 或 https:// 开头）',
      generateFailed: '生成失败，请稍后重试',
      linkNotFound: '短链不存在',
      redirectFailed: '跳转失败，请稍后再试'
    }
  },
  en: {
    nav: { dashboard: 'Dashboard' },
    hero: {
      title: 'Shorten links, share more easily',
      subtitle: 'Generate short links with alias, analytics, and API'
    },
    form: {
      urlPlaceholder: 'Paste a long URL, e.g., https://example.com',
      aliasPlaceholder: 'Optional alias, leave empty to auto-generate',
      generate: 'Create Short Link'
    },
    result: {
      created: 'Short link created',
      copy: 'Copy Short Link',
      downloadQr: 'Download QR',
      copied: 'Copied to clipboard'
    },
    history: {
      title: 'History',
      filterPlaceholder: 'Filter by code or domain',
      refresh: 'Refresh',
      refreshing: 'Refreshing…',
      empty: 'No history yet'
    },
    historyPagination: {
      total: 'Total {count}, Page {page} / {pages}',
      prev: 'Prev',
      next: 'Next'
    },
    actions: { copy: 'Copy', edit: 'Edit', delete: 'Delete', save: 'Save', cancel: 'Cancel', newAlias: 'New code/alias', copied: 'Copied' },
    footer: { contactUs: 'Contact Us', suffix: '© 2025 TinyFlow · Open Source · Privacy · Feedback ·' },
    common: { home: 'Home', unknown: 'Unknown', loading: 'Loading…', noData: 'No data' },
    dashboard: {
      title: 'My Short Link Stats', backHome: 'Back to Home', searchPlaceholder: 'Search code or domain', refresh: 'Refresh',
      overview: 'Overview', toggleShowPie: 'Show Pie', toggleShowTrend: '7-Day Trend',
      todayDist: "Today's Click Distribution", totalDist: 'Total Click Distribution', noDataOrNone: 'No data or no short links',
      descTop5Trend: 'Shows 7-day trends of top 5 short links aligned by date',
      table: { shortCode: 'Short Code', url: 'URL', total: 'Total', today: 'Today', share: 'Share (%)', createdAt: 'Created At', actions: 'Actions' },
      exportCsv: 'Export CSV',
      pagination: { total: 'Total {count}, Page {page} / {pages}', prev: 'Prev', next: 'Next' },
      other: 'Other'
    },
    stats: {
      breadcrumb: 'Home / Dashboard / {code}', backHome: 'Back to Home', backDashboard: 'Back to Dashboard', copyShort: 'Copy Short URL',
      referrerShare: 'Referrer Share (Pie)', details: 'Details',
      labels: { shortCode: 'Short Code', shortUrl: 'Short URL', totalVisits: 'Total Visits', todayVisits: 'Visits Today', createdAt: 'Created At', topCities: 'Top Cities', devices: 'Devices' },
      noData: 'No data', retry: 'Retry', errorNotFound: 'Link not found or unauthorized', unknown: 'Unknown'
    },
    errors: {
      invalidUrl: 'Please enter a valid URL (starts with http:// or https://)',
      generateFailed: 'Failed to create, please try again later',
      linkNotFound: 'Short link not found',
      redirectFailed: 'Redirect failed, please try again later'
    }
  }
}

export const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: 'en',
  messages
})

export function setLocale(loc) {
  try { localStorage.setItem('locale', loc) } catch {}
  i18n.global.locale.value = loc
}