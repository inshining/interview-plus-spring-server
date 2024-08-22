package com.ddoddii.resume.dto.interview;

import com.ddoddii.resume.dto.evaluation.EvaluationResultDTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InterviewResultDTO {
    private long interviewId;
    private int companyId;
    private String companyName;
    private int jobId;
    private int departmentId;
    private LocalDateTime createdAt;
    private List<EvaluationResultDTO> personalFeedback;
    private List<EvaluationResultDTO> techFeedback;
    private List<EvaluationResultDTO> behaviorFeedback;
    private List<EvaluationResultDTO> introduceFeedback;
}
