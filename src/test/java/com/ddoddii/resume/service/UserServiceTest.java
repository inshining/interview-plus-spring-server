package com.ddoddii.resume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;

import com.ddoddii.resume.dto.user.*;
import com.ddoddii.resume.error.errorcode.UserErrorCode;
import com.ddoddii.resume.error.exception.BadCredentialsException;
import com.ddoddii.resume.error.exception.DuplicateIdException;
import com.ddoddii.resume.error.exception.NotExistIdException;
import com.ddoddii.resume.model.RefreshToken;
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
        given(userRepository.findByEmail(anyString())).willReturn(Optional.ofNullable(user));

        String grantType = "Bearer";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(JwtTokenDTO.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshToken).build());


        // when
        UserAuthResponseDTO userAuthResponseDTO = userService.emailSignUpAndLogin(userEmailSignUpRequestDTO);

        // then
        assertNotNull(userAuthResponseDTO);
        assertEquals(userEmailSignUpRequestDTO.getEmail(), userAuthResponseDTO.getUser().getEmail());
        assertEquals(userEmailSignUpRequestDTO.getName(), userAuthResponseDTO.getUser().getName());
        assertEquals(LoginType.EMAIL, userAuthResponseDTO.getUser().getLoginType());
        assertEquals(1L, userAuthResponseDTO.getUser().getUserId());
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
        assertThrows(DuplicateIdException.class, () -> userService.emailSignUpAndLogin(userEmailSignUpRequestDTO));
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
        UserAuthResponseDTO response = userService.emailLogin(request, LoginType.EMAIL);

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
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.emailLogin(request, LoginType.EMAIL));
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
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.emailLogin(request, LoginType.EMAIL));

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

    @DisplayName("성공: 구글 로그인 - 기존 회원")
    @Test
    void googleLoginExistingUser() {
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken(password)
                .name(name)
                .email(email)
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.ofNullable(user));

        String grantType = "Bearer";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(JwtTokenDTO.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshToken).build());

        // when
        UserAuthResponseDTO response = userService.googleLogin(request);

        // then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getUser().getEmail());
        assertEquals(name, response.getUser().getName());
        assertEquals(grantType, response.getToken().getGrantType());
    }

    @DisplayName("성공: 구글 로그인 - 신규 회원")
    @Test
    void googleLoginNewUser() {
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken(password)
                .name(name)
                .email(email)
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userRepository.findById(anyLong())).willReturn(Optional.ofNullable(user));

        String grantType = "Bearer";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenProvider.createToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(JwtTokenDTO.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshToken).build());

        // when
        UserAuthResponseDTO response = userService.googleLogin(request);

        // then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getUser().getEmail());
        assertEquals(name, response.getUser().getName());
        assertEquals(grantType, response.getToken().getGrantType());
    }

    @DisplayName("실패: 구글 로그인 - 비밀번호가 일치하지 않는 경우")
    @Test
    void googleLoginWithWrongPassword() {
        // given
        UserGoogleLoginRequestDTO request = UserGoogleLoginRequestDTO.builder()
                .idToken("wrongPassword")
                .name(name)
                .email(email)
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.ofNullable(user));

        // when
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.googleLogin(request));

        // then
        assertEquals(UserErrorCode.BAD_CREDENTIALS, exception.getErrorCode());
    }

    @DisplayName("성공: 프레시 토큰 생성")
    @Test
    void generateNewAccessToken() {
        // given
        String token = "token1234";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .refreshToken(token)
                .build();

        given(refreshTokenService.findByRefreshToken(anyString())).willReturn(Optional.ofNullable(refreshToken));

        String grantType = "Bearer";
        String accessToken = "accessToken";
        String refreshTokenStr = "refreshToken";

        given(tokenProvider.createToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(JwtTokenDTO.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr).build());

        // when
        JwtTokenDTO jwtTokenDTO = userService.generateNewAccessToken(token);

        // then
        assertNotNull(jwtTokenDTO);
        assertEquals(grantType, jwtTokenDTO.getGrantType());
        assertEquals(accessToken, jwtTokenDTO.getAccessToken());
        assertEquals(refreshTokenStr, jwtTokenDTO.getRefreshToken());
    }

    @DisplayName("실패: 프레시 토큰 생성 - 프레시 토큰이 존재하지 않는 경우")
    @Test
    void generateNewAccessTokenWithNoRefreshToken() {
        // given
        String refreshTokenParm = "token123";
        given(refreshTokenService.findByRefreshToken(anyString())).willReturn(Optional.empty());

        // when
        NotExistIdException exception = assertThrows(NotExistIdException.class, () -> userService.generateNewAccessToken(refreshTokenParm));

        // then
        assertEquals("Refresh Token not found", exception.getErrorCode().getMessage());

    }
}