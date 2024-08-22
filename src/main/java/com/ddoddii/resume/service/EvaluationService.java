package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.evaluation.AnswerRequestDTO;
import com.ddoddii.resume.model.Evaluation;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.eunm.QuestionType;
import com.ddoddii.resume.model.question.BehaviorQuestion;
import com.ddoddii.resume.model.question.IntroduceQuestion;
import com.ddoddii.resume.model.question.PersonalQuestion;
import com.ddoddii.resume.model.question.TechQuestion;
import com.ddoddii.resume.repository.BehaviorQuestionRepository;
import com.ddoddii.resume.repository.EvaluationRepository;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.IntroduceQuestionRepository;
import com.ddoddii.resume.repository.PersonalQuestionRepository;
import com.ddoddii.resume.repository.TechQuestionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class EvaluationService {

    @Value("classpath:/prompts/system.st")
    private Resource systemResource;

    @Value("classpath:/prompts/perQ-eval-user.st")
    private Resource perQEvalUserResource;

    @Value("classpath:/prompts/techQ-eval-user.st")
    private Resource techQEvalUserResource;

    @Value("classpath:/prompts/introduceQ-eval-user.st")
    private Resource introduceQEvalUserResource;

    @Value("classpath:/prompts/behavQ-eval-user.st")
    private Resource behavQEvalUserResource;

    private final InterviewRepository interviewRepository;
    private final PersonalQuestionRepository personalQuestionRepository;
    private final TechQuestionRepository techQuestionRepository;
    private final IntroduceQuestionRepository introduceQuestionRepository;
    private final BehaviorQuestionRepository behaviorQuestionRepository;
    private final EvaluationRepository evaluationRepository;
    private final OpenAiChatClient chatClient;

    public String evaluatePerQAnswer(AnswerRequestDTO answerRequestDTO) {
        Interview currentInterview = interviewRepository.findInterviewById(answerRequestDTO.getInterviewId());
        PersonalQuestion personalQuestion = personalQuestionRepository.findPersonalQuestionById(
                answerRequestDTO.getQuestionId());
        String criteria = personalQuestion.getCriteria();
        String question = personalQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generatePersonalEvaluationPrompt(question, answer, criteria);
        ChatResponse response = chatClient.call(prompt);
        String gptEvaluation = response.getResult().getOutput().getContent();
        saveEvaluation(currentInterview, answerRequestDTO.getQuestionId(), QuestionType.PERSONAL, question, answer,
                gptEvaluation);

        return gptEvaluation;
    }

    public String evaluateTechQAnswer(AnswerRequestDTO answerRequestDTO) {
        Interview currentInterview = interviewRepository.findInterviewById(answerRequestDTO.getInterviewId());
        TechQuestion techQuestion = techQuestionRepository.findTechQuestionById(answerRequestDTO.getQuestionId());
        String exampleAnswer = techQuestion.getExampleAnswer();
        String question = techQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generateTechEvaluationPrompt(question, exampleAnswer, answer);
        ChatResponse response = chatClient.call(prompt);
        String gptEvaluation = response.getResult().getOutput().getContent();

        saveEvaluation(currentInterview, answerRequestDTO.getQuestionId(), QuestionType.PERSONAL, question, answer,
                gptEvaluation);

        return gptEvaluation;
    }

    public String evaluateIntroduceQAnswer(AnswerRequestDTO answerRequestDTO) {
        Interview currentInterview = interviewRepository.findInterviewById(answerRequestDTO.getInterviewId());
        IntroduceQuestion introduceQuestion = introduceQuestionRepository.findIntroduceQuestionById(
                answerRequestDTO.getQuestionId());
        String criteria = introduceQuestion.getCriteria();
        String question = introduceQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generateIntroduceEvaluationPrompt(question, answer, criteria);
        ChatResponse response = chatClient.call(prompt);
        String gptEvaluation = response.getResult().getOutput().getContent();

        saveEvaluation(currentInterview, answerRequestDTO.getQuestionId(), QuestionType.PERSONAL, question, answer,
                gptEvaluation);

        return gptEvaluation;
    }

    public String evaluateBehaviorQAnswer(AnswerRequestDTO answerRequestDTO) {
        Interview currentInterview = interviewRepository.findInterviewById(answerRequestDTO.getInterviewId());
        BehaviorQuestion behaviorQuestion = behaviorQuestionRepository.findBehaviorQuestionById(
                answerRequestDTO.getQuestionId());
        String criteria = behaviorQuestion.getCriteria();
        String question = behaviorQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generateBehavEvaluationPrompt(question, answer, criteria);
        ChatResponse response = chatClient.call(prompt);
        String gptEvaluation = response.getResult().getOutput().getContent();

        saveEvaluation(currentInterview, answerRequestDTO.getQuestionId(), QuestionType.PERSONAL, question, answer,
                gptEvaluation);

        return gptEvaluation;
    }

    private void saveEvaluation(Interview currentInterview, long questionId, QuestionType questionType,
                                String askedQuestion,
                                String userAnswer, String gptEvaluation) {
        Evaluation evaluation = Evaluation.builder()
                .interview(currentInterview)
                .questionId(questionId)
                .questionType(questionType)
                .askedQuestion(askedQuestion)
                .gptEvaluation(gptEvaluation)
                .userAnswer(userAnswer)
                .build();
        evaluationRepository.save(evaluation);
    }

    private Prompt generatePersonalEvaluationPrompt(String question, String answer, String criteria) {
        try {
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
            Message systemMessage = systemPromptTemplate.createMessage();

            PromptTemplate userPromptTemplate = new PromptTemplate(perQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "criteria", criteria, "answer", answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

    private Prompt generateTechEvaluationPrompt(String question, String exampleAnswer, String answer) {
        try {
            // Log system prompt template content
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
            Message systemMessage = systemPromptTemplate.createMessage();

            PromptTemplate userPromptTemplate = new PromptTemplate(techQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "example", exampleAnswer, "answer",
                    answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

    private Prompt generateIntroduceEvaluationPrompt(String question, String answer, String criteria) {
        try {
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
            Message systemMessage = systemPromptTemplate.createMessage();

            PromptTemplate userPromptTemplate = new PromptTemplate(introduceQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "criteria", criteria, "answer", answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

    private Prompt generateBehavEvaluationPrompt(String question, String answer, String criteria) {
        try {
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
            Message systemMessage = systemPromptTemplate.createMessage();

            PromptTemplate userPromptTemplate = new PromptTemplate(behavQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "criteria", criteria, "answer", answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

}
