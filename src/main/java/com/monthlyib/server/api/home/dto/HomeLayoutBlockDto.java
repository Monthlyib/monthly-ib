package com.monthlyib.server.api.home.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutBlockDto {

    private String id;

    private String type;

    private HomeLayoutBlockPropsDto props;
}
