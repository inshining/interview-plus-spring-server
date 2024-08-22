package com.ddoddii.resume.dto.interview;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class InterviewStartRequestDTO {
    private String interviewRound;
    private Integer companyId;
    private String companyName;
    private int departmendId;
    private int jobId;
    private int resumeId;
}
