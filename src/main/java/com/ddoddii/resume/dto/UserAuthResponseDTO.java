package com.ddoddii.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserAuthResponseDTO {
    private UserDTO user;
    private JwtTokenDTO token;
}
