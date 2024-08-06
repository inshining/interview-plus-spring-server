package com.ddoddii.resume.dto.interview;

import java.time.LocalDateTime;
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
public class PendingResultDTO {
    private int companyId;
    private int jobId;
    private int departmentId;
    private LocalDateTime createdAt;
}
