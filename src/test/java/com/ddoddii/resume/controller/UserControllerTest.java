package com.ddoddii.resume.controller;

import com.ddoddii.resume.dto.user.*;
import com.ddoddii.resume.error.errorcode.UserErrorCode;
import com.ddoddii.resume.error.exception.DuplicateIdException;
import com.ddoddii.resume.error.exception.NotExistIdException;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.security.JwtFilter;
import com.ddoddii.resume.service.EmailService;
import com.ddoddii.resume.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;


    // jwtFilter 빈을 주입하지 않으면 테스트가 실패합니다.
    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;


    private String email = "abc@email.com";
    private String name = "userName";
    private String password = "password1234";

    @DisplayName("성공 이메일 회원가입")
    @Test
    void emailSignUpSuccess() throws Exception{
        // given
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .userId(1L)
                .loginType(LoginType.EMAIL)
                .build();
        JwtTokenDTO jwtTokenDTO =JwtTokenDTO.builder().refreshToken("refreshToken").accessToken("accessToken").grantType("Bearer").build();
        UserAuthResponseDTO userAuthResponseDTO =UserAuthResponseDTO.builder().user(userDTO).token(jwtTokenDTO).build();
        when(userService.emailSignUpAndLogin(any(UserEmailSignUpRequestDTO.class))).thenReturn(userAuthResponseDTO);

        this.mockMvc.perform(post("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailSignUpRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.email").value(email))
                .andExpect(jsonPath("user.name").value(name))
                .andExpect(jsonPath("user.loginType").value(LoginType.EMAIL.toString()))
                .andExpect(jsonPath("token.grantType").value("Bearer"))
                .andExpect(jsonPath("token.accessToken").value("accessToken"))
                .andExpect(jsonPath("token.refreshToken").value("refreshToken"));
    }

    @DisplayName("실패 이메일 회원가입 - 이메일 형식이 아닌 경우")
    @Test
    void emailSignUpFail_WrongEmail() throws Exception{
        // given
        email = "abcemail.com";
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEmailSignUpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("실패 이메일 회원가입 - 이메일 값이 안 들어 온 경우")
    @Test
    void emailSignUpFail_BlankEmail() throws Exception{
        // given
        email = "";
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailSignUpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("실패 이메일 회원가입 - 비밀번호가 없는 경우")
    @Test
    void emailSignUpFail_NoPassword() throws Exception {
        // given
        password = "";
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailSignUpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("실패 이메일 회원가입 - 이미 존재하는 회원인 경우")
    @Test
    void emailSignUpFail_DuplicateEmail() throws Exception {
        // given
        when(userService.emailSignUpAndLogin(any(UserEmailSignUpRequestDTO.class))).thenThrow(new DuplicateIdException(UserErrorCode.DUPLICATE_USER));

        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEmailSignUpRequestDTO)))
                .andExpect(status().isConflict())
        .andExpect(jsonPath("message").value(UserErrorCode.DUPLICATE_USER.getMessage()));
    }

    @DisplayName("성공 이메일 로그인")
    @Test
    void emailLoginSuccess() throws Exception {
        // given
        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .userId(1L)
                .loginType(LoginType.EMAIL)
                .build();

        JwtTokenDTO jwtTokenDTO =JwtTokenDTO.builder().refreshToken("refreshToken").accessToken("accessToken").grantType("Bearer").build();
        UserAuthResponseDTO userAuthResponseDTO =UserAuthResponseDTO.builder().user(userDTO).token(jwtTokenDTO).build();
        when(userService.emailLogin(any(UserEmailLoginRequestDTO.class), any(LoginType.class))).thenReturn(userAuthResponseDTO);

        this.mockMvc.perform(post("/api/users/email-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.email").value(email))
                .andExpect(jsonPath("user.name").value(name))
                .andExpect(jsonPath("user.loginType").value(LoginType.EMAIL.toString()))
                .andExpect(jsonPath("token.grantType").value("Bearer"))
                .andExpect(jsonPath("token.accessToken").value("accessToken"))
                .andExpect(jsonPath("token.refreshToken").value("refreshToken"));
    }

    @DisplayName("실패 이메일 로그인 - 존재하지 않은 이메일")
    @Test
    void emailLoginFail_NotExistEmail() throws Exception {
        // given
        when(userService.emailLogin(any(UserEmailLoginRequestDTO.class), any(LoginType.class))).thenThrow(new DuplicateIdException(UserErrorCode.BAD_CREDENTIALS));

        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/email-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(UserErrorCode.BAD_CREDENTIALS.getMessage()));
    }

    @DisplayName("실패 이메일 로그인 - 비밀번호가 틀린 경우")
    @Test
    void emailLoginFail_WrongPassword() throws Exception {
        // given
        when(userService.emailLogin(any(UserEmailLoginRequestDTO.class), any(LoginType.class))).thenThrow(new DuplicateIdException(UserErrorCode.BAD_CREDENTIALS));


        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        this.mockMvc.perform(post("/api/users/email-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(UserErrorCode.BAD_CREDENTIALS.getMessage()));
    }

    @DisplayName("성공 구글 로그인")
    @Test
    void emailGoogleLogin_Success() throws Exception{
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken("idToken")
                .email(email)
                .name(name)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .name(name)
                .email(email)
                .userId(1L)
                .loginType(LoginType.GOOGLE)
                .build();

        JwtTokenDTO jwtTokenDTO =JwtTokenDTO.builder().refreshToken("refreshToken").accessToken("accessToken").grantType("Bearer").build();
        UserAuthResponseDTO userAuthResponseDTO =UserAuthResponseDTO.builder().user(userDTO).token(jwtTokenDTO).build();
        when(userService.googleLogin(any())).thenReturn(userAuthResponseDTO);

        this.mockMvc.perform(post("/api/users/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.email").value(email))
                .andExpect(jsonPath("user.name").value(name))
                .andExpect(jsonPath("user.loginType").value(LoginType.GOOGLE.toString()))
                .andExpect(jsonPath("token.grantType").value("Bearer"))
                .andExpect(jsonPath("token.accessToken").value("accessToken"))
                .andExpect(jsonPath("token.refreshToken").value("refreshToken"));
    }

    @DisplayName("실패 구글 로그인 - 비밀번호가 틀린 경우")
    @Test
    void emailGoogleLogin_Fail() throws Exception{
        // given
        when(userService.googleLogin(any())).thenThrow(new DuplicateIdException(UserErrorCode.BAD_CREDENTIALS));

        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken("idToken")
                .email(email)
                .name(name)
                .build();

        this.mockMvc.perform(post("/api/users/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value(UserErrorCode.BAD_CREDENTIALS.getMessage()));
    }

    @DisplayName("성공 리프레시 토큰 발행")
    @Test
    void refreshToken_Success() throws Exception{
        // given
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("oldRefreshToken");

        JwtTokenDTO jwtTokenDTO =JwtTokenDTO.builder().refreshToken("refreshToken").accessToken("accessToken").grantType("Bearer").build();
        when(userService.generateNewAccessToken(any())).thenReturn(jwtTokenDTO);

        this.mockMvc.perform(post("/api/users/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("grantType").value("Bearer"))
                .andExpect(jsonPath("accessToken").value("accessToken"))
                .andExpect(jsonPath("refreshToken").value("refreshToken"));
    }

    @DisplayName("실패 리프레시 토큰 발행 - 만료된 토큰")
    @Test
    void refreshToken_Fail() throws Exception{
        // given
        when(userService.generateNewAccessToken(any())).thenThrow(new NotExistIdException(UserErrorCode.NOT_EXIST_USER));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("oldRefreshToken");

        this.mockMvc.perform(post("/api/users/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value(UserErrorCode.NOT_EXIST_USER.getMessage()));
    }

}