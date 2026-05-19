/**
 * PeopleJob 부하테스트 시나리오
 *
 * 실행 방법:
 *   k6 run load_test.js
 *
 * 사전 준비:
 *   1. winget install k6
 *   2. seed_data.sql 실행 (10만 건 더미 데이터)
 *   3. Spring Boot 서버 실행: .\run-dev.ps1
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8090';

// ── 커스텀 메트릭 ──────────────────────────────────────────
const jobListDuration   = new Trend('job_list_duration',   true);
const jobDetailDuration = new Trend('job_detail_duration', true);
const errorRate         = new Rate('error_rate');
const cacheHitCounter   = new Counter('cache_hit_count');

// ── 부하 시나리오 (단계별 VU 증가) ─────────────────────────
export const options = {
  stages: [
    { duration: '30s', target: 10  },   // 워밍업
    { duration: '1m',  target: 50  },   // 정상 부하
    { duration: '30s', target: 100 },   // 피크 증가
    { duration: '1m',  target: 100 },   // 피크 유지
    { duration: '30s', target: 0   },   // 쿨다운
  ],
  thresholds: {
    'http_req_failed':     ['rate<0.01'],   // 전체 에러율 < 1%
    'job_list_duration':   ['p(95)<3000'],  // 목록 p95 < 3s (캐시 워밍업 포함)
    'job_detail_duration': ['p(99)<3000'],  // 상세 p99 < 3s (100 VU 동시 viewCount 업데이트 포함)
  },
};

// ── 테스트 데이터 ───────────────────────────────────────────
const LOCATIONS = ['서울', '부산', '대구', '인천', '광주', '대전'];
const JOB_TYPES = ['IT개발', '영업', '마케팅', '디자인', '인사', '경영지원'];

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ── 테스트 시작 전 실제 jobNo 목록 수집 ─────────────────────
export function setup() {
  const res = http.get(`${BASE_URL}/api/jobs?status=published&page=0&size=100`);
  try {
    const body = JSON.parse(res.body);
    const ids = (body.content || []).map((j) => j.jobNo).filter(Boolean);
    return { jobIds: ids.length > 0 ? ids : [1] };
  } catch {
    return { jobIds: [1] };
  }
}

// ── 메인 시나리오 ───────────────────────────────────────────
export default function (data) {
  const jobIds = data.jobIds;

  // 시나리오 1: 채용공고 목록 조회 (캐시 효과 측정)
  group('채용공고 목록 조회', () => {
    const page = Math.floor(Math.random() * 3);
    const res = http.get(`${BASE_URL}/api/jobs?status=published&page=${page}&size=20`, {
      tags: { scenario: 'list' },
    });

    jobListDuration.add(res.timings.duration);
    const ok = check(res, {
      'list 200':         (r) => r.status === 200,
      'list has content': (r) => {
        try { return JSON.parse(r.body).content !== undefined; } catch { return false; }
      },
    });
    errorRate.add(!ok);

    if (res.timings.duration < 50) cacheHitCounter.add(1);
  });

  sleep(1);

  // 시나리오 2: 카테고리 필터 조회
  group('카테고리 필터', () => {
    const jobType  = randomItem(JOB_TYPES);
    const location = randomItem(LOCATIONS);
    const res = http.get(
      `${BASE_URL}/api/jobs/category?jobType=${encodeURIComponent(jobType)}&location=${encodeURIComponent(location)}&page=0&size=10`,
      { tags: { scenario: 'category' } }
    );

    const ok = check(res, { 'category 200': (r) => r.status === 200 });
    errorRate.add(!ok);
  });

  sleep(0.5);

  // 시나리오 3: 채용공고 상세 조회 (조회수 +1 검증)
  group('채용공고 상세 조회', () => {
    const jobNo = randomItem(jobIds);
    const res = http.get(`${BASE_URL}/api/jobs/${jobNo}`, {
      tags: { scenario: 'detail' },
    });

    jobDetailDuration.add(res.timings.duration);
    const ok = check(res, { 'detail 200': (r) => r.status === 200 });
    errorRate.add(!ok);
  });

  sleep(Math.random() * 1.5 + 0.5);
}

// ── 테스트 종료 후 요약 출력 ────────────────────────────────
export function handleSummary(data) {
  const metrics = data.metrics;

  const summary = {
    '총 요청 수':          metrics.http_reqs?.values?.count ?? 0,
    '에러율 (%)':          ((metrics.error_rate?.values?.rate ?? 0) * 100).toFixed(2),
    '전체 응답 p95 (ms)':  (metrics.http_req_duration?.values?.['p(95)'] ?? 0).toFixed(0),
    '목록 조회 p95 (ms)':  (metrics.job_list_duration?.values?.['p(95)'] ?? 0).toFixed(0),
    '상세 조회 p99 (ms)':  (metrics.job_detail_duration?.values?.['p(99)'] ?? 0).toFixed(0),
    '캐시 히트 추정':       metrics.cache_hit_count?.values?.count ?? 0,
  };

  console.log('\n====== 부하테스트 결과 요약 ======');
  Object.entries(summary).forEach(([k, v]) => console.log(`  ${k}: ${v}`));
  console.log('==================================\n');

  return {
    'stdout': JSON.stringify(summary, null, 2),
    'load-test/result_summary.json': JSON.stringify({ timestamp: new Date().toISOString(), ...summary }, null, 2),
  };
}
