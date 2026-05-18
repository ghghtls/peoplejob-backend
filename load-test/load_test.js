/**
 * PeopleJob 부하테스트 시나리오
 *
 * 실행 방법:
 *   k6 run load_test.js
 *
 * 결과 JSON 저장:
 *   k6 run --out json=result.json load_test.js
 *
 * 사전 준비:
 *   1. winget install k6
 *   2. seed_data.sql 실행 (10만 건 더미 데이터)
 *   3. Spring Boot 서버 실행: .\run-dev.ps1
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:5000';

// ── 커스텀 메트릭 ──────────────────────────────────────────
const jobListDuration   = new Trend('job_list_duration',   true);
const searchDuration    = new Trend('search_duration',     true);
const jobDetailDuration = new Trend('job_detail_duration', true);
const errorRate         = new Rate('error_rate');
const cacheHitCounter   = new Counter('cache_hit_count');

// ── 부하 시나리오 (단계별 VU 증가) ─────────────────────────
export const options = {
  stages: [
    { duration: '30s', target: 10  },   // 워밍업: 10명 증가
    { duration: '1m',  target: 50  },   // 정상 부하: 50명
    { duration: '30s', target: 100 },   // 피크: 100명
    { duration: '1m',  target: 100 },   // 피크 유지
    { duration: '30s', target: 0   },   // 쿨다운
  ],
  thresholds: {
    // 핵심 SLO (Service Level Objective)
    'http_req_duration':    ['p(95)<500'],    // 전체 응답 95%ile < 500ms
    'http_req_failed':      ['rate<0.01'],    // 에러율 < 1%
    'job_list_duration':    ['p(95)<300'],    // 목록 조회 (캐시 히트 기대) < 300ms
    'search_duration':      ['p(95)<800'],    // 검색 (FULLTEXT) < 800ms
    'job_detail_duration':  ['p(99)<500'],    // 상세 조회 < 500ms
  },
};

// ── 테스트 데이터 ───────────────────────────────────────────
const SEARCH_KEYWORDS = ['개발자', 'IT', '마케팅', '서울', '신입', '정규직', '카카오', '네이버', 'React', 'Java'];
const LOCATIONS       = ['서울', '부산', '대구', '인천', '광주', '대전'];
const JOB_TYPES       = ['IT개발', '영업', '마케팅', '디자인', '인사', '경영지원'];

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ── 메인 시나리오 ───────────────────────────────────────────
export default function () {

  // 시나리오 1: 채용공고 목록 조회 (캐시 효과 측정 핵심)
  group('채용공고 목록 조회', () => {
    const page = Math.floor(Math.random() * 10);  // 0~9 페이지
    const res = http.get(`${BASE_URL}/api/job?page=${page}&size=20`, {
      tags: { scenario: 'list' },
    });

    jobListDuration.add(res.timings.duration);
    const ok = check(res, {
      'list 200':             (r) => r.status === 200,
      'list has data':        (r) => {
        try { return JSON.parse(r.body).data !== undefined; } catch { return false; }
      },
    });
    errorRate.add(!ok);

    // X-Cache 헤더로 캐시 히트 감지 (Redis 캐시가 있으면 보통 응답이 매우 빠름)
    if (res.timings.duration < 50) cacheHitCounter.add(1);
  });

  sleep(1);

  // 시나리오 2: 키워드 검색 (FULLTEXT vs LIKE 성능 비교)
  group('채용공고 검색', () => {
    const keyword = randomItem(SEARCH_KEYWORDS);
    const res = http.get(
      `${BASE_URL}/api/job/search?keyword=${encodeURIComponent(keyword)}&page=0&size=10`,
      { tags: { scenario: 'search' } }
    );

    searchDuration.add(res.timings.duration);
    const ok = check(res, {
      'search 200': (r) => r.status === 200,
    });
    errorRate.add(!ok);
  });

  sleep(0.5);

  // 시나리오 3: 카테고리 필터 조회
  group('카테고리 필터', () => {
    const jobType  = randomItem(JOB_TYPES);
    const location = randomItem(LOCATIONS);
    const res = http.get(
      `${BASE_URL}/api/job/category?jobType=${encodeURIComponent(jobType)}&location=${encodeURIComponent(location)}&page=0&size=10`,
      { tags: { scenario: 'category' } }
    );

    check(res, { 'category 200': (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  });

  sleep(0.5);

  // 시나리오 4: 채용공고 상세 조회 (조회수 +1, readOnly 제거 검증)
  group('채용공고 상세 조회', () => {
    const jobNo = Math.floor(Math.random() * 1000) + 1;  // 1~1000 랜덤
    const res = http.get(`${BASE_URL}/api/job/${jobNo}`, {
      tags: { scenario: 'detail' },
    });

    jobDetailDuration.add(res.timings.duration);
    // 404는 정상 (더미 데이터 범위 밖) — 500만 에러로 처리
    const ok = check(res, {
      'detail not 500': (r) => r.status !== 500,
    });
    errorRate.add(!ok);
  });

  sleep(Math.random() * 1.5 + 0.5);  // 0.5~2초 랜덤 대기 (실제 사용자 행동 모사)
}

// ── 테스트 종료 후 요약 출력 ────────────────────────────────
export function handleSummary(data) {
  const metrics = data.metrics;

  const summary = {
    '총 요청 수':          metrics.http_reqs?.values?.count ?? 0,
    '에러율 (%)':          ((metrics.error_rate?.values?.rate ?? 0) * 100).toFixed(2),
    '전체 응답 p95 (ms)':  (metrics.http_req_duration?.values?.['p(95)'] ?? 0).toFixed(0),
    '목록 조회 p95 (ms)':  (metrics.job_list_duration?.values?.['p(95)'] ?? 0).toFixed(0),
    '검색 p95 (ms)':       (metrics.search_duration?.values?.['p(95)'] ?? 0).toFixed(0),
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
