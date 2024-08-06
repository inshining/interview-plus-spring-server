package com.ddoddii.resume.dto.user;

import com.ddoddii.resume.model.eunm.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDTO {
    private long userId;
    private String name;
    private String email;
    private LoginType loginType;
    private String defaultResume;
    private Integer remainInterview;
}
