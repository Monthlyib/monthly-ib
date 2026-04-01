package com.monthlyib.server.api.home.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutRowDto {

    private String id;

    private String layout;

    private List<HomeLayoutColumnDto> columns;
}
