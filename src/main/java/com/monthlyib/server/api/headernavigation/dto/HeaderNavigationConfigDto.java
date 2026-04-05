package com.monthlyib.server.api.headernavigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderNavigationConfigDto {

    private List<HeaderNavigationMenuDto> menus;
}
