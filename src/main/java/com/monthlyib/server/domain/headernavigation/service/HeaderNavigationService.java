package com.monthlyib.server.domain.headernavigation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.headernavigation.dto.HeaderNavigationAdminResponseDto;
import com.monthlyib.server.api.headernavigation.dto.HeaderNavigationConfigDto;
import com.monthlyib.server.api.headernavigation.dto.HeaderNavigationMenuDto;
import com.monthlyib.server.api.headernavigation.dto.HeaderNavigationPublicResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.headernavigation.entity.HeaderNavigationPage;
import com.monthlyib.server.domain.headernavigation.repository.HeaderNavigationPageJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
public class HeaderNavigationService {

    private static final String PAGE_KEY = "HEADER_NAVIGATION";
    private static final List<HeaderNavigationMenuDto> DEFAULT_MENUS = List.of(
            menu("ai-tools", "AI Tools", "/aitools", false, 0, List.of()),
            menu("monthly-ib", "월간 IB", "/ib", false, 1, List.of()),
            menu("course", "영상강의", "/course", false, 2, List.of()),
            menu("resources", "자료실", "/board", false, 3, List.of(
                    menu("board-news", "IB 입시뉴스", "/board", false, 0, List.of()),
                    menu("board-calculator", "합격예측 계산기", "/board/calculator", false, 1, List.of()),
                    menu("board-download", "자료실", "/board/download", false, 2, List.of()),
                    menu("board-free", "자유게시판", "/board/free", false, 3, List.of())
            )),
            menu("tutoring", "튜터링 예약", "/tutoring", false, 4, List.of(
                    menu("tutoring-booking", "튜터링 예약", "/tutoring", false, 0, List.of()),
                    menu("question", "질문하기", "/question", false, 1, List.of())
            )),
            menu("learningtest", "학습유형 테스트", "/learningtest", false, 5, List.of()),
            menu("offline-class", "학원 현장강의", "http://monthlyib.co.kr/contact", true, 6, List.of())
    );

    private final HeaderNavigationPageJpaRepository headerNavigationPageJpaRepository;
    private final ObjectMapper objectMapper;

    public HeaderNavigationPublicResponseDto getPublishedConfig() {
        return headerNavigationPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(this::toPublicResponse)
                .orElseGet(() -> HeaderNavigationPublicResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .config(createDefaultConfig())
                        .updatedAt(null)
                        .build());
    }

    public HeaderNavigationAdminResponseDto getAdminConfig(User user) {
        validateAdmin(user);
        return headerNavigationPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(this::toAdminResponse)
                .orElseGet(() -> HeaderNavigationAdminResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .config(createDefaultConfig())
                        .updatedBy(null)
                        .updatedAt(null)
                        .build());
    }

    public HeaderNavigationAdminResponseDto saveConfig(HeaderNavigationConfigDto requestDto, User user) {
        validateAdmin(user);

        HeaderNavigationConfigDto sanitized = sanitizeConfig(requestDto);
        String json = writeConfig(sanitized);

        HeaderNavigationPage page = headerNavigationPageJpaRepository.findByPageKey(PAGE_KEY)
                .orElseGet(() -> HeaderNavigationPage.create(PAGE_KEY, json, user.getUserId()));

        if (page.getId() != null) {
            page.updateConfig(json, user.getUserId());
        }

        headerNavigationPageJpaRepository.save(page);
        return toAdminResponse(page);
    }

