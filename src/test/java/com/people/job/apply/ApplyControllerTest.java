package com.people.job.apply;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.user.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplyControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void checkApply_nonExistentIds_returnsFalse() {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                "/api/apply/check?jobNo=99999&userNo=99999", Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
    }

    @Test
    void applyToJob_newApplication_returns200() {
        // 새 유저 등록
        String userid = "apply_test_" + System.currentTimeMillis();
        UserDTO userDto = UserDTO.builder()
                .userid(userid)
                .password("Test1234!")
                .email("apply_test@example.com")
                .username("지원테스터")
                .userType("INDIVIDUAL")
                .build();
        ResponseEntity<Map> registerResp = restTemplate.postForEntity(
                "/api/users/register", userDto, Map.class);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 지원 (시딩된 데이터 jobNo=1, resumeNo=99999 사용)
        long uniqueResumeNo = System.currentTimeMillis(); // 중복 방지용 unique resumeNo
        ApplyDTO applyDto = ApplyDTO.builder()
                .jobNo(1L)
                .userNo(99999L)
                .resumeNo(uniqueResumeNo)
                .message("CI 테스트 지원입니다.")
                .build();

        ResponseEntity<String> applyResp = restTemplate.postForEntity(
                "/api/apply", applyDto, String.class);

        assertThat(applyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void applyFlow_apply_thenCheckApply_returnsTrue() {
        long jobNo = 1L;
        long userNo = 88888L;
        long resumeNo = System.currentTimeMillis(); // 매번 고유값

        // 지원
        ApplyDTO applyDto = ApplyDTO.builder()
                .jobNo(jobNo)
                .userNo(userNo)
                .resumeNo(resumeNo)
                .status("PENDING")
                .build();
        ResponseEntity<String> applyResp = restTemplate.postForEntity(
                "/api/apply", applyDto, String.class);
        assertThat(applyResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 지원 여부 확인
        ResponseEntity<Boolean> checkResp = restTemplate.getForEntity(
                "/api/apply/check?jobNo=" + jobNo + "&userNo=" + userNo, Boolean.class);
        assertThat(checkResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkResp.getBody()).isTrue();
    }

    @Test
    void applyDuplicate_secondApply_returns400() {
        long jobNo = 2L;
        long resumeNo = System.currentTimeMillis();

        ApplyDTO first = ApplyDTO.builder()
                .jobNo(jobNo)
                .userNo(77777L)
                .resumeNo(resumeNo)
                .build();
        restTemplate.postForEntity("/api/apply", first, String.class);

        // 같은 resumeNo + jobNo로 재지원 → 이미 지원한 공고 예외
        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/apply", first, String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
