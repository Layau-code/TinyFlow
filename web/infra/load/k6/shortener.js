import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    redirect_rps: {
      executor: 'constant-arrival-rate',
      rate: 200, // requests per second
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
    shorten_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 100,
      stages: [
        { target: 50, duration: '30s' },
        { target: 100, duration: '30s' },
        { target: 10, duration: '30s' },
      ],
    },
  },
  thresholds: {
    'http_req_duration{scenario:redirect_rps}': ['p(95)<300'],
    'http_req_failed{scenario:redirect_rps}': ['rate<0.01'],
    'http_req_duration{scenario:shorten_spike}': ['p(95)<800'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const codes = [];
  for (let i = 0; i < 100; i++) {
    const longUrl = `https://example.com/item/${i}?t=${Date.now()}`;
    const payload = JSON.stringify({ longUrl });
    const res = http.post(`${BASE}/api/shorten`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });
    try {
      const body = res.json();
      if (body && body.data && body.data.shortCode) {
        codes.push(body.data.shortCode);
      }
    } catch (e) {
      // ignore parse errors in setup
    }
  }
  return { codes };
}

export default function (data) {
  const codes = data.codes;
  const i = (__ITER + __VU) % codes.length;
  const code = codes[i];
  const res = http.get(`${BASE}/api/redirect/${code}`, { redirects: 0 });
  check(res, {
    'redirect status 302': (r) => r.status === 302,
  });
  sleep(0.2);
}