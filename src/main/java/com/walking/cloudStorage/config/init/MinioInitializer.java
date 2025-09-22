package com.walking.cloudStorage.config.init;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private final String bucket;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());

                log.info("Minio bucket '{}' created successfully", bucket);
            } else {
                log.info("Minio bucket '{}' already exists", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Minio bucket '{}'", bucket, e);
            throw new MinioException();
        }
    }
}
