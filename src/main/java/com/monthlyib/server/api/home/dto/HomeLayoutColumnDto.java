package com.monthlyib.server.api.home.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutColumnDto {

    private String id;

    private List<HomeLayoutBlockDto> blocks;
}
