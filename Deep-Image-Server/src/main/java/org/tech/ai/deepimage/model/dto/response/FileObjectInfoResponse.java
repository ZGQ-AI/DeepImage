package org.tech.ai.deepimage.model.dto.response;

import io.minio.messages.Item;
import io.minio.StatObjectResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MinIO file object information
 * Used to encapsulate detailed information of objects in MinIO
 * Note: This is a MinIO layer DTO, not related to business logic
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileObjectInfoResponse {

    /**
     * Object name (storage path)
     */
    private String objectName;

    /**
     * File size (bytes)
     */
    private Long size;

    /**
     * ETag (entity tag, used to identify object version)
     */
    private String etag;

    /**
     * Content type (MIME type)
     */
    private String contentType;

    /**
     * Last modified time
     */
    private LocalDateTime lastModified;

    /**
     * File access URL
     */
    private String url;

    /**
     * Whether it is a directory
     */
    private Boolean isDirectory;

    /**
     * Build FileObjectInfo from MinIO Item
     * Item is the object information returned by listObjects
     * 
     * @param item MinIO Item object
     * @return FileObjectInfo
     */
    public static FileObjectInfoResponse from(Item item) {
        return FileObjectInfoResponse.builder()
                .objectName(item.objectName())
                .size(item.size())
                .etag(item.etag())
                .isDirectory(item.isDir())
                .lastModified(item.lastModified() != null
                        ? LocalDateTime.ofInstant(item.lastModified().toInstant(), ZoneId.systemDefault())
                        : null)
                .build();
    }

    /**
     * Build FileObjectInfo from MinIO StatObjectResponse
     * StatObjectResponse is the detailed object information returned by statObject
     * 
     * @param stat MinIO StatObjectResponse object
     * @return FileObjectInfo
     */
    public static FileObjectInfoResponse from(StatObjectResponse stat) {
        return FileObjectInfoResponse.builder()
                .objectName(stat.object())
                .size(stat.size())
                .etag(stat.etag())
                .contentType(stat.contentType())
                .lastModified(stat.lastModified() != null
                        ? LocalDateTime.ofInstant(stat.lastModified().toInstant(), ZoneId.systemDefault())
                        : null)
                .build();
    }
}

