package com.ddoddii.resume.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncrypterTest {

    @DisplayName("암호화 성공")
    @Test
    void encryptSuccess() {
        // given
        String password = "password";

        // when
        String encryptedPassword = PasswordEncrypter.encrypt(password);

        // then
        assertNotNull(encryptedPassword);
        assertTrue(PasswordEncrypter.isMatch(password, encryptedPassword));
    }

    @DisplayName("암호화 실패")
    @Test
    void encryptFail() {
        // given
        String password = "password";
        String wrongPassword = "wrongPassword";

        // when
        String encryptedPassword = PasswordEncrypter.encrypt(password);

        // then
        assertNotNull(encryptedPassword);
        assertFalse(PasswordEncrypter.isMatch(wrongPassword, encryptedPassword));
    }
}