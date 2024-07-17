package com.ddoddii.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@Builder
<<<<<<<< HEAD:src/main/java/com/ddoddii/resume/dto/UserGoogleLoginRequestDTO.java
public class UserGoogleLoginRequestDTO {
    private String idToken;
    private String name;
    private String email;
========
public class UserAuthResponseDTO {
    private UserDTO user;
    private JwtTokenDTO token;
>>>>>>>> feature/login:src/main/java/com/ddoddii/resume/dto/UserAuthResponseDTO.java
}
