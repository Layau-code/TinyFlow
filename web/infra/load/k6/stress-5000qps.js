import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const successCounter = new Counter('successful_requests');

export const options = {
  scenarios: {
    stress_5000: {
      executor: 'ramping-arrival-rate',
      startRate: 1000,
      timeUnit: '1s',
      preAllocatedVUs: 200,
      maxVUs: 1000,
      stages: [
        { target: 1000, duration: '30s' },  // 预热
        { target: 2000, duration: '30s' },  // 爬升到 2000
        { target: 3000, duration: '30s' },  // 爬升到 3000
        { target: 5000, duration: '1m' },   // 冲刺到 5000
        { target: 5000, duration: '2m' },   // 持续 5000 QPS
        { target: 1000, duration: '30s' },  // 降压
      ],
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<100'],
    'http_req_failed': ['rate<0.05'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  console.log('🚀 开始 5000 QPS 压力测试');
  console.log('📋 获取现有短链列表...');
  
  const res = http.get(`${BASE}/api/urls?page=0&size=1000`);
  
  if (res.status !== 200) {
    console.error(`❌ 获取短链列表失败: ${res.status}`);
    return { codes: [] };
  }
  
  const codes = [];
  try {
    const body = res.json();
    if (body && body.data && body.data.content) {
      for (const item of body.data.content) {
        if (item.shortCode) {
          codes.push(item.shortCode);
        }
      }
    }
  } catch (e) {
    console.error(`解析响应失败: ${e}`);
  }
  
  console.log(`✅ 成功获取 ${codes.length} 个现有短链`);
  return { codes };
}

export default function (data) {
  const codes = data.codes;
  if (!codes || codes.length === 0) {
    return;
  }
  
  const idx = Math.floor(Math.random() * codes.length);
  const code = codes[idx];
  
  const res = http.get(`${BASE}/api/redirect/${code}`, { 
    redirects: 0,
    timeout: '5s',
  });
  
  const success = check(res, {
    '状态码 302': (r) => r.status === 302,
    '响应时间 < 100ms': (r) => r.timings.duration < 100,
  });
  
  if (!success) {
    errorRate.add(1);
  } else {
    successCounter.add(1);
  }
}

export function teardown(data) {
  console.log('🏁 5000 QPS 压测完成');
  console.log(`使用了 ${data.codes.length} 个短链`);
}
