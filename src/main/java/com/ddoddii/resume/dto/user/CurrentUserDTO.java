package com.ddoddii.resume.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {
    private long userId;
    private String name;
    private String email;
    private Integer remainInterview;
}
