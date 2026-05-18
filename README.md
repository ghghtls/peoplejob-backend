# PeopleJob Backend

구인구직 플랫폼 **피플잡**의 Spring Boot 백엔드 서버입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 |
| Database | MySQL 8.0 |
| Cache | Redis |
| ORM | Spring Data JPA (Hibernate 6) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| API Docs | Swagger / SpringDoc OpenAPI 2.6.0 |
| Monitoring | Spring Actuator + Prometheus |
| Build | Maven |
| CI | GitHub Actions |

---

## 프로젝트 구조

```
src/main/java/com/people/job/
├── admin/          # 관리자 기능
├── apply/          # 채용 지원
├── board/          # 게시판
├── config/         # 설정 (Security, CORS, Data 초기화 등)
├── email/          # 이메일 발송
├── file/           # 파일 업로드/다운로드
├── health/         # Redis 헬스체크
├── inquiry/        # 문의사항
├── job/            # 채용공고
├── mypage/         # 마이페이지
├── notice/         # 공지사항
├── notification/   # 알림
├── payment/        # 결제
├── resume/         # 이력서
├── scrap/          # 스크랩
├── scheduler/      # 스케줄러
└── user/           # 사용자 / 인증
```

---

## 로컬 개발 환경 설정

### 사전 요구사항

- Java 17 이상
- MySQL 8.0 (포트 3307)
- Redis (WSL 또는 로컬)



### 실행

```powershell
# Redis 시작 (WSL)
wsl redis-server --daemonize yes

# 백엔드 실행
.\run-dev.ps1
```

또는 Maven으로 직접 실행:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

서버 기동 확인:
- 헬스체크: http://localhost:8090/actuator/health
- Swagger UI: http://localhost:8090/swagger-ui.html

---

## API 엔드포인트

### 인증 (Users)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/users/register` | 회원가입 | X |
| POST | `/api/users/login` | 로그인 | X |
| POST | `/api/users/verify` | 이메일 인증 | X |
| GET | `/api/users/check/{userid}` | 아이디 중복확인 | X |
| GET | `/api/users/profile/{userNo}` | 회원정보 조회 | O |
| PUT | `/api/users/profile/{userNo}` | 회원정보 수정 | O |
| PUT | `/api/users/password/{userNo}` | 비밀번호 변경 | O |
| DELETE | `/api/users/profile/{userNo}` | 회원탈퇴 | O |

### 채용공고 (Jobs)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/jobs` | 채용공고 목록 (페이징) | X |
| GET | `/api/jobs/{jobNo}` | 채용공고 상세 | X |
| GET | `/api/jobs/search` | 채용공고 검색 | X |
| POST | `/api/jobs` | 채용공고 등록 | COMPANY |
| PUT | `/api/jobs/{jobNo}` | 채용공고 수정 | COMPANY |
| DELETE | `/api/jobs/{jobNo}` | 채용공고 삭제 | COMPANY |
| POST | `/api/jobs/{jobNo}/publish` | 공고 게시 | COMPANY |
| PUT | `/api/jobs/{jobNo}/status` | 상태 변경 | COMPANY |

### 지원 (Apply)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/apply` | 지원하기 | O |
| GET | `/api/apply/check` | 지원 여부 확인 | O |
| GET | `/api/apply/job/{jobopeningNo}` | 공고별 지원자 목록 | O |
| PUT | `/api/apply/{applyNo}/status` | 지원 상태 변경 | O |
| DELETE | `/api/apply/{applyNo}` | 지원 취소 | O |

### 이력서 (Resume)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/resume` | 이력서 등록 | O |
| GET | `/api/resume/{id}` | 이력서 상세 | O |
| PUT | `/api/resume/{id}` | 이력서 수정 | O |
| DELETE | `/api/resume/{id}` | 이력서 삭제 | O |
| GET | `/api/resume/user/{userNo}` | 사용자별 이력서 | O |
| GET | `/api/resume/search` | 키워드 검색 | O |
| GET | `/api/resume/jobtype/{jobType}` | 직종별 조회 | O |
| GET | `/api/resume/location/{location}` | 지역별 조회 | O |

### 스크랩 (Scrap)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/scrap` | 스크랩 추가 | O |
| GET | `/api/scrap/{userNo}` | 내 스크랩 목록 | O |
| GET | `/api/scrap/check` | 스크랩 여부 확인 | O |
| DELETE | `/api/scrap/{scrapNo}` | 스크랩 삭제 | O |

