package productservice.service.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.productservice.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements FileService {

    @Value("${bucket.name}")
    public String bucketName;

    private final AmazonS3Client awsS3Client;

    private final RedisTemplate<String, byte[]> byteArrayRedisTemplate;

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
                    "An exception occurred while uploading the file");
        }

        awsS3Client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

        String fileUrl = awsS3Client.getResourceUrl(bucketName, key);

        log.info("File uploaded successfully. File URL: {}", fileUrl);

        return key;
    }

    @Override
    public void deleteFile(String key) {
        log.info("Start - Deleting file from S3 with key: {}", key);

        try {
            awsS3Client.deleteObject(bucketName, key);

            log.info("Successfully deleted file with key: {}", key);

            byteArrayRedisTemplate.delete("image:" + key);

        } catch (Exception e) {
            log.error("Error deleting file with key: {} from S3. Error: {}", key, e.getMessage());

            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
        }
    }

    public byte[] getFileBytes(String key) {
        byte[] cachedFileBytes = byteArrayRedisTemplate.opsForValue().get(key);

        if (cachedFileBytes != null) {
            log.info("File bytes retrieved from Redis cache for key: {}", key);
            return cachedFileBytes;
        }

        log.info("File bytes not found in cache, fetching from S3 for key: {}", key);
        byte[] fileBytes = fetchFileFromS3(key);

        cacheFileBytesInRedis(key, fileBytes);

        return fileBytes;
    }

    private void cacheFileBytesInRedis(String key, byte[] fileBytes) {
        byteArrayRedisTemplate.opsForValue().set("image:" + key, fileBytes, 12, TimeUnit.HOURS);
        log.info("File bytes cached in Redis with key: {} and expiration time: {} hours", key, 12);
    }

    private byte[] fetchFileFromS3(String key) {
        try (S3Object s3Object = awsS3Client.getObject(new GetObjectRequest(bucketName, "image:" + key));
             InputStream inputStream = s3Object.getObjectContent();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error fetching file from S3 with key: {}. Error: {}", key, e.getMessage());
            throw new RuntimeException("Error fetching file from S3: " + e.getMessage());
        }
    }

    public Optional<String> getFileContentType(String key) {
        log.info("Fetching file metadata for key: {}", key);
        try {
            S3Object object = awsS3Client.getObject(bucketName, key);
            ObjectMetadata metadata = object.getObjectMetadata();
            return Optional.ofNullable(metadata.getContentType());
        } catch (Exception e) {
            log.error("Error retrieving file metadata for key: {}", key, e);
            return Optional.empty();
        }
    }
}
