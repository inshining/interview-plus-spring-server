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
import org.junit.jupiter.api.BeforeEach;
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

    private final String email = "abc@google.com";
    private final String password = "password1234";
    private final String name = "abc";
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email(email)
                .password(PasswordEncrypter.encrypt(password))
                .name(name)
                .id(1L)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .build();
    }

    @DisplayName("이메일 회원가입")
    @Test
    void emailSignUp() {
        // given
        UserEmailSignUpRequestDTO userEmailSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);

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
                .email(email)
                .password(password)
                .name(name)
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // then
        assertThrows(DuplicateIdException.class, () -> userService.emailSignUp(userEmailSignUpRequestDTO));
    }

    @DisplayName("이메일 로그인 - 성공")
    @Test
    void emailLogin() {
        // given

        UserEmailLoginRequestDTO request = UserEmailLoginRequestDTO.builder()
                .email(email)
                .password(password)
                .build();

        Optional<User> savedUser = Optional.ofNullable(user);
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
        assertEquals(name, response.getUser().getName());
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
                .email(email)
                .password("")
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.ofNullable(user));

        // when
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.emailLogin(request));

        // then
        assertEquals(UserErrorCode.BAD_CREDENTIALS, exception.getErrorCode());
    }

    @DisplayName("성공: 구글 회원가입 - 새로운 회원")
    @Test
    void googleSignUp() {
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken("idToken")
                .name(name)
                .email(email)
                .build();

        given(userRepository.save(any(User.class))).willReturn(user);
        // when
        UserDTO userDTO = userService.googleSignUp(request);

        // then
        assertNotNull(userDTO);
        assertEquals(request.getEmail(), userDTO.getEmail());
        assertEquals(request.getName(), userDTO.getName());
        assertEquals(LoginType.EMAIL, userDTO.getLoginType());
        assertEquals(1L, userDTO.getUserId());
    }

    @DisplayName("실패: 구글 회원가입 - 이미 존재하는 회원")
    @Test
    void googleSignUpWithExistedUser() {
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken("idToken")
                .name(name)
                .email(email)
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // then
        DuplicateIdException duplicateIdException = assertThrows(DuplicateIdException.class, () -> userService.googleSignUp(request));
        assertEquals(UserErrorCode.DUPLICATE_USER, duplicateIdException.getErrorCode());
    }
}