package com.monthlyib.server.auth.service;

import com.monthlyib.server.auth.entity.VerifyNumEntity;
import com.monthlyib.server.auth.repository.VerifyNumRepository;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.openapi.user.dto.VerifyNumDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerifyNumService {

    private final VerifyNumRepository verifyNumRepository;

    public VerifyNumDto createNum(String email, String verifyNum) {
        VerifyNumEntity entity = new VerifyNumEntity(email, verifyNum);
        return VerifyNumDto.of(verifyNumRepository.save(entity));
    }

    public VerifyNumDto getNum(String email) {
        VerifyNumEntity entity = verifyNumRepository.findById(email)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.EXPIRED_REFRESH_TOKEN));
        return VerifyNumDto.of(entity);
    }

    public void deleteRefresh(String email) {
        verifyNumRepository.deleteById(email);
    }

    public List<VerifyNumDto> getAll() {
        ArrayList<VerifyNumDto> list = new ArrayList<>();
        verifyNumRepository.findAll().forEach(a-> list.add(VerifyNumDto.of(a)));
        return list;
    }
}
