package com.ddoddii.resume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;

import com.ddoddii.resume.dto.user.UserDTO;
import com.ddoddii.resume.dto.user.UserEmailSignUpRequestDTO;
import com.ddoddii.resume.error.exception.DuplicateIdException;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.repository.RefreshTokenRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.ddoddii.resume.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}