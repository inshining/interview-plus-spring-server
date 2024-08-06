package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.evaluation.AnswerRequestDTO;
import com.ddoddii.resume.model.question.PersonalQuestion;
import com.ddoddii.resume.model.question.TechQuestion;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.PersonalQuestionRepository;
import com.ddoddii.resume.repository.TechQuestionRepository;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private Resource perQEvalSystemResource;

    @Value("classpath:/prompts/perQ-eval-user.st")
    private Resource perQEvalUserResource;

    @Value("classpath:/prompts/techQ-eval-user.st")
    private Resource techQEvalUserResource;

    private final InterviewRepository interviewRepository;
    private final PersonalQuestionRepository personalQuestionRepository;
    private final TechQuestionRepository techQuestionRepository;
    private final OpenAiChatClient chatClient;

    public String evaluatePerQAnswer(AnswerRequestDTO answerRequestDTO) {
        PersonalQuestion personalQuestion = personalQuestionRepository.findPersonalQuestionById(
                answerRequestDTO.getQuestionId());
        String criteria = personalQuestion.getCriteria();
        String question = personalQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generatePersonalEvaluationPrompt(question, answer, criteria);
        ChatResponse response = chatClient.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    public String evaluateTechQAnswer(AnswerRequestDTO answerRequestDTO) {
        TechQuestion techQuestion = techQuestionRepository.findTechQuestionById(answerRequestDTO.getQuestionId());
        String exampleAnswer = techQuestion.getExampleAnswer();
        String question = techQuestion.getQuestion();
        String answer = answerRequestDTO.getAnswer();
        Prompt prompt = generateTechEvaluationPrompt(question, exampleAnswer, answer);
        ChatResponse response = chatClient.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    private Prompt generatePersonalEvaluationPrompt(String question, String answer, String criteria) {
        try {
            // Log system prompt template content
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(perQEvalSystemResource);
            Message systemMessage = systemPromptTemplate.createMessage();
            System.out.println("System Message: " + systemMessage.getContent());

            // Log user prompt template content
            String userTemplateContent = new String(Files.readAllBytes(perQEvalUserResource.getFile().toPath()),
                    StandardCharsets.UTF_8);
            System.out.println("User Template Content: " + userTemplateContent);

            PromptTemplate userPromptTemplate = new PromptTemplate(perQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "criteria", criteria, "answer", answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);
            System.out.println("User Message: " + userMessage.getContent());

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

    private Prompt generateTechEvaluationPrompt(String question, String exampleAnswer, String answer) {
        try {
            // Log system prompt template content
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(perQEvalSystemResource);
            Message systemMessage = systemPromptTemplate.createMessage();

            PromptTemplate userPromptTemplate = new PromptTemplate(perQEvalUserResource);
            Map<String, Object> valuesMap = Map.of("question", question, "exampleAnswer", exampleAnswer, "answer",
                    answer);
            Message userMessage = userPromptTemplate.createMessage(valuesMap);

            return new Prompt(List.of(userMessage, systemMessage));
        } catch (Exception e) {
            log.error("Error generating prompt", e);
            throw new RuntimeException("Error generating prompt", e);
        }
    }

}
