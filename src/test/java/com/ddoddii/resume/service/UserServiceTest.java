package com.ddoddii.resume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;

import com.ddoddii.resume.dto.user.*;
import com.ddoddii.resume.error.errorcode.UserErrorCode;
import com.ddoddii.resume.error.exception.BadCredentialsException;
import com.ddoddii.resume.error.exception.DuplicateIdException;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.repository.RefreshTokenRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.ddoddii.resume.security.TokenProvider;
import com.ddoddii.resume.util.PasswordEncrypter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @DisplayName("이메일 회원가입")
    @Test
    void emailSignUp() {
        // given
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email("abc@google.com")
                .password("1234")
                .name("abc")
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(User.builder()
                .email(userEmailSignUpRequestDTO.getEmail())
                .password(userEmailSignUpRequestDTO.getPassword())
                .name(userEmailSignUpRequestDTO.getName())
                .id(1L)
                .build());

        // when
        UserDTO userDTO = userService.emailSignUp(userEmailSignUpRequestDTO);

        // then
        assertNotNull(userDTO);
        assertEquals(userEmailSignUpRequestDTO.getEmail(), userDTO.getEmail());
        assertEquals(userEmailSignUpRequestDTO.getName(), userDTO.getName());
        assertEquals(LoginType.EMAIL, userDTO.getLoginType());
        assertEquals(1L, userDTO.getUserId());
    }


    @DisplayName("이메일 회원가입 - 이미 존재하는 이메일")
    @Test
    void emailSignUpWithExistedEmail() {
        // given
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email("abc@google.com")
                .password("1234")
                .name("abc")
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // then
        assertThrows(DuplicateIdException.class, () -> userService.emailSignUp(userEmailSignUpRequestDTO));
    }

    @DisplayName("이메일 로그인 - 성공")
    @Test
    void emailLogin() {
        // given
        String userName = "abc";

        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email("abc@example.com")
                .password("1234")
                .build();

        Optional<User> savedUser = Optional.ofNullable(User.builder()
                .email(request.getEmail())
                .password(PasswordEncrypter.encrypt(request.getPassword()))
                .name("abc")
                .id(1L)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .build());
        given(userRepository.findByEmail(anyString())).willReturn(savedUser);

        String grantType = "Bearer";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(JwtTokenDTO.builder()
                        .grantType(grantType)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken).build());

        // when
        UserAuthResponseDTO response = userService.emailLogin(request);

        // then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getUser().getEmail());
        assertEquals(userName, response.getUser().getName());
        assertEquals(grantType, response.getToken().getGrantType());
    }

    @DisplayName("실패: 이메일 로그인 - 일치하는 이용자가 없을 경우")
    @Test
    void emailLoginWithWrongEmail() {
        // given
        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email("")
                .password("")
                .build();

        // then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.emailLogin(request));
        assertEquals(UserErrorCode.BAD_CREDENTIALS, exception.getErrorCode());
    }

    @DisplayName("실패: 이메일 로그인 - 비밀번호 일치하지 않음")
    @Test
    void emailLoginWithWrongPassword() {
        // given
        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email("")
                .password("")
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.ofNullable(User.builder()
                .email(request.getEmail())
                .password(PasswordEncrypter.encrypt("1234"))
                .name("abc")
                .id(1L)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .build()));

        // when
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.emailLogin(request));

        // then
        assertEquals(UserErrorCode.BAD_CREDENTIALS, exception.getErrorCode());
    }
}