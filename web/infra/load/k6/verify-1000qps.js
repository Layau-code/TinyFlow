import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const successCounter = new Counter('successful_requests');

export const options = {
  scenarios: {
    target_1000_qps: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 200,
      maxVUs: 500,
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<500'],
    'http_req_failed': ['rate<0.01'],
    'errors': ['rate<0.01'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  console.log('🎯 验证目标: 1000 QPS 稳定运行 2 分钟');
  const codes = [];
  
  // 准备 500 个短链
  for (let i = 0; i < 500; i++) {
    const longUrl = `https://example.com/verify/${i}?ts=${Date.now()}`;
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
      } catch (e) {}
    }
    
    if ((i + 1) % 100 === 0) {
      console.log(`已创建 ${i + 1}/500 个短链`);
    }
  }
  
  console.log(`✅ 准备完成: ${codes.length} 个短链`);
  return { codes };
}

export default function (data) {
  const codes = data.codes;
  if (codes.length === 0) return;
  
  const idx = Math.floor(Math.random() * codes.length);
  const code = codes[idx];
  
  const res = http.get(`${BASE}/api/redirect/${code}`, { 
    redirects: 0,
    timeout: '3s',
  });
  
  const success = check(res, {
    '状态码 302': (r) => r.status === 302,
    'P95 < 500ms': (r) => r.timings.duration < 500,
  });
  
  if (!success) {
    errorRate.add(1);
  } else {
    successCounter.add(1);
  }
}

export function teardown(data) {
  console.log('🏁 验证压测完成');
}
