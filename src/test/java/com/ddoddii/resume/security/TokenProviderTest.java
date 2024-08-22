package com.ddoddii.resume.security;

import com.ddoddii.resume.dto.user.JwtTokenDTO;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.model.eunm.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTest {

    private static final String accessToken = "00214AD545CC8299180041E22E854E26B0F782B00BB231B6F8A1BD339D273FF600214AD545CC8299180041E22E854E26B0F782B00BB231B6F8A1BD339D273FF6";
    private static final String refreshToken = "00214AD545CC8299180041E22E854E26B0F782B00BB231B6F8A1BD339D273FF600214AD545CC8299180041E22E854E26B0F782B00BB231B6F8A1BD339D273FF6";
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(accessToken, refreshToken, 1000, 1000);
        tokenProvider.afterPropertiesSet();
    }


    @DisplayName("성공: 토큰 생성")
    @Test
    void createTokenSuccess() {
        //given
        String email = "abc@email.com";
        String password = "password1234";
        String name = "userName";


        User user = User.builder()
                .id(1L)
                .email(email)
                .password(password)
                .name(name)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .remainInterview(5)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // Authorities 를 가지는 Authentication 객체
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                customUserDetails.getAuthorities()
        );

        // when
        JwtTokenDTO loginToken = tokenProvider.createToken(authenticationToken);

        // then
        assertNotNull(loginToken.getAccessToken());
        assertNotNull(loginToken.getRefreshToken());
        assertNotNull(loginToken.getGrantType());
        assertEquals("Bearer", loginToken.getGrantType());
    }

}