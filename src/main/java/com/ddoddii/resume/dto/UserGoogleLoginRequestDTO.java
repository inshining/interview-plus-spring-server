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
public class UserGoogleLoginRequestDTO {
    private String idToken;
    private String name;
    private String email;
}
