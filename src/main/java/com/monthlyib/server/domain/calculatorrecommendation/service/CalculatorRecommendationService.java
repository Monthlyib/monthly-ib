package com.monthlyib.server.domain.calculatorrecommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.calculatorrecommendation.dto.*;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.calculatorrecommendation.entity.CalculatorRecommendationPage;
import com.monthlyib.server.domain.calculatorrecommendation.repository.CalculatorRecommendationPageJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CalculatorRecommendationService {

    private static final String PAGE_KEY = "BOARD_CALCULATOR";
    private static final List<CalculatorRecommendationGroupDto> DEFAULT_GROUPS = List.of(
            defaultGroup("Group1", "Group 1", 2, List.of(
                    subject("English Literature", true, true),
                    subject("English Language", true, true),
                    subject("Korean", true, true)
            )),
            defaultGroup("Group2", "Group 2", 2, List.of(
                    subject("English B", true, true),
                    subject("Mandarin B", true, true),
                    subject("Spanish B", true, true)
            )),
            defaultGroup("Group3", "Group 3", 2, List.of(
                    subject("Economics", true, true),
                    subject("Business & Management", true, true),
                    subject("Psychology", true, true),
                    subject("Geography", true, true),
                    subject("History", true, true),
                    subject("Global Politics", true, true),
                    subject("Digital Society", true, true),
                    subject("Philosophy", true, true),
                    subject("Social & Cultural Anthropology", true, true),
                    subject("World Religions", true, false)
            )),
            defaultGroup("Group4", "Group 4", 2, List.of(
                    subject("Physics", true, true),
                    subject("Chemistry", true, true),
                    subject("Biology", true, true),
                    subject("Design Technology", true, true),
                    subject("Sports, Exercise & Health Science", true, true),
                    subject("Environmental System & Societies", true, true)
            )),
            defaultGroup("Group5", "Group 5", 1, List.of(
                    subject("Math AA", true, true),
                    subject("Math AI", true, true)
            )),
            defaultGroup("Group6", "Group 6", 1, List.of(
                    subject("Visual Arts", true, true),
                    subject("Dance", true, true),
                    subject("Music", true, true),
                    subject("Film", true, true),
                    subject("Theatre", true, true)
            ))
    );

    private final CalculatorRecommendationPageJpaRepository calculatorRecommendationPageJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "calculatorRecommendationPublicConfig", key = "'" + PAGE_KEY + "'")
    public CalculatorRecommendationPublicResponseDto getPublishedConfig() {
        return calculatorRecommendationPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(this::toPublicResponse)
                .orElseGet(() -> CalculatorRecommendationPublicResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .config(null)
                        .updatedAt(null)
                        .build());
    }

    @Transactional(readOnly = true)
    public CalculatorRecommendationAdminResponseDto getAdminConfig() {
        return calculatorRecommendationPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(this::toAdminResponse)
                .orElseGet(() -> CalculatorRecommendationAdminResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .config(null)
                        .updatedBy(null)
                        .updatedAt(null)
                        .build());
    }

    @CacheEvict(cacheNames = "calculatorRecommendationPublicConfig", key = "'" + PAGE_KEY + "'")
    public CalculatorRecommendationAdminResponseDto saveConfig(CalculatorRecommendationConfigDto requestDto, User user) {
        CalculatorRecommendationConfigDto sanitized = sanitizeConfig(requestDto);
        String json = writeConfig(sanitized);

        CalculatorRecommendationPage page = calculatorRecommendationPageJpaRepository.findByPageKey(PAGE_KEY)
                .orElseGet(() -> CalculatorRecommendationPage.create(PAGE_KEY, json, user.getUserId()));

        if (page.getId() != null) {
            page.updateConfig(json, user.getUserId());
        }

        calculatorRecommendationPageJpaRepository.save(page);
        return toAdminResponse(page);
    }

    private CalculatorRecommendationPublicResponseDto toPublicResponse(CalculatorRecommendationPage page) {
        return CalculatorRecommendationPublicResponseDto.builder()
                .pageKey(page.getPageKey())
                .config(readConfig(page.getConfigJson()))
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private CalculatorRecommendationAdminResponseDto toAdminResponse(CalculatorRecommendationPage page) {
        return CalculatorRecommendationAdminResponseDto.builder()
                .pageKey(page.getPageKey())
                .config(readConfig(page.getConfigJson()))
                .updatedBy(page.getUpdatedBy())
                .updatedAt(page.getUpdatedAt())
                .build();
    }

    private CalculatorRecommendationConfigDto readConfig(String json) {
        try {
            return sanitizeConfig(objectMapper.readValue(json, CalculatorRecommendationConfigDto.class));
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "추천학교 설정을 읽을 수 없습니다.");
        }
    }

    private String writeConfig(CalculatorRecommendationConfigDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "추천학교 설정을 저장할 수 없습니다.");
        }
    }

    private CalculatorRecommendationConfigDto sanitizeConfig(CalculatorRecommendationConfigDto requestDto) {
        List<CalculatorRecommendationGroupDto> sanitizedGroups = sanitizeGroups(
                Optional.ofNullable(requestDto)
                        .map(CalculatorRecommendationConfigDto::getGroups)
                        .orElseGet(ArrayList::new)
        );

        List<CalculatorRecommendationBandDto> sanitizedBands = sanitizeBands(
                Optional.ofNullable(requestDto)
                        .map(CalculatorRecommendationConfigDto::getScoreBands)
                        .orElseGet(ArrayList::new)
        );

        List<String> bandKeys = sanitizedBands.stream()
                .map(CalculatorRecommendationBandDto::getKey)
                .toList();

        List<CalculatorRecommendationCountryDto> sanitizedCountries = sanitizeCountries(
                Optional.ofNullable(requestDto)
                        .map(CalculatorRecommendationConfigDto::getCountries)
                        .orElseGet(ArrayList::new),
                bandKeys
        );

        return CalculatorRecommendationConfigDto.builder()
                .groups(sanitizedGroups)
                .scoreBands(sanitizedBands)
                .countries(sanitizedCountries)
                .build();
    }

    private List<CalculatorRecommendationGroupDto> sanitizeGroups(List<CalculatorRecommendationGroupDto> requestGroups) {
        Map<String, CalculatorRecommendationGroupDto> requestGroupMap = requestGroups.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        group -> normalizeGroupKey(group.getKey()),
                        group -> group,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        return DEFAULT_GROUPS.stream()
                .map(defaultGroup -> sanitizeGroup(
                        defaultGroup,
                        requestGroupMap.getOrDefault(defaultGroup.getKey(), defaultGroup)
                ))
                .toList();
    }

    private CalculatorRecommendationGroupDto sanitizeGroup(
            CalculatorRecommendationGroupDto defaultGroup,
            CalculatorRecommendationGroupDto requestGroup
    ) {
        List<CalculatorRecommendationGroupSubjectDto> subjects = sanitizeGroupSubjects(
                Optional.ofNullable(requestGroup)
                        .map(CalculatorRecommendationGroupDto::getSubjects)
                        .orElseGet(ArrayList::new),
                defaultGroup.getSubjects()
        );

        int maxSelectableCount = Optional.ofNullable(requestGroup)
                .map(CalculatorRecommendationGroupDto::getMaxSelectableCount)
                .filter(value -> value > 0)
                .map(value -> Math.min(value, 6))
                .orElse(defaultGroup.getMaxSelectableCount());

        String label = normalizeText(Optional.ofNullable(requestGroup)
                .map(CalculatorRecommendationGroupDto::getLabel)
                .orElse(defaultGroup.getLabel()));

        return CalculatorRecommendationGroupDto.builder()
                .key(defaultGroup.getKey())
                .label(StringUtils.hasText(label) ? label : defaultGroup.getLabel())
                .maxSelectableCount(maxSelectableCount)
                .subjects(subjects)
                .build();
    }

    private List<CalculatorRecommendationGroupSubjectDto> sanitizeGroupSubjects(
            List<CalculatorRecommendationGroupSubjectDto> requestSubjects,
            List<CalculatorRecommendationGroupSubjectDto> defaultSubjects
    ) {
        Set<String> usedNames = new HashSet<>();
        List<CalculatorRecommendationGroupSubjectDto> subjects = new ArrayList<>();

        for (CalculatorRecommendationGroupSubjectDto requestSubject : requestSubjects) {
            String name = normalizeText(Optional.ofNullable(requestSubject)
                    .map(CalculatorRecommendationGroupSubjectDto::getName)
                    .orElse(null));

            if (!StringUtils.hasText(name) || usedNames.contains(name)) {
                continue;
            }

            boolean slEnabled = Optional.ofNullable(requestSubject)
                    .map(CalculatorRecommendationGroupSubjectDto::getSlEnabled)
                    .orElse(Boolean.TRUE);
            boolean hlEnabled = Optional.ofNullable(requestSubject)
                    .map(CalculatorRecommendationGroupSubjectDto::getHlEnabled)
                    .orElse(Boolean.TRUE);

            if (!slEnabled && !hlEnabled) {
                slEnabled = true;
                hlEnabled = true;
            }

            usedNames.add(name);
            subjects.add(subject(name, slEnabled, hlEnabled));
        }

        if (subjects.isEmpty()) {
            return defaultSubjects.stream()
                    .map(subject -> subject(subject.getName(), subject.getSlEnabled(), subject.getHlEnabled()))
                    .toList();
        }

        return subjects;
    }

    private List<CalculatorRecommendationBandDto> sanitizeBands(List<CalculatorRecommendationBandDto> requestBands) {
        Set<String> usedKeys = new HashSet<>();
        List<CalculatorRecommendationBandDto> bands = new ArrayList<>();

        for (CalculatorRecommendationBandDto requestBand : requestBands) {
            Integer minScore = Optional.ofNullable(requestBand)
                    .map(CalculatorRecommendationBandDto::getMinScore)
                    .orElse(0);

            String key = normalizeText(Optional.ofNullable(requestBand)
                    .map(CalculatorRecommendationBandDto::getKey)
                    .orElse(String.valueOf(minScore)));

            if (!StringUtils.hasText(key) || usedKeys.contains(key)) {
                continue;
            }

            usedKeys.add(key);

            String label = normalizeText(Optional.ofNullable(requestBand)
                    .map(CalculatorRecommendationBandDto::getLabel)
                    .orElse(key + "+"));

            if (!StringUtils.hasText(label)) {
                label = key + "+";
            }

            bands.add(CalculatorRecommendationBandDto.builder()
                    .key(key)
                    .label(label)
                    .minScore(minScore)
                    .build());
        }

        return bands.stream()
                .sorted(Comparator.comparingInt((CalculatorRecommendationBandDto band) -> Optional.ofNullable(band.getMinScore()).orElse(0))
                        .reversed())
                .toList();
    }

    private List<CalculatorRecommendationCountryDto> sanitizeCountries(
            List<CalculatorRecommendationCountryDto> requestCountries,
            List<String> bandKeys
    ) {
        Set<String> usedCodes = new HashSet<>();
        List<CalculatorRecommendationCountryDto> countries = new ArrayList<>();

        for (CalculatorRecommendationCountryDto requestCountry : requestCountries) {
            String code = normalizeCode(Optional.ofNullable(requestCountry)
                    .map(CalculatorRecommendationCountryDto::getCode)
                    .orElse(null));

            if (!StringUtils.hasText(code) || usedCodes.contains(code) || "all".equals(code)) {
                continue;
            }

            usedCodes.add(code);

            String label = normalizeText(Optional.ofNullable(requestCountry)
                    .map(CalculatorRecommendationCountryDto::getLabel)
                    .orElse(code.toUpperCase(Locale.ROOT)));

            List<CalculatorRecommendationSchoolDto> schools = sanitizeSchools(
                    Optional.ofNullable(requestCountry)
                            .map(CalculatorRecommendationCountryDto::getSchools)
                            .orElseGet(ArrayList::new),
                    bandKeys
            );

            countries.add(CalculatorRecommendationCountryDto.builder()
                    .code(code)
                    .label(StringUtils.hasText(label) ? label : code.toUpperCase(Locale.ROOT))
                    .schools(schools)
                    .build());
        }

        return countries;
    }

    private List<CalculatorRecommendationSchoolDto> sanitizeSchools(
            List<CalculatorRecommendationSchoolDto> requestSchools,
            List<String> bandKeys
    ) {
        Set<String> usedIds = new HashSet<>();
        List<CalculatorRecommendationSchoolDto> schools = new ArrayList<>();

        for (CalculatorRecommendationSchoolDto requestSchool : requestSchools) {
            String id = resolveSchoolId(Optional.ofNullable(requestSchool)
                    .map(CalculatorRecommendationSchoolDto::getId)
                    .orElse(null));

            while (usedIds.contains(id)) {
                id = resolveSchoolId(null);
            }
            usedIds.add(id);

            String name = normalizeText(Optional.ofNullable(requestSchool)
                    .map(CalculatorRecommendationSchoolDto::getName)
                    .orElse("새 학교"));

            List<String> sanitizedBandKeys = Optional.ofNullable(requestSchool)
                    .map(CalculatorRecommendationSchoolDto::getBandKeys)
                    .orElseGet(ArrayList::new)
                    .stream()
                    .map(this::normalizeText)
                    .filter(StringUtils::hasText)
                    .filter(bandKeys::contains)
                    .distinct()
                    .collect(Collectors.toList());

            schools.add(CalculatorRecommendationSchoolDto.builder()
                    .id(id)
                    .name(StringUtils.hasText(name) ? name : "새 학교")
                    .img(normalizeText(Optional.ofNullable(requestSchool).map(CalculatorRecommendationSchoolDto::getImg).orElse("")))
                    .ibScore(normalizeText(Optional.ofNullable(requestSchool).map(CalculatorRecommendationSchoolDto::getIbScore).orElse("")))
                    .rank(normalizeText(Optional.ofNullable(requestSchool).map(CalculatorRecommendationSchoolDto::getRank).orElse("")))
                    .tuition(normalizeText(Optional.ofNullable(requestSchool).map(CalculatorRecommendationSchoolDto::getTuition).orElse("")))
                    .bandKeys(sanitizedBandKeys)
                    .build());
        }

        return schools;
    }

    private String resolveSchoolId(String id) {
        String normalized = normalizeText(id);
        return StringUtils.hasText(normalized) ? normalized : UUID.randomUUID().toString();
    }

    private String normalizeCode(String code) {
        String normalized = normalizeText(code);
        return StringUtils.hasText(normalized) ? normalized.toLowerCase(Locale.ROOT) : "";
    }

    private String normalizeGroupKey(String key) {
        String normalized = normalizeText(key);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }

        return normalized.replace(" ", "");
    }

    private String normalizeText(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }

    private static CalculatorRecommendationGroupDto defaultGroup(
            String key,
            String label,
            int maxSelectableCount,
            List<CalculatorRecommendationGroupSubjectDto> subjects
    ) {
        return CalculatorRecommendationGroupDto.builder()
                .key(key)
                .label(label)
                .maxSelectableCount(maxSelectableCount)
                .subjects(subjects)
                .build();
    }

    private static CalculatorRecommendationGroupSubjectDto subject(
            String name,
            boolean slEnabled,
            boolean hlEnabled
    ) {
        return CalculatorRecommendationGroupSubjectDto.builder()
                .name(name)
                .slEnabled(slEnabled)
                .hlEnabled(hlEnabled)
                .build();
    }
}
