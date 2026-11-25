import http from 'k6/http';
import { check, fail } from 'k6';

// Basic load test for the authenticated farms-listing endpoint
// (`GET /api/v1/farms`). It logs in once in `setup()` to obtain a bearer token,
// then hammers the first page of the listing with a constant pool of virtual
// users. Seed the data first with `./seed.sh` so the user exists and the
// listing is large enough for the N+1 fix to matter.
//
// Tunable via environment variables (all optional):
//   BASE_URL            API base URL          (default http://localhost:8080)
//   LOAD_TEST_EMAIL     login email           (default loadtest@fincaria.local)
//   LOAD_TEST_PASSWORD  login password        (default loadtest-password)
//   VUS                 concurrent users      (default 20)
//   DURATION            test duration         (default 30s)
//   PAGE / SIZE         pagination parameters (default 0 / 20)

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EMAIL = __ENV.LOAD_TEST_EMAIL || 'loadtest@fincaria.local';
const PASSWORD = __ENV.LOAD_TEST_PASSWORD || 'loadtest-password';
const PAGE = __ENV.PAGE || '0';
const SIZE = __ENV.SIZE || '20';

export const options = {
  scenarios: {
    farms_listing: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 20),
      duration: __ENV.DURATION || '30s',
    },
  },
  thresholds: {
    // Fail the run if more than 1% of requests error or the p95 latency of the
    // listing endpoint exceeds 500ms.
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:GET /api/v1/farms}': ['p(95)<500'],
  },
};

export function setup() {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: EMAIL, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  if (res.status !== 200) {
    fail(`login failed (${res.status}): ${res.body} — did you run ./seed.sh first?`);
  }
  return { token: res.json('token') };
}

export default function (data) {
  const res = http.get(`${BASE_URL}/api/v1/farms?page=${PAGE}&size=${SIZE}`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { name: 'GET /api/v1/farms' },
  });
  check(res, { 'status is 200': (r) => r.status === 200 });
}
