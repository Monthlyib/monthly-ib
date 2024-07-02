package com.monthlyib.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {


    @Schema(description = "Result Data")
    private Result result;

    @Schema(description = "Response Data Body")
    private T data;

    @Schema(description = "Response Data PageInfo")
    private PageInfo pageInfo;

    public static <T> PageResponseDto<T> of(Page page, T data, Result result) {
        PageResponseDto<T> response = new PageResponseDto<>();
        response.result = result;
        response.data = data;
        response.pageInfo = new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return response;
    }

    public static <T> PageResponseDto<T> of(Result result) {
        PageResponseDto<T> response = new PageResponseDto<>();
        response.result = result;
        response.data = null;
        response.pageInfo = new PageInfo();
        return response;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static private class PageInfo {
        @Schema(description = "현재 페이지")
        private int page;
        @Schema(description = "페이지 사이즈")
        private int size;
        @Schema(description = "조회된 총 데이터")
        private long totalElements;
        @Schema(description = "조회된 총 페이지")
        private int totalPages;
    }
}
