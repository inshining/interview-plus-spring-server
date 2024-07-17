package com.ddoddii.resume.dto;

import com.ddoddii.resume.model.eunm.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
<<<<<<<< HEAD:src/main/java/com/ddoddii/resume/dto/UserDTO.java
public class UserDTO {
    private long userId;
    private String name;
    private String email;
    private LoginType loginType;
    private String defaultResume;
    private Integer remainInterview;
========
public class UserGoogleLoginRequestDTO {
    private String idToken;
    private String name;
    private String email;
>>>>>>>> e5e14e9 (feat : 로그인 수):src/main/java/com/ddoddii/resume/dto/UserGoogleLoginRequestDTO.java
}
