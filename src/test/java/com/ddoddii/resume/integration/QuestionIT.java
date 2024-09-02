package com.ddoddii.resume.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.ddoddii.resume.MockOpenAiChatModelConfig;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.model.eunm.RoleType;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(MockOpenAiChatModelConfig.class)
public class QuestionIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private TechQuestionRepository techQuestionRepository;

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
    @DisplayName("성공: 개인 질문 생성")
    @Test
    public void testGeneratePersonalQuestion() throws Exception {
        String text = "[\n" +
                "  {\n" +
                "    \"question\": \"최근 프로젝트에서 맡았던 역할과 주요 성과에 대해 설명해주세요t123.\",\n" +
                "    \"criteria\": [\n" +
                "      \"책임감: 프로젝트에서 맡은 역할과 그에 대한 책임감을 명확히 설명하는지\",\n" +
                "      \"성과: 구체적인 성과를 수치나 예시로 설명하는지\",\n" +
                "      \"협업: 팀원들과의 협업 과정과 그 중요성을 인식하고 있는지\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"소프트웨어 개발 과정에서 직면한 가장 큰 어려움은 무엇이었고, 이를 어떻게 해결했는지 말씀해주세요.\",\n" +
                "    \"criteria\": [\n" +
                "      \"문제 해결 능력: 문제를 명확히 인식하고 해결하기 위한 전략을 설명하는지\",\n" +
                "      \"기술적 스킬: 문제 해결에 사용된 기술적 방법이나 도구를 설명하는지\",\n" +
                "      \"창의성: 창의적인 접근법이나 비전통적인 해결책을 제시하는지\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"지원하신 직무에 필요한 기술 스택에 대해 설명해주세요. 그리고 왜 그것들이 중요한지 말씀해주세요.\",\n" +
                "    \"criteria\": [\n" +
                "      \"기술적 이해: 필요한 기술 스택에 대한 깊은 이해를 가지고 있는지\",\n" +
                "      \"적용 능력: 해당 기술을 실제 프로젝트에 어떻게 적용했는지 설명하는지\",\n" +
                "      \"중요성 인식: 각 기술 스택이 왜 중요한지, 그 이유를 명확히 설명하는지\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"이전 직무에서 협업 도구를 활용하여 팀과 소통한 경험에 대해 설명해주세요.\",\n" +
                "    \"criteria\": [\n" +
                "      \"도구 활용 능력: 사용한 협업 도구에 대한 이해와 활용 능력을 설명하는지\",\n" +
                "      \"효과적인 소통: 도구를 통해 팀과의 소통이 어떻게 효과적으로 이루어졌는지 설명하는지\",\n" +
                "      \"팀워크: 협업 도구가 팀워크 향상에 어떤 기여를 했는지 설명하는지\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"프로젝트 관리 경험에 대해 말씀해주세요. 특히, 프로젝트 계획, 일정 관리, 자원 배분 등에 대해 구체적으로 설명해주세요.\",\n" +
                "    \"criteria\": [\n" +
                "      \"계획 능력: 프로젝트 계획을 세우는 과정과 방법을 명확히 설명하는지\",\n" +
                "      \"일정 관리: 일정 관리의 중요성을 인식하고, 이를 효율적으로 관리한 경험을 설명하는지\",\n" +
                "      \"자원 배분: 프로젝트 자원을 효율적으로 배분하고 관리한 경험이 있는지 설명하는지\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        JsonNode rootNode = objectMapper.readTree(text);

        List<String> questions = new ArrayList<>();

        for (JsonNode questionNode : rootNode) {
            String question = questionNode.get("question").asText();
            questions.add(question);
        }


        List<Generation> gens = List.of(new Generation(text));

        ChatResponse chatResponse = new ChatResponse(gens);
        Mockito.when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);

        mockMvc.perform(get("/api/question/personal/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").value(questions.get(0)))
                .andExpect(jsonPath("$[1].question").value(questions.get(1)))
                .andExpect(jsonPath("$[2].question").value(questions.get(2)))
                .andExpect(jsonPath("$[3].question").value(questions.get(3)))
                .andExpect(jsonPath("$[4].question").value(questions.get(4)));
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("실패: 개인 질문 생성 - 이미 개인 질문 존재하는 경우")
    @Test
    void testGeneratePersonalQuestionWithExist() throws Exception {

        PersonalQuestion pq1 = PersonalQuestion.builder().interview(savedInterview).resume(savedResume).build();
        personalQuestionRepository.save(pq1);

        mockMvc.perform(get("/api/question/personal/1"))
                .andExpect(status().isConflict());
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 기술 질문 생성")
    @Test
    void testTechQuestion() throws Exception{
        TechQuestion q1 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q1.setQuestion("질문1");
        TechQuestion q2 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q2.setQuestion("질문2");
        TechQuestion q3 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q3.setQuestion("질문3");
        techQuestionRepository.save(q1);
        techQuestionRepository.save(q2);
        techQuestionRepository.save(q3);

        this.mockMvc.perform(get("/api/question/tech/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").exists())
                .andExpect(jsonPath("$[1].question").exists())
                .andExpect(jsonPath("$[2].question").exists());
    }

    @WithMockUser(username = EMAIL)
    @DisplayName("성공: 행동 질문 생성")
    @Test
    void testBehaviorQuestion() throws Exception{
        TechQuestion q1 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q1.setQuestion("질문1");
        TechQuestion q2 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q2.setQuestion("질문2");
        TechQuestion q3 = TechQuestion.builder()
                .position(POSITION)
                .build();
        q3.setQuestion("질문3");
        techQuestionRepository.save(q1);
        techQuestionRepository.save(q2);
        techQuestionRepository.save(q3);

        this.mockMvc.perform(get("/api/question/behavior/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").exists())
                .andExpect(jsonPath("$[1].question").exists())
                .andExpect(jsonPath("$[2].question").exists());
    }

}
