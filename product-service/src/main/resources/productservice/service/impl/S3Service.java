package org.productservice.service.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements FileService {

    @Value("${bucket.name}")
    public String bucketName;

    private final AmazonS3Client awsS3Client;

    @Override
    public String uploadFile(MultipartFile file) {
        var filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        var key = UUID.randomUUID().toString() + "." + filenameExtension;

        log.info("Start - Uploading file to S3 with key: {}", key);

        var metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            awsS3Client.putObject(bucketName, key, file.getInputStream(), metadata);

            log.info("Successfully uploaded file with key: {}", key);
        } catch (IOException ioException) {
            log.error("IOException occurred while uploading file: {}", ioException.getMessage());

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An Exception occured while uploading the file");
        }

        awsS3Client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

        String fileUrl = awsS3Client.getResourceUrl(bucketName, key);

        log.info("File uploaded successfully. File URL: {}", fileUrl);

        return fileUrl;
    }

    @Override
    public void deleteFile(String key) {
        log.info("Start - Deleting file from S3 with key: {}", key);

        try {
            awsS3Client.deleteObject(bucketName, key);

            log.info("Successfully deleted file with key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting file with key: {} from S3. Error: {}", key, e.getMessage());

            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
        }
    }
}
