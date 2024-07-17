package com.ddoddii.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
<<<<<<<< HEAD:src/main/java/com/ddoddii/resume/dto/UserGoogleLoginRequestDTO.java
@ToString
public class UserGoogleLoginRequestDTO {
    private String idToken;
    private String name;
    private String email;
}
========
public class GoogleLoginResponseDTO {
    private UserDTO userDTO;
    private JwtTokenDTO tokenDTO;
}
>>>>>>>> e5e14e9 (feat : 로그인 수):src/main/java/com/ddoddii/resume/dto/GoogleLoginResponseDTO.java
