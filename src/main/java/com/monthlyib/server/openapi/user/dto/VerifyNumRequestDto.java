package com.monthlyib.server.openapi.user.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyNumRequestDto {

    private String email;

    private String verifyNum;

    private Boolean pwdReset;

}
