package com.ddoddii.resume.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AuthIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    private static final String email = "abc@mail.com";

    private static final String MYSQL_IMAGE = "mysql:8.0.39";
    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer(MYSQL_IMAGE);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        String name = "test";
        String encryptedPassword = "$2a$10$3";
        final Integer REMAIN_INTERVIEW = 5;
        User user = User.builder()
                .name(name)
                .email(email)
                .password(encryptedPassword)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .remainInterview(REMAIN_INTERVIEW)
                .build();
        this.userRepository.save(user);
    }

    @Test
    @WithMockUser(username = email, roles = "USER")
    void testMockUser() throws Exception{
        // given
        this.mvc.perform(get("/api/users/current-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.remainInterview").value(5))
                .andExpect(jsonPath("$.loginType").value("EMAIL"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        // when
        // then
    }
}
