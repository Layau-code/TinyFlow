import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 自定义指标
const errorRate = new Rate('errors');
const trend95 = new Trend('http_req_duration_p95');
const successCounter = new Counter('successful_requests');

export const options = {
  scenarios: {
    max_qps_ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 500,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 1000,
      stages: [
        // 阶段1: 预热 500 QPS (30秒)
        { target: 500, duration: '30s' },
        // 阶段2: 缓慢增压 500 -> 1000 (30秒)
        { target: 1000, duration: '30s' },
        // 阶段3: 持续 1000 QPS (60秒)
        { target: 1000, duration: '60s' },
        // 阶段4: 快速增压 1000 -> 2000 (30秒)
        { target: 2000, duration: '30s' },
        // 阶段5: 持续 2000 QPS (60秒)
        { target: 2000, duration: '60s' },
        // 阶段6: 极限增压 2000 -> 3000 (30秒)
        { target: 3000, duration: '30s' },
        // 阶段7: 持续 3000 QPS (60秒)
        { target: 3000, duration: '60s' },
        // 阶段8: 继续增压 3000 -> 4000 (30秒)
        { target: 4000, duration: '30s' },
        // 阶段9: 持续 4000 QPS (60秒)
        { target: 4000, duration: '60s' },
        // 阶段10: 冷却降压 (30秒)
        { target: 500, duration: '30s' },
      ],
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<1000'], // P95 < 1s
    'http_req_failed': ['rate<0.05'],    // 错误率 < 5%
    'errors': ['rate<0.05'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  console.log('🚀 开始准备压测数据...');
  const codes = [];
  
  // 准备 1000 个短链用于压测
  for (let i = 0; i < 1000; i++) {
    const longUrl = `https://example.com/perf-test/${i}?timestamp=${Date.now()}`;
    const payload = JSON.stringify({ longUrl });
    const res = http.post(`${BASE}/api/shorten`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (res.status === 200) {
      try {
        const body = res.json();
        if (body && body.data && body.data.shortCode) {
          codes.push(body.data.shortCode);
        }
      } catch (e) {
        console.error(`解析响应失败: ${e}`);
      }
    }
    
    if ((i + 1) % 100 === 0) {
      console.log(`已创建 ${i + 1}/1000 个短链...`);
    }
  }
  
  console.log(`✅ 压测数据准备完成，共 ${codes.length} 个短链`);
  return { codes };
}

export default function (data) {
  const codes = data.codes;
  if (codes.length === 0) {
    console.error('❌ 没有可用的短链数据！');
    return;
  }
  
  // 随机选择一个短链
  const idx = Math.floor(Math.random() * codes.length);
  const code = codes[idx];
  
  const res = http.get(`${BASE}/api/redirect/${code}`, { 
    redirects: 0,
    timeout: '5s',
  });
  
  const success = check(res, {
    '状态码 302': (r) => r.status === 302,
    '响应时间 < 1s': (r) => r.timings.duration < 1000,
  });
  
  if (!success) {
    errorRate.add(1);
  } else {
    successCounter.add(1);
  }
  
  trend95.add(res.timings.duration);
}

export function teardown(data) {
  console.log('🏁 压测完成');
  console.log(`共使用 ${data.codes.length} 个短链进行测试`);
}
