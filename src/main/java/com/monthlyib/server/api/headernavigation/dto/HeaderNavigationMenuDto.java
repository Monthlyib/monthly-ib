package com.monthlyib.server.api.headernavigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HeaderNavigationMenuDto {

    private String key;

    private String label;

    private String href;

    private Boolean visible;

    private Boolean external;

    private Integer order;

    private List<HeaderNavigationMenuDto> children;
}
