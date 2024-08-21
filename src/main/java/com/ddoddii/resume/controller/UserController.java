package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.user.DuplicateEmailRequestDTO;
import com.ddoddii.resume.dto.user.JwtTokenDTO;
import com.ddoddii.resume.dto.user.RefreshTokenRequestDTO;
import com.ddoddii.resume.dto.user.UserAuthResponseDTO;
import com.ddoddii.resume.dto.user.UserDTO;
import com.ddoddii.resume.dto.user.UserEmailLoginRequestDTO;
import com.ddoddii.resume.dto.user.UserEmailSignUpRequestDTO;
import com.ddoddii.resume.dto.user.UserEmailVerificationRequestDTO;
import com.ddoddii.resume.dto.user.UserEmailVerificationResponseDTO;
import com.ddoddii.resume.dto.user.UserGoogleLoginRequestDTO;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.service.EmailService;
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
    private final EmailService emailService;

    @PostMapping("/")
    public ResponseEntity<UserAuthResponseDTO> emailSignUp(@RequestBody @Valid UserEmailSignUpRequestDTO request) {
        UserAuthResponseDTO response = userService.emailSignUpAndLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-login")
    public ResponseEntity<UserAuthResponseDTO> emailLogin(@RequestBody @Valid UserEmailLoginRequestDTO request) {
        UserAuthResponseDTO response = userService.emailLogin(request, LoginType.EMAIL);
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
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO currentUser = userService.getCurrentUserDTO();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<UserEmailVerificationResponseDTO> getVerificationCode(@RequestBody
                                                                                UserEmailVerificationRequestDTO userEmailVerificationRequestDTO) {
        String verificationCode = emailService.validateUserEmail(userEmailVerificationRequestDTO.getEmail());
        UserEmailVerificationResponseDTO dto = UserEmailVerificationResponseDTO.builder()
                .verificationCode(verificationCode)
                .build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/guest")
    public ResponseEntity<UserAuthResponseDTO> gusetSignUpAndLogin() {
        UserAuthResponseDTO response = userService.guestSignUpAndLogin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("guest/email-login")
    public ResponseEntity<String> guesetEmailSignUp(
            @RequestBody @Valid UserEmailSignUpRequestDTO request) {
        userService.guestEmailSignUpAndLogin(request);
        return ResponseEntity.ok("Guest Email Signup Success");
    }

    @PostMapping("guest/google-login")
    public ResponseEntity<String> guestGoogleLogin(@RequestBody @Valid UserGoogleLoginRequestDTO request) {
        userService.guestGoogleSignUpAndLogin(request);
        return ResponseEntity.ok("Guest Google Signup Success");
    }
}
