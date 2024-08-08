package com.ddoddii.resume.service;

import com.ddoddii.resume.dto.user.DuplicateEmailRequestDTO;
import com.ddoddii.resume.dto.user.JwtTokenDTO;
import com.ddoddii.resume.dto.user.UserAuthResponseDTO;
import com.ddoddii.resume.dto.user.UserDTO;
import com.ddoddii.resume.dto.user.UserEmailLoginRequestDTO;
import com.ddoddii.resume.dto.user.UserEmailSignUpRequestDTO;
import com.ddoddii.resume.dto.user.UserGoogleLoginRequestDTO;
import com.ddoddii.resume.error.errorcode.UserErrorCode;
import com.ddoddii.resume.error.exception.BadCredentialsException;
import com.ddoddii.resume.error.exception.DuplicateIdException;
import com.ddoddii.resume.error.exception.NotExistIdException;
import com.ddoddii.resume.model.RefreshToken;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.repository.RefreshTokenRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.ddoddii.resume.security.CustomUserDetails;
import com.ddoddii.resume.security.TokenProvider;
import com.ddoddii.resume.util.PasswordEncrypter;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
사용자의 회원가입, 로그인을 담당하는 서비스 레이어
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private static final Integer REMAIN_INTERVIEW = 5;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 사용자 회원가입시 사용자 정보와 로그인 토큰도 함께 반환
    public UserAuthResponseDTO emailSignUpAndLogin(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO) {
        UserDTO newUser = emailSignUp(userEmailSignUpRequestDTO);
        UserEmailLoginRequestDTO loginRequestDTO = UserEmailLoginRequestDTO.builder()
                .email(newUser.getEmail())
                .password(userEmailSignUpRequestDTO.getPassword())
                .build();
        return emailLogin(loginRequestDTO);
    }

    // 이메일 회원가입
    public UserDTO emailSignUp(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO) {
        if (userRepository.existsByEmail(userEmailSignUpRequestDTO.getEmail())) {
            throw new DuplicateIdException(UserErrorCode.DUPLICATE_USER);
        }

        // 사용자 비밀번호 암호화
        String encryptedPassword = encryptPassword(userEmailSignUpRequestDTO.getPassword());

        // 사용자 정보 저장
        User user = User.signUpUser(userEmailSignUpRequestDTO.getName(), userEmailSignUpRequestDTO.getEmail(), encryptedPassword);

        // 명시적으로 save 유저 변수 추출
        User saveUser = userRepository.save(user);

        // 유저 정보 반환
        return UserDTO.builder().
                userId(saveUser.getId())
                .name(saveUser.getName())
                .email(saveUser.getEmail())
                .loginType(LoginType.EMAIL)
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }

    // 사용자 로그인
    public UserAuthResponseDTO emailLogin(UserEmailLoginRequestDTO userLoginRequestDTO) {
        // 이메일로 데이터베이스에서 사용자 찾기
        User user = userRepository.findByEmail(userLoginRequestDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException(UserErrorCode.BAD_CREDENTIALS));
        // 비밀번호 일치하는지 검증
        if (!PasswordEncrypter.isMatch(userLoginRequestDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(UserErrorCode.BAD_CREDENTIALS);
        }
        // JWT 토큰 만들기
        JwtTokenDTO loginToken = getJwtTokenDTO(user);

        //refresh token 저장
        refreshTokenService.saveRefreshToken(user.getEmail(), loginToken.getRefreshToken());

        //UserDTO
        UserDTO loggedInUser = UserDTO.builder().
                userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .loginType(LoginType.EMAIL)
                .remainInterview(user.getRemainInterview())
                .build();

        return UserAuthResponseDTO.builder()
                .user(loggedInUser)
                .token(loginToken)
                .build();
    }

    // 구글 회원가입
    public UserDTO googleSignUp(UserGoogleLoginRequestDTO userGoogleLoginRequestDTO) {
        // 이미 존재하는 이메일인지 확인
        if (userRepository.existsByEmail(userGoogleLoginRequestDTO.getEmail())) {
            throw new DuplicateIdException(UserErrorCode.DUPLICATE_USER);
        }

        // 비밀번호 암호화
        String encryptPassword = encryptPassword(userGoogleLoginRequestDTO.getIdToken());
        User user = User.signUpUser(userGoogleLoginRequestDTO.getName(), userGoogleLoginRequestDTO.getEmail(), encryptPassword);
        user.setLoginType(LoginType.GOOGLE);

        User saveUser = userRepository.save(user);
        logger.info("{} 가 저장되었습니다. ", user.getEmail());

        return UserDTO.builder().
                userId(saveUser.getId())
                .name(saveUser.getName())
                .email(saveUser.getEmail())
                // TODO: Enum 값이 잘못 설정되어 있음.
                // LoginType.GOOGLE 로 설정해야 함.
                .loginType(LoginType.EMAIL)
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }

    // 구글 로그인
    public UserAuthResponseDTO googleLogin(UserGoogleLoginRequestDTO userGoogleLoginRequestDTO) {
        Optional<User> optionalUser = userRepository.findByEmail(userGoogleLoginRequestDTO.getEmail());

        User user;
        //유저 존재하는지 확인
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            log.debug("@구글 로그인 : {} 유저는 존재합니다", user.getName());
            if (!PasswordEncrypter.isMatch(userGoogleLoginRequestDTO.getIdToken(), user.getPassword())) {
                log.debug("@구글 로그인 : {} 유저 비밀번호 오류입니다", user.getName());
                throw new BadCredentialsException(UserErrorCode.BAD_CREDENTIALS);
            }
        } else {
            // 유저가 존재하지 않으면 회원가입 진행
            log.debug("@구글 로그인 : 회원가입 진행합니다");
            UserDTO newUser = googleSignUp(userGoogleLoginRequestDTO);

            // TODO: 무엇을 검증하기 위한 코드인지 설명이 필요.
            // googleSignUp 메소드이 성공적으로 동작했다면 디비에 유저 정보가 저장되는 것이 보장되어 있음.
            user = userRepository.findById(newUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("Error during user sign-up"));
        }
        // JWT 토큰 만들기
        JwtTokenDTO loginToken = getJwtTokenDTO(user);

        //refresh token 저장
        refreshTokenService.saveRefreshToken(user.getEmail(), loginToken.getRefreshToken());

        //UserDTO
        UserDTO loggedInUser = UserDTO.builder().
                userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .loginType(LoginType.GOOGLE)
                .remainInterview(user.getRemainInterview())
                .build();
        log.debug("@구글 로그인 : {} 로그인 성공", loggedInUser.getEmail());

        return UserAuthResponseDTO.builder()
                .user(loggedInUser)
                .token(loginToken)
                .build();
    }

    public boolean checkDuplicateEmail(DuplicateEmailRequestDTO requestDTO) {
        return userRepository.existsByEmail(requestDTO.getEmail());
    }


    // 사용자 삭제
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotExistIdException(UserErrorCode.NOT_EXIST_USER));
        userRepository.delete(user);
    }

    // 사용자 비밀번호 변경
    public void changeUserPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotExistIdException(UserErrorCode.NOT_EXIST_USER));
        String newEncryptedPassword = PasswordEncrypter.encrypt(newPassword);
        user.setPassword(newEncryptedPassword);
        userRepository.save(user);
    }

    // refreshToken 기반 accessToken 재발급
    public JwtTokenDTO generateNewAccessToken(String token) {
        RefreshToken refreshToken = refreshTokenService.findByRefreshToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found"));
        User user = refreshToken.getUser();

        // JWT 토큰 만들기
        JwtTokenDTO newToken = getJwtTokenDTO(user);

        refreshTokenService.saveRefreshToken(user.getEmail(), newToken.getRefreshToken());
        return newToken;
    }

    // 현재 로그인한 유저 정보 반환
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return UserDTO.builder().
                userId(user.getId())
                .name(user.getName())
                .loginType(user.getLoginType())
                .email(user.getEmail())
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }

    // 비밀번호 암호화
    private String encryptPassword(String password) {
        return PasswordEncrypter.encrypt(password);
    }

    /**
     * 사용자 정보를 이용하여 토큰 발행하는 메소드
     * @param user 사용자 정보
     * @return JwtTokenDTO
     */
    private JwtTokenDTO getJwtTokenDTO(User user) {
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                customUserDetails.getAuthorities()
        );

        return tokenProvider.createToken(authenticationToken);
    }
}
