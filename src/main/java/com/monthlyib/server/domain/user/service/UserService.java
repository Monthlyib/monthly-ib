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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private final ApplicationEventPublisher publisher;

    private final VerifyNumService verifyNumService;

    private final NaverAuthService naverAuthService;

    private final FileService fileService;

    public Page<UserResponseDto> findAll(int page, User user) {

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
            Token token = tokenizer.delegateToken(findUser);
            return LoginApiResponseDto.of(token.getAccessToken(), findUser);
        } else {
            throw new ServiceLogicException(ErrorCode.WRONG_PASSWORD);
        }
    }

    public LoginApiResponseDto loginSocial(
            SocialLoginDto socialLoginDto,
            HttpServletResponse response
    ) {
        String email = httpClientService.generateLoginRequest(socialLoginDto);
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, socialLoginDto.getLoginType());
            Token token = tokenizer.delegateToken(user);
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

    public LoginApiResponseDto loginSocialNaver(NaverLoginRequest naverLoginRequest, HttpServletResponse response) {
        OauthInfo info = naverAuthService.getNaverInfo(naverLoginRequest);
        String email = info.getEmail();
        try {
            User user = createOrVerifiedUserByEmailAndLoginType(email, info.getLoginType().name());
            Token token = tokenizer.delegateToken(user);
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

    public UserResponseDto findUserById(Long userId) {

        return UserResponseDto.of(findUserEntity(userId), firstUserImage(userId));
    }

    public UserResponseDto updateUser(Long userId, UserPatchRequestDto dto) {
        User findUser = findUserEntity(userId);
        String password = dto.getPassword();
        if (password != null) {
            String encodePassword = passwordEncoder.encode(password);
            dto.setPassword(encodePassword);
        }
        User updateUser = findUser.updateUser(dto);
        return UserResponseDto.of(userRepository.save(updateUser), firstUserImage(userId));
    }


    public UserResponseDto updateSocialUser(Long userId, UserSocialPatchRequestDto dto) {
        User findUser = findUserEntity(userId);
        User updateUser = findUser.updateSocialUser(dto);
        updateUser.setUserStatus(UserStatus.ACTIVE);
        return UserResponseDto.of(userRepository.save(updateUser), firstUserImage(userId));
    }

    public UserResponseDto createUser(UserPostRequestDto dto) {
        Optional<User> user = verifyEmail(dto.getEmail());
        if (user.isPresent()) throw new ServiceLogicException(ErrorCode.USER_EXIST);
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
        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
        if (!verifyNum.equals(num.getVerifyNum())) {
            throw new ServiceLogicException(ErrorCode.WRONG_VERIFY_NUM);
        }
        if (dto.getPwdReset()) {
            String substring = UUID.randomUUID().toString().substring(0, 12);
            String encodePassword = passwordEncoder.encode(substring);
            findUser.setPassword(encodePassword);
            publisher.publishEvent(new UserSendEmailEvent(
                    this,
                    email,
                    findUser.getNickName(),
                    "회원님의 재설정 비밀번호는  " +substring+"  입니다."
            ));
            userRepository.save(findUser);
        }
        return UserResponseDto.of(findUser, null);
    }

    public void verifyUsername(UsernameVerifyDto dto) {
        String username = dto.getUsername();
        verifyUsername(username);
    }


    public void deleteUser(Long userId, User user) {
        if (user.getAuthority().equals(Authority.ADMIN)) {
            User findUser = findUserEntity(userId);
            findUser.setUserStatus(UserStatus.INACTIVE);
            userRepository.save(findUser);
        }

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


    public LoginApiResponseDto refreshToken(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
            RefreshDto refresh = refreshService.getRefresh(user.getUsername());
            tokenizer.verifyAccessToken(refresh.getRefreshToken());
            Token token = tokenizer.delegateToken(user);
            return LoginApiResponseDto.of(token.getAccessToken(), user);
        } catch (ServiceLogicException e) {
            throw e;
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


}
