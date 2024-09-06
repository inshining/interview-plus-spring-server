package com.ddoddii.resume.error.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode{
    DuplicatePersonalQuestion(HttpStatus.CONFLICT, "Duplicate Personal Question");

    private final HttpStatus httpStatus;
    private final String message;
}
