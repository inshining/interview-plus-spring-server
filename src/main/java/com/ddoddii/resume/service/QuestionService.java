package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.question.CommonQuestionDTO;
import com.ddoddii.resume.dto.question.PersonalQuestionDTO;
import com.ddoddii.resume.error.errorcode.ResumeErrorCode;
import com.ddoddii.resume.error.exception.JsonParseException;
import com.ddoddii.resume.error.exception.NotExistResumeException;
import com.ddoddii.resume.error.exception.NotResumeOwnerException;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.question.BaseQuestionEntity;
import com.ddoddii.resume.model.question.BehaviorQuestion;
import com.ddoddii.resume.model.question.IntroduceQuestion;
import com.ddoddii.resume.model.question.PersonalQuestion;
import com.ddoddii.resume.model.question.TechQuestion;
import com.ddoddii.resume.repository.BehaviorQuestionRepository;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.IntroduceQuestionRepository;
import com.ddoddii.resume.repository.PersonalQuestionRepository;
import com.ddoddii.resume.repository.ResumeRepository;
import com.ddoddii.resume.repository.TechQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-openai.yaml")
@Transactional
@Slf4j
public class QuestionService {
    @Value("classpath:/prompts/perQ-gen-system.st")
    private Resource perQGenSystemResource;

    @Value("classpath:/prompts/perQ-gen-user.st")
    private Resource perQGenUserResource;


    private final OpenAiChatClient chatClient;
    private final UserService userService;
    private final PersonalQuestionRepository personalQuestionRepository;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final TechQuestionRepository techQuestionRepository;
    private final IntroduceQuestionRepository introduceQuestionRepository;
    private final BehaviorQuestionRepository behaviorQuestionRepository;
    private final ObjectMapper objectMapper;

    //개인 질문 생성
    public List<PersonalQuestionDTO> generatePersonalQuestion(long interviewId) {
        Interview interview = interviewRepository.findInterviewById(interviewId);
        Resume resume = checkResumeOwner(interview.getResume().getId());
        String position = resume.getPosition();
        String resumeContent = resume.getContent();
        Prompt prompt = generatePrompt(position, resumeContent);
        ChatResponse response = chatClient.call(prompt);
        List<PersonalQuestionDTO> personalQuestionDTOS = parseQuestions(response);

        for (PersonalQuestionDTO personalQuestionDTO : personalQuestionDTOS) {
            PersonalQuestion savedPersonalQuestion = savePersonalQuestion(personalQuestionDTO, resume, interview);
            personalQuestionDTO.setQuestionId(savedPersonalQuestion.getId());
        }

        return personalQuestionDTOS;
    }

    // 기술 질문 가져오기
    public List<CommonQuestionDTO> getTechQuestion(long interviewId, int questionAmount) {
        Interview interview = interviewRepository.findInterviewById(interviewId);
        Resume resume = checkResumeOwner(interview.getResume().getId());
        String position = resume.getPosition();

        List<TechQuestion> techQuestions = techQuestionRepository.findTechQuestionByPosition(position);
        if (techQuestions.isEmpty()) {
            techQuestions = techQuestionRepository.findAll();
        }

        Collections.shuffle(techQuestions);

        // Select the first 'questionAmount' questions
        List<TechQuestion> selectedQuestions = techQuestions.subList(0, questionAmount);

        // Convert to DTOs
        return selectedQuestions.stream()
                .map(this::convertCommonQuestionToDTO)
                .collect(Collectors.toList());
    }

    // 자기소개 질문 가져오기
    public List<CommonQuestionDTO> getIntroduceQuestion(long interviewId) {
        Interview interview = interviewRepository.findInterviewById(interviewId);
        //Resume resume = checkResumeOwner(interview.getResume().getId());
        List<IntroduceQuestion> introduceQuestions = introduceQuestionRepository.findAll();

        return introduceQuestions.stream()
                .map(this::convertCommonQuestionToDTO)
                .collect(Collectors.toList());
    }

    // 인성 질문 가져오기
    public List<CommonQuestionDTO> getBehaviorQuestion(long interviewId, int questionAmount) {
        //Interview interview = interviewRepository.findInterviewById(interviewId);
        List<BehaviorQuestion> behaviorQuestions = behaviorQuestionRepository.findAll();
        Collections.shuffle(behaviorQuestions);

        List<BehaviorQuestion> selectedQuestions = behaviorQuestions.subList(0, questionAmount);

        return selectedQuestions.stream()
                .map(this::convertCommonQuestionToDTO)
                .collect(Collectors.toList());
    }

    // 레쥬메 권한 확인
    private Resume checkResumeOwner(long resumeId) {
        User currentUser = userService.getCurrentUser();
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new NotExistResumeException(
                ResumeErrorCode.NOT_EXIST_RESUME));
        if (resume.getUser() != currentUser) {
            throw new NotResumeOwnerException(ResumeErrorCode.NOT_RESUME_OWNER);
        }
        return resume;
    }

    // 포지션, 레쥬메 기반으로 프롬프트 생성
    private Prompt generatePrompt(String position, String resumeContent) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(perQGenSystemResource);
        Message systemMessage = systemPromptTemplate.createMessage();
        PromptTemplate userPromptTemplate = new PromptTemplate(perQGenUserResource);
        Message userMessage = userPromptTemplate.createMessage(
                Map.of("position", position, "resume", resumeContent));
        return new Prompt(List.of(userMessage, systemMessage));
    }

    // String 에서 Json 형태로 파싱
    private List<PersonalQuestionDTO> parseQuestions(ChatResponse response) {
        String jsonResponse = response.getResult().getOutput().getContent();
        List<PersonalQuestionDTO> questionList = new ArrayList<>();
        List<String> criteria = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            for (JsonNode questionNode : rootNode) {
                String question = questionNode.get("question").asText();
                log.info("parsed question :" + question);
                for (JsonNode criteriaNode : questionNode.get("criteria")) {
                    criteria.add(criteriaNode.asText());
                }
                questionList.add(PersonalQuestionDTO.builder().question(question).criteria(criteria).build());
            }
        } catch (IOException | JsonParseException | JsonProcessingException e) {
            return questionList;
        }
        return questionList;
    }

    // 개인 질문 데이터베이스에 저장
    private PersonalQuestion savePersonalQuestion(PersonalQuestionDTO personalQuestionDTO, Resume resume,
                                                  Interview interview) {
        String criterias = StringUtils.join(personalQuestionDTO.getCriteria(), ", ");
        PersonalQuestion personalQuestion = PersonalQuestion.builder()
                .resume(resume)
                .question(personalQuestionDTO.getQuestion())
                .criteria(criterias)
                .interview(interview)
                .build();
        return personalQuestionRepository.save(personalQuestion);
    }

    private CommonQuestionDTO convertCommonQuestionToDTO(BaseQuestionEntity questionEntity) {
        // Implement your conversion logic here
        return CommonQuestionDTO.builder()
                .questionId(questionEntity.getId())
                .question(questionEntity.getQuestion())
                .build();
    }


}
