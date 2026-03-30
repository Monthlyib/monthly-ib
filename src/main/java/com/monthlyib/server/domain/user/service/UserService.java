package com.monthlyib.server.domain.user.service;

import com.monthlyib.server.api.user.dto.UserPatchRequestDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.api.user.dto.UserSocialPatchRequestDto;
import com.monthlyib.server.auth.dto.OauthInfo;
import com.monthlyib.server.auth.jwt.JwtTokenizer;
import com.monthlyib.server.auth.service.NaverAuthService;
import com.monthlyib.server.auth.service.RefreshService;
import com.monthlyib.server.auth.service.VerifyNumService;
import com.monthlyib.server.auth.token.Token;
import com.monthlyib.server.constant.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.entity.UserImage;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.event.UserSendEmailEvent;
import com.monthlyib.server.event.UserVerificationEvent;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.openapi.user.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final JwtTokenizer tokenizer;

    private final RefreshService refreshService;

    private final HttpClientService httpClientService;

    private final ApplicationEventPublisher publisher;

    private final VerifyNumService verifyNumService;

    private final NaverAuthService naverAuthService;

    private final FileService fileService;

    public Page<UserResponseDto> findAll(int page, User user) {
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED); // 접근 거부 예외 처리
        }
        
        return userRepository.findAll(PageRequest.of(page, 15, Sort.by("createAt").descending()))
                .map(u -> UserResponseDto.of(u, firstUserImage(u.getUserId())));
    }
    

    public LoginApiResponseDto userLogin(LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        User findUser = findUserByUsername(username);
        UserStatus userStatus = findUser.getUserStatus();
        verifyUserStatus(userStatus);
        if (password.equals(findUser.getPassword()) || passwordEncoder.matches(password, findUser.getPassword())) {
            Token token = tokenizer.delegateToken(findUser);
            attachRefreshTokenCookie(request, response, token.getRefreshToken());
            return LoginApiResponseDto.of(token.getAccessToken(), findUser);
        } else {
            throw new ServiceLogicException(ErrorCode.WRONG_PASSWORD);
        }
    }

    public LoginApiResponseDto loginSocial(
            SocialLoginDto socialLoginDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        String email = httpClientService.generateLoginRequest(socialLoginDto);
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, socialLoginDto.getLoginType());
            Token token = tokenizer.delegateToken(user);
            attachRefreshTokenCookie(request, response, token.getRefreshToken());
            response.setHeader("userId", String.valueOf(user.getUserId()));
            response.setHeader("userStatus", user.getUserStatus().name());
            return LoginApiResponseDto.of(token.getAccessToken(), user);
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.USER_EXIST)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
                response.setHeader("userLoginType", user.getLoginType().name());
            }
            throw e;
        }
    }

    public LoginApiResponseDto loginSocialNaver(NaverLoginRequest naverLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        OauthInfo info = naverAuthService.getNaverInfo(naverLoginRequest);
        String email = info.getEmail();
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, info.getLoginType().name());
            Token token = tokenizer.delegateToken(user);
            attachRefreshTokenCookie(request, response, token.getRefreshToken());
            response.setHeader("userId", String.valueOf(user.getUserId()));
            response.setHeader("userStatus", user.getUserStatus().name());
            return LoginApiResponseDto.of(token.getAccessToken(), user);
        } catch (ServiceLogicException e) {
            if (e.getErrorCode().equals(ErrorCode.USER_EXIST)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
                response.setHeader("userLoginType", user.getLoginType().name());
            }
            throw e;
        }
    }

    public UserResponseDto findUserById(Long userId, User requestUser) {
        validateAdminOrSelf(requestUser, userId);
        return UserResponseDto.of(findUserEntity(userId), firstUserImage(userId));
    }

    public UserResponseDto findUserByIdForSystem(Long userId) {
        return UserResponseDto.of(findUserEntity(userId), firstUserImage(userId));
    }

    public UserResponseDto updateUser(Long userId, UserPatchRequestDto dto, User requestUser) {
        validateAdminOrSelf(requestUser, userId);
        if (!isAdmin(requestUser)) {
            dto.setAuthority(null);
            dto.setUserStatus(null);
            dto.setMemo(null);
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
        validateAdminOrSelf(requestUser, userId);
        User findUser = findUserEntity(userId);
        User updateUser = findUser.updateSocialUser(dto);
        updateUser.setUserStatus(UserStatus.ACTIVE);
        return UserResponseDto.of(userRepository.save(updateUser), firstUserImage(userId));
    }

    public UserResponseDto createUser(UserPostRequestDto dto) {
        Optional<User> user = verifyEmail(dto.getEmail());
        if (user.isPresent())
            throw new ServiceLogicException(ErrorCode.USER_EXIST);
        try {
            verifyNum(VerifyNumRequestDto.builder().email(dto.getEmail()).verifyNum(dto.getVerifyNum()).build());
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.EXPIRED_VERIFY);
        }

        String password = dto.getPassword();
        String pwd = passwordEncoder.encode(password);
        dto.setPassword(pwd);
        User newUser = User.createUser(dto);
        User saveUser = userRepository.save(newUser);
        return UserResponseDto.of(saveUser, firstUserImage(saveUser.getUserId()));
    }

    public void verifyEmailNumPost(EmailRequestDto dto) {
        String email = dto.getEmail();
        publisher.publishEvent(new UserVerificationEvent(this, email));
    }

    public void verifyPwdEmail(EmailRequestDto dto) {
        String email = dto.getEmail();
        publisher.publishEvent(new UserVerificationEvent(this, email));
    }

    public UserResponseDto verifyNum(VerifyNumRequestDto dto) {
        String verifyNum = dto.getVerifyNum();
        String email = dto.getEmail();
        VerifyNumDto num = verifyNumService.getNum(email);

        // Verify if the provided verification number matches
        if (!verifyNum.equals(num.getVerifyNum())) {
            throw new ServiceLogicException(ErrorCode.WRONG_VERIFY_NUM);
        }

        // Check if pwdReset is true and proceed with user lookup and reset if needed
        if (dto.getPwdReset() != null && dto.getPwdReset()) {
            User findUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

            // Generate and encode a new random password
            String newPassword = UUID.randomUUID().toString().substring(0, 12);
            String encodedPassword = passwordEncoder.encode(newPassword);
            findUser.setPassword(encodedPassword);

            // Send the reset password to the user's email
            publisher.publishEvent(new UserSendEmailEvent(
                    this,
                    email,
                    findUser.getNickName(),
                    "회원님의 재설정 비밀번호는 " + newPassword + " 입니다."));

            // Save the updated user with new password
            userRepository.save(findUser);
            return UserResponseDto.of(findUser, null);
        }

        // Return null or an appropriate response when verification succeeds without
        // pwdReset
        return null;
    }

    public void verifyUsername(UsernameVerifyDto dto) {
        String username = dto.getUsername();
        verifyUsername(username);
    }

    public void deleteUser(Long userId, User user) {
        if (!isAdmin(user) && !isSameUser(user, userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
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
                // 회원가입
                return userRepository.save(User.createEmptyUser(email, loginType));
            } else {
                throw e;
            }
        }
    }

    public LoginApiResponseDto refreshToken(Long userId, HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
            String refreshToken = getCookieValue(request, REFRESH_TOKEN_COOKIE);
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new ServiceLogicException(ErrorCode.NOT_FOUND_COOKIE);
            }
            RefreshDto refresh = refreshService.getRefresh(user.getUsername());
            tokenizer.verifyAccessToken(refreshToken);
            if (!Objects.equals(refresh.getRefreshToken(), refreshToken)) {
                throw new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            }
            if (!Objects.equals(user.getUsername(), tokenizer.getUsername(refreshToken))) {
                throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
            }
            Token token = tokenizer.delegateToken(user);
            attachRefreshTokenCookie(request, response, token.getRefreshToken());
            return LoginApiResponseDto.of(token.getAccessToken(), user);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception ex) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public UserResponseDto createOrUpdateUserImage(Long userId, MultipartFile[] files, User requestUser) {
        validateAdminOrSelf(requestUser, userId);
        User findUser = findUserEntity(userId);
        List<UserImage> currentList = userRepository.findAllUserImage(userId);
        if (!currentList.isEmpty()) {
            currentList.forEach(m -> fileService.deleteAwsFile(m.getFileName(), AwsProperty.USER_IMAGE));
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

    private void validateAdminOrSelf(User requestUser, Long targetUserId) {
        if (!isAdmin(requestUser) && !isSameUser(requestUser, targetUserId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean isAdmin(User user) {
        return user.getAuthority().equals(Authority.ADMIN);
    }

    private boolean isSameUser(User user, Long targetUserId) {
        return user.getUserId().equals(targetUserId);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void attachRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        boolean secure = request.isSecure() || Optional.ofNullable(request.getHeader("Origin"))
                .map(origin -> origin.startsWith("https://"))
                .orElse(false);
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60L * 60 * 24 * 35);

        if (secure) {
            builder.secure(true).sameSite("None");
        } else {
            builder.secure(false).sameSite("Lax");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

}
