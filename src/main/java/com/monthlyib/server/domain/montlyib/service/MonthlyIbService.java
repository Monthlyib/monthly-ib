package com.monthlyib.server.domain.montlyib.service;

import com.monthlyib.server.api.monthlyib.dto.*;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbPdfFile;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbThumbnailFile;
import com.monthlyib.server.domain.montlyib.repository.MonthlyIbRepository;
import com.monthlyib.server.file.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MonthlyIbService {

    private static final Safelist MONTHLY_IB_CONTENT_SAFELIST = Safelist.relaxed()
            .addTags("span", "div", "section", "article", "h1", "h2", "h3")
            .addAttributes(":all", "class")
            .addAttributes("img", "src", "alt", "width", "height")
            .addProtocols("img", "src", "http", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    private final MonthlyIbRepository monthlyIbRepository;

    private final FileService fileService;

    public Page<MonthlyIbSimpleResponseDto> findAllMonthlyIb(
            int page,
            MonthlyIbSearchDto dto
    ) {
        return monthlyIbRepository.findAllMonthlyIb(PageRequest.of(page, 10, Sort.by("createAt").descending()), dto);
    }

    public MonthlyIbResponseDto findMonthlyIbById(Long monthlyIbId) {
        return monthlyIbRepository.findMonthlyIbDtoById(monthlyIbId);
    }

    public MonthlyIb findEntityById(Long monthlyIbId) {
        return monthlyIbRepository.findMonthlyIbById(monthlyIbId);
    }

    public List<MonthlyIbPdfFileResponseDto> findPdfFiles(Long monthlyIbId) {
        return monthlyIbRepository.findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
    }

    public MonthlyIbResponseDto createMonthlyIb(MonthlyIbPostDto dto) {
        dto.setTitle(normalizeTitle(dto.getTitle()));
        dto.setContent(sanitizeContent(dto.getContent()));
        MonthlyIb newMonthlyIb = MonthlyIb.create(dto);
        MonthlyIb saveMonthlyIb = monthlyIbRepository.saveMonthlyIb(newMonthlyIb);
        return MonthlyIbResponseDto.of(saveMonthlyIb, List.of());
    }

    public MonthlyIbResponseDto createOrUpdateMonthlyIbThumbnail(Long monthlyIbId, MultipartFile[] file) {
        MonthlyIb findMonthlyIb = monthlyIbRepository.findMonthlyIbById(monthlyIbId);
        if (!findMonthlyIb.getMonthlyIbThumbnailFileId().equals(0L)) {
            Long monthlyIbThumbnailFileId = findMonthlyIb.getMonthlyIbThumbnailFileId();
            MonthlyIbThumbnailFile findThumbnail = monthlyIbRepository.findMonthlyIbThumbnailFileById(monthlyIbThumbnailFileId);
            fileService.deleteAwsFile(findThumbnail.getFileName(), AwsProperty.MONTHLYIB_IMAGE);
            monthlyIbRepository.deleteMonthlyIbThumbnailFileByMonthlyIbId(monthlyIbId);
        }
        MonthlyIbResponseDto result = null;
        for (MultipartFile multipartFile : file) {
            String url = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.MONTHLYIB_IMAGE);
            MonthlyIbThumbnailFile newThumbnail = MonthlyIbThumbnailFile.create(url, multipartFile.getOriginalFilename(), findMonthlyIb.getMonthlyIbId());
            MonthlyIbThumbnailFile saveThumbnail = monthlyIbRepository.saveMonthlyIbThumbnailFile(newThumbnail);
            findMonthlyIb.setMonthlyIbThumbnailFileId(saveThumbnail.getMonthlyIbThumbnailFileId());
            findMonthlyIb.setMonthlyIbThumbnailFileName(saveThumbnail.getFileName());
            findMonthlyIb.setMonthlyIbThumbnailFileUrl(saveThumbnail.getUrl());
            MonthlyIb saveMonthlyIb = monthlyIbRepository.saveMonthlyIb(findMonthlyIb);
            List<MonthlyIbPdfFileResponseDto> list = monthlyIbRepository.findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
            result = MonthlyIbResponseDto.of(saveMonthlyIb, list);
        }
        return result;
    }

    public MonthlyIbResponseDto createOrUpdateMonthlyIbPdf(Long monthlyIbId, MultipartFile[] file) {
        MonthlyIb findMonthlyIb = monthlyIbRepository.findMonthlyIbById(monthlyIbId);
        List<MonthlyIbPdfFileResponseDto> currentList = monthlyIbRepository.findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
        if (!currentList.isEmpty()) {
            currentList.forEach( m ->
                    fileService.deleteAwsFile(m.getFileName(), AwsProperty.MONTHLYIB_PDF)
                    );
        }
        monthlyIbRepository.deleteMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
        MonthlyIbResponseDto result = null;
        List<MonthlyIbPdfFileResponseDto> list = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            String url = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.MONTHLYIB_PDF);
            String filename = multipartFile.getOriginalFilename();
            MonthlyIbPdfFile newPdf = MonthlyIbPdfFile.create(monthlyIbId, filename, url);
            MonthlyIbPdfFile savePdf = monthlyIbRepository.saveMonthlyIbPdfFile(newPdf);
            list.add(MonthlyIbPdfFileResponseDto.of(savePdf));
        }
        result = MonthlyIbResponseDto.of(findMonthlyIb, list);
        return result;
    }

    public MonthlyIbResponseDto updateMonthlyIb(Long monthlyIbId, MonthlyIbPatchDto dto) {
        MonthlyIb findMonthlyIb = monthlyIbRepository.findMonthlyIbById(monthlyIbId);
        findMonthlyIb.setTitle(normalizeTitle(dto.getTitle()));
        findMonthlyIb.setContent(sanitizeContent(dto.getContent()));
        MonthlyIb saveMonthlyIb = monthlyIbRepository.saveMonthlyIb(findMonthlyIb);
        List<MonthlyIbPdfFileResponseDto> list = monthlyIbRepository.findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
        return MonthlyIbResponseDto.of(saveMonthlyIb, list);
    }

    public MonthlyIbContentImageResponseDto uploadContentImage(MultipartFile image) {
        String imageUrl = fileService.saveMultipartFileForAws(image, AwsProperty.MONTHLYIB_CONTENT);
        return MonthlyIbContentImageResponseDto.of(imageUrl);
    }

    public void deleteMonthlyIb(Long monthlyIbId) {
        MonthlyIb monthlyIbById = monthlyIbRepository.findMonthlyIbById(monthlyIbId);
        fileService.deleteAwsFile(monthlyIbById.getMonthlyIbThumbnailFileName(), AwsProperty.MONTHLYIB_IMAGE);
        monthlyIbRepository.deleteMonthlyIbThumbnailFileByMonthlyIbId(monthlyIbId);

        List<MonthlyIbPdfFileResponseDto> currentList = monthlyIbRepository.findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);
        if (!currentList.isEmpty()) {
            currentList.forEach( m ->
                    fileService.deleteAwsFile(m.getFileName(), AwsProperty.MONTHLYIB_PDF)
            );
        }
        monthlyIbRepository.deleteMonthlyIbPdfFileByMonthlyIbId(monthlyIbId);

        monthlyIbRepository.deleteMonthlyIb(monthlyIbId);

    }

    public boolean hasMeaningfulContent(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        Document document = Jsoup.parseBodyFragment(content);
        if (!document.text().trim().isEmpty()) {
            return true;
        }

        return !document.select("img, video, iframe, ul, ol, blockquote, h1, h2, h3").isEmpty();
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
    }

    private String sanitizeContent(String content) {
        String rawContent = content == null ? "" : content.trim();
        return Jsoup.clean(rawContent, "", MONTHLY_IB_CONTENT_SAFELIST, new Document.OutputSettings().prettyPrint(false));
    }


}
