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
public class UserEmailLoginRequestDTO {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
