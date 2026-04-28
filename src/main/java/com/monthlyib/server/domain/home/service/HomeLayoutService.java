package com.monthlyib.server.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.home.dto.*;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.home.entity.HomeLayoutPage;
import com.monthlyib.server.domain.home.repository.HomeLayoutPageJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeLayoutService {

    private static final String PAGE_KEY = "HOME";
    private static final String HOME_MEDIA_PATH = "home-layout/";
    private static final Set<String> UNIQUE_BLOCK_TYPES = Set.of(
            "existingHero",
            "existingSearch",
            "existingGuideLinks",
            "existingMemberActivity",
            "existingReviewCarousel"
    );
    private static final Set<String> ALLOWED_BLOCK_TYPES = Set.of(
            "existingHero",
            "existingSearch",
            "existingGuideLinks",
            "existingMemberActivity",
            "existingReviewCarousel",
            "richText",
            "image",
            "video",
            "button",
            "spacer"
    );
    private static final Map<String, Integer> LAYOUT_COLUMN_COUNTS = Map.of(
            "one", 1,
            "two", 2,
            "three", 3
    );
    private static final Set<String> EMBED_VIDEO_HOSTS = Set.of(
            "youtube.com",
            "www.youtube.com",
            "youtu.be",
            "vimeo.com",
            "www.vimeo.com",
            "player.vimeo.com"
    );
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".webm", ".ogg", ".mov");

    private final HomeLayoutPageJpaRepository homeLayoutPageJpaRepository;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "homePublishedLayout", key = "'" + PAGE_KEY + "'")
    public HomeLayoutPublishedResponseDto getPublishedLayout() {
        return homeLayoutPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(page -> HomeLayoutPublishedResponseDto.builder()
                        .pageKey(page.getPageKey())
                        .layout(readLayout(page.getPublishedJson()))
                        .publishedAt(page.getPublishedAt())
                        .build())
                .orElseGet(() -> HomeLayoutPublishedResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .layout(createDefaultLayout())
                        .publishedAt(null)
                        .build());
    }

    public HomeLayoutAdminResponseDto getAdminLayout() {
        HomeLayoutPage page = ensurePage();
        return toAdminResponse(page);
    }

    public HomeLayoutAdminResponseDto saveDraft(HomeLayoutContentDto requestDto, User user) {
        HomeLayoutPage page = ensurePage();
        HomeLayoutContentDto sanitized = sanitizeLayout(requestDto, false);
        page.updateDraft(writeLayout(sanitized), user.getUserId());
        homeLayoutPageJpaRepository.save(page);
        return toAdminResponse(page);
    }

    @CacheEvict(cacheNames = "homePublishedLayout", key = "'" + PAGE_KEY + "'")
    public HomeLayoutAdminResponseDto publish(User user) {
        HomeLayoutPage page = ensurePage();
        HomeLayoutContentDto strictLayout = sanitizeLayout(readLayout(page.getDraftJson()), true);
        page.updateDraft(writeLayout(strictLayout), user.getUserId());
        page.publish(user.getUserId());
        homeLayoutPageJpaRepository.save(page);
        return toAdminResponse(page);
    }

    public HomeLayoutAdminResponseDto resetDraft(User user) {
        HomeLayoutPage page = ensurePage();
        page.resetDraft(user.getUserId());
        homeLayoutPageJpaRepository.save(page);
        return toAdminResponse(page);
    }

    public HomeLayoutMediaResponseDto uploadMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        boolean image = contentType.startsWith("image/");
        boolean video = contentType.startsWith("video/");
        if (!image && !video) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "이미지 또는 영상 파일만 업로드할 수 있습니다.");
        }

        String addPath = HOME_MEDIA_PATH + (image ? "images/" : "videos/");
        String fileUrl = fileService.saveMultipartFileForAws(file, AwsProperty.STORAGE, addPath);
        return HomeLayoutMediaResponseDto.builder()
                .fileUrl(fileUrl)
                .mediaType(image ? "image" : "video")
                .fileName(file.getOriginalFilename())
                .build();
    }

    private HomeLayoutAdminResponseDto toAdminResponse(HomeLayoutPage page) {
        return HomeLayoutAdminResponseDto.builder()
                .pageKey(page.getPageKey())
                .draft(readLayout(page.getDraftJson()))
                .published(readLayout(page.getPublishedJson()))
                .draftUpdatedBy(page.getDraftUpdatedBy())
                .publishedBy(page.getPublishedBy())
                .draftUpdatedAt(page.getDraftUpdatedAt())
                .publishedAt(page.getPublishedAt())
                .build();
    }

    private HomeLayoutPage ensurePage() {
        return homeLayoutPageJpaRepository.findByPageKey(PAGE_KEY)
                .orElseGet(() -> {
                    String defaultJson = writeLayout(createDefaultLayout());
                    return homeLayoutPageJpaRepository.save(HomeLayoutPage.createDefault(PAGE_KEY, defaultJson));
                });
    }

    private HomeLayoutContentDto readLayout(String json) {
        try {
            HomeLayoutContentDto dto = objectMapper.readValue(json, HomeLayoutContentDto.class);
            return sanitizeLayout(dto, false);
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "홈 레이아웃을 읽을 수 없습니다.");
        }
    }

    private String writeLayout(HomeLayoutContentDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "홈 레이아웃을 저장할 수 없습니다.");
        }
    }

    private HomeLayoutContentDto sanitizeLayout(HomeLayoutContentDto dto, boolean strict) {
        List<HomeLayoutRowDto> requestRows = Optional.ofNullable(dto)
                .map(HomeLayoutContentDto::getRows)
                .orElseGet(ArrayList::new);

        Map<String, Integer> uniqueUsage = new HashMap<>();
        List<HomeLayoutRowDto> rows = new ArrayList<>();
        for (HomeLayoutRowDto requestRow : requestRows) {
            rows.add(sanitizeRow(requestRow, strict, uniqueUsage));
        }

        if (strict) {
            long blockCount = rows.stream()
                    .flatMap(row -> Optional.ofNullable(row.getColumns()).orElseGet(ArrayList::new).stream())
                    .flatMap(column -> Optional.ofNullable(column.getBlocks()).orElseGet(ArrayList::new).stream())
                    .count();

            if (rows.isEmpty() || blockCount == 0) {
                throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "최소 한 개 이상의 블록이 필요합니다.");
            }
        }

        return HomeLayoutContentDto.builder()
                .rows(rows)
                .build();
    }

    private HomeLayoutRowDto sanitizeRow(HomeLayoutRowDto row, boolean strict, Map<String, Integer> uniqueUsage) {
        String layout = normalizeText(Optional.ofNullable(row).map(HomeLayoutRowDto::getLayout).orElse("one"));
        if (!LAYOUT_COLUMN_COUNTS.containsKey(layout)) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "허용되지 않은 홈 레이아웃입니다.");
        }

        int expectedColumns = LAYOUT_COLUMN_COUNTS.get(layout);
        List<HomeLayoutColumnDto> requestColumns = Optional.ofNullable(row)
                .map(HomeLayoutRowDto::getColumns)
                .orElseGet(ArrayList::new);

        List<HomeLayoutColumnDto> normalizedColumns = new ArrayList<>();
        for (int i = 0; i < expectedColumns; i++) {
            HomeLayoutColumnDto requestColumn =
                    i < requestColumns.size() ? requestColumns.get(i) : HomeLayoutColumnDto.builder().blocks(new ArrayList<>()).build();
            normalizedColumns.add(sanitizeColumn(requestColumn, strict, uniqueUsage));
        }

        if (requestColumns.size() > expectedColumns) {
            HomeLayoutColumnDto lastColumn = normalizedColumns.get(expectedColumns - 1);
            List<HomeLayoutBlockDto> mergedBlocks = new ArrayList<>(Optional.ofNullable(lastColumn.getBlocks()).orElseGet(ArrayList::new));
            for (int i = expectedColumns; i < requestColumns.size(); i++) {
                HomeLayoutColumnDto overflowColumn = sanitizeColumn(requestColumns.get(i), strict, uniqueUsage);
                mergedBlocks.addAll(Optional.ofNullable(overflowColumn.getBlocks()).orElseGet(ArrayList::new));
            }
            lastColumn.setBlocks(mergedBlocks);
        }

        return HomeLayoutRowDto.builder()
                .id(resolveId(Optional.ofNullable(row).map(HomeLayoutRowDto::getId).orElse(null), "row"))
                .layout(layout)
                .columns(normalizedColumns)
                .build();
    }

    private HomeLayoutColumnDto sanitizeColumn(HomeLayoutColumnDto column, boolean strict, Map<String, Integer> uniqueUsage) {
        List<HomeLayoutBlockDto> requestBlocks = Optional.ofNullable(column)
                .map(HomeLayoutColumnDto::getBlocks)
                .orElseGet(ArrayList::new);

        List<HomeLayoutBlockDto> blocks = new ArrayList<>();
        for (HomeLayoutBlockDto requestBlock : requestBlocks) {
            blocks.add(sanitizeBlock(requestBlock, strict, uniqueUsage));
        }

        return HomeLayoutColumnDto.builder()
                .id(resolveId(Optional.ofNullable(column).map(HomeLayoutColumnDto::getId).orElse(null), "column"))
                .blocks(blocks)
                .build();
    }

    private HomeLayoutBlockDto sanitizeBlock(HomeLayoutBlockDto block, boolean strict, Map<String, Integer> uniqueUsage) {
        String type = normalizeText(Optional.ofNullable(block).map(HomeLayoutBlockDto::getType).orElse(null));
        if (!ALLOWED_BLOCK_TYPES.contains(type)) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "허용되지 않은 블록 타입입니다.");
        }

        if (UNIQUE_BLOCK_TYPES.contains(type)) {
            int count = uniqueUsage.merge(type, 1, Integer::sum);
            if (count > 1) {
                throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "중복 배치할 수 없는 블록입니다.");
            }
        }

        HomeLayoutBlockPropsDto props = Optional.ofNullable(block)
                .map(HomeLayoutBlockDto::getProps)
                .orElseGet(HomeLayoutBlockPropsDto::new);
        HomeLayoutBlockPropsDto sanitizedProps = switch (type) {
            case "existingHero" -> HomeLayoutBlockPropsDto.builder().build();
            case "existingSearch" -> sanitizeExistingBlockProps(props, "궁금한 키워드를 검색해보세요!", "검색 안내");
            case "existingGuideLinks" -> sanitizeExistingBlockProps(props, "IB 입시가이드", null);
            case "existingMemberActivity" -> sanitizeExistingBlockProps(props, "나의 프로필 관리", null);
            case "existingReviewCarousel" -> sanitizeExistingBlockProps(props, "수강생 리뷰", null);
            case "richText" -> HomeLayoutBlockPropsDto.builder()
                    .title(normalizeText(props.getTitle()))
                    .html(sanitizeRichTextHtml(props.getHtml()))
                    .build();
            case "image" -> sanitizeImageProps(props, strict);
            case "video" -> sanitizeVideoProps(props, strict);
            case "button" -> sanitizeButtonProps(props, strict);
            case "spacer" -> sanitizeSpacerProps(props);
            default -> throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "지원하지 않는 블록입니다.");
        };

        return HomeLayoutBlockDto.builder()
                .id(resolveId(Optional.ofNullable(block).map(HomeLayoutBlockDto::getId).orElse(null), "block"))
                .type(type)
                .props(sanitizedProps)
                .build();
    }

    private HomeLayoutBlockPropsDto sanitizeExistingBlockProps(HomeLayoutBlockPropsDto props, String defaultTitle, String defaultDescription) {
        return HomeLayoutBlockPropsDto.builder()
                .title(Optional.ofNullable(normalizeText(props.getTitle())).orElse(defaultTitle))
                .description(Optional.ofNullable(normalizeText(props.getDescription())).orElse(defaultDescription))
                .build();
    }

    private HomeLayoutBlockPropsDto sanitizeImageProps(HomeLayoutBlockPropsDto props, boolean strict) {
        String fileUrl = normalizeText(props.getFileUrl());
        if (strict && !isValidHomeMediaUrl(fileUrl)) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "이미지 블록에는 업로드된 홈 미디어만 사용할 수 있습니다.");
        }

        return HomeLayoutBlockPropsDto.builder()
                .fileUrl(fileUrl)
                .alt(normalizeText(props.getAlt()))
                .caption(normalizeText(props.getCaption()))
                .linkUrl(validateOptionalLinkUrl(props.getLinkUrl(), strict))
                .build();
    }

    private HomeLayoutBlockPropsDto sanitizeVideoProps(HomeLayoutBlockPropsDto props, boolean strict) {
        String sourceType = Optional.ofNullable(normalizeText(props.getSourceType())).orElse("embedUrl");
        if (!sourceType.equals("embedUrl") && !sourceType.equals("uploadedFile")) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "지원하지 않는 영상 타입입니다.");
        }

        String fileUrl = normalizeText(props.getFileUrl());
        String embedUrl = normalizeText(props.getEmbedUrl());

        if (strict) {
            if (sourceType.equals("uploadedFile") && !isValidHomeMediaUrl(fileUrl)) {
                throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "업로드된 홈 영상만 사용할 수 있습니다.");
            }
            if (sourceType.equals("embedUrl") && !isValidEmbedUrl(embedUrl)) {
                throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "허용되지 않은 영상 URL입니다.");
            }
        }

        return HomeLayoutBlockPropsDto.builder()
                .sourceType(sourceType)
                .fileUrl(fileUrl)
                .embedUrl(embedUrl)
                .caption(normalizeText(props.getCaption()))
                .build();
    }

    private HomeLayoutBlockPropsDto sanitizeButtonProps(HomeLayoutBlockPropsDto props, boolean strict) {
        String label = normalizeText(props.getLabel());
        String href = validateOptionalLinkUrl(props.getHref(), strict);
        if (strict && (!StringUtils.hasText(label) || !StringUtils.hasText(href))) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "버튼 블록에는 라벨과 링크가 필요합니다.");
        }

        return HomeLayoutBlockPropsDto.builder()
                .label(label)
                .href(href)
                .variant(Optional.ofNullable(normalizeText(props.getVariant())).orElse("primary"))
                .build();
    }

    private HomeLayoutBlockPropsDto sanitizeSpacerProps(HomeLayoutBlockPropsDto props) {
        int height = Optional.ofNullable(props.getHeight()).orElse(48);
        if (height < 12) {
            height = 12;
        }
        if (height > 320) {
            height = 320;
        }

        return HomeLayoutBlockPropsDto.builder()
                .height(height)
                .build();
    }

    private String sanitizeRichTextHtml(String html) {
        String source = Optional.ofNullable(html).orElse("<p></p>");
        Safelist safelist = Safelist.relaxed()
                .addTags("div", "span")
                .addAttributes(":all", "class")
                .addProtocols("a", "href", "http", "https", "mailto");
        String cleaned = Jsoup.clean(source, safelist);
        return StringUtils.hasText(cleaned) ? cleaned : "<p></p>";
    }

    private boolean isValidHomeMediaUrl(String fileUrl) {
        return StringUtils.hasText(fileUrl) && fileUrl.contains(AwsProperty.STORAGE.getName() + HOME_MEDIA_PATH);
    }

    private boolean isValidEmbedUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }

        try {
            URI uri = URI.create(url);
            String scheme = Optional.ofNullable(uri.getScheme()).orElse("");
            String host = Optional.ofNullable(uri.getHost()).orElse("").toLowerCase(Locale.ROOT);
            String path = Optional.ofNullable(uri.getPath()).orElse("").toLowerCase(Locale.ROOT);

            if (!scheme.equals("http") && !scheme.equals("https")) {
                return false;
            }

            if (EMBED_VIDEO_HOSTS.contains(host)) {
                return true;
            }

            return VIDEO_EXTENSIONS.stream().anyMatch(path::endsWith);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String validateOptionalLinkUrl(String url, boolean strict) {
        String normalized = normalizeText(url);
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }

        boolean valid = normalized.startsWith("/") || normalized.startsWith("http://") || normalized.startsWith("https://");
        if (strict && !valid) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST, "허용되지 않은 링크 형식입니다.");
        }
        return valid ? normalized : null;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveId(String id, String prefix) {
        return StringUtils.hasText(id) ? id : prefix + "-" + UUID.randomUUID();
    }

    private HomeLayoutContentDto createDefaultLayout() {
        return HomeLayoutContentDto.builder()
                .rows(List.of(
                        createSingleColumnRow("existingHero", HomeLayoutBlockPropsDto.builder().build()),
                        createSingleColumnRow("existingSearch", HomeLayoutBlockPropsDto.builder()
                                .title("궁금한 키워드를 검색해보세요!")
                                .description("검색 안내")
                                .build()),
                        createSingleColumnRow("existingGuideLinks", HomeLayoutBlockPropsDto.builder()
                                .title("IB 입시가이드")
                                .build()),
                        createSingleColumnRow("existingMemberActivity", HomeLayoutBlockPropsDto.builder()
                                .title("나의 프로필 관리")
                                .build()),
                        createSingleColumnRow("existingReviewCarousel", HomeLayoutBlockPropsDto.builder()
                                .title("수강생 리뷰")
                                .build())
                ))
                .build();
    }

    private HomeLayoutRowDto createSingleColumnRow(String blockType, HomeLayoutBlockPropsDto props) {
        String rowId = "row-" + UUID.randomUUID();
        String columnId = "column-" + UUID.randomUUID();
        return HomeLayoutRowDto.builder()
                .id(rowId)
                .layout("one")
                .columns(List.of(
                        HomeLayoutColumnDto.builder()
                                .id(columnId)
                                .blocks(List.of(
                                        HomeLayoutBlockDto.builder()
                                                .id("block-" + UUID.randomUUID())
                                                .type(blockType)
                                                .props(props)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
