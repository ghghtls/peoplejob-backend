package com.people.job.user;

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
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void register_newUser_returns200() {
        UserDTO dto = UserDTO.builder()
                .userid("ci_user_" + System.currentTimeMillis())
                .password("Test1234!")
                .email("ci_test@example.com")
                .username("CI테스터")
                .userType("INDIVIDUAL")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users/register", dto, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_thenLogin_success() {
        String userid = "ci_login_" + System.currentTimeMillis();
        String password = "Test1234!";

        UserDTO registerDto = UserDTO.builder()
                .userid(userid)
                .password(password)
                .email("ci_login@example.com")
                .username("CI로그인테스터")
                .userType("INDIVIDUAL")
                .build();
        restTemplate.postForEntity("/api/users/register", registerDto, Map.class);

        UserDTO loginDto = UserDTO.builder()
                .userid(userid)
                .password(password)
                .build();
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users/login", loginDto, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
    }

    @Test
    void login_wrongPassword_returns400() {
        UserDTO dto = UserDTO.builder()
                .userid("nonexistent_user_xyz")
                .password("wrongpassword")
                .build();

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users/login", dto, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void checkUserid_nonExistent_returnsAvailable() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/users/check/absolutely_nonexistent_user_xyz_999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("available", true);
    }

    @Test
    void checkUserid_existing_returnsNotAvailable() {
        String userid = "ci_check_" + System.currentTimeMillis();
        UserDTO dto = UserDTO.builder()
                .userid(userid)
                .password("Test1234!")
                .email("ci_check@example.com")
                .username("중복체크테스터")
                .userType("INDIVIDUAL")
                .build();
        restTemplate.postForEntity("/api/users/register", dto, Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/users/check/" + userid, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("available", false);
    }
}
