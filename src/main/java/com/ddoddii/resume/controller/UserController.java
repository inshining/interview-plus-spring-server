package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.DuplicateEmailRequestDTO;
import com.ddoddii.resume.dto.JwtTokenDTO;
import com.ddoddii.resume.dto.RefreshTokenRequestDTO;
import com.ddoddii.resume.dto.UserAuthResponseDTO;
import com.ddoddii.resume.dto.UserEmailLoginRequestDTO;
import com.ddoddii.resume.dto.UserEmailSignUpRequestDTO;
import com.ddoddii.resume.dto.UserGoogleLoginRequestDTO;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserAuthResponseDTO> emailSignUp(@RequestBody @Valid UserEmailSignUpRequestDTO user) {
        UserAuthResponseDTO response = userService.emailSignUpAndLogin(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-login")
    public ResponseEntity<UserAuthResponseDTO> emailLogin(@RequestBody @Valid UserEmailLoginRequestDTO request) {
        UserAuthResponseDTO response = userService.emailLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google-login")
    public ResponseEntity<UserAuthResponseDTO> googleLogin(@RequestBody @Valid UserGoogleLoginRequestDTO request) {
        UserAuthResponseDTO response = userService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/duplicate-email")
    public ResponseEntity<Boolean> checkDuplicateEmail(@RequestBody DuplicateEmailRequestDTO requestDTO) {
        Boolean response = userService.checkDuplicateEmail(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtTokenDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        JwtTokenDTO jwtTokenDTO = userService.generateNewAccessToken(refreshTokenRequestDTO.getToken());
        return ResponseEntity.ok(jwtTokenDTO);
    }

    @GetMapping("/current-user")
    public ResponseEntity<String> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user.getEmail());
    }

}
