import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const successCounter = new Counter('successful_requests');

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 100,
      maxVUs: 300,
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<500'],
    'http_req_failed': ['rate<0.05'],
    'errors': ['rate<0.05'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  console.log('📋 获取现有短链列表...');
  
  // 从API获取现有的短链列表
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
    console.error('❌ 没有可用的短链！');
    return;
  }
  
  // 随机选择一个短链
  const idx = Math.floor(Math.random() * codes.length);
  const code = codes[idx];
  
  const res = http.get(`${BASE}/api/redirect/${code}`, { 
    redirects: 0,
    timeout: '3s',
  });
  
  const success = check(res, {
    '状态码 302': (r) => r.status === 302,
    '响应时间 < 500ms': (r) => r.timings.duration < 500,
  });
  
  if (!success) {
    errorRate.add(1);
  } else {
    successCounter.add(1);
  }
}

export function teardown(data) {
  console.log('🏁 压测完成');
  console.log(`使用了 ${data.codes.length} 个短链`);
}
