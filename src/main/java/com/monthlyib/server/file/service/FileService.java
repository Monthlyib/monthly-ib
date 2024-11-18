package com.monthlyib.server.file.service;


import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {


    private final String CONTENT_DISPOSITION_TYPE = "attachment";

    private final AwsService awsService;

    public String saveMultipartFileForAws(MultipartFile file, AwsProperty path) {
            Optional<File> convert = convert(file);
            if (convert.isPresent()) {
                File newFile = convert.get();
                String fullFileName = path.getName() + newFile.getName();
                try {
                    awsService.verifyFileExistsByFullFileName(fullFileName);
                    String url = awsService.putS3(newFile, fullFileName);
                    removeLocalFile(newFile);
                    return url;
                } catch (Exception e) {
                    removeLocalFile(newFile);
                    throw e;
                }
            } else {
                throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
            }
    }
    public String saveMultipartFileForAws(MultipartFile file, AwsProperty path, String addPath) {
        Optional<File> convert = convert(file);
        if (convert.isPresent()) {
            File newFile = convert.get();
            String fullFileName = path.getName()+ addPath + newFile.getName();
            try {
                awsService.verifyFileExistsByFullFileName(fullFileName);
                String url = awsService.putS3(newFile, fullFileName);
                removeLocalFile(newFile);
                return url;
            } catch (Exception e) {
                removeLocalFile(newFile);
                throw e;
            }
        } else {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }
    }


    public void deleteAwsFile(String fileName, AwsProperty path) {
        awsService.deleteS3(path.getName() + fileName);
    }
    public void deleteAwsFile(String fullPath) {
        awsService.deleteS3(fullPath);
    }

    public void deleteAwsDir(String dirPath, AwsProperty path) {
        awsService.deleteAwsDir(path.getName() + dirPath);
    }






    public HttpHeaders buildHeaders(String baseFileName, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(data.length);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(createContentDisposition(baseFileName));
        return headers;
    }
    private ContentDisposition createContentDisposition(String baseFileName) {
        return ContentDisposition.builder(CONTENT_DISPOSITION_TYPE)
                .filename(baseFileName, StandardCharsets.UTF_8)
                .build();
    }



    public Optional<File> convert(MultipartFile file) {
        String fileName = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.FILE_NOT_NULL));
        File convertFile = new File(fileName);
        try {
            if (convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }

        return Optional.empty();
    }

    public boolean chunkUpload(MultipartFile file, int chunkNumber, int totalChunks, String baseFileName, String uploadDir){
        try {

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 임시 저장 파일 이름
            String filename = file.getOriginalFilename() + ".part" + chunkNumber;

            Path filePath = Paths.get(uploadDir, filename);
            // 임시 저장
            Files.write(filePath, file.getBytes());

            // 마지막 조각이 전송 됐을 경우
            if (chunkNumber == totalChunks-1) {
                String[] split = file.getOriginalFilename().split("\\.");
                String outputFilename = baseFileName + "." + split[split.length-1];
                Path outputFile = Paths.get(uploadDir, outputFilename);
                Files.createFile(outputFile);

                // 임시 파일들을 하나로 합침
                for (int i = 0; i < totalChunks; i++) {
                    Path chunkFile = Paths.get(uploadDir, file.getOriginalFilename() + ".part" + i);
                    Files.write(outputFile, Files.readAllBytes(chunkFile), StandardOpenOption.APPEND);
                    // 합친 후 삭제
                    Files.delete(chunkFile);
                }
                log.info("File uploaded successfully");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }

    }

    public Optional<String> convert(MultipartFile file, String baseFileName, String path) {
        String fileName = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.FILE_NOT_NULL));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        File convertFile = new File(path+baseFileName+extension);
        try {
            if (convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                return Optional.of(baseFileName+extension);
            }
        } catch (IOException e) {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }

        return Optional.empty();
    }

    public void removeLocalFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("로컬 저장 파일 삭제 완료.");
        } else {
            log.info("로컬 저장 파일 삭제 실패.");
        }
    }

    public void removeLocalFile(String fileFullName) {
        File file = new File(fileFullName);
        if (file.exists()) {
            file.delete();
        } else {
            throw new ServiceLogicException(ErrorCode.AWS_FILE_NOT_FOUND);
        }
    }
    public void setDisposition(String filename, HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        String browser = getBrowser(request);
        String dispositionPrefix = "attachment; filename=";
        String encodedFilename = null;

        if (browser.equals("MSIE")) {
            encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll(
                    "\\+", "%20");
        } else if (browser.equals("Firefox")) {
            encodedFilename = "\""
                    + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Opera")) {
            encodedFilename = "\""
                    + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Chrome")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                if (c > '~') {
                    sb.append(URLEncoder.encode("" + c, "UTF-8"));
                } else {
                    sb.append(c);
                }
            }
            encodedFilename = sb.toString();
        } else {
            throw new IOException("Not supported browser");
        }

        response.setHeader("Content-Disposition", dispositionPrefix
                + encodedFilename);

        if ("Opera".equals(browser)) {
            response.setContentType("application/octet-stream;charset=UTF-8");
        }

    }

    private String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        } else if (header.indexOf("Firefox") > -1) {
            return "Firefox";
        } else if (header.indexOf("Mozilla") > -1) {
            if (header.indexOf("Firefox") > -1) {
                return "Firefox";
            }else{
                return "MSIE";
            }
        }
        return "MSIE";
    }
}
