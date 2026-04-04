package com.monthlyib.server.domain.calculatorrecommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.calculatorrecommendation.dto.*;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.calculatorrecommendation.entity.CalculatorRecommendationPage;
import com.monthlyib.server.domain.calculatorrecommendation.repository.CalculatorRecommendationPageJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CalculatorRecommendationService {

    private static final String PAGE_KEY = "BOARD_CALCULATOR";

    private final CalculatorRecommendationPageJpaRepository calculatorRecommendationPageJpaRepository;
    private final ObjectMapper objectMapper;

    public CalculatorRecommendationPublicResponseDto getPublishedConfig() {
        return calculatorRecommendationPageJpaRepository.findByPageKey(PAGE_KEY)
                .map(this::toPublicResponse)
                .orElseGet(() -> CalculatorRecommendationPublicResponseDto.builder()
                        .pageKey(PAGE_KEY)
                        .config(null)
                        .updatedAt(null)
                        .build());
    }

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
                .scoreBands(sanitizedBands)
                .countries(sanitizedCountries)
                .build();
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

    private String normalizeText(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }
}
