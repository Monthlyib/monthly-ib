package com.monthlyib.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Schema(description = "응답 상태 코드")
    private Integer status;

    @Schema(description = "응답 상태 메세지")
    private String message;

    public static Result ok() {
        return Result.builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .build();
    }

    public static Result create() {
        return Result.builder()
                .status(HttpStatus.CREATED.value())
                .message("Create")
                .build();
    }

    public static Result error() {
        return Result.builder()
                .status(0)
                .message("Error")
                .build();
    }
}