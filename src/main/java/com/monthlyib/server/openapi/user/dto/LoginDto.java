package com.monthlyib.server.openapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LoginDto {

    @Schema(description = "아이디", defaultValue = "admin")
    private String username;

    @Schema(description = "비밀번호", defaultValue = "1111!")
    private String password;

}