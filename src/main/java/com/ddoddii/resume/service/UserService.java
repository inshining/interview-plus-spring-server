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
import com.ddoddii.resume.model.Resume;
import com.ddoddii.resume.model.User;
import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.model.eunm.RoleType;
import com.ddoddii.resume.repository.RefreshTokenRepository;
import com.ddoddii.resume.repository.ResumeRepository;
import com.ddoddii.resume.repository.UserRepository;
import com.ddoddii.resume.security.CustomUserDetails;
import com.ddoddii.resume.security.TokenProvider;
import com.ddoddii.resume.util.PasswordEncrypter;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final ResumeRepository resumeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private static final Integer REMAIN_INTERVIEW = 5;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 사용자 회원가입시 사용자 정보와 로그인 토큰도 함께 반환
    public UserAuthResponseDTO emailSignUpAndLogin(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO) {
        UserDTO newUser = signUp(userEmailSignUpRequestDTO, LoginType.EMAIL);
        UserEmailLoginRequestDTO loginRequestDTO = UserEmailLoginRequestDTO.builder()
                .email(newUser.getEmail())
                .password(userEmailSignUpRequestDTO.getPassword())
                .build();
        return emailLogin(loginRequestDTO, LoginType.EMAIL);
    }

    // 이메일 회원가입
    public UserDTO signUp(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO, LoginType loginType) {
        if (userRepository.existsByEmail(userEmailSignUpRequestDTO.getEmail())) {
            throw new DuplicateIdException(UserErrorCode.DUPLICATE_USER);
        }

        // 사용자 비밀번호 암호화
        String encryptedPassword = encryptPassword(userEmailSignUpRequestDTO.getPassword());

        // 사용자 정보 저장
        User user = getUser(userEmailSignUpRequestDTO, encryptedPassword, loginType);

        // 명시적으로 save 유저 변수 추출
        User saveUser = userRepository.save(user);

        // 유저 정보 반환
        return UserDTO.builder().
                userId(saveUser.getId())
                .name(saveUser.getName())
                .email(saveUser.getEmail())
                .loginType(saveUser.getLoginType())
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }

    // 사용자 로그인
    public UserAuthResponseDTO emailLogin(UserEmailLoginRequestDTO userLoginRequestDTO, LoginType loginType) {
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
                .loginType(loginType)
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
        User user = User.signUpUser(userGoogleLoginRequestDTO.getName(), userGoogleLoginRequestDTO.getEmail(),
                encryptPassword);
        user.setLoginType(LoginType.GOOGLE);

        User saveUser = userRepository.save(user);
        logger.info("{} 가 저장되었습니다. ", user.getEmail());

        return UserDTO.builder().
                userId(saveUser.getId())
                .name(saveUser.getName())
                .email(saveUser.getEmail())
                .loginType(saveUser.getLoginType())
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
                .loginType(user.getLoginType())
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

    public UserAuthResponseDTO guestSignUpAndLogin() {
        int length = 15;
        boolean useLetters = true;
        boolean useNumbers = false;
        String generatedRandomId = RandomStringUtils.random(length, useLetters, useNumbers);
        String randomPassword = RandomStringUtils.random(length, useLetters, useNumbers);
        UserEmailSignUpRequestDTO guestSignUpRequestDTO = UserEmailSignUpRequestDTO.builder()
                .email(generatedRandomId)
                .name("guest")
                .password(randomPassword)
                .build();
        UserDTO guestUser = signUp(guestSignUpRequestDTO, LoginType.GUEST);
        UserEmailLoginRequestDTO guestLoginRequestDTO = UserEmailLoginRequestDTO.builder()
                .email(guestUser.getEmail())
                .password(guestSignUpRequestDTO.getPassword())
                .build();
        return emailLogin(guestLoginRequestDTO, LoginType.GUEST);
    }

    // 게스트 회원가입 & 로그인
    public void guestEmailSignUpAndLogin(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO
    ) {
        User guestUser = getCurrentUser();
        guestUser.setName(userEmailSignUpRequestDTO.getName());
        guestUser.setEmail(userEmailSignUpRequestDTO.getEmail());
        guestUser.setPassword(userEmailSignUpRequestDTO.getPassword());
        userRepository.save(guestUser);
    }

    public void guestGoogleSignUpAndLogin(UserGoogleLoginRequestDTO userGoogleLoginRequestDTO) {
        User guestUser = getCurrentUser();
        guestUser.setName(userGoogleLoginRequestDTO.getName());
        guestUser.setEmail(userGoogleLoginRequestDTO.getEmail());
        String encryptedIdToken = PasswordEncrypter.encrypt(userGoogleLoginRequestDTO.getIdToken());
        guestUser.setPassword(encryptedIdToken);
        userRepository.save(guestUser);
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
                // TODO: 예외처리 명확히 명시하기 (NotFoundRefreshTokenException)
                .orElseThrow(() -> new NotExistIdException(UserErrorCode.NOT_EXIST_USER));
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
        Resume defaultResume = null;
        List<Resume> resumes = resumeRepository.findByUser(user);
        for (Resume resume : resumes) {
            if (resume.isDefault()) {
                defaultResume = resume;
                break;
            }
        }
        return UserDTO.builder().
                userId(user.getId())
                .name(user.getName())
                .loginType(user.getLoginType())
                .email(user.getEmail())
                .defaultResume(defaultResume != null ? defaultResume.getName() : null)
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }


    // 사용자 유저 정보 반환
    private User getUser(UserEmailSignUpRequestDTO userEmailSignUpRequestDTO, String encryptedPassword,
                         LoginType loginType) {
        User user = new User();
        user.setPassword(encryptedPassword);
        user.setName(userEmailSignUpRequestDTO.getName());
        user.setEmail(userEmailSignUpRequestDTO.getEmail());
        user.setRole(RoleType.ROLE_USER);
        user.setRemainInterview(REMAIN_INTERVIEW);
        user.setLoginType(loginType);
        return user;
    }

    // 비밀번호 암호화
    private String encryptPassword(String password) {
        return PasswordEncrypter.encrypt(password);
    }

    /**
     * 사용자 정보를 이용하여 토큰 발행하는 메소드
     *
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
