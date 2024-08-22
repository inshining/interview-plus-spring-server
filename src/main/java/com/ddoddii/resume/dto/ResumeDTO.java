package com.ddoddii.resume.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class ResumeDTO {
    private String name;
    private String position;
    private String content;
    @JsonProperty("isDefault")
    private boolean isDefault;
}
