package org.tech.ai.deepimage.model.dto.response;

import io.minio.messages.Item;
import io.minio.StatObjectResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MinIO文件对象信息
 * 用于封装MinIO中对象的详细信息
 * 注意：这是MinIO层的DTO，与业务无关
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileObjectInfoResponse {

    /**
     * 对象名称(存储路径)
     */
    private String objectName;

    /**
     * 文件大小(字节)
     */
    private Long size;

    /**
     * ETag (实体标签,用于标识对象版本)
     */
    private String etag;

    /**
     * 内容类型 (MIME类型)
     */
    private String contentType;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 是否为目录
     */
    private Boolean isDirectory;

    /**
     * 从MinIO Item构建FileObjectInfo
     * Item是listObjects返回的对象信息
     * 
     * @param item MinIO Item对象
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
     * 从MinIO StatObjectResponse构建FileObjectInfo
     * StatObjectResponse是statObject返回的对象详细信息
     * 
     * @param stat MinIO StatObjectResponse对象
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

