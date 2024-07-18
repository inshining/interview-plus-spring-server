package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.company.CompanyDeptDTO;
import com.ddoddii.resume.dto.company.CompanyJobDTO;
import com.ddoddii.resume.dto.company.CompanyNameDTO;
import com.ddoddii.resume.service.CompanyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/name")
    public ResponseEntity<List<CompanyNameDTO>> getCompanyNames() {
        List<CompanyNameDTO> companyNameDTOs = companyService.getCompanyNames();
        return ResponseEntity.ok(companyNameDTOs);
    }

    @GetMapping("/department")
    public ResponseEntity<List<CompanyDeptDTO>> getCompanyDepts() {
        List<CompanyDeptDTO> companyDeptsDTOs = companyService.getCompanyDepts();
        return ResponseEntity.ok(companyDeptsDTOs);
    }

    @GetMapping("/job")
    public ResponseEntity<List<CompanyJobDTO>> getCompanyJobs() {
        List<CompanyJobDTO> companyJobDTOs = companyService.getCompanyJobs();
        return ResponseEntity.ok(companyJobDTOs);
    }


}
