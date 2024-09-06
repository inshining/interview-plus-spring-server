package com.ddoddii.resume.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.ddoddii.resume.dto.ResumeDTO;
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.repository.ResumeRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class ResumeIT {
    private  static final String MYSQL_IMAGE = "mysql:8.0.39";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Container
    static JdbcDatabaseContainer MY_SQL_CONTAINER = new MySQLContainer(MYSQL_IMAGE)
            .withDatabaseName("resume-plus");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private User user;

    private static final String EMAIL = "d23123dnan@emailll.com";

    @BeforeEach
    void setUp(){
        user = User.builder()
                .email(EMAIL)
                .id(1L)
                .name("abc")
                .password("1234")
                .role(RoleType.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    @DisplayName("성공: 이력서 리스팅")
    @WithMockUser(username = EMAIL)
    @Test
    void testResume() throws Exception{
        Resume r1 = Resume.builder()
                .content("content")
                .position("position")
                .user(user)
                .build();

        Resume r2 = Resume.builder()
                .content("content2")
                .position("position2")
                .user(user)
                .build();

        resumeRepository.save(r1);
        resumeRepository.save(r2);

        this.mockMvc.perform(get("/api/resume/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("content"))
                .andExpect(jsonPath("$[0].position").value("position"))
                .andExpect(jsonPath("$[1].content").value("content2"))
                .andExpect(jsonPath("$[1].position").value("position2"));
    }

    @DisplayName("성공: 이력서 업로드")
    @WithMockUser(username = EMAIL)
    @Test
    void testUploadResume() throws Exception{

        User realUser = userRepository.findByEmail(EMAIL).get();

        String content = objectMapper.writeValueAsString(ResumeDTO.builder()
                .content("content")
                .position("position")
                .build());
        this.mockMvc.perform(post("/api/resume/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(realUser.getId()))
                .andExpect(jsonPath("$.resumeId").exists());
    }

    @DisplayName("성공: 이력서 수정")
    @WithMockUser(username = EMAIL)
    @Test
    void testModifyResume() throws Exception{
        Resume resume = Resume.builder()
                .content("content")
                .position("position")
                .user(user)
                .build();

        resumeRepository.save(resume);

        String content = objectMapper.writeValueAsString(ResumeDTO.builder()
                .content("content2")
                .position("position2")
                .build());

        this.mockMvc.perform(patch("/api/resume/"+resume.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Resume Updated"));
    }

    @DisplayName("성공: 이력서 삭제")
    @WithMockUser(username = EMAIL)
    @Test
    void testDeleteResume() throws Exception{
        Resume resume = Resume.builder()
                .content("content")
                .position("position")
                .user(user)
                .build();

        resumeRepository.save(resume);

        this.mockMvc.perform(delete("/api/resume/"+resume.getId()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").value("Resume Deleted"));
    }

}
