export const overview = {
  shortCode: 'demo123',
  longUrl: 'https://example.com/demo',
  createdAt: Date.now() - 1000 * 60 * 60 * 24 * 30,
  totalVisits: 12345,
  todayVisits: 345,
}

export const detailed = {
  pv: 12345,
  uv: 6789,
  pvUvRatio: 1.82,
  firstClick: Date.now() - 1000 * 60 * 60 * 24 * 25,
  lastClick: Date.now() - 1000 * 60 * 60 * 2,
  hourDistribution: Array.from({ length: 24 }, (_, i) => ({ key: `${i}:00`, count: Math.floor(Math.random() * 500) })),
  weekdayDistribution: ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'].map((d)=>({ key: d, count: Math.floor(Math.random()*2000) })),
  deviceDistribution: [{ key: 'mobile', count: 8000 }, { key: 'desktop', count: 3000 }, { key: 'tablet', count: 345 }],
  browserDistribution: [{ key: 'Chrome', count: 7000 }, { key: 'Safari', count: 3000 }, { key: 'Firefox', count: 1345 }],
  cityDistribution: [
    { key: 'Beijing', count: 3200 },
    { key: 'Shanghai', count: 2600 },
    { key: 'Guangzhou', count: 1800 },
    { key: 'Shenzhen', count: 1500 },
    { key: 'Hangzhou', count: 900 },
  ],
  countryDistribution: [
    { key: 'China', count: 11000 },
    { key: 'USA', count: 800 },
    { key: 'Japan', count: 300 },
  ],
  sourceDistribution: [
    { key: 'google.com', count: 4000 },
    { key: 'bing.com', count: 1200 },
    { key: 'direct', count: 6000 },
  ],
  refererDistribution: [
    { key: 'https://referrer.example/a', count: 1200 },
    { key: 'https://referrer.example/b', count: 900 },
  ],
}

export const trend = Array.from({ length: 30 }, (_, i) => {
  const date = new Date()
  date.setDate(date.getDate() - (29 - i))
  return { date: date.toISOString().slice(0,10), visits: Math.floor(200 + Math.random() * 800) }
})

export const events = Array.from({ length: 80 }, (_, i) => ({ ts: Date.now() - i * 60000 * 15, ip: `192.168.1.${i%255}`, deviceType: ['mobile','desktop','tablet'][i%3], city: ['Beijing','Shanghai','Hangzhou'][i%3], country: ['China','USA'][i%2], sourceHost: i%2? 'google.com' : 'direct' }))

export default { overview, detailed, trend, events }

