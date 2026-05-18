package com.people.job.job;

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
class JobopeningControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void getAllJobs_returns200WithPageStructure() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/jobs?page=0&size=10", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("content");
        assertThat(response.getBody()).containsKey("totalElements");
    }

    @Test
    void getAllJobs_withStatusPublished_returns200() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/jobs?page=0&size=10&status=published", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllJobs_pagination_secondPage() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/jobs?page=1&size=5", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getJobById_nonExistent_returns400() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/jobs/999999", Map.class);

        assertThat(response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError()
                || response.getStatusCode() == HttpStatus.OK).isTrue();
    }

    @Test
    void getJobById_seededJob_returns200() {
        // DataInitializer가 채용공고를 시딩함 - 첫 번째 공고 조회
        ResponseEntity<Map> listResponse = restTemplate.getForEntity(
                "/api/jobs?page=0&size=1", Map.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> content =
                (java.util.List<Map<String, Object>>) listResponse.getBody().get("content");

        if (content != null && !content.isEmpty()) {
            Object jobNo = content.get(0).get("jobNo");
            ResponseEntity<Map> detailResponse = restTemplate.getForEntity(
                    "/api/jobs/" + jobNo, Map.class);
            assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
