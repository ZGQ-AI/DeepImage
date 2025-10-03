package org.tech.ai.deepimage.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
public class FileUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 生成MinIO对象名称
     * 格式: {userId}/{businessType}/{date}/{uuid}.{extension}
     * 例如: 1001/avatar/20251002/abc123def456.jpg
     * 
     * @param userId 用户ID
     * @param businessType 业务类型（如：avatar、document、image等）
     * @param extension 文件扩展名（如：jpg、png、pdf）
     * @return MinIO对象名称
     */
    public static String generateObjectName(Long userId, String businessType, String extension) {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%d/%s/%s/%s.%s", 
                userId, 
                businessType.toLowerCase(), 
                date, 
                uuid, 
                extension);
    }
    
    /**
     * 获取文件扩展名（不含点）
     * 
     * @param filename 文件名
     * @return 扩展名（小写），如果没有扩展名返回空字符串
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 获取文件名（不含扩展名）
     * 
     * @param filename 文件名
     * @return 不含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }
        
        return filename.substring(0, lastDotIndex);
    }
    
    /**
     * 格式化文件大小为人类可读格式
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小（如：1.5 MB）
     */
    public static String formatFileSize(Long size) {
        if (size == null || size < 0) {
            return "0 B";
        }
        
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 检查文件名是否合法
     * 
     * @param filename 文件名
     * @return 是否合法
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含非法字符
        String invalidChars = "[\\\\/:*?\"<>|]";
        return !filename.matches(".*" + invalidChars + ".*");
    }
    
    /**
     * 根据Content-Type判断是否为图片
     * 
     * @param contentType MIME类型
     * @return 是否为图片
     */
    public static boolean isImage(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        return contentType.toLowerCase().startsWith("image/");
    }
    
    /**
     * 根据Content-Type判断是否为视频
     * 
     * @param contentType MIME类型
     * @return 是否为视频
     */
    public static boolean isVideo(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        return contentType.toLowerCase().startsWith("video/");
    }
    
    /**
     * 根据Content-Type判断是否为文档
     * 
     * @param contentType MIME类型
     * @return 是否为文档
     */
    public static boolean isDocument(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("pdf") 
                || lowerContentType.contains("word") 
                || lowerContentType.contains("excel") 
                || lowerContentType.contains("powerpoint")
                || lowerContentType.contains("text");
    }
}

