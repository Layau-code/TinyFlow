import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const successCounter = new Counter('successful_requests');

export const options = {
  scenarios: {
    verify_3000: {
      executor: 'constant-arrival-rate',
      rate: 3000,
      timeUnit: '1s',
      duration: '5m',
      preAllocatedVUs: 100,
      maxVUs: 500,
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<50', 'p(99)<100'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  console.log('🚀 开始 3000 QPS 稳定性验证测试（5分钟）');
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
  console.log('⏱️  持续压测 5 分钟，观察系统稳定性...');
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
    '响应时间 < 50ms': (r) => r.timings.duration < 50,
    '响应时间 < 100ms': (r) => r.timings.duration < 100,
  });
  
  if (!success) {
    errorRate.add(1);
  } else {
    successCounter.add(1);
  }
}

export function teardown(data) {
  console.log('🏁 3000 QPS 稳定性测试完成');
  console.log(`使用了 ${data.codes.length} 个短链`);
}