### 게시판 (Board)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/board` | 게시글 목록 | X |
| GET | `/api/board/{boardNo}` | 게시글 상세 | X |
| GET | `/api/board/category/{category}` | 카테고리별 조회 | X |
| GET | `/api/board/search` | 게시글 검색 | X |
| POST | `/api/board` | 게시글 등록 | O |
| PUT | `/api/board/{boardNo}` | 게시글 수정 | O |
| DELETE | `/api/board/{boardNo}` | 게시글 삭제 | O |
| PATCH | `/api/board/{boardNo}/view` | 조회수 증가 | X |

### 공지사항 (Notice)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/notice` | 공지사항 목록 | X |
| GET | `/api/notice/{noticeNo}` | 공지사항 상세 | X |
| GET | `/api/notice/important` | 중요 공지사항 | X |
| GET | `/api/notice/search` | 공지사항 검색 | X |
| POST | `/api/notice` | 공지사항 등록 | ADMIN |
| PUT | `/api/notice/{noticeNo}` | 공지사항 수정 | ADMIN |
| DELETE | `/api/notice/{noticeNo}` | 공지사항 삭제 | ADMIN |

### 문의사항 (Inquiry)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/inquiry` | 문의 등록 | O |
| GET | `/api/inquiry/my` | 내 문의 목록 | O |
| GET | `/api/inquiry/{inquiryNo}` | 문의 상세 | O |
| PUT | `/api/inquiry/{inquiryNo}` | 문의 수정 | O |
| DELETE | `/api/inquiry/{inquiryNo}` | 문의 삭제 | O |
| PUT | `/api/inquiry/{inquiryNo}/answer` | 답변 등록 | ADMIN |

### 파일 (Files)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/files/upload/resume/image` | 이력서 이미지 업로드 | O |
| POST | `/api/files/upload/resume/file` | 이력서 파일 업로드 | O |
| POST | `/api/files/upload/job/file` | 채용공고 파일 업로드 | O |
| POST | `/api/files/upload/board/image` | 게시판 이미지 업로드 | O |
| GET | `/api/files/download` | 파일 다운로드 | O |
| POST | `/api/files/download/multiple` | 다중 파일 ZIP 다운로드 | O |
| DELETE | `/api/files/delete` | 파일 삭제 | O |

### 관리자 (Admin)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/admin/dashboard` | 대시보드 | ADMIN |
| GET | `/api/admin/users` | 전체 사용자 조회 | ADMIN |
| GET | `/api/admin/jobs` | 전체 채용공고 조회 | ADMIN |
| GET | `/api/admin/inquiries` | 전체 문의 조회 | ADMIN |
| GET | `/api/admin/payments` | 전체 결제 조회 | ADMIN |
| GET | `/api/admin/excel/{type}` | Excel 다운로드 | ADMIN |

### 이메일 (Email)
| Method | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/email/send-verification` | 인증코드 발송 | X |
| POST | `/api/email/verify` | 인증코드 확인 | X |
| POST | `/api/email/send-reset-password` | 비밀번호 재설정 링크 발송 | X |
| POST | `/api/email/reset-password` | 비밀번호 재설정 | X |

---

## 인증 방식

JWT Bearer Token 방식을 사용합니다.

```
Authorization: Bearer {token}
```

로그인 성공 시 JWT 토큰이 응답 바디에 반환됩니다. 이후 요청에 해당 토큰을 헤더에 포함해야 합니다.

### 사용자 역할

| 역할 | 설명 |
|------|------|
| `ROLE_USER` | 일반 개인회원 |
| `ROLE_COMPANY` | 기업회원 (채용공고 등록 가능) |
| `ROLE_ADMIN` | 관리자 |

---

## Spring Profiles

| 프로파일 | 설명 | 실행 방법 |
|----------|------|----------|
| `dev` | 로컬 개발 환경 (MySQL 3307, DDL auto-update) | `--spring.profiles.active=dev` |
| `prod` | 운영 환경 (DDL none, SQL 로그 비활성화) | `--spring.profiles.active=prod` |
| `test` | 테스트 환경 (H2 인메모리 DB) | `--spring.profiles.active=test` |

---

## CI/CD

GitHub Actions를 통해 자동화된 빌드 및 테스트가 실행됩니다.

**트리거:** `master`, `develop` 브랜치 push 또는 PR

**파이프라인:**
1. **Test & Build** — 단위 테스트 실행 → JAR 빌드
2. **Code Quality** — SpotBugs 정적 분석 + JaCoCo 커버리지 리포트 (최소 50%)

---

## 모니터링

| 엔드포인트 | 설명 |
|-----------|------|
| `/actuator/health` | 서버 상태 확인 |
| `/actuator/info` | 앱 정보 |
| `/actuator/metrics` | 메트릭 수집 |
| `/actuator/prometheus` | Prometheus 메트릭 |

---

## API 문서

서버 실행 후 아래 URL에서 Swagger UI를 통해 전체 API를 확인할 수 있습니다.

```
http://localhost:8090/swagger-ui.html
```
