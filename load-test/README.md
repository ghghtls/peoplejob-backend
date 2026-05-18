# PeopleJob 부하테스트

## 실행 순서

### 1. k6 설치
```powershell
winget install k6
```

### 2. 더미 데이터 삽입 (10만 건)
MySQL Workbench 또는 CLI에서 실행:
```sql
source C:/proj/peoplejob-backend/load-test/seed_data.sql
```
또는 CLI:
```powershell
mysql -u root -p peoplejob < load-test\seed_data.sql
```

### 3. 서버 실행 (힙 1GB)
```powershell
.\run-dev.ps1
```

### 4. 부하테스트 실행
```powershell
cd C:\proj\peoplejob-backend
k6 run load-test\load_test.js
```

### 5. 결과 JSON 저장
```powershell
k6 run --out json=load-test\result.json load-test\load_test.js
```

---

## 시나리오 구성

| 시나리오 | 엔드포인트 | 검증 포인트 |
|----------|-----------|------------|
| 목록 조회 | `GET /api/job` | Redis 캐시 히트 (p95 < 300ms) |
| 키워드 검색 | `GET /api/job/search` | FULLTEXT 인덱스 (p95 < 800ms) |
| 카테고리 필터 | `GET /api/job/category` | 복합 인덱스 효율 |
| 상세 조회 | `GET /api/job/{id}` | 조회수 write (p99 < 500ms) |

## 부하 단계

```
0s    → 30s  : 0 → 10 VU  (워밍업)
30s   → 1m30s: 10 → 50 VU (정상 부하)
1m30s → 2m   : 50 → 100 VU (피크)
2m    → 3m   : 100 VU 유지
3m    → 3m30s: 100 → 0 VU (쿨다운)
```

## SLO (목표 기준)

| 지표 | 목표 |
|------|------|
| 에러율 | < 1% |
| 전체 응답 p95 | < 500ms |
| 목록 조회 p95 | < 300ms |
| 검색 p95 | < 800ms |
| 상세 조회 p99 | < 500ms |

## 포트폴리오 어필 포인트

결과를 캡처해서 아래 비교표를 채우세요:

| 최적화 항목 | Before | After | 개선율 |
|------------|--------|-------|--------|
| 목록 조회 p95 | ?ms | ?ms (Redis 캐시) | ?% |
| 검색 p95 | ?ms (LIKE) | ?ms (FULLTEXT) | ?% |
| 100 VU 에러율 | ?% | ?% | ?% |
