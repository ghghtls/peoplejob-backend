package com.people.job.config;

import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.board.entity.BoardEntity;
import com.people.job.board.repository.BoardRepository;
import com.people.job.inquiry.entity.InquiryEntity;
import com.people.job.inquiry.repository.InquiryRepository;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.notice.entity.NoticeEntity;
import com.people.job.notice.repository.NoticeRepository;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JobopeningRepository jobRepository;
    private final BoardRepository boardRepository;
    private final NoticeRepository noticeRepository;
    private final ResumeRepository resumeRepository;
    private final InquiryRepository inquiryRepository;
    private final ApplyRepository applyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean adminExists = userRepository.findByUserid("admin").isPresent();

        if (!adminExists) {
            seedMainData();
        } else {
            log.info("[DataInitializer] 기본 더미 데이터 이미 존재. 자료실/취업뉴스만 체크합니다.");
        }

        seedResourceBoards();
        seedInquiries();
        seedApplicants();
    }

    @Transactional
    public void seedMainData() {

        log.info("[DataInitializer] 더미 데이터 삽입 시작...");

        // ── 1. 관리자 계정 ──────────────────────────────────────────────
        UserEntity admin = userRepository.save(UserEntity.builder()
                .userid("admin")
                .password(passwordEncoder.encode("admin"))
                .username("관리자")
                .email("admin@peoplejob.com")
                .phone("02-0000-0000")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        // ── 2. 기업 회원 ─────────────────────────────────────────────────
        UserEntity company1 = userRepository.save(UserEntity.builder()
                .userid("company1")
                .password(passwordEncoder.encode("Company1!"))
                .username("테크스타트")
                .email("hr@techstart.co.kr")
                .phone("02-1234-5678")
                .userType(UserEntity.UserType.COMPANY)
                .role(UserEntity.UserRole.COMPANY)
                .companyName("테크스타트(주)")
                .businessNumber("123-45-67890")
                .companyPhone("02-1234-5678")
                .ceoName("김대표")
                .companyType("스타트업")
                .employeeCount(50)
                .establishedYear("2019")
                .website("https://techstart.co.kr")
                .companyDescription("AI·SaaS 전문 스타트업입니다. 함께 성장할 인재를 모십니다.")
                .isActive(true)
                .isEmailVerified(true)
                .build());

        UserEntity company2 = userRepository.save(UserEntity.builder()
                .userid("company2")
                .password(passwordEncoder.encode("Company2!"))
                .username("글로벌코퍼레이션")
                .email("recruit@globalcorp.com")
                .phone("02-9876-5432")
                .userType(UserEntity.UserType.COMPANY)
                .role(UserEntity.UserRole.COMPANY)
                .companyName("글로벌코퍼레이션(주)")
                .businessNumber("234-56-78901")
                .companyPhone("02-9876-5432")
                .ceoName("이사장")
                .companyType("중견기업")
                .employeeCount(300)
                .establishedYear("2005")
                .website("https://globalcorp.com")
                .companyDescription("글로벌 시장을 선도하는 종합 IT 기업입니다.")
                .isActive(true)
                .isEmailVerified(true)
                .build());

        UserEntity company3 = userRepository.save(UserEntity.builder()
                .userid("company3")
                .password(passwordEncoder.encode("Company3!"))
                .username("핀테크뱅크")
                .email("people@fintechbank.io")
                .phone("02-5555-1234")
                .userType(UserEntity.UserType.COMPANY)
                .role(UserEntity.UserRole.COMPANY)
                .companyName("핀테크뱅크(주)")
                .businessNumber("345-67-89012")
                .companyPhone("02-5555-1234")
                .ceoName("박대표")
                .companyType("핀테크")
                .employeeCount(120)
                .establishedYear("2017")
                .website("https://fintechbank.io")
                .companyDescription("블록체인·결제 혁신을 이끄는 핀테크 기업입니다.")
                .isActive(true)
                .isEmailVerified(true)
                .build());

        // ── 3. 개인 회원 ─────────────────────────────────────────────────
        UserEntity user1 = userRepository.save(UserEntity.builder()
                .userid("user1")
                .password(passwordEncoder.encode("User1234!"))
                .username("김개발")
                .email("kim@example.com")
                .phone("010-1111-2222")
                .address("서울시 강남구")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        UserEntity user2 = userRepository.save(UserEntity.builder()
                .userid("user2")
                .password(passwordEncoder.encode("User1234!"))
                .username("이디자인")
                .email("lee@example.com")
                .phone("010-3333-4444")
                .address("서울시 마포구")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        UserEntity user3 = userRepository.save(UserEntity.builder()
                .userid("user3")
                .password(passwordEncoder.encode("User1234!"))
                .username("박마케터")
                .email("park@example.com")
                .phone("010-5555-6666")
                .address("경기도 성남시")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        UserEntity user4 = userRepository.save(UserEntity.builder()
                .userid("user4")
                .password(passwordEncoder.encode("User1234!"))
                .username("최데이터")
                .email("choi@example.com")
                .phone("010-7777-8888")
                .address("서울시 서초구")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build());

        // ── 4. 채용공고 (PUBLISHED) ───────────────────────────────────────
        List<JobopeningEntity> jobs = List.of(
            buildJob(company1.getUserNo(), "테크스타트(주)", "백엔드 개발자 (Java/Spring) 채용",
                "Java Spring Boot 기반 백엔드 개발자를 채용합니다.\n\n[담당 업무]\n- REST API 설계 및 개발\n- 데이터베이스 설계 및 최적화\n- 클라우드 인프라 운영 (AWS)\n\n[지원 자격]\n- 경력 2년 이상\n- Java, Spring Boot 숙련자\n- MySQL/PostgreSQL 경험자\n\n[우대 사항]\n- MSA 경험자\n- Redis, Kafka 경험자",
                "서울 강남구", "정규직", "연 4,000 ~ 6,000만원", "풀타임", "2년 이상", "대졸 이상",
                LocalDate.now().plusMonths(1)),
            buildJob(company1.getUserNo(), "테크스타트(주)", "프론트엔드 개발자 (React/Flutter) 모집",
                "React 및 Flutter 기반의 프론트엔드 개발자를 모집합니다.\n\n[담당 업무]\n- 웹/앱 UI 개발 및 유지보수\n- 백엔드 API 연동\n- 성능 최적화\n\n[지원 자격]\n- 경력 1년 이상\n- React 또는 Flutter 실무 경험\n\n[우대 사항]\n- TypeScript 경험\n- 반응형 웹 개발 경험",
                "서울 강남구", "정규직", "연 3,500 ~ 5,500만원", "풀타임", "1년 이상", "학력 무관",
                LocalDate.now().plusDays(20)),
            buildJob(company2.getUserNo(), "글로벌코퍼레이션(주)", "데이터 엔지니어 채용 (~경력 5년)",
                "빅데이터 처리 파이프라인 구축 경험이 있는 데이터 엔지니어를 채용합니다.\n\n[담당 업무]\n- 대용량 데이터 파이프라인 설계 및 구축\n- ETL 프로세스 개발·운영\n- 데이터 품질 관리\n\n[필수 역량]\n- Python, SQL 능숙\n- Spark, Hadoop 경험\n- 데이터 웨어하우스 구축 경험",
                "서울 중구", "정규직", "연 5,000 ~ 8,000만원", "풀타임", "3년 이상", "대졸 이상",
                LocalDate.now().plusMonths(2)),
            buildJob(company2.getUserNo(), "글로벌코퍼레이션(주)", "DevOps / 클라우드 엔지니어 모집",
                "AWS 기반 클라우드 인프라를 운영할 DevOps 엔지니어를 모집합니다.\n\n[담당 업무]\n- CI/CD 파이프라인 구축 및 운영\n- Kubernetes 클러스터 관리\n- 모니터링 및 장애 대응\n\n[필수 역량]\n- AWS 인프라 운영 경험 3년 이상\n- Docker, Kubernetes 숙련\n- Terraform 경험자",
                "서울 중구", "정규직", "연 5,500 ~ 9,000만원", "풀타임", "3년 이상", "학력 무관",
                LocalDate.now().plusDays(45)),
            buildJob(company2.getUserNo(), "글로벌코퍼레이션(주)", "UX/UI 디자이너 채용",
                "사용자 중심의 UI/UX를 설계할 디자이너를 채용합니다.\n\n[담당 업무]\n- 서비스 화면 설계 및 디자인\n- 사용자 리서치 및 UT 진행\n- 디자인 시스템 관리\n\n[필수 역량]\n- Figma 숙련 필수\n- 앱/웹 UI 디자인 포트폴리오",
                "서울 종로구", "정규직", "연 3,500 ~ 5,000만원", "풀타임", "2년 이상", "관련학과 졸업",
                LocalDate.now().plusDays(30)),
            buildJob(company3.getUserNo(), "핀테크뱅크(주)", "블록체인 개발자 (Solidity/Web3)",
                "블록체인 기반 DeFi 서비스 개발자를 채용합니다.\n\n[담당 업무]\n- 스마트 컨트랙트 개발 (Solidity)\n- Web3.js/ethers.js 연동\n- 블록체인 네트워크 운영\n\n[필수 역량]\n- Solidity 개발 경험 2년 이상\n- EVM 생태계 이해\n- DeFi 프로토콜 이해",
                "서울 서초구", "정규직", "연 6,000 ~ 10,000만원", "풀타임", "2년 이상", "학력 무관",
                LocalDate.now().plusMonths(1)),
            buildJob(company3.getUserNo(), "핀테크뱅크(주)", "결제 시스템 백엔드 개발자",
                "간편결제·정산 시스템 백엔드 개발자를 모집합니다.\n\n[담당 업무]\n- 결제·정산 API 설계 및 구현\n- 금융 규제 준수 시스템 구축\n- 대용량 트랜잭션 처리 최적화\n\n[필수 역량]\n- Java/Kotlin 백엔드 경력 3년 이상\n- 금융 도메인 경험 우대\n- PCI-DSS 이해",
                "서울 서초구", "정규직", "연 5,000 ~ 8,000만원", "풀타임", "3년 이상", "대졸 이상",
                LocalDate.now().plusDays(25)),
            buildJob(company1.getUserNo(), "테크스타트(주)", "AI/ML 엔지니어 채용",
                "AI 모델 개발 및 서비스화 경험을 갖춘 엔지니어를 채용합니다.\n\n[담당 업무]\n- 머신러닝·딥러닝 모델 개발\n- MLOps 파이프라인 구축\n- 모델 서빙 및 성능 최적화\n\n[필수 역량]\n- Python, TensorFlow/PyTorch\n- 논문 구현 경험\n- 실서비스 ML 배포 경험",
                "서울 강남구", "정규직", "연 5,500 ~ 9,000만원", "풀타임", "2년 이상", "관련학과 석사 이상",
                LocalDate.now().plusMonths(2)),
            buildJob(company2.getUserNo(), "글로벌코퍼레이션(주)", "마케팅 매니저 (디지털 마케팅)",
                "디지털 마케팅 전략을 수립하고 실행할 마케팅 매니저를 모집합니다.\n\n[담당 업무]\n- SNS·검색 광고 운영 (구글, 네이버, 메타)\n- 콘텐츠 기획 및 제작\n- 캠페인 성과 분석 및 보고\n\n[필수 역량]\n- 디지털 마케팅 실무 2년 이상\n- GA4, Meta Ads 운영 경험",
                "서울 중구", "정규직", "연 3,500 ~ 5,000만원", "풀타임", "2년 이상", "학력 무관",
                LocalDate.now().plusDays(15)),
            buildJob(company3.getUserNo(), "핀테크뱅크(주)", "보안 엔지니어 (정보보안 전문가)",
                "금융 IT 보안 전문가를 채용합니다.\n\n[담당 업무]\n- 취약점 분석 및 모의해킹\n- 보안 정책 수립 및 운영\n- 개인정보보호법 컴플라이언스\n\n[필수 역량]\n- 정보보안기사 또는 CISSP 보유\n- 금융보안 경험 3년 이상\n- 침해사고 대응 경험",
                "서울 서초구", "정규직", "연 5,500 ~ 8,500만원", "풀타임", "3년 이상", "대졸 이상",
                LocalDate.now().plusDays(40)),
            buildJob(company1.getUserNo(), "테크스타트(주)", "Node.js 백엔드 개발자 (신입/경력)",
                "Node.js 기반 서버 개발자를 채용합니다. 신입도 지원 가능합니다!\n\n[담당 업무]\n- REST API 설계 및 개발\n- 실시간 서버 개발 (Socket.io)\n- 데이터베이스 설계\n\n[지원 자격]\n- Node.js 개발 경험 (신입 환영)\n- JavaScript/TypeScript 이해\n\n[우대]\n- 토이 프로젝트 보유자",
                "서울 강남구", "정규직", "연 2,800 ~ 4,500만원", "풀타임", "신입", "학력 무관",
                LocalDate.now().plusMonths(3)),
            buildJob(company2.getUserNo(), "글로벌코퍼레이션(주)", "QA 엔지니어 (자동화 테스트)",
                "테스트 자동화 전문 QA 엔지니어를 모집합니다.\n\n[담당 업무]\n- 테스트 전략 수립 및 자동화 구축\n- Selenium/Playwright 기반 E2E 테스트\n- 버그 리포팅 및 품질 관리\n\n[필수 역량]\n- QA 실무 2년 이상\n- 테스트 자동화 도구 사용 경험",
                "서울 중구", "정규직", "연 4,000 ~ 6,000만원", "풀타임", "2년 이상", "학력 무관",
                LocalDate.now().plusDays(50))
        );
        jobs.forEach(job -> {
            job.publish();
            jobRepository.save(job);
        });

        // ── 5. 공지사항 ───────────────────────────────────────────────────
        List<NoticeEntity> notices = List.of(
            NoticeEntity.builder()
                .title("[공지] PeopleJob 서비스 오픈 안내")
                .content("안녕하세요, PeopleJob 운영팀입니다.\n\n드디어 PeopleJob 채용 플랫폼이 정식 오픈하였습니다!\n\n■ 주요 서비스\n- 채용공고 검색 및 지원\n- 이력서 작성 및 관리\n- 기업 회원 채용공고 등록\n- 인재 검색 서비스\n\n많은 이용 부탁드립니다. 감사합니다.")
                .writer("관리자").isImportant(true).isActive(true).build(),
            NoticeEntity.builder()
                .title("[안내] 이력서 작성 가이드 업데이트")
                .content("이력서 작성에 도움이 되는 가이드가 업데이트되었습니다.\n\n■ 주요 변경사항\n- 직무별 이력서 예시 추가\n- 자기소개서 작성 팁 보강\n- 포트폴리오 첨부 방법 안내\n\n[자료실] 메뉴에서 확인하실 수 있습니다.")
                .writer("관리자").isImportant(false).isActive(true).build(),
            NoticeEntity.builder()
                .title("[공지] 기업회원 채용공고 등록 정책 안내")
                .content("기업회원의 채용공고 등록 관련 정책을 안내드립니다.\n\n■ 등록 절차\n1. 기업 회원가입\n2. 채용공고 작성 및 저장\n3. '게시' 버튼 클릭 후 즉시 노출\n\n■ 주의사항\n- 허위 채용공고 등록 시 계정 정지\n- 마감일 경과 시 자동 마감 처리")
                .writer("관리자").isImportant(true).isActive(true).build(),
            NoticeEntity.builder()
                .title("[안내] 5월 서버 점검 일정")
                .content("서비스 안정화를 위한 정기 서버 점검이 진행됩니다.\n\n■ 점검 일시: 2025년 5월 25일(일) 02:00 ~ 04:00\n■ 영향 범위: 전체 서비스 일시 중단\n\n점검 시간 동안 서비스 이용이 불가합니다.\n이용에 불편을 드려 죄송합니다.")
                .writer("관리자").isImportant(false).isActive(true).build(),
            NoticeEntity.builder()
                .title("[공지] 개인정보처리방침 개정 안내")
                .content("2025년 6월 1일부로 개인정보처리방침이 개정됩니다.\n\n■ 주요 변경사항\n- 수집 항목 명확화\n- 제3자 제공 범위 구체화\n- 보유 기간 상세화\n\n변경된 내용은 시행일 이후 PeopleJob 서비스에 적용됩니다.")
                .writer("관리자").isImportant(false).isActive(true).build(),
            NoticeEntity.builder()
                .title("[이벤트] 신규 가입 이벤트 안내")
                .content("PeopleJob 오픈 기념 신규 가입 이벤트를 진행합니다!\n\n■ 이벤트 기간: 2025년 5월 ~ 6월 말\n■ 대상: 신규 가입 회원 전체\n■ 혜택: 프리미엄 이력서 템플릿 무료 제공\n\n지금 바로 가입하고 혜택을 받으세요!")
                .writer("관리자").isImportant(true).isActive(true).build()
        );
        noticeRepository.saveAll(notices);

        // ── 6. 게시판 ─────────────────────────────────────────────────────
        List<BoardEntity> boards = List.of(

            // 자료실
            BoardEntity.builder()
                .category("자료실").title("자소서 작성 가이드")
                .content("■ 자기소개서 작성의 핵심 원칙\n\n자기소개서는 단순한 경험 나열이 아닌, 지원 직무에 맞는 '나'를 설득력 있게 표현하는 글입니다.\n\n━━━━━━━━━━━━━━━━━━━━━━\n1. 직무 중심으로 작성하기\n━━━━━━━━━━━━━━━━━━━━━━\n· 지원 직무의 핵심 역량을 파악한 후, 그에 맞는 경험을 선별하세요.\n· '나는 이런 사람이다' 보다 '나는 이 직무에 이런 기여를 할 수 있다'로 작성하세요.\n\n━━━━━━━━━━━━━━━━━━━━━━\n2. STAR 기법 활용하기\n━━━━━━━━━━━━━━━━━━━━━━\n· Situation: 어떤 상황이었는지\n· Task: 내가 맡은 역할/과제\n· Action: 구체적으로 무엇을 했는지\n· Result: 어떤 결과를 얻었는지 (수치화 권장)\n\n[예시]\n\"팀 내 데이터 처리 속도가 느려 배포 지연이 잦았습니다(S). 저는 쿼리 최적화 담당을 자원했고(T), 인덱스 재설계와 N+1 문제를 해결했습니다(A). 결과적으로 처리 속도가 70% 개선되어 배포 주기가 2주에서 1주로 단축되었습니다(R).\"\n\n━━━━━━━━━━━━━━━━━━━━━━\n3. 항목별 작성 팁\n━━━━━━━━━━━━━━━━━━━━━━\n[성장과정] 직무 역량 형성에 영향을 준 경험 중심으로\n[지원동기] 해당 기업/직무를 선택한 구체적 이유\n[직무역량] 포트폴리오, 수치, 수상경력으로 증명\n[입사 후 포부] 3년·5년 후 구체적인 목표 제시\n\n━━━━━━━━━━━━━━━━━━━━━━\n4. 자주 하는 실수\n━━━━━━━━━━━━━━━━━━━━━━\n· 두루뭉술한 표현: \"열심히 했습니다\" → \"주 5회 스터디를 6개월간 운영했습니다\"\n· 회사 정보 없는 지원동기: 기업 홈페이지·뉴스 참고 후 작성\n· 글자 수 초과/미달: 권장 분량의 90~100% 채우기\n· 오탈자: 제출 전 3회 이상 교정 필수")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(10))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("자료실").title("2026 채용 설명회 자료집")
                .content("■ 2026 PeopleJob 채용 설명회 참여 기업 안내\n\n━━━━━━━━━━━━━━━━━━━━━━\n행사 개요\n━━━━━━━━━━━━━━━━━━━━━━\n· 일시: 2026년 3월 15일 (토) 10:00 ~ 17:00\n· 장소: 서울 코엑스 D홀\n· 규모: 50개 기업, 예상 방문객 3,000명\n\n━━━━━━━━━━━━━━━━━━━━━━\n참여 기업 목록 (일부)\n━━━━━━━━━━━━━━━━━━━━━━\n[IT/개발]\n· 테크스타트(주) — 백엔드, 프론트엔드, AI 엔지니어\n· 글로벌코퍼레이션(주) — 데이터, DevOps, QA\n· 핀테크뱅크(주) — 블록체인, 결제시스템, 보안\n\n[제조/서비스]\n· 한국제조(주) — 생산관리, 품질, 영업\n· 서비스코리아(주) — CS, 마케팅, 기획\n\n[공공/금융]\n· 코리아파이낸스 — IT, 리스크관리\n· 공공기관 A — 행정, 전산\n\n━━━━━━━━━━━━━━━━━━━━━━\n프로그램 일정\n━━━━━━━━━━━━━━━━━━━━━━\n10:00 ~ 11:00  개막식 및 기조연설\n11:00 ~ 13:00  기업 부스 방문 (1라운드)\n13:00 ~ 14:00  점심 및 네트워킹\n14:00 ~ 16:00  기업 부스 방문 (2라운드)\n16:00 ~ 17:00  현직자 멘토링 세션\n\n━━━━━━━━━━━━━━━━━━━━━━\n참가자 준비물\n━━━━━━━━━━━━━━━━━━━━━━\n· 이력서 10부 이상 인쇄\n· 명함 (있는 경우)\n· 포트폴리오 USB 또는 링크\n· 복장: 비즈니스 캐주얼 이상 권장\n\n사전 등록 시 기업 면접 우선 배정 혜택이 있습니다.")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(7))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("자료실").title("면접 준비 체크리스트")
                .content("■ 면접 전날 ~ 당일 완벽 준비 체크리스트\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-3 ~ D-1] 내용 준비\n━━━━━━━━━━━━━━━━━━━━━━\n□ 자기소개 1분 스피치 연습 (3회 이상)\n□ 지원 기업 홈페이지·뉴스·IR 자료 숙지\n□ 자소서 내용 전체 재검토 (면접관이 물어볼 포인트 파악)\n□ 직무 관련 예상 질문 30개 작성 및 답변 준비\n□ 산업 트렌드 및 최신 이슈 파악\n□ 포트폴리오/프로젝트 핵심 내용 정리\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-1] 실전 준비\n━━━━━━━━━━━━━━━━━━━━━━\n□ 면접 장소·교통편 확인 (구글맵으로 경로 저장)\n□ 복장 점검 (다림질, 신발 닦기)\n□ 이력서·포트폴리오 출력본 준비\n□ 필기구, 메모지 챙기기\n□ 충분한 수면 (7시간 이상)\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-Day] 당일 체크\n━━━━━━━━━━━━━━━━━━━━━━\n□ 면접 시간 30분 전 도착\n□ 대기실에서 핵심 키워드 마지막 확인\n□ 핸드폰 무음 설정\n□ 입장 전 거울로 용모 최종 점검\n\n━━━━━━━━━━━━━━━━━━━━━━\n[면접 중 주의사항]\n━━━━━━━━━━━━━━━━━━━━━━\n· 질문은 끝까지 듣고 답변하기\n· 모르는 내용은 솔직히 인정하고 배울 의지 표현\n· 답변 시 두괄식 구성 (핵심 → 부연 → 마무리)\n· 눈 맞춤: 면접관 3명이라면 3명 모두에게\n· 답변 시간: 1개 질문에 1~2분 이내\n\n━━━━━━━━━━━━━━━━━━━━━━\n[자주 묻는 질문 TOP 10]\n━━━━━━━━━━━━━━━━━━━━━━\n1. 자기소개 해주세요\n2. 지원 동기가 무엇인가요?\n3. 본인의 강점·약점은?\n4. 가장 힘들었던 경험과 극복 방법은?\n5. 팀 프로젝트에서 갈등이 생겼을 때 어떻게 했나요?\n6. 5년 후 자신의 모습은?\n7. 마지막으로 하고 싶은 말은?\n8. 희망 연봉은 얼마인가요?\n9. 입사 가능 시기는 언제인가요?\n10. 다른 회사에도 지원했나요?")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(5))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            // 취업뉴스
            BoardEntity.builder()
                .category("취업뉴스").title("2026 상반기 IT 채용 트렌드 — AI·클라우드 인력 수요 급증")
                .content("2026년 상반기 IT 업계 채용 시장은 AI·ML 엔지니어와 클라우드 아키텍처 인력에 대한 수요가 전년 대비 40% 이상 증가한 것으로 나타났습니다.\n\n■ 주요 트렌드\n\n1. AI/ML 직군 폭발적 성장\n생성형 AI 서비스 도입이 본격화되면서 LLM 파인튜닝, RAG 시스템 구축, MLOps 경험자에 대한 수요가 크게 늘었습니다. 관련 직군의 평균 연봉은 전년 대비 15% 상승했습니다.\n\n2. 클라우드 네이티브 인력 부족\nAWS, GCP, Azure 멀티클라우드 운영 경험과 Kubernetes, Terraform 기술을 갖춘 DevOps 엔지니어는 여전히 공급이 부족한 상태입니다.\n\n3. 보안 전문가 수요 증가\n사이버 보안 위협이 증가하면서 정보보안 기사, CISSP 보유자를 찾는 기업이 늘고 있습니다.\n\n■ 평균 연봉 (신입 기준)\n· 백엔드 개발자: 3,800만원\n· 프론트엔드 개발자: 3,500만원\n· AI/ML 엔지니어: 4,200만원\n· DevOps 엔지니어: 4,000만원\n\n지금이 IT 직군으로의 전환 또는 성장을 위한 최적의 시기입니다.")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(3))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("취업뉴스").title("중소기업 청년 채용 장려금 2026년 확대 — 최대 1,200만원 지원")
                .content("고용노동부는 중소기업의 청년 인력 채용을 촉진하기 위해 2026년부터 청년 채용 장려금을 기존 900만원에서 최대 1,200만원으로 확대한다고 발표했습니다.\n\n■ 지원 내용\n\n· 지원 대상: 5인 이상 중소기업에 청년(15~34세)을 정규직으로 신규 채용한 기업\n· 지원 금액: 1인당 최대 1,200만원 (6개월 단위 지급)\n· 지원 기간: 최대 2년\n\n■ 신청 방법\n\n1. 고용24 (www.work24.go.kr) 접속\n2. '청년 채용 장려금' 검색\n3. 기업 공인인증서로 신청\n\n■ 구직자 혜택\n\n장려금 지원 대상 기업에 취업 시, 기업 안정성이 높고 처우 개선 가능성이 큽니다. PeopleJob의 채용공고 필터에서 '중소기업 장려금 대상' 기업을 확인해보세요.\n\n■ 유의사항\n\n· 기존 직원 대체 채용은 제외\n· 최저임금 이상 지급 필수\n· 4대 보험 가입 필수")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(6))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("취업뉴스").title("국민취업지원제도 2026 — 구직촉진수당 월 60만원으로 인상")
                .content("취업을 준비 중인 청년·중장년층을 위한 국민취업지원제도가 2026년부터 구직촉진수당을 월 50만원에서 60만원으로 인상합니다.\n\n■ 국민취업지원제도란?\n\n취업을 원하는 사람에게 취업지원서비스를 제공하고, 저소득 구직자에게는 생계를 위한 소득도 지원하는 제도입니다.\n\n■ 1유형 (구직촉진수당 지급)\n\n· 대상: 15~69세 중 가구 중위소득 60% 이하\n· 수당: 월 60만원 × 최대 6개월\n· 취업활동계획 수립 및 구직활동 의무 이행 필요\n\n■ 2유형 (취업지원서비스만 제공)\n\n· 대상: 특정 계층 (청년, 경력단절여성, 중장년 등)\n· 취업지원서비스 제공 (직업훈련, 취업알선 등)\n\n■ 신청 방법\n\n1. 고용센터 방문 또는 국민취업지원제도 홈페이지 (kua.go.kr)\n2. 온라인 신청 후 취업지원사 면담\n3. 취업활동계획 수립 후 수당 지급 시작\n\n지금 바로 신청해 취업 준비에 전념하세요!")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(9))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("취업뉴스").title("면접 합격률 높이는 자기소개 스피치 전략 5가지")
                .content("첫인상을 결정하는 1분 자기소개, 어떻게 하면 면접관의 마음을 사로잡을 수 있을까요?\n\n■ 전략 1: 두괄식 구성\n\n'저는 [직무 역량 핵심 키워드]를 갖춘 [이름]입니다.'로 시작하세요. 면접관은 자기소개를 들으며 다음 질문을 생각합니다. 첫 문장에서 기억에 남을 키워드를 심어두세요.\n\n■ 전략 2: 숫자로 증명하기\n\n\"열심히 했습니다\"보다 \"6개월 간 주 3회 스터디를 운영해 팀 합격률 80%를 달성했습니다\"가 훨씬 강렬합니다.\n\n■ 전략 3: 지원 직무와 연결하기\n\n자기소개의 마지막은 반드시 '지원 직무'와 연결하세요. \"이 경험을 바탕으로 귀사의 ○○ 직무에서 즉시 기여할 수 있습니다\"처럼 마무리하면 좋습니다.\n\n■ 전략 4: 10번 이상 소리 내어 연습\n\n머릿속으로 외우는 것과 실제로 말하는 것은 다릅니다. 녹음해서 들어보고, 속도와 어조를 다듬으세요.\n\n■ 전략 5: 60~75초로 조절\n\n1분 자기소개라 했을 때 정확히 60~75초가 되도록 연습하세요. 너무 짧으면 준비 부족, 너무 길면 핵심이 없어 보입니다.")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(12))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            BoardEntity.builder()
                .category("취업뉴스").title("2026 공공기관 채용 로드맵 — NCS 블라인드 채용 강화")
                .content("2026년 공공기관 채용은 NCS(국가직무능력표준) 기반 블라인드 채용을 더욱 강화하는 방향으로 개편됩니다.\n\n■ 주요 변경사항\n\n1. 학력·학교명 완전 비공개\n이력서에 출신 대학명 기재 금지가 전면 시행됩니다. 직무능력 중심으로 평가합니다.\n\n2. NCS 직업기초능력 평가 비중 확대\n의사소통, 문제해결, 수리, 자원관리 능력 평가 비중이 기존 40%에서 50%로 상향됩니다.\n\n3. AI 면접 도입 기관 증가\n1차 면접에 AI 면접을 도입하는 공공기관이 2025년 30개에서 2026년 70개로 확대됩니다.\n\n■ 준비 전략\n\n· NCS 직업기초능력 기출문제 꾸준히 풀기\n· 직무기술서 세밀하게 분석 후 경험 매칭\n· AI 면접 특성 파악: 눈 맞춤(카메라), 목소리 톤, 답변 구조\n\n■ 상반기 채용 일정 (예정)\n\n· 1월~2월: 채용 공고 발표\n· 3월~4월: 서류 접수\n· 4월~5월: 필기시험\n· 5월~6월: 면접 및 최종 합격\n\n워크넷(work.go.kr)에서 관심 기관의 채용 일정을 미리 등록해두세요.")
                .writer(admin.getUserid()).regdate(LocalDate.now().minusDays(14))
                .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),

            // 기존 자유게시판/취업정보 게시글
            BoardEntity.builder()
                .category("자유게시판").title("개발자 취업 준비 꿀팁 공유합니다")
                .content("안녕하세요! 취업 준비하면서 도움이 됐던 꿀팁들 공유할게요.\n\n1. 포트폴리오는 GitHub에 꾸준히 정리하세요\n2. 코딩테스트는 프로그래머스, 백준으로 매일 1문제씩\n3. CS 기초 (운영체제, 네트워크, 자료구조)는 필수\n4. 면접에서는 프로젝트 경험을 STAR 기법으로 정리하면 좋아요\n\n취업 성공하세요! 파이팅!")
                .writer(user1.getUserid()).regdate(LocalDate.now().minusDays(5))
                .viewCount(42).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("자유게시판").title("스타트업 vs 대기업 어디가 더 좋을까요?")
                .content("취업 고민 중인데 스타트업이랑 대기업 중 어떤 곳이 커리어에 더 유리할까요?\n\n제 상황:\n- 개발 경력 1년\n- 성장보다는 안정을 원함\n- 연봉은 비슷한 수준\n\n경험 있으신 분들 조언 부탁드립니다!")
                .writer(user2.getUserid()).regdate(LocalDate.now().minusDays(3))
                .viewCount(28).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("취업정보").title("2025 상반기 IT 기업 공채 일정 정리")
                .content("2025년 상반기 주요 IT 기업 공채 일정 정리해드립니다.\n\n■ 네이버: 3월 ~ 4월\n■ 카카오: 4월 ~ 5월\n■ 라인: 3월 ~ 5월\n■ 쿠팡: 수시 채용\n■ 당근마켓: 수시 채용\n\n각 기업 채용 페이지 즐겨찾기 해두시고 놓치지 마세요!")
                .writer(user3.getUserid()).regdate(LocalDate.now().minusDays(7))
                .viewCount(156).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("취업정보").title("이직 준비할 때 연봉 협상 어떻게 하셨나요?")
                .content("이직 면접에서 연봉 협상 시 보통 얼마나 올려서 협상하시나요?\n\n저는 현재 연봉에서 20% 올려서 불렀다가 오퍼가 안 오더라고요... 너무 높게 불렀나요?\n\n선배님들의 경험 공유 부탁드립니다.")
                .writer(user4.getUserid()).regdate(LocalDate.now().minusDays(2))
                .viewCount(91).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("자유게시판").title("재택근무 vs 사무실 출근 여러분은 어느 쪽이 좋으세요?")
                .content("코로나 이후 재택근무가 보편화됐는데 여러분은 어느 쪽이 더 잘 맞으시나요?\n\n[재택 장점] 출퇴근 시간 절약, 편한 환경\n[사무실 장점] 동료와 소통, 업무 집중도\n\n저는 개인적으로 하이브리드가 제일 좋더라고요!")
                .writer(user1.getUserid()).regdate(LocalDate.now().minusDays(1))
                .viewCount(33).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("취업정보").title("포트폴리오 README 잘 쓰는 방법")
                .content("GitHub 포트폴리오 README를 효과적으로 작성하는 방법을 공유합니다.\n\n■ 필수 포함 요소\n1. 프로젝트 소개 (1~2줄 요약)\n2. 주요 기능 (스크린샷 포함)\n3. 기술 스택 (배지 활용)\n4. 설치 및 실행 방법\n5. 트러블슈팅 경험\n\n면접관이 5분 안에 파악할 수 있도록 간결하게 작성하세요!")
                .writer(user2.getUserid()).regdate(LocalDate.now().minusDays(4))
                .viewCount(67).isActive(true).allowComment(true).allowUpload(false).build(),
            BoardEntity.builder()
                .category("자유게시판").title("개발자 번아웃 극복 경험 공유")
                .content("3년차 개발자인데 최근 번아웃이 심하게 와서 힘드네요.\n\n비슷한 경험 있으신 분들 어떻게 극복하셨나요?\n\n저는 사이드 프로젝트로 좋아하는 걸 만들어보거나, 독서나 운동으로 환기를 시키는 중인데 쉽지 않더라고요...\n\n함께 이겨낸 경험들 나눠주세요!")
                .writer(user3.getUserid()).regdate(LocalDate.now())
                .viewCount(18).isActive(true).allowComment(true).allowUpload(false).build()
        );
        boardRepository.saveAll(boards);

        // ── 7. 이력서 (인재검색) ─────────────────────────────────────────
        List<ResumeEntity> resumes = List.of(
            ResumeEntity.builder()
                .title("5년차 풀스택 개발자 이력서 (Java/React)")
                .content("안녕하세요. Java Spring Boot와 React를 주력으로 개발해온 5년차 풀스택 개발자 김개발입니다.\n\n금융 도메인 SaaS 서비스 개발 경험이 있으며, 대용량 트래픽 처리 및 MSA 전환 프로젝트를 리드한 경험이 있습니다. 코드 품질과 테스트 문화를 중요시하며, 팀과 함께 성장하는 것을 좋아합니다.")
                .education("한국대학교 컴퓨터공학과 졸업 (2019)")
                .career("- 현) ABC테크(주) 백엔드 개발팀 (2021.03 ~ 재직 중)\n  · Spring Boot 기반 REST API 개발\n  · MSA 아키텍처 전환 리드\n- 전) XYZ소프트 개발팀 (2019.08 ~ 2021.02)\n  · 사내 ERP 시스템 개발 및 유지보수")
                .certificate("정보처리기사 (2019), AWS Solutions Architect Associate (2022)")
                .hopeJobtype("백엔드 개발").hopeLocation("서울").salary("연 5,500만원 이상")
                .workType("풀타임").userNo(user1.getUserNo()).isActive(true).build(),
            ResumeEntity.builder()
                .title("UX/UI 디자이너 포트폴리오 (Figma 전문)")
                .content("사용자 경험을 최우선으로 생각하는 UI/UX 디자이너 이디자인입니다.\n\n모바일 앱부터 웹 서비스까지 다양한 디지털 프로덕트를 디자인해왔습니다. 데이터 기반 의사결정을 선호하며, 개발팀과의 원활한 협업을 위해 기본적인 프론트엔드 지식도 갖추고 있습니다.")
                .education("서울예술대학교 시각디자인학과 졸업 (2020)")
                .career("- 현) 디지털에이전시 UX팀 (2021.01 ~ 재직 중)\n  · 앱/웹 UI 디자인 및 프로토타이핑\n  · 사용자 리서치 및 UT 진행\n- 전) 스타트업 디자이너 (2020.03 ~ 2020.12)\n  · 브랜드 아이덴티티 디자인")
                .certificate("GTQ 1급 (2019), Adobe Certified Expert (2021)")
                .hopeJobtype("UI/UX 디자인").hopeLocation("서울").salary("연 4,500만원 이상")
                .workType("풀타임").userNo(user2.getUserNo()).isActive(true).build(),
            ResumeEntity.builder()
                .title("디지털 마케터 이력서 - 퍼포먼스 마케팅 전문")
                .content("퍼포먼스 마케팅 전문 3년차 마케터 박마케터입니다.\n\n구글·메타·네이버 광고 운영을 통해 ROAS 300% 달성 경험이 있으며, 데이터 분석 기반의 의사결정을 중시합니다. 콘텐츠 마케팅과 SEO에도 강점이 있습니다.")
                .education("경희대학교 경영학과 졸업 (2021)")
                .career("- 현) 이커머스 스타트업 마케팅팀 (2022.01 ~ 재직 중)\n  · 퍼포먼스 광고 운영 (월 예산 3억)\n  · SNS 콘텐츠 기획 및 관리\n- 전) 마케팅 대행사 (2021.03 ~ 2021.12)\n  · 다수 클라이언트 광고 운영")
                .certificate("구글 애널리틱스 자격증 (2022), Facebook Blueprint (2023)")
                .hopeJobtype("마케팅").hopeLocation("서울/경기").salary("연 4,000만원 이상")
                .workType("풀타임").userNo(user3.getUserNo()).isActive(true).build(),
            ResumeEntity.builder()
                .title("데이터 분석가 / ML 엔지니어 (Python 전문)")
                .content("데이터로 비즈니스 문제를 해결하는 것을 좋아하는 데이터 분석가 최데이터입니다.\n\n Python, SQL, Tableau를 활용한 데이터 분석 및 시각화 경험이 풍부하며, 머신러닝 모델 개발 및 배포 경험도 보유하고 있습니다.")
                .education("KAIST 데이터사이언스학과 석사 졸업 (2022)")
                .career("- 현) 빅데이터 컨설팅 회사 (2022.09 ~ 재직 중)\n  · 고객사 데이터 분석 프로젝트\n  · 예측 모델 개발 (이탈률 예측, 수요 예측)\n- 인턴) 대형 포털 데이터팀 (2021.07 ~ 2021.12)\n  · 로그 데이터 분석 및 대시보드 구축")
                .certificate("ADsP (2021), SQL 전문가 (2022), TensorFlow Developer (2023)")
                .hopeJobtype("데이터 분석/ML").hopeLocation("서울").salary("연 5,000만원 이상")
                .workType("풀타임").userNo(user4.getUserNo()).isActive(true).build(),
            ResumeEntity.builder()
                .title("신입 iOS 개발자 이력서 (Swift/SwiftUI)")
                .content("Swift와 SwiftUI를 독학으로 습득한 취업 준비 중인 iOS 개발자입니다.\n\n앱스토어에 2개의 앱을 출시한 경험이 있으며, 사용자 편의성을 중심으로 개발하는 것을 중요하게 생각합니다. 성장 가능성을 보여드릴 자신이 있습니다!")
                .education("인하대학교 소프트웨어학과 졸업 (2024)")
                .career("- 개인 앱 개발 및 출시 (2023 ~ 현재)\n  · 할일 관리 앱 출시 (다운로드 500+)\n  · 독서 기록 앱 출시 (다운로드 200+)\n- 스타트업 iOS 개발 인턴 (2023.07 ~ 2023.12)")
                .certificate("정보처리기사 (2024)")
                .hopeJobtype("iOS 개발").hopeLocation("서울/경기").salary("연 3,000만원 이상")
                .workType("풀타임").userNo(user1.getUserNo()).isActive(true).build()
        );
        resumeRepository.saveAll(resumes);

        // ── 8. 지원 내역 ─────────────────────────────────────────────────
        try {
            applyRepository.saveAll(List.of(
                ApplyEntity.builder()
                    .jobNo(jobs.get(0).getJobNo()).userNo(user1.getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("ACCEPTED")
                    .applyDate(LocalDate.now().minusDays(20))
                    .message("백엔드 개발 경험을 살려 기여하고 싶습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(1).getJobNo()).userNo(user1.getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("PENDING")
                    .applyDate(LocalDate.now().minusDays(5)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(4).getJobNo()).userNo(user2.getUserNo())
                    .resumeNo(resumes.get(1).getResumeNo()).status("REVIEWED")
                    .applyDate(LocalDate.now().minusDays(15))
                    .message("Figma 포트폴리오 첨부했습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(5).getJobNo()).userNo(user2.getUserNo())
                    .resumeNo(resumes.get(1).getResumeNo()).status("REJECTED")
                    .applyDate(LocalDate.now().minusDays(30)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(8).getJobNo()).userNo(user3.getUserNo())
                    .resumeNo(resumes.get(2).getResumeNo()).status("PENDING")
                    .applyDate(LocalDate.now().minusDays(3))
                    .message("디지털 마케팅 전문성을 활용하고 싶습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(2).getJobNo()).userNo(user4.getUserNo())
                    .resumeNo(resumes.get(3).getResumeNo()).status("ACCEPTED")
                    .applyDate(LocalDate.now().minusDays(25))
                    .message("데이터 파이프라인 구축 경험이 있습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(7).getJobNo()).userNo(user4.getUserNo())
                    .resumeNo(resumes.get(3).getResumeNo()).status("REVIEWED")
                    .applyDate(LocalDate.now().minusDays(10)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(6).getJobNo()).userNo(user1.getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("CANCELED")
                    .applyDate(LocalDate.now().minusDays(40)).build()
            ));
            log.info("[DataInitializer] 더미 데이터 삽입 완료: 사용자 7명, 채용공고 12개, 공지사항 6개, 게시판 15개(자료실3/취업뉴스5/일반7), 이력서 5개, 지원 8건");
        } catch (Exception e) {
            log.warn("[DataInitializer] 지원 내역 더미 데이터 삽입 실패 (FK 제약 문제일 수 있음, 무시하고 계속): {}", e.getMessage());
        }
    }

    @Transactional
    public void seedResourceBoards() {
        if (boardRepository.findByCategory("자료실").isEmpty()) {
            log.info("[DataInitializer] 자료실 게시글 추가...");
            boardRepository.saveAll(List.of(
                BoardEntity.builder()
                    .category("자료실").title("자소서 작성 가이드")
                    .content("■ 자기소개서 작성의 핵심 원칙\n\n자기소개서는 단순한 경험 나열이 아닌, 지원 직무에 맞는 '나'를 설득력 있게 표현하는 글입니다.\n\n━━━━━━━━━━━━━━━━━━━━━━\n1. 직무 중심으로 작성하기\n━━━━━━━━━━━━━━━━━━━━━━\n· 지원 직무의 핵심 역량을 파악한 후, 그에 맞는 경험을 선별하세요.\n· '나는 이런 사람이다' 보다 '나는 이 직무에 이런 기여를 할 수 있다'로 작성하세요.\n\n━━━━━━━━━━━━━━━━━━━━━━\n2. STAR 기법 활용하기\n━━━━━━━━━━━━━━━━━━━━━━\n· Situation: 어떤 상황이었는지\n· Task: 내가 맡은 역할/과제\n· Action: 구체적으로 무엇을 했는지\n· Result: 어떤 결과를 얻었는지 (수치화 권장)\n\n[예시]\n\"팀 내 데이터 처리 속도가 느려 배포 지연이 잦았습니다(S). 저는 쿼리 최적화 담당을 자원했고(T), 인덱스 재설계와 N+1 문제를 해결했습니다(A). 결과적으로 처리 속도가 70% 개선되어 배포 주기가 2주에서 1주로 단축되었습니다(R).\"\n\n━━━━━━━━━━━━━━━━━━━━━━\n3. 항목별 작성 팁\n━━━━━━━━━━━━━━━━━━━━━━\n[성장과정] 직무 역량 형성에 영향을 준 경험 중심으로\n[지원동기] 해당 기업/직무를 선택한 구체적 이유\n[직무역량] 포트폴리오, 수치, 수상경력으로 증명\n[입사 후 포부] 3년·5년 후 구체적인 목표 제시\n\n━━━━━━━━━━━━━━━━━━━━━━\n4. 자주 하는 실수\n━━━━━━━━━━━━━━━━━━━━━━\n· 두루뭉술한 표현: \"열심히 했습니다\" → \"주 5회 스터디를 6개월간 운영했습니다\"\n· 회사 정보 없는 지원동기: 기업 홈페이지·뉴스 참고 후 작성\n· 글자 수 초과/미달: 권장 분량의 90~100% 채우기\n· 오탈자: 제출 전 3회 이상 교정 필수")
                    .writer("admin").regdate(LocalDate.now().minusDays(10))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("자료실").title("2026 채용 설명회 자료집")
                    .content("■ 2026 PeopleJob 채용 설명회 참여 기업 안내\n\n━━━━━━━━━━━━━━━━━━━━━━\n행사 개요\n━━━━━━━━━━━━━━━━━━━━━━\n· 일시: 2026년 3월 15일 (토) 10:00 ~ 17:00\n· 장소: 서울 코엑스 D홀\n· 규모: 50개 기업, 예상 방문객 3,000명\n\n━━━━━━━━━━━━━━━━━━━━━━\n참여 기업 목록 (일부)\n━━━━━━━━━━━━━━━━━━━━━━\n[IT/개발]\n· 테크스타트(주) — 백엔드, 프론트엔드, AI 엔지니어\n· 글로벌코퍼레이션(주) — 데이터, DevOps, QA\n· 핀테크뱅크(주) — 블록체인, 결제시스템, 보안\n\n[제조/서비스]\n· 한국제조(주) — 생산관리, 품질, 영업\n· 서비스코리아(주) — CS, 마케팅, 기획\n\n[공공/금융]\n· 코리아파이낸스 — IT, 리스크관리\n· 공공기관 A — 행정, 전산\n\n━━━━━━━━━━━━━━━━━━━━━━\n프로그램 일정\n━━━━━━━━━━━━━━━━━━━━━━\n10:00 ~ 11:00  개막식 및 기조연설\n11:00 ~ 13:00  기업 부스 방문 (1라운드)\n13:00 ~ 14:00  점심 및 네트워킹\n14:00 ~ 16:00  기업 부스 방문 (2라운드)\n16:00 ~ 17:00  현직자 멘토링 세션\n\n━━━━━━━━━━━━━━━━━━━━━━\n참가자 준비물\n━━━━━━━━━━━━━━━━━━━━━━\n· 이력서 10부 이상 인쇄\n· 명함 (있는 경우)\n· 포트폴리오 USB 또는 링크\n· 복장: 비즈니스 캐주얼 이상 권장\n\n사전 등록 시 기업 면접 우선 배정 혜택이 있습니다.")
                    .writer("admin").regdate(LocalDate.now().minusDays(7))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("자료실").title("면접 준비 체크리스트")
                    .content("■ 면접 전날 ~ 당일 완벽 준비 체크리스트\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-3 ~ D-1] 내용 준비\n━━━━━━━━━━━━━━━━━━━━━━\n□ 자기소개 1분 스피치 연습 (3회 이상)\n□ 지원 기업 홈페이지·뉴스·IR 자료 숙지\n□ 자소서 내용 전체 재검토 (면접관이 물어볼 포인트 파악)\n□ 직무 관련 예상 질문 30개 작성 및 답변 준비\n□ 산업 트렌드 및 최신 이슈 파악\n□ 포트폴리오/프로젝트 핵심 내용 정리\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-1] 실전 준비\n━━━━━━━━━━━━━━━━━━━━━━\n□ 면접 장소·교통편 확인 (구글맵으로 경로 저장)\n□ 복장 점검 (다림질, 신발 닦기)\n□ 이력서·포트폴리오 출력본 준비\n□ 필기구, 메모지 챙기기\n□ 충분한 수면 (7시간 이상)\n\n━━━━━━━━━━━━━━━━━━━━━━\n[D-Day] 당일 체크\n━━━━━━━━━━━━━━━━━━━━━━\n□ 면접 시간 30분 전 도착\n□ 대기실에서 핵심 키워드 마지막 확인\n□ 핸드폰 무음 설정\n□ 입장 전 거울로 용모 최종 점검\n\n━━━━━━━━━━━━━━━━━━━━━━\n[면접 중 주의사항]\n━━━━━━━━━━━━━━━━━━━━━━\n· 질문은 끝까지 듣고 답변하기\n· 모르는 내용은 솔직히 인정하고 배울 의지 표현\n· 답변 시 두괄식 구성 (핵심 → 부연 → 마무리)\n· 눈 맞춤: 면접관 3명이라면 3명 모두에게\n· 답변 시간: 1개 질문에 1~2분 이내\n\n━━━━━━━━━━━━━━━━━━━━━━\n[자주 묻는 질문 TOP 10]\n━━━━━━━━━━━━━━━━━━━━━━\n1. 자기소개 해주세요\n2. 지원 동기가 무엇인가요?\n3. 본인의 강점·약점은?\n4. 가장 힘들었던 경험과 극복 방법은?\n5. 팀 프로젝트에서 갈등이 생겼을 때 어떻게 했나요?\n6. 5년 후 자신의 모습은?\n7. 마지막으로 하고 싶은 말은?\n8. 희망 연봉은 얼마인가요?\n9. 입사 가능 시기는 언제인가요?\n10. 다른 회사에도 지원했나요?")
                    .writer("admin").regdate(LocalDate.now().minusDays(5))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build()
            ));
        }

        if (boardRepository.findByCategory("취업뉴스").isEmpty()) {
            log.info("[DataInitializer] 취업뉴스 게시글 추가...");
            boardRepository.saveAll(List.of(
                BoardEntity.builder()
                    .category("취업뉴스").title("2026 상반기 IT 채용 트렌드 — AI·클라우드 인력 수요 급증")
                    .content("2026년 상반기 IT 업계 채용 시장은 AI·ML 엔지니어와 클라우드 아키텍처 인력에 대한 수요가 전년 대비 40% 이상 증가한 것으로 나타났습니다.\n\n■ 주요 트렌드\n\n1. AI/ML 직군 폭발적 성장\n생성형 AI 서비스 도입이 본격화되면서 LLM 파인튜닝, RAG 시스템 구축, MLOps 경험자에 대한 수요가 크게 늘었습니다. 관련 직군의 평균 연봉은 전년 대비 15% 상승했습니다.\n\n2. 클라우드 네이티브 인력 부족\nAWS, GCP, Azure 멀티클라우드 운영 경험과 Kubernetes, Terraform 기술을 갖춘 DevOps 엔지니어는 여전히 공급이 부족한 상태입니다.\n\n3. 보안 전문가 수요 증가\n사이버 보안 위협이 증가하면서 정보보안 기사, CISSP 보유자를 찾는 기업이 늘고 있습니다.\n\n■ 평균 연봉 (신입 기준)\n· 백엔드 개발자: 3,800만원\n· 프론트엔드 개발자: 3,500만원\n· AI/ML 엔지니어: 4,200만원\n· DevOps 엔지니어: 4,000만원\n\n지금이 IT 직군으로의 전환 또는 성장을 위한 최적의 시기입니다.")
                    .writer("admin").regdate(LocalDate.now().minusDays(3))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("취업뉴스").title("중소기업 청년 채용 장려금 2026년 확대 — 최대 1,200만원 지원")
                    .content("고용노동부는 중소기업의 청년 인력 채용을 촉진하기 위해 2026년부터 청년 채용 장려금을 기존 900만원에서 최대 1,200만원으로 확대한다고 발표했습니다.\n\n■ 지원 내용\n\n· 지원 대상: 5인 이상 중소기업에 청년(15~34세)을 정규직으로 신규 채용한 기업\n· 지원 금액: 1인당 최대 1,200만원 (6개월 단위 지급)\n· 지원 기간: 최대 2년\n\n■ 신청 방법\n\n1. 고용24 접속\n2. '청년 채용 장려금' 검색\n3. 기업 공인인증서로 신청\n\n■ 구직자 혜택\n\n장려금 지원 대상 기업에 취업 시, 기업 안정성이 높고 처우 개선 가능성이 큽니다.\n\n■ 유의사항\n\n· 기존 직원 대체 채용은 제외\n· 최저임금 이상 지급 필수\n· 4대 보험 가입 필수")
                    .writer("admin").regdate(LocalDate.now().minusDays(6))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("취업뉴스").title("국민취업지원제도 2026 — 구직촉진수당 월 60만원으로 인상")
                    .content("취업을 준비 중인 청년·중장년층을 위한 국민취업지원제도가 2026년부터 구직촉진수당을 월 50만원에서 60만원으로 인상합니다.\n\n■ 국민취업지원제도란?\n\n취업을 원하는 사람에게 취업지원서비스를 제공하고, 저소득 구직자에게는 생계를 위한 소득도 지원하는 제도입니다.\n\n■ 1유형 (구직촉진수당 지급)\n\n· 대상: 15~69세 중 가구 중위소득 60% 이하\n· 수당: 월 60만원 × 최대 6개월\n· 취업활동계획 수립 및 구직활동 의무 이행 필요\n\n■ 신청 방법\n\n1. 고용센터 방문 또는 국민취업지원제도 홈페이지\n2. 온라인 신청 후 취업지원사 면담\n3. 취업활동계획 수립 후 수당 지급 시작")
                    .writer("admin").regdate(LocalDate.now().minusDays(9))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("취업뉴스").title("면접 합격률 높이는 자기소개 스피치 전략 5가지")
                    .content("첫인상을 결정하는 1분 자기소개, 어떻게 하면 면접관의 마음을 사로잡을 수 있을까요?\n\n■ 전략 1: 두괄식 구성\n\n'저는 [직무 역량 핵심 키워드]를 갖춘 [이름]입니다.'로 시작하세요.\n\n■ 전략 2: 숫자로 증명하기\n\n\"열심히 했습니다\"보다 \"6개월 간 주 3회 스터디를 운영해 팀 합격률 80%를 달성했습니다\"가 훨씬 강렬합니다.\n\n■ 전략 3: 지원 직무와 연결하기\n\n자기소개의 마지막은 반드시 '지원 직무'와 연결하세요.\n\n■ 전략 4: 10번 이상 소리 내어 연습\n\n머릿속으로 외우는 것과 실제로 말하는 것은 다릅니다. 녹음해서 들어보고, 속도와 어조를 다듬으세요.\n\n■ 전략 5: 60~75초로 조절\n\n1분 자기소개라 했을 때 정확히 60~75초가 되도록 연습하세요.")
                    .writer("admin").regdate(LocalDate.now().minusDays(12))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build(),
                BoardEntity.builder()
                    .category("취업뉴스").title("2026 공공기관 채용 로드맵 — NCS 블라인드 채용 강화")
                    .content("2026년 공공기관 채용은 NCS(국가직무능력표준) 기반 블라인드 채용을 더욱 강화하는 방향으로 개편됩니다.\n\n■ 주요 변경사항\n\n1. 학력·학교명 완전 비공개\n이력서에 출신 대학명 기재 금지가 전면 시행됩니다.\n\n2. NCS 직업기초능력 평가 비중 확대\n의사소통, 문제해결, 수리, 자원관리 능력 평가 비중이 기존 40%에서 50%로 상향됩니다.\n\n3. AI 면접 도입 기관 증가\n1차 면접에 AI 면접을 도입하는 공공기관이 2025년 30개에서 2026년 70개로 확대됩니다.\n\n■ 준비 전략\n\n· NCS 직업기초능력 기출문제 꾸준히 풀기\n· 직무기술서 세밀하게 분석 후 경험 매칭\n· AI 면접 특성 파악: 눈 맞춤(카메라), 목소리 톤, 답변 구조\n\n■ 상반기 채용 일정 (예정)\n\n· 1~2월: 채용 공고 발표\n· 3~4월: 서류 접수\n· 4~5월: 필기시험\n· 5~6월: 면접 및 최종 합격")
                    .writer("admin").regdate(LocalDate.now().minusDays(14))
                    .viewCount(0).isActive(true).allowComment(false).allowUpload(false).build()
            ));
        }
    }

    @Transactional
    public void seedInquiries() {
        if (inquiryRepository.count() > 0) return;
        log.info("[DataInitializer] 문의사항 더미 데이터 삽입...");
        inquiryRepository.saveAll(List.of(
            InquiryEntity.builder()
                .title("채용공고 등록 후 노출이 안 됩니다")
                .content("기업 회원으로 채용공고를 등록했는데 일반 회원 화면에서 보이지 않습니다. 어떻게 해야 하나요?")
                .writer("company1").email("hr@techstart.co.kr").phone("02-1234-5678")
                .category("서비스이용").isAnswered(false).build(),
            InquiryEntity.builder()
                .title("이력서 파일 첨부가 안 됩니다")
                .content("이력서 작성 화면에서 PDF 파일을 첨부하려고 하는데 계속 오류가 발생합니다. 파일 크기는 2MB입니다.")
                .writer("user1").email("kim@example.com").phone("010-1111-2222")
                .category("기술문의").isAnswered(true)
                .answer("안녕하세요. 현재 PDF 첨부 기능은 최대 5MB까지 지원합니다. 브라우저 캐시를 지우고 다시 시도해 주시기 바랍니다. 문제가 지속될 경우 고객센터로 연락주세요.")
                .answerBy("admin").build(),
            InquiryEntity.builder()
                .title("비밀번호를 잊어버렸어요")
                .content("가입 시 사용한 이메일로 비밀번호 재설정 메일이 오지 않습니다. 확인 부탁드립니다.")
                .writer("user2").email("lee@example.com").phone("010-3333-4444")
                .category("계정문의").isAnswered(true)
                .answer("비밀번호 재설정 메일은 발송 후 5분 이내에 도착합니다. 스팸 메일함도 확인해 주세요. 이메일 주소가 올바른지 다시 확인 후 재시도 부탁드립니다.")
                .answerBy("admin").build(),
            InquiryEntity.builder()
                .title("지원 취소를 하고 싶습니다")
                .content("실수로 채용공고에 지원했는데 취소할 수 있나요? 마이페이지 지원 내역에서 취소 버튼이 보이지 않습니다.")
                .writer("user3").email("park@example.com").phone("010-5555-6666")
                .category("서비스이용").isAnswered(false).build(),
            InquiryEntity.builder()
                .title("기업 회원 정보 수정 방법 문의")
                .content("기업 회원가입 시 사업자등록번호를 잘못 입력했습니다. 수정이 가능한가요?")
                .writer("company2").email("recruit@globalcorp.com").phone("02-9876-5432")
                .category("계정문의").isAnswered(false).build(),
            InquiryEntity.builder()
                .title("채용공고 노출 기간 문의")
                .content("채용공고는 등록 후 최대 몇 일 동안 노출되나요? 마감일을 설정하지 않으면 어떻게 되나요?")
                .writer("company3").email("people@fintechbank.io").phone("02-5555-1234")
                .category("서비스이용").isAnswered(true)
                .answer("채용공고는 마감일까지 노출됩니다. 마감일을 설정하지 않으면 수동으로 마감 처리하기 전까지 계속 노출됩니다. 관리 > 채용공고 관리에서 언제든지 마감 처리할 수 있습니다.")
                .answerBy("admin").build()
        ));
        log.info("[DataInitializer] 문의사항 더미 데이터 삽입 완료: 6건");
    }

    @Transactional
    public void seedApplicants() {
        if (applyRepository.count() > 0) return;
        log.info("[DataInitializer] 지원 내역 더미 데이터 삽입...");

        var u1 = userRepository.findByUserid("user1");
        var u2 = userRepository.findByUserid("user2");
        var u3 = userRepository.findByUserid("user3");
        var u4 = userRepository.findByUserid("user4");
        List<ResumeEntity> resumes = resumeRepository.findAll();
        List<JobopeningEntity> jobs = jobRepository.findAll();

        if (u1.isEmpty() || u2.isEmpty() || u3.isEmpty() || u4.isEmpty()
                || jobs.size() < 9 || resumes.size() < 4) {
            log.warn("[DataInitializer] 지원 내역 더미 데이터 삽입 건너뜀: 필수 데이터 없음");
            return;
        }

        try {
            applyRepository.saveAll(List.of(
                ApplyEntity.builder()
                    .jobNo(jobs.get(0).getJobNo()).userNo(u1.get().getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("ACCEPTED")
                    .applyDate(LocalDate.now().minusDays(20))
                    .message("백엔드 개발 경험을 살려 기여하고 싶습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(1).getJobNo()).userNo(u1.get().getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("PENDING")
                    .applyDate(LocalDate.now().minusDays(5)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(4).getJobNo()).userNo(u2.get().getUserNo())
                    .resumeNo(resumes.get(1).getResumeNo()).status("REVIEWED")
                    .applyDate(LocalDate.now().minusDays(15))
                    .message("Figma 포트폴리오 첨부했습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(5).getJobNo()).userNo(u2.get().getUserNo())
                    .resumeNo(resumes.get(1).getResumeNo()).status("REJECTED")
                    .applyDate(LocalDate.now().minusDays(30)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(8).getJobNo()).userNo(u3.get().getUserNo())
                    .resumeNo(resumes.get(2).getResumeNo()).status("PENDING")
                    .applyDate(LocalDate.now().minusDays(3))
                    .message("디지털 마케팅 전문성을 활용하고 싶습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(2).getJobNo()).userNo(u4.get().getUserNo())
                    .resumeNo(resumes.get(3).getResumeNo()).status("ACCEPTED")
                    .applyDate(LocalDate.now().minusDays(25))
                    .message("데이터 파이프라인 구축 경험이 있습니다.").build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(7).getJobNo()).userNo(u4.get().getUserNo())
                    .resumeNo(resumes.get(3).getResumeNo()).status("REVIEWED")
                    .applyDate(LocalDate.now().minusDays(10)).build(),
                ApplyEntity.builder()
                    .jobNo(jobs.get(6).getJobNo()).userNo(u1.get().getUserNo())
                    .resumeNo(resumes.get(0).getResumeNo()).status("CANCELED")
                    .applyDate(LocalDate.now().minusDays(40)).build()
            ));
            log.info("[DataInitializer] 지원 내역 더미 데이터 삽입 완료: 8건");
        } catch (Exception e) {
            log.warn("[DataInitializer] 지원 내역 더미 데이터 삽입 실패 (FK 제약 문제일 수 있음, 무시하고 계속): {}", e.getMessage());
        }
    }

    private JobopeningEntity buildJob(Long userNo, String company, String title, String content,
                                      String location, String jobType, String salary, String workType,
                                      String experience, String education, LocalDate deadline) {
        return JobopeningEntity.builder()
                .userNo(userNo)
                .company(company)
                .title(title)
                .content(content)
                .location(location)
                .jobType(jobType)
                .salary(salary)
                .workType(workType)
                .experience(experience)
                .education(education)
                .deadline(deadline)
                .status(JobopeningEntity.JobStatus.DRAFT)
                .isActive(true)
                .build();
    }
}
