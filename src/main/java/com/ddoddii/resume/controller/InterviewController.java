package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.interview.InterviewResultDTO;
import com.ddoddii.resume.dto.interview.InterviewStartRequestDTO;
import com.ddoddii.resume.dto.interview.InterviewStartResponseDTO;
import com.ddoddii.resume.service.InterviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<InterviewResultDTO>> getInterviewResultsByStatus(
            @RequestParam(value = "status", required = false) String status) {
        List<InterviewResultDTO> results = null;
        if ("done".equalsIgnoreCase(status)) {
            results = interviewService.getInterviewResults();
        }
        if ("pending".equalsIgnoreCase(status)) {
            results = interviewService.getPendingInterviewResults();
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDTO> getInterviewResult(@PathVariable long interviewId) {
        InterviewResultDTO interviewResultDTO = interviewService.getInterviewResult(interviewId);
        return ResponseEntity.ok(interviewResultDTO);
    }


    @DeleteMapping("/{interviewId}")
    public ResponseEntity<String> deleteInterview(@PathVariable long interviewId) {
        interviewService.deleteCurrentInterview(interviewId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Interview Deleted");
    }

}
