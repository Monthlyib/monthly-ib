package com.monthlyib.server.openapi.user.dto;


import com.monthlyib.server.auth.entity.VerifyNumEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyNumRequestDto {

    private String email;

    private String verifyNum;

}
