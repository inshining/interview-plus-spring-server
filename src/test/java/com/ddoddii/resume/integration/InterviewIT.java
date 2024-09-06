package com.ddoddii.resume.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.ddoddii.resume.dto.interview.InterviewStartRequestDTO;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.ResumeRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class InterviewIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    private User savedUser;

    private Resume savedResume;

    private  static final String MYSQL_IMAGE = "mysql:8.0.39";

    @Container
    static JdbcDatabaseContainer MY_SQL_CONTAINER = new MySQLContainer(MYSQL_IMAGE)
            .withDatabaseName("resume-plus");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    private  static  final String EMAIL = "d23123dnan@emailll.com";

    @BeforeEach
    void setUp(){
        User user = User.builder()
                .email(EMAIL)
                .id(1L)
                .name("abc")
                .password("1234")
                .role(RoleType.ROLE_USER)
                .build();

        savedUser = userRepository.save(user);

        // given
        savedResume = resumeRepository.save(Resume.builder()
                .position("개발자")
                .content("내용")
                .name("이름")
                .user(savedUser)
                .build());
    }

    @AfterEach
    void cleanUp(){
        interviewRepository.deleteAll();
        resumeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 인터뷰 시작")
    @Test
    void startInterview() throws Exception{
        InterviewStartRequestDTO interviewStartRequestDTO = InterviewStartRequestDTO.builder()
                .interviewRound("1차 면접")
                .companyId(1)
                .companyName("테스트 회사")
                .departmendId(1)
                .jobId(1)
                .resumeId(savedResume.getId())
                .build();

        this.mockMvc.perform(post("/api/interview/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(interviewStartRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviewId").exists());
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 인터뷰 결과 조회")
    @Test
    void getInterviewResults() throws Exception{
        Interview interview1 = Interview.builder()
                .interviewRound(InterviewRound.FIRST)
                .companyId(1)
                .companyName("테스트 회사")
                .departmentId(1)
                .jobId(1)
                .resume(savedResume)
                .user(savedUser)
                .build();

        Interview interview2 = Interview.builder()
                .interviewRound(InterviewRound.FIRST)
                .companyId(1)
                .companyName("테스트 회사")
                .departmentId(1)
                .jobId(1)
                .resume(savedResume)
                .user(savedUser)
                .build();

        interviewRepository.save(interview1);
        interviewRepository.save(interview2);

        this.mockMvc.perform(get("/api/interview/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].interviewId").exists())
                .andExpect(jsonPath("$[1].interviewId").exists())
                .andExpect(jsonPath("$[2]").doesNotExist())
                .andExpect(jsonPath("$[0].companyName").value("테스트 회사"))
                .andExpect(jsonPath("$[1].companyName").value("테스트 회사"))
                .andExpect(jsonPath("$[0].departmentId").value(1))
                .andExpect(jsonPath("$[1].departmentId").value(1))
                .andExpect(jsonPath("$[0].jobId").value(1))
                .andExpect(jsonPath("$[1].jobId").value(1));
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 인터뷰 삭제")
    @Test
    void deleteInterview() throws Exception{
        Interview interview = Interview.builder()
                .interviewRound(InterviewRound.FIRST)
                .companyId(1)
                .companyName("테스트 회사")
                .departmentId(1)
                .jobId(1)
                .resume(savedResume)
                .user(savedUser)
                .build();

        interviewRepository.save(interview);

        this.mockMvc.perform(delete("/api/interview/" + interview.getId()))
                .andExpect(status().isNoContent());

        List<Interview> interviews = interviewRepository.findAll();
        Assertions.assertEquals(0, interviews.size());
    }

}
