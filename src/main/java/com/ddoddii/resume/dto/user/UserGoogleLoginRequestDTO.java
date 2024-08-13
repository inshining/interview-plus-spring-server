package com.ddoddii.resume.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGoogleLoginRequestDTO {

    @NotBlank
    private String idToken;
    private String name;

    @Email
    @NotBlank
    private String email;
}

