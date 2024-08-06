package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.interview.InterviewResultDTO;
import com.ddoddii.resume.dto.interview.InterviewStartRequestDTO;
import com.ddoddii.resume.dto.interview.InterviewStartResponseDTO;
import com.ddoddii.resume.error.errorcode.ResumeErrorCode;
import com.ddoddii.resume.error.exception.NotExistResumeException;
import com.ddoddii.resume.model.Evaluation;
import com.ddoddii.resume.model.Interview;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.InterviewRound;
import com.ddoddii.resume.repository.InterviewRepository;
import com.ddoddii.resume.repository.ResumeRepository;
import java.util.List;
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
    private final UserService userService;

    public InterviewStartResponseDTO startInterview(InterviewStartRequestDTO interviewStartRequestDTO) {
        Interview interview = new Interview();
        User currentUser = userService.getCurrentUser();
        interview.setInterviewRound(InterviewRound.fromString(interviewStartRequestDTO.getInterviewRound()));
        interview.setCompanyId(interviewStartRequestDTO.getCompanyId());
        interview.setJobId(interviewStartRequestDTO.getJobId());
        interview.setDepartmentId(interview.getDepartmentId());
        interview.setResume(resumeRepository.findById(interviewStartRequestDTO.getResumeId())
                .orElseThrow(() -> new NotExistResumeException(
                        ResumeErrorCode.NOT_EXIST_RESUME)));
        interview.setUser(currentUser);

        Interview savedInterview = interviewRepository.save(interview);

        int interviewId = savedInterview.getId().intValue();
        return InterviewStartResponseDTO.builder().
                interviewId(interviewId)
                .build();
    }

    public List<InterviewResultDTO> getInterviewResults() {
        User currentUser = userService.getCurrentUser();
        List<Interview> interviews = interviewRepository.findByUser(currentUser);
        return interviews.stream()
                .map(this::convertToInterviewResultDTO)
                .collect(Collectors.toList());
    }


    private InterviewResultDTO convertToInterviewResultDTO(Interview interview) {
        InterviewResultDTO dto = new InterviewResultDTO();
        dto.setCreatedAt(interview.getCreatedAt());
        dto.setCompanyId(interview.getCompanyId());
        dto.setJobId(interview.getJobId());
        dto.setDepartmentId(interview.getDepartmentId());

        List<Evaluation> introduceEvals =
                interview.getIntroduceEval() != null ? interview.getIntroduceEval() : List.of();
        List<Evaluation> personalEvals =
                interview.getPersonalEval() != null ? interview.getPersonalEval() : List.of();
        List<Evaluation> behaviorEvals =
                interview.getBehaviorEval() != null ? interview.getBehaviorEval() : List.of();
        List<Evaluation> techEvals =
                interview.getTechEval() != null ? interview.getTechEval() : List.of();

        dto.setIntroduceEval(introduceEvals);
        dto.setPersonalEval(personalEvals);
        dto.setBehaviorEval(behaviorEvals);
        dto.setTechEval(techEvals);
        return dto;
    }


}
