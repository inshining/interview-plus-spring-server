package com.ddoddii.resume.dto.company;

import com.ddoddii.resume.model.eunm.CompanyDepartment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDeptDTO {
    private String companyDept;

    public CompanyDeptDTO(CompanyDepartment dept) {
    }
}