    private HeaderNavigationPublicResponseDto toPublicResponse(HeaderNavigationPage page) {
        return HeaderNavigationPublicResponseDto.builder()
                .pageKey(page.getPageKey())
                .config(readConfig(page.getConfigJson()))
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private HeaderNavigationAdminResponseDto toAdminResponse(HeaderNavigationPage page) {
        return HeaderNavigationAdminResponseDto.builder()
                .pageKey(page.getPageKey())
                .config(readConfig(page.getConfigJson()))
                .updatedBy(page.getUpdatedBy())
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private HeaderNavigationConfigDto readConfig(String json) {
        try {
            return sanitizeConfig(objectMapper.readValue(json, HeaderNavigationConfigDto.class));
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "헤더 메뉴 설정을 읽을 수 없습니다.");
        }
    }

    private String writeConfig(HeaderNavigationConfigDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "헤더 메뉴 설정을 저장할 수 없습니다.");
        }
    }

    private HeaderNavigationConfigDto createDefaultConfig() {
        return HeaderNavigationConfigDto.builder()
                .menus(cloneMenus(DEFAULT_MENUS))
                .build();
    }

    private HeaderNavigationConfigDto sanitizeConfig(HeaderNavigationConfigDto requestDto) {
        List<HeaderNavigationMenuDto> sanitizedMenus = sanitizeMenus(
                Optional.ofNullable(requestDto)
                        .map(HeaderNavigationConfigDto::getMenus)
                        .orElseGet(ArrayList::new),
                0
        );

        if (sanitizedMenus.isEmpty()) {
            sanitizedMenus = cloneMenus(DEFAULT_MENUS);
        }

        return HeaderNavigationConfigDto.builder()
                .menus(sanitizedMenus)
                .build();
    }

    private List<HeaderNavigationMenuDto> sanitizeMenus(List<HeaderNavigationMenuDto> requestMenus, int depth) {
        List<HeaderNavigationMenuDto> menus = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();

        requestMenus.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(menu -> Optional.ofNullable(menu.getOrder()).orElse(Integer.MAX_VALUE)))
                .forEach(menu -> {
                    HeaderNavigationMenuDto sanitized = sanitizeMenu(menu, depth, usedKeys, menus.size());
                    if (sanitized != null) {
                        menus.add(sanitized);
                    }
                });

        AtomicInteger order = new AtomicInteger(0);
        return menus.stream()
                .map(menu -> menu.toBuilder()
                        .order(order.getAndIncrement())
                        .children(depth == 0 ? reindexChildren(menu.getChildren()) : List.of())
                        .build())
                .toList();
    }

    private List<HeaderNavigationMenuDto> reindexChildren(List<HeaderNavigationMenuDto> children) {
        AtomicInteger order = new AtomicInteger(0);
        return Optional.ofNullable(children).orElseGet(ArrayList::new).stream()
                .map(child -> child.toBuilder()
                        .order(order.getAndIncrement())
                        .children(List.of())
                        .build())
                .toList();
    }

    private HeaderNavigationMenuDto sanitizeMenu(
            HeaderNavigationMenuDto requestMenu,
            int depth,
            Set<String> usedKeys,
            int fallbackIndex
    ) {
        String label = normalizeText(requestMenu.getLabel());
        String href = normalizeText(requestMenu.getHref());
        boolean hasChildren = depth == 0 && requestMenu.getChildren() != null && !requestMenu.getChildren().isEmpty();

        if (!StringUtils.hasText(label)) {
            if (!hasChildren) {
                return null;
            }
            label = StringUtils.hasText(href) ? href : "새 메뉴";
        }

        if (!hasChildren && !StringUtils.hasText(href)) {
            return null;
        }

        String keySeed = normalizeText(requestMenu.getKey());
        if (!StringUtils.hasText(keySeed)) {
            keySeed = StringUtils.hasText(label) ? label : href;
        }
        String key = ensureUniqueKey(normalizeKey(keySeed), usedKeys, fallbackIndex);
        boolean visible = Optional.ofNullable(requestMenu.getVisible()).orElse(Boolean.TRUE);
        boolean external = Optional.ofNullable(requestMenu.getExternal()).orElse(Boolean.FALSE);
        List<HeaderNavigationMenuDto> children =
                depth == 0 ? sanitizeMenus(Optional.ofNullable(requestMenu.getChildren()).orElseGet(ArrayList::new), 1) : List.of();

        return HeaderNavigationMenuDto.builder()
                .key(key)
                .label(label)
                .href(href)
                .visible(visible)
                .external(external)
                .order(Optional.ofNullable(requestMenu.getOrder()).orElse(fallbackIndex))
                .children(children)
                .build();
    }

    private List<HeaderNavigationMenuDto> cloneMenus(List<HeaderNavigationMenuDto> menus) {
        return Optional.ofNullable(menus).orElseGet(ArrayList::new).stream()
                .map(menu -> HeaderNavigationMenuDto.builder()
                        .key(menu.getKey())
                        .label(menu.getLabel())
                        .href(menu.getHref())
                        .visible(menu.getVisible())
                        .external(menu.getExternal())
                        .order(menu.getOrder())
                        .children(cloneMenus(menu.getChildren()))
                        .build())
                .toList();
    }

    private void validateAdmin(User user) {
        if (user == null || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private static HeaderNavigationMenuDto menu(
            String key,
            String label,
            String href,
            boolean external,
            int order,
            List<HeaderNavigationMenuDto> children
    ) {
        return HeaderNavigationMenuDto.builder()
                .key(key)
                .label(label)
                .href(href)
                .visible(Boolean.TRUE)
                .external(external)
                .order(order)
                .children(children)
                .build();
    }

    private String ensureUniqueKey(String normalizedKey, Set<String> usedKeys, int fallbackIndex) {
        String baseKey = StringUtils.hasText(normalizedKey) ? normalizedKey : "menu-" + (fallbackIndex + 1);
        String candidate = baseKey;
        int suffix = 2;
        while (usedKeys.contains(candidate)) {
            candidate = baseKey + "-" + suffix++;
        }
        usedKeys.add(candidate);
        return candidate;
    }

    private String normalizeKey(String value) {
        String normalized = normalizeText(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return StringUtils.hasText(normalized) ? normalized : "";
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
