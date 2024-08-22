package com.ddoddii.resume.dto.evaluation;

import java.util.List;
import java.util.Map;
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
public class EvaluationResultDTO {
    private long questionId;
    private String question;
    private String userAnswer;
    private List<Map<String, Object>> gptEvaluation;
}
