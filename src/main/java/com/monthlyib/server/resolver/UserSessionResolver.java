package com.monthlyib.server.resolver;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserSessionResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 지원하는 파라미터 , 어노테이션 체크
        // AOP 방식으로 실행하기 위한 리졸버
        boolean annotation = parameter.hasParameterAnnotation(UserSession.class);
        boolean parameterType = parameter.getParameterType().equals(User.class);

        return annotation && parameterType;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        // support parameter 에서 true 반환시 여기 실행
        // JwtAuthorizationFilter 에서 Context에 userId 넣어둠
        // 사용자 정보 셋팅
        RequestAttributes requestContext = RequestContextHolder.getRequestAttributes();
        Object userId = requestContext.getAttribute("userId", RequestAttributes.SCOPE_REQUEST);
        User user = userRepository.findById(Long.parseLong(userId.toString()))
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        User setUser = User.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickName(user.getNickName())
                .userStatus(user.getUserStatus())
                .subscriptionId(user.getSubscriptionId())
                .roles(user.getRoles())
                .loginType(user.getLoginType())
                .authority(user.getAuthority())
                .build();
        setUser.setCreateAt(user.getCreateAt());
        return setUser;
    }
}