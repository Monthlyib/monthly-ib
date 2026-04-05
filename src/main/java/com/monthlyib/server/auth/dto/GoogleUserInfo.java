package com.monthlyib.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleUserInfo {

    private String sub;

    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    private String name;
}
