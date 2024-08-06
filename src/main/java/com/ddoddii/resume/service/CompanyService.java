package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.company.CompanyDeptDTO;
import com.ddoddii.resume.dto.company.CompanyJobDTO;
import com.ddoddii.resume.dto.company.CompanyNameDTO;
import com.ddoddii.resume.model.company.CompanyDept;
import com.ddoddii.resume.model.company.CompanyJob;
import com.ddoddii.resume.model.company.CompanyName;
import com.ddoddii.resume.repository.CompanyDeptRepository;
import com.ddoddii.resume.repository.CompanyJobRepository;
import com.ddoddii.resume.repository.CompanyNameRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {
    // 기업 이름
    private final CompanyNameRepository companyNameRepository;
    // 직무
    private final CompanyDeptRepository companyDeptRepository;
    // 직군
    private final CompanyJobRepository companyJobRepository;

    public List<CompanyNameDTO> getCompanyNames() {
        List<CompanyName> companyNames = companyNameRepository.findAll();
        return companyNames.stream()
                .map(companyName -> new CompanyNameDTO(companyName.getId(), companyName.getName()))
                .collect(Collectors.toList());
    }

    public List<CompanyDeptDTO> getCompanyDepts() {
        List<CompanyDept> companyDepts = companyDeptRepository.findAll();
        return companyDepts.stream()
                .map(companyDept -> new CompanyDeptDTO(companyDept.getId(), companyDept.getDept()))
                .collect(Collectors.toList());
    }

    public List<CompanyJobDTO> getCompanyJobs() {
        List<CompanyJob> companyJobs = companyJobRepository.findAll();
        return companyJobs.stream()
                .map(companyJob -> new CompanyJobDTO(companyJob.getId(), companyJob.getJob()))
                .collect(Collectors.toList());
    }
}
