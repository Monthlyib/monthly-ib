package com.monthlyib.server.file.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.DeleteObjectsResponse;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.amazonaws.services.cloudformation.model.PolicyAction.Delete;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AwsService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    private final String SLASH_STRING = "/";

    public byte[] downloadZipFile(
            String baseFileName
    ) {
        try {
            log.info(baseFileName);
            String s3FullFileName = getS3ZipFileName(baseFileName);
            log.info(s3FullFileName);
            verifyFileByFullFileName(s3FullFileName);
            S3Object object = getS3(s3FullFileName);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (IOException io) {
            throw new ServiceLogicException(ErrorCode.AWS_IO_ERROR);
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.AWS_BAD_REQUEST);
        }
    }
    public void verifyFileExistsByFullFileName(String fullFileName) {
        log.info("S3 VerifyFileExistsByFullFileName!");
        if (amazonS3.doesObjectExist(bucket, fullFileName)) {
            throw new ServiceLogicException(ErrorCode.AWS_FILE_EXIST);
        }
    }
    private void verifyFileByFullFileName(String fullFileName) {
        log.info("S3 VerifyFileExistsByFullFileName!");
        if (!amazonS3.doesObjectExist(bucket, fullFileName)) {
            throw new ServiceLogicException(ErrorCode.AWS_FILE_NOT_FOUND);
        }
    }

    private String getS3ZipFileName(
            String baseFileName
    ) {
        return AwsProperty.ZIP_DIR_NAME.getName() + SLASH_STRING+ baseFileName;
    }

    public String putS3(
            File uploadFile,
            String fileName
    ) {
        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
        );
        log.info("Put S3 Object! = {}", fileName);
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public S3Object getS3(
            String fileName
    ) {
        log.info("Get S3 Object! = {}", fileName);
        return amazonS3.getObject(
                bucket,fileName
        );
    }

    public void deleteS3(String fileName) {
        amazonS3.deleteObject(
                new DeleteObjectRequest(bucket, fileName)
        );
        log.info("Delete S3 Object! = {}", fileName);
    }

    public void deleteAwsDir(String path) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setPrefix(path);
        ObjectListing s3Objects;
        List<String> fileNames = new ArrayList<>();
        do {
            s3Objects = amazonS3.listObjects(listObjectsRequest);
            for (S3ObjectSummary s3ObjectSummary : s3Objects.getObjectSummaries()) {
                String key = s3ObjectSummary.getKey();
                if (!key.endsWith(SLASH_STRING)) {
                    fileNames.add(key);
                }
            }
            listObjectsRequest.setMarker(s3Objects.getNextMarker());
        } while (s3Objects.isTruncated());
        fileNames.forEach(this::deleteS3);
    }

}
