package com.ddoddii.resume.integration;

import com.ddoddii.resume.MockOpenAiChatModelConfig;
import com.ddoddii.resume.dto.evaluation.AnswerRequestDTO;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.model.question.BehaviorQuestion;
import com.ddoddii.resume.model.question.PersonalQuestion;
import com.ddoddii.resume.model.question.TechQuestion;
import com.ddoddii.resume.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(MockOpenAiChatModelConfig.class)
public class EvalIT {

    @Value("classpath:/evaluation.example")
    private Resource exampleEval;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private TechQuestionRepository techQuestionRepository;

    @Autowired
    private BehaviorQuestionRepository behaviorQuestionRepository;
    @Autowired
    private PersonalQuestionRepository personalQuestionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpenAiChatClient chatClient;


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

    private static User savedUser;
    private static Resume savedResume;

    private Interview savedInterview;

    private  static  final String EMAIL = "d23123dnan@emailll.com";

    private  static final String POSITION = "개발자";

    @BeforeEach
    public void setUp() {
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
                .position(POSITION)
                .content("내용")
                .name("이름")
                .user(savedUser)
                .build());

        Interview interview = Interview.builder()
                .interviewRound(InterviewRound.FIRST)
                .companyId(1)
                .companyName("테스트 회사")
                .departmentId(1)
                .jobId(1)
                .resume(savedResume)
                .user(savedUser)
                .build();
        savedInterview = interviewRepository.save(interview);
    }

    @AfterEach
    public void tearDown() {
        personalQuestionRepository.deleteAll();
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 개인 질문 평가")
    @Test
    public void testEvalPersonalQuestion() throws Exception {
        PersonalQuestion pq = PersonalQuestion.builder()
                .interview(savedInterview)
                .resume(savedResume)
                .criteria("기준")
                .question("질문")
                .build();

        PersonalQuestion savedPq = personalQuestionRepository.save(pq);

        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .interviewId(savedInterview.getId())
                .questionId(savedPq.getId())
                .answer("답변")
                .build();

        String text = exampleEval.getContentAsString(Charset.defaultCharset());

        List<Generation> gens = List.of(new Generation(text));
        ChatResponse chatResponse = new ChatResponse(gens);
        Mockito.when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);

        mockMvc.perform(post("/api/answer/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].criteria").isString());
    }

}
