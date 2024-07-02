package com.monthlyib.server.domain.tutoring.service;

import com.monthlyib.server.api.tutoring.dto.*;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.constant.TutoringTime;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import com.monthlyib.server.domain.tutoring.repository.TutoringRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.event.UserTutoringConfirmEvent;
import com.monthlyib.server.event.UserVerificationEvent;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TutoringService {

    private final TutoringRepository tutoringRepository;

    private final ApplicationEventPublisher publisher;

    private final UserRepository userRepository;


    public TutoringSimpleResponseDto findTutoringSimple(TutoringSearchDto dto) {
        LocalDate date = Optional.ofNullable(dto.getDate()).orElse(LocalDate.now());

        List<TutoringRemainDto> response = new ArrayList<>();
        Arrays.stream(TutoringTime.values()).forEach(t -> {
            List<Tutoring> findTutoring = tutoringRepository.findAllByDate(date, t.getHour(), t.getMinute());
            int requestTutoring = findTutoring.size();
            int totalTutoring = 3;
            TutoringRemainDto tutoringRemainDto = TutoringRemainDto.of(
                    t.getHour(),
                    t.getMinute(),
                    totalTutoring - requestTutoring,
                    totalTutoring,
                    findTutoring.stream().map(Tutoring::getTutoringId).toList()
            );
            response.add(tutoringRemainDto);
        });

        return TutoringSimpleResponseDto.of(dto.getDate(), response);
    }

    public TutoringDetailResponseDto findTutoringDetail(TutoringAdminSearchDto dto, User user) {
        Authority authority = user.getAuthority();
        Long userId = null;
        if (!authority.equals(Authority.ADMIN)) {
            userId = user.getUserId();
        }
        int page = dto.getPage();
        Page<TutoringResponseDto> response = tutoringRepository.findAllDtoByDate(
                dto.getDate(),
                dto.getStatus(),
                userId,
                PageRequest.of(page, 30, Sort.by("createAt").descending())
        );
        return TutoringDetailResponseDto.of(dto.getDate(), PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    public List<TutoringResponseDto> findTimeTutoring(TutoringTimeSearchDto dto) {
        return tutoringRepository.findAllByDate(dto.getDate(), dto.getHour(), dto.getMinute())
                .stream().map(TutoringResponseDto::of).toList();
    }

    public TutoringResponseDto createTutoring(TutoringPostRequestDto dto, User user) {
        List<Tutoring> find = tutoringRepository.findAllByDate(dto.getDate(), dto.getHour(), dto.getMinute());
        if (find.size() == 3) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST);
        }
        Tutoring newTutoring = Tutoring.create(dto, user.getUsername(), user.getNickName());
        Tutoring save = tutoringRepository.save(newTutoring);
        return TutoringResponseDto.of(save);
    }

    public TutoringResponseDto updateTutoring(TutoringPatchRequestDto dto, User user, Long tutoringId) {
        Tutoring findTutoring = tutoringRepository.findByTutoringId(tutoringId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (!user.getAuthority().equals(Authority.ADMIN) && !findTutoring.getRequestUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        findTutoring.setDetail(Optional.ofNullable(dto.getDetail()).orElse(findTutoring.getDetail()));
        TutoringStatus tutoringStatus = dto.getTutoringStatus();
        findTutoring.setTutoringStatus(Optional.ofNullable(tutoringStatus).orElse(findTutoring.getTutoringStatus()));
        if (tutoringStatus.equals(TutoringStatus.CONFIRM)) {
            User findUser = userRepository.findById(findTutoring.getRequestUserId())
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
            publisher.publishEvent(new UserTutoringConfirmEvent(this, findUser.getEmail(), findUser.getUsername()));
        }
        Tutoring save = tutoringRepository.save(findTutoring);
        return TutoringResponseDto.of(save);
    }

    public void deleteTutoring(Long tutoringId, User user) {
        Tutoring findTutoring = tutoringRepository.findByTutoringId(tutoringId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (!user.getAuthority().equals(Authority.ADMIN) && !findTutoring.getRequestUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        tutoringRepository.delete(tutoringId);
    }
}
