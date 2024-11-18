package com.monthlyib.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {

    @Schema(description = "Result Data")
    private Result result;

    @Schema(description = "Response Data Body")
    private T data;

    public static <T> ResponseDto<T> of(T data, Result result) {
        ResponseDto<T> response = new ResponseDto<>();
        response.result = result;
        response.data = data;
        return response;
    }

    public static <T> ResponseDto<T> of(Result result) {
        ResponseDto<T> response = new ResponseDto<>();
        response.result = result;
        response.data = null;
        return response;
    }
}