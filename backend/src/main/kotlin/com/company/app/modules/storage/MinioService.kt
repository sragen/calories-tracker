package com.company.app.modules.storage

import io.minio.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class MinioService(
    private val minioClient: MinioClient,
    @Value("\${app.minio.bucket}") private val bucket: String,
    @Value("\${app.minio.public-url}") private val publicUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        runCatching {
            val exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
                log.info("MinIO bucket '$bucket' created")
            }
        }.onFailure { log.warn("MinIO bucket init failed: ${it.message}") }
    }

    fun uploadImage(file: MultipartFile, folder: String = "ai-scans"): String {
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val objectName = "$folder/${UUID.randomUUID()}.$ext"
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType ?: "image/jpeg")
                .build()
        )
        return "$publicUrl/$bucket/$objectName"
    }

    fun readBytes(file: MultipartFile): ByteArray = file.bytes
}
