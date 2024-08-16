package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.interview.InterviewResultDTO;
import com.ddoddii.resume.dto.interview.InterviewStartRequestDTO;
import com.ddoddii.resume.dto.interview.InterviewStartResponseDTO;
import com.ddoddii.resume.service.InterviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview/")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/")
    public ResponseEntity<InterviewStartResponseDTO> startInterview(
            @RequestBody InterviewStartRequestDTO interviewStartRequestDTO) {
        InterviewStartResponseDTO interviewStartResponseDTO = interviewService.startInterview(interviewStartRequestDTO);
        return ResponseEntity.ok(interviewStartResponseDTO);
    }

    @GetMapping("/")
    public ResponseEntity<List<InterviewResultDTO>> getInterviewResults() {
        List<InterviewResultDTO> interviewResultDTOS = interviewService.getInterviewResults();
        return ResponseEntity.ok(interviewResultDTOS);
    }
}
