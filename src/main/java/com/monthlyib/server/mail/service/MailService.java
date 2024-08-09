package com.monthlyib.server.mail.service;

import com.monthlyib.server.api.mail.dto.MailPostRequestDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.event.UserSendEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final ApplicationEventPublisher publisher;

    private final UserService userService;

    public boolean sendMail(MailPostRequestDto dto) {
        List<Long> targetUserId = dto.getTargetUserId();
        for (Long id : targetUserId) {
            UserResponseDto findUser = userService.findUserById(id);
            publisher.publishEvent(new UserSendEmailEvent(this, findUser.getEmail(), findUser.getNickName(), dto.getContent()));
        }
        return true;
    }
}
