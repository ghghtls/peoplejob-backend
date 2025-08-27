package com.people.job.peoplejob_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PeoplejobBackendApplicationTests {

	@Test
	void contextLoads() {
		// Spring Context가 정상적으로 로드되는지 확인
		// 이 테스트는 애플리케이션의 모든 빈이 올바르게 구성되었는지 검증합니다.
	}

	@Test
	void mainApplicationStarts() {
		// 메인 애플리케이션이 정상적으로 시작되는지 확인
		// Spring Boot의 기본 설정이 올바른지 검증합니다.
	}
}