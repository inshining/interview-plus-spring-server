package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.evaluation.EvaluationResultDTO;
import com.ddoddii.resume.dto.interview.InterviewResultDTO;
import com.ddoddii.resume.dto.interview.InterviewStartRequestDTO;
import com.ddoddii.resume.dto.interview.InterviewStartResponseDTO;
import com.ddoddii.resume.error.errorcode.ResumeErrorCode;
import com.ddoddii.resume.error.exception.NotExistResumeException;
import com.ddoddii.resume.model.Evaluation;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.model.eunm.QuestionType;
import com.ddoddii.resume.repository.EvaluationRepository;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserService userService;

    public InterviewStartResponseDTO startInterview(InterviewStartRequestDTO interviewStartRequestDTO) {
        Interview interview = new Interview();
        User currentUser = userService.getCurrentUser();
        interview.setInterviewRound(InterviewRound.fromString(interviewStartRequestDTO.getInterviewRound()));
        if (interviewStartRequestDTO.getCompanyId() != null) {
            interview.setCompanyId(interviewStartRequestDTO.getCompanyId());
        }
        if (interviewStartRequestDTO.getCompanyName() != null) {
            interview.setCompanyName(interviewStartRequestDTO.getCompanyName());
        }
        interview.setJobId(interviewStartRequestDTO.getJobId());
        interview.setDepartmentId(interviewStartRequestDTO.getDepartmendId());
        interview.setResume(resumeRepository.findById(interviewStartRequestDTO.getResumeId())
                .orElseThrow(() -> new NotExistResumeException(
                        ResumeErrorCode.NOT_EXIST_RESUME)));
        interview.setUser(currentUser);

        Interview savedInterview = interviewRepository.save(interview);

        // TODO: 왜 int로 변환하나요? InterviewStartResponseDTO의 interviewId는 Long으로 변경하면 안될까요?
        int interviewId = savedInterview.getId().intValue();
        return InterviewStartResponseDTO.builder().
                interviewId(interviewId)
                .build();
    }

    public InterviewResultDTO getInterviewResult(long interviewId) {
        Interview interview = interviewRepository.findInterviewById(interviewId);
        return convertToInterviewResultDTO(interview);
    }

    public List<InterviewResultDTO> getInterviewResults() {
        User currentUser = userService.getCurrentUser();
        List<Interview> interviews = interviewRepository.findByUser(currentUser);
        List<InterviewResultDTO> interviewResultDTOS = new ArrayList<>();
        for (Interview interview : interviews) {
            if (interview.getPersonalEval().isEmpty() | interview.getBehaviorEval().isEmpty() | interview.getTechEval()
                    .isEmpty() | interview.getIntroduceEval().isEmpty()) {
                continue;
            }
            InterviewResultDTO interviewResultDTO = convertToInterviewResultDTO(interview);
            interviewResultDTOS.add(interviewResultDTO);
        }
        return interviewResultDTOS;
    }


    public List<InterviewResultDTO> getPendingInterviewResults() {
        User currentUser = userService.getCurrentUser();
        List<Interview> interviews = interviewRepository.findByUser(currentUser);
        List<InterviewResultDTO> pendingInterviewResultDTOs = new ArrayList<>();
        for (Interview interview : interviews) {
            if (interview.getPersonalEval().isEmpty() | interview.getBehaviorEval().isEmpty() | interview.getTechEval()
                    .isEmpty() | interview.getIntroduceEval().isEmpty()) {
                InterviewResultDTO pendingInterviewResultDTO = convertToPendingInterviewResultDTO(interview);
                pendingInterviewResultDTOs.add(pendingInterviewResultDTO);
            }
        }
        return pendingInterviewResultDTOs;
    }

    public void deleteCurrentInterview(long interviewId) {
        Interview interview = interviewRepository.findInterviewById(interviewId);
        interviewRepository.delete(interview);
    }


    private InterviewResultDTO convertToInterviewResultDTO(Interview interview) {
        // Retrieve evaluations for the specific interview
        List<Evaluation> evaluations = evaluationRepository.findByInterviewId(interview.getId());

        // Filter evaluations by their question type
        List<EvaluationResultDTO> personalFeedback = evaluations.stream()
                .filter(evaluation -> evaluation.getQuestionType() == QuestionType.PERSONAL)
                .map(this::convertToEvaluationResultDTO)
                .collect(Collectors.toList());

        List<EvaluationResultDTO> techFeedback = evaluations.stream()
                .filter(evaluation -> evaluation.getQuestionType() == QuestionType.TECH)
                .map(this::convertToEvaluationResultDTO)
                .collect(Collectors.toList());

        List<EvaluationResultDTO> behaviorFeedback = evaluations.stream()
                .filter(evaluation -> evaluation.getQuestionType() == QuestionType.BEHAVIOR)
                .map(this::convertToEvaluationResultDTO)
                .collect(Collectors.toList());

        List<EvaluationResultDTO> introduceFeedback = evaluations.stream()
                .filter(evaluation -> evaluation.getQuestionType() == QuestionType.INTRODUCE)
                .map(this::convertToEvaluationResultDTO)
                .collect(Collectors.toList());

        return InterviewResultDTO.builder()
                .interviewId(interview.getId())
                .companyId(interview.getCompanyId())
                .companyName(interview.getCompanyName())
                .jobId(interview.getJobId())
                .departmentId(interview.getDepartmentId())
                .createdAt(interview.getCreatedAt())
                .personalFeedback(personalFeedback)
                .techFeedback(techFeedback)
                .behaviorFeedback(behaviorFeedback)
                .introduceFeedback(introduceFeedback)
                .build();
    }

    private InterviewResultDTO convertToPendingInterviewResultDTO(Interview interview) {
        return InterviewResultDTO.builder()
                .interviewId(interview.getId())
                .companyId(interview.getCompanyId())
                .companyName(interview.getCompanyName())
                .jobId(interview.getJobId())
                .departmentId(interview.getDepartmentId())
                .createdAt(interview.getCreatedAt())
                .personalFeedback(new ArrayList<>())
                .techFeedback(new ArrayList<>())
                .behaviorFeedback(new ArrayList<>())
                .introduceFeedback(new ArrayList<>())
                .build();
    }

    private EvaluationResultDTO convertToEvaluationResultDTO(Evaluation evaluation) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> gptEvaluationParsed = new ArrayList<>();

        try {
            gptEvaluationParsed = objectMapper.readValue(evaluation.getGptEvaluation(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return EvaluationResultDTO.builder()
                .questionId(evaluation.getQuestionId())
                .question(evaluation.getAskedQuestion())
                .userAnswer(evaluation.getUserAnswer())
                .gptEvaluation(gptEvaluationParsed)
                .build();
    }
}
