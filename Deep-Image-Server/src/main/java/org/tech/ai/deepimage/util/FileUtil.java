package org.tech.ai.deepimage.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
     * 文件扩展名到MIME类型的映射
     */
    private static final Map<String, String> EXTENSION_TO_MIME = new HashMap<>();
    
    static {
        EXTENSION_TO_MIME.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME.put("png", "image/png");
        EXTENSION_TO_MIME.put("gif", "image/gif");
        EXTENSION_TO_MIME.put("webp", "image/webp");
        EXTENSION_TO_MIME.put("bmp", "image/bmp");
        EXTENSION_TO_MIME.put("svg", "image/svg+xml");
        EXTENSION_TO_MIME.put("ico", "image/x-icon");
    }
    
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
     * 根据文件扩展名获取MIME类型
     * 
     * @param extension 文件扩展名（不区分大小写）
     * @return MIME类型，如果未找到则返回默认类型（image/jpeg）
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "image/jpeg";
        }
        return EXTENSION_TO_MIME.getOrDefault(extension.toLowerCase(), "image/jpeg");
    }
    
    /**
     * 判断Content-Type是否为图片类型
     * 
     * @param contentType Content-Type头
     * @return 是否为图片
     */
    public static boolean isImageType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}

