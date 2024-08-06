package com.ddoddii.resume.dto.interview;

import com.ddoddii.resume.model.Evaluation;
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
    private int companyId;
    private int jobId;
    private int departmentId;
    private LocalDateTime createdAt;
    private List<Evaluation> introduceEval;
    private List<Evaluation> personalEval;
    private List<Evaluation> techEval;
    private List<Evaluation> behaviorEval;
}
