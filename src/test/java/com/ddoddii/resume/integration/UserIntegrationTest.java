package com.ddoddii.resume.integration;

import com.ddoddii.resume.dto.user.*;
import com.ddoddii.resume.repository.RefreshTokenRepository;
import com.ddoddii.resume.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UserIntegrationTest{

    private static final String MYSQL_IMAGE = "mysql:8.0.39";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String email = "abc@email.com";
    private String name = "abc1234";
    private final String password = "password1234";

    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer(MYSQL_IMAGE);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    @AfterEach
    void tearDown(){
        // 독립된 테스트를 보장하기 위해서 생성한 테이블을 삭제한다.
        // 이 클래스틑 통합 테스트으로 서버와 클라이언트가 각각 독립된 프로세스로 돌아간다. (web layer만 모킹하는 테스트와 다름)
        // 따라서 @Transactional을 사용할 수 없기 때문에 테스트마다 삭제 로직을 추가한다.
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("성공: 회원가입")
    @Test
    void signUpTest() {
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(email, response.getBody().getUser().getEmail());
        assertEquals(name, response.getBody().getUser().getName());
        assertEquals("Bearer", response.getBody().getToken().getGrantType());
        assertNotNull(response.getBody().getToken().getAccessToken());
        assertNotNull(response.getBody().getToken().getRefreshToken());
    }

    @DisplayName("실패: 이미 존재하는 이메일")
    @Test
    void signUpDuplicateEmailTest() {

        String duplicateEmail = "dup@email.com";
        String duplicateName = "dup1234";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(duplicateEmail)
                .name(duplicateName)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 200);

        ResponseEntity<UserAuthResponseDTO> response2 = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response2.getStatusCode().value(), 409);
    }

    @DisplayName("실패: 올바르지 않은 이메일 형식")
    @Test
    void signUpInvalidEmailTest() {

        String invalidEmail = "invalidemail.com";
        String invalidName = "invalid1234";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(invalidEmail)
                .name(invalidName)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 400);
    }

    @DisplayName("실패: 이메일 입력 안됨")
    @Test
    void signUpEmptyEmailTest() {

        String invalidEmail = "";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(invalidEmail)
                .name(name)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 400);
    }

    @DisplayName("실패: 비밀번호 입력 안됨")
    @Test
    void signUpEmptyPasswordTest() {

        String emptyPassword = "";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(emptyPassword).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 400);
    }

    @DisplayName("성공: 로그인")
    @Test
    void loginTest() {

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 200);

        UserEmailSignUpRequestDTO request = UserEmailSignUpRequestDTO.builder().email(email).password(password).build();

        ResponseEntity<UserAuthResponseDTO> response2 = this.testRestTemplate.postForEntity("/api/users/email-login", request, UserAuthResponseDTO.class);
        assertEquals(response2.getStatusCode().value(), 200);
        assertEquals(response2.getBody().getUser().getEmail(), email);
        assertEquals(response2.getBody().getUser().getName(), name);
        assertEquals(response2.getBody().getToken().getGrantType(), "Bearer");

        assertNotNull(response2.getBody().getToken().getAccessToken());
        assertNotNull(response2.getBody().getToken().getRefreshToken());
    }

    @DisplayName("실패: 로그인 존재하지 않은 이메일")
    @Test
    void loginNotExistEmailTest() {

        UserEmailSignUpRequestDTO request = UserEmailSignUpRequestDTO.builder().email("notexistemail.com").password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/email-login", request, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 400);
    }
    // TODO: 비밀번호가 틀린 경우 테스트 코드 작성

    @DisplayName("성공: 이메일 중복 체크")
    @Test
    void duplicateEmailTest() {

        String duplicateEmail = "duplicatedEmail@email.com";
        String duplicateName = "duplicated1234";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(duplicateEmail)
                .name(duplicateName)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);

        assertEquals(response.getStatusCode().value(), 200);

        DuplicateEmailRequestDTO duplicateEmailRequestDTO = DuplicateEmailRequestDTO.builder().email(duplicateEmail).build();
        ResponseEntity<Boolean> response2 = this.testRestTemplate.postForEntity("/api/users/duplicate-email", duplicateEmailRequestDTO, Boolean.class);

        assertEquals(response2.getStatusCode().value(), 200);
        assertTrue(response2.getBody().booleanValue());
    }

    @DisplayName("성공: 리프레시 토큰")
    @Test
    void refreshTokenTest(){

        email = "refreshTest@email.com";
        name = "refreshTest1234";

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);
        assertEquals(response.getStatusCode().value(), 200);

        String refreshToken = response.getBody().getToken().getRefreshToken();
        RefreshTokenRequestDTO request2 = new RefreshTokenRequestDTO(refreshToken);

        ResponseEntity<JwtTokenDTO> response2 = this.testRestTemplate.postForEntity("/api/users/refresh-token", request2, JwtTokenDTO.class);
        assertEquals(response2.getStatusCode().value(), 200);
        assertEquals("Bearer", response2.getBody().getGrantType());
        assertNotNull(response2.getBody().getAccessToken());
        assertNotNull(response2.getBody().getRefreshToken());
    }

    @DisplayName("실패: 존재하지 않는 리프레시 토큰")
    @Test
    void refreshTokenNotExistTokenTest()  {

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("notexistrefreshtoken");

        ResponseEntity<JwtTokenDTO> response = this.testRestTemplate.postForEntity("/api/users/refresh-token", request, JwtTokenDTO.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @DisplayName("성공: 현재 존재하는 유저 정보")
    @Test
    void getCurrentUserTest()  {

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password).build();

        ResponseEntity<UserAuthResponseDTO> response = this.testRestTemplate.postForEntity("/api/users/", userEmailSignUpRequestDTO, UserAuthResponseDTO.class);

        // access token 실행하기
        this.testRestTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders().add("Authorization", "Bearer " + response.getBody().getToken().getAccessToken());
                    return execution.execute(request, body);
                }
        ));

        ResponseEntity<UserDTO> response2 = this.testRestTemplate.getForEntity("/api/users/current-user", UserDTO.class);
        assertEquals(response2.getStatusCode().value(), 200);
        assertEquals(response2.getBody().getEmail(), email);
        assertEquals(response2.getBody().getName(), name);
    }

}
