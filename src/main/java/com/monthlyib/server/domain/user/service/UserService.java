package com.monthlyib.server.domain.user.service;

import com.monthlyib.server.api.user.dto.UserPatchRequestDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.api.user.dto.UserSocialPatchRequestDto;
import com.monthlyib.server.auth.dto.OauthInfo;
import com.monthlyib.server.auth.service.GoogleAuthService;
import com.monthlyib.server.auth.jwt.JwtTokenizer;
import com.monthlyib.server.auth.service.NaverAuthService;
import com.monthlyib.server.auth.service.RefreshService;
import com.monthlyib.server.auth.service.VerifyNumService;
import com.monthlyib.server.auth.token.Token;
import com.monthlyib.server.constant.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.entity.UserImage;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.mail.service.EmailSender;
import com.monthlyib.server.openapi.user.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final JwtTokenizer tokenizer;

    private final RefreshService refreshService;

    private final HttpClientService httpClientService;

    private final VerifyNumService verifyNumService;

    private final NaverAuthService naverAuthService;

    private final GoogleAuthService googleAuthService;

    private final FileService fileService;

    private final EmailSender emailSender;

    @Value("${mail.subject.user.verification}")
    private String verificationSubject;

    @Value("${mail.subject.user.password-reset}")
    private String passwordResetSubject;

    @Value("${mail.template.name.user.join}")
    private String verificationTemplateName;

    @Value("${mail.template.name.user.password-reset}")
    private String passwordResetTemplateName;

    public Page<UserResponseDto> findAll(int page, User user) {
        verifyAdmin(user);

        return userRepository.findAll(PageRequest.of(page, 15, Sort.by("createAt").descending()))
                .map( u -> {
                    return UserResponseDto.of(u, firstUserImage(u.getUserId()));
                });
    }


    public LoginApiResponseDto userLogin(LoginDto loginDto) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        User findUser = findUserByUsername(username);
        UserStatus userStatus = findUser.getUserStatus();
        verifyUserStatus(userStatus);
        if (password.equals(findUser.getPassword()) || passwordEncoder.matches(password, findUser.getPassword())) {
            return issueLoginResponse(findUser);
        } else {
            throw new ServiceLogicException(ErrorCode.WRONG_PASSWORD);
        }
    }

    public LoginApiResponseDto loginSocial(
            SocialLoginDto socialLoginDto,
            HttpServletResponse response
    ) {
        if (LoginType.GOOGLE.name().equalsIgnoreCase(socialLoginDto.getLoginType())) {
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google 로그인은 전용 인증 플로우를 사용해야 합니다."
            );
        }

        String email = httpClientService.generateLoginRequest(socialLoginDto);
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, socialLoginDto.getLoginType());
            LoginApiResponseDto loginResponse = issueLoginResponse(user);
            response.setHeader("userId", String.valueOf(user.getUserId()));
            response.setHeader("userStatus", user.getUserStatus().name());
            return loginResponse;
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.USER_EXIST)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
                response.setHeader("userLoginType", user.getLoginType().name());
            }
            throw e;
        }
    }

    public LoginApiResponseDto loginSocialNaver(NaverLoginRequest naverLoginRequest, HttpServletResponse response) {
        OauthInfo info = naverAuthService.getNaverInfo(naverLoginRequest);
        String email = info.getEmail();
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, info.getLoginType().name());
            LoginApiResponseDto loginResponse = issueLoginResponse(user);
            response.setHeader("userId", String.valueOf(user.getUserId()));
            response.setHeader("userStatus", user.getUserStatus().name());
            return loginResponse;
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.USER_EXIST)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
                response.setHeader("userLoginType", user.getLoginType().name());
            }
            throw e;
        }
    }

    public LoginApiResponseDto loginSocialGoogle(GoogleLoginRequest googleLoginRequest, HttpServletResponse response) {
        OauthInfo info = googleAuthService.getGoogleInfo(googleLoginRequest);
        String email = info.getEmail();
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, info.getLoginType().name());
            LoginApiResponseDto loginResponse = issueLoginResponse(user);
            response.setHeader("userId", String.valueOf(user.getUserId()));
            response.setHeader("userStatus", user.getUserStatus().name());
            return loginResponse;
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.USER_EXIST)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
                response.setHeader("userLoginType", user.getLoginType().name());
            }
            throw e;
        }
    }

    public UserResponseDto findUserById(Long userId, User user) {
        verifyAdminOrSelf(user, userId);

        return UserResponseDto.of(findUserEntity(userId), firstUserImage(userId));
    }

    public UserResponseDto updateUser(Long userId, UserPatchRequestDto dto, User user) {
        verifyAdminOrSelf(user, userId);
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            dto.setAuthority(null);
            dto.setUserStatus(null);
        }
        User findUser = findUserEntity(userId);
        String password = dto.getPassword();
        if (password != null) {
            String encodePassword = passwordEncoder.encode(password);
            dto.setPassword(encodePassword);
        }
        User updateUser = findUser.updateUser(dto);
        return UserResponseDto.of(userRepository.save(updateUser), firstUserImage(userId));
    }


    public UserResponseDto updateSocialUser(Long userId, UserSocialPatchRequestDto dto, User requestUser) {
        verifyAdminOrSelf(requestUser, userId);
        User findUser = findUserEntity(userId);
        verifySocialPatchRequest(dto);
        if (!findUser.getUsername().equals(dto.getUsername())) {
            verifyUsername(dto.getUsername());
        }
        User updateUser = findUser.updateSocialUser(dto);
        updateUser.setUserStatus(UserStatus.ACTIVE);
        return UserResponseDto.of(userRepository.save(updateUser), firstUserImage(userId));
    }

    public UserResponseDto createUser(UserPostRequestDto dto) {
        Optional<User> user = verifyEmail(dto.getEmail());
        if (user.isPresent()) throw new ServiceLogicException(ErrorCode.USER_EXIST);
        verifyNum(VerifyNumRequestDto.builder().email(dto.getEmail()).verifyNum(dto.getVerifyNum()).build());

        String password = dto.getPassword();
        String pwd = passwordEncoder.encode(password);
        dto.setPassword(pwd);
        User newUser = User.createUser(dto);
        User saveUser = userRepository.save(newUser);
        verifyNumService.deleteNum(dto.getEmail());
        return UserResponseDto.of(saveUser, firstUserImage(saveUser.getUserId()));
    }

    public void verifyEmailNumPost(EmailRequestDto dto) {
        sendVerificationEmail(dto.getEmail());
    }


    public void verifyPwdEmail(EmailRequestDto dto) {
        sendVerificationEmail(dto.getEmail());
    }

    public void verifyNum(VerifyNumRequestDto dto) {
        String verifyNum = dto.getVerifyNum() == null ? "" : dto.getVerifyNum().trim();
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim();

        if (email.isBlank()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "이메일을 입력해주세요.");
        }
        if (verifyNum.isBlank()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "인증번호를 입력해주세요.");
        }

        VerifyNumDto num = verifyNumService.getNum(email);
        if (num.getCreatedAt() != null && num.getCreatedAt().plusMinutes(30).isBefore(LocalDateTime.now())) {
            verifyNumService.deleteNum(email);
            throw new ServiceLogicException(ErrorCode.EXPIRED_VERIFY);
        }
        if (!verifyNum.equals(num.getVerifyNum())) {
            throw new ServiceLogicException(ErrorCode.WRONG_VERIFY_NUM);
        }
    }

    public void resetPassword(PasswordResetRequestDto dto) {
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim();

        if (username.isBlank()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "아이디를 입력해주세요.");
        }
        if (email.isBlank()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "이메일을 입력해주세요.");
        }

        verifyNum(VerifyNumRequestDto.builder()
                .email(email)
                .verifyNum(dto.getVerifyNum())
                .build());

        User user = findUserByUsername(username);
        verifyUserStatus(user.getUserStatus());

        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new ServiceLogicException(ErrorCode.NOT_FOUND_USER, "입력한 아이디와 이메일이 일치하는 회원을 찾을 수 없습니다.");
        }

        if (!LoginType.BASIC.equals(user.getLoginType())) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "소셜 로그인 계정은 해당 소셜 로그인으로 이용해주세요.");
        }

        String temporaryPassword = generateTemporaryPassword(10);
        String[] to = new String[]{email};
        String message = user.getNickName() + "님, 임시 비밀번호는 " + temporaryPassword + " 입니다. 로그인 후 반드시 비밀번호를 변경해주세요.";

        try {
            emailSender.sendEmail(
                    to,
                    passwordResetSubject,
                    message,
                    passwordResetTemplateName,
                    java.util.Map.of("recipientName", user.getNickName())
            );
        } catch (MailSendException e) {
            log.error("Failed to send password reset email to {}", email, e);
            throw new ServiceLogicException(ErrorCode.MAIL_SEND_FAILED, "임시 비밀번호 메일 발송에 실패했습니다. 다시 시도해주세요.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Password reset email sending interrupted for {}", email, e);
            throw new ServiceLogicException(ErrorCode.MAIL_SEND_FAILED, "임시 비밀번호 메일 발송에 실패했습니다. 다시 시도해주세요.");
        }

        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        verifyNumService.deleteNum(email);
    }

    public void verifyUsername(UsernameVerifyDto dto) {
        String username = dto.getUsername();
        verifyUsername(username);
    }


    public void deleteUser(Long userId, User user) {
        verifyAdmin(user);
        User findUser = findUserEntity(userId);
        findUser.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(findUser);

    }

    public void deleteUser(Long userId) {
        User findUser = findUserEntity(userId);
        findUser.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(findUser);
    }


    private User createOrVerifiedUserByEmailAndLoginType(String email, String loginType) {
        try {
            User findUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
            if (findUser.getUserStatus().equals(UserStatus.INACTIVE)) {
                throw new ServiceLogicException(ErrorCode.USER_INACTIVE);
            } else if (!findUser.getLoginType().equals(LoginType.valueOf(loginType.toUpperCase()))) {
                throw new ServiceLogicException(ErrorCode.USER_EXIST);
            } else {
                return findUser;
            }
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.NOT_FOUND_USER)) {
                //회원가입
                return userRepository.save(User.createEmptyUser(email, loginType));
            } else {
                throw e;
            }
        }
    }

    private void sendVerificationEmail(String email) {
        String resolvedEmail = email == null ? "" : email.trim();
        if (resolvedEmail.isBlank()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "이메일을 입력해주세요.");
        }

        String verificationNum = generateVerificationNumber(6);
        String[] to = new String[]{resolvedEmail};
        String message = resolvedEmail + "님, 인증번호는 " + verificationNum + " 입니다.";

        verifyNumService.createNum(resolvedEmail, verificationNum);

        try {
            emailSender.sendEmail(to, verificationSubject, message, verificationTemplateName);
        } catch (MailSendException e) {
            verifyNumService.deleteNum(resolvedEmail);
            log.error("Failed to send verification email to {}", resolvedEmail, e);
            throw new ServiceLogicException(ErrorCode.VERIFY_EMAIL_SEND_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            verifyNumService.deleteNum(resolvedEmail);
            log.error("Verification email sending interrupted for {}", resolvedEmail, e);
            throw new ServiceLogicException(ErrorCode.VERIFY_EMAIL_SEND_FAILED);
        }
    }

    private String generateVerificationNumber(int len) {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();

        while (numStr.length() < len) {
            String ran = Integer.toString(rand.nextInt(10));
            if (numStr.indexOf(ran) < 0) {
                numStr.append(ran);
            }
        }

        return numStr.toString();
    }

    private String generateTemporaryPassword(int len) {
        final String candidates = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        Random rand = new Random();
        StringBuilder password = new StringBuilder();

        while (password.length() < len) {
            password.append(candidates.charAt(rand.nextInt(candidates.length())));
        }

        return password.toString();
    }

    private void verifyAdmin(User user) {
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void verifyAdminOrSelf(User user, Long userId) {
        if (!user.getAuthority().equals(Authority.ADMIN) && !user.getUserId().equals(userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void verifySocialPatchRequest(UserSocialPatchRequestDto dto) {
        if (dto == null
                || isBlank(dto.getUsername())
                || isBlank(dto.getNickName())
                || isBlank(dto.getBirth())
                || isBlank(dto.getSchool())
                || isBlank(dto.getGrade())
                || isBlank(dto.getAddress())
                || isBlank(dto.getCountry())) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "필수 회원 정보가 누락되었습니다.");
        }

        if (!dto.isTermsOfUseCheck() || !dto.isPrivacyTermsCheck()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "필수 약관 동의가 필요합니다.");
        }
    }


    public LoginApiResponseDto refreshToken(Long userId, RefreshTokenRequestDto requestDto) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
            String requestRefreshToken = Optional.ofNullable(requestDto)
                    .map(RefreshTokenRequestDto::getRefreshToken)
                    .filter(token -> !token.isBlank())
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN));
            RefreshDto refresh = refreshService.getRefresh(user.getUsername());
            tokenizer.verifyAccessToken(requestRefreshToken);
            if (!refresh.getRefreshToken().equals(requestRefreshToken)) {
                throw new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            }
            String refreshUsername = tokenizer.getUsername(requestRefreshToken);
            if (!user.getUsername().equals(refreshUsername)) {
                throw new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            }
            long refreshSessionVersion = Long.parseLong(String.valueOf(
                    tokenizer.getClaims(
                            requestRefreshToken,
                            tokenizer.encodeBase64SecretKey(tokenizer.getSecretKey())
                    ).getBody().getOrDefault("sessionVersion", 0L)
            ));
            long currentSessionVersion = user.getSessionVersion() == null ? 0L : user.getSessionVersion();
            if (refreshSessionVersion != currentSessionVersion) {
                throw new ServiceLogicException(ErrorCode.SESSION_EXPIRED_BY_NEW_LOGIN);
            }
            user.touchLastAccessAt();
            userRepository.save(user);
            Token token = tokenizer.delegateToken(user);
            return LoginApiResponseDto.of(token, user);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (Exception ex) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public UserResponseDto createOrUpdateUserImage(Long userId, MultipartFile[] files) {
        User findUser = findUserEntity(userId);
        List<UserImage> currentList = userRepository.findAllUserImage(userId);
        if (!currentList.isEmpty()) {
            currentList.forEach( m ->
                    fileService.deleteAwsFile(m.getFileName(), AwsProperty.USER_IMAGE)
            );
        }
        userRepository.deleteAllUserImage(userId);
        UserImage image = null;
        for (MultipartFile file : files) {
            String url = fileService.saveMultipartFileForAws(file, AwsProperty.USER_IMAGE);
            String filename = file.getOriginalFilename();
            UserImage userImage = UserImage.create(filename, url, userId);
            image = userRepository.saveUserImage(userImage);
        }
        return UserResponseDto.of(findUser, image);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
    }

    public void verifyUsername(String username) {
        Optional<User> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            throw new ServiceLogicException(ErrorCode.USER_EXIST);
        }
    }

    public Optional<User> verifyEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
    }

    public void verifyUserStatus(UserStatus userStatus) {
        if (userStatus.equals(UserStatus.INACTIVE)) {
            throw new ServiceLogicException(ErrorCode.BLOCK_OR_INACTIVE_USER);
        }
    }

    private UserImage firstUserImage(Long userId) {
        List<UserImage> image = userRepository.findAllUserImage(userId);
        if (image.isEmpty()) {
            return null;
        } else {
            return image.get(0);
        }
    }

    private LoginApiResponseDto issueLoginResponse(User user) {
        User persistedUser = rotateUserSession(user);
        Token token = tokenizer.delegateToken(persistedUser);
        return LoginApiResponseDto.of(token, persistedUser);
    }

    private User rotateUserSession(User user) {
        long currentSessionVersion = user.getSessionVersion() == null ? 0L : user.getSessionVersion();
        user.setSessionVersion(currentSessionVersion + 1L);
        user.touchLastAccessAt();
        return userRepository.save(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }


}
