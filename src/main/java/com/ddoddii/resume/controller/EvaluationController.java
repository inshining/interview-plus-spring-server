package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.evaluation.AnswerRequestDTO;
import com.ddoddii.resume.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answer/")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/personal")
    public ResponseEntity<String> evaluatePersonalQuestion(@RequestBody AnswerRequestDTO answerRequestDTO) {
        String perQuestionEval = evaluationService.evaluatePerQAnswer(answerRequestDTO);
        return ResponseEntity.ok(perQuestionEval);
    }

    @PostMapping("/tech")
    public ResponseEntity<String> evaluateTechQuestion(@RequestBody AnswerRequestDTO answerRequestDTO) {
        String perQuestionEval = evaluationService.evaluateTechQAnswer(answerRequestDTO);
        return ResponseEntity.ok(perQuestionEval);
    }

}
