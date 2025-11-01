package org.tech.ai.deepimage.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * File utility class
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
public class FileUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Mapping from file extension to MIME type
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
     * Generate MinIO object name
     * Format: {userId}/{businessType}/{date}/{uuid}.{extension}
     * Example: 1001/avatar/20251002/abc123def456.jpg
     * 
     * @param userId User ID
     * @param businessType Business type (e.g., avatar, document, image)
     * @param extension File extension (e.g., jpg, png, pdf)
     * @return MinIO object name
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
     * Get file extension (without dot)
     * 
     * @param filename Filename
     * @return Extension (lowercase), empty string if no extension
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
     * Get filename without extension
     * 
     * @param filename Filename
     * @return Filename without extension
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
     * Format file size in human-readable format
     * 
     * @param size File size (bytes)
     * @return Formatted size (e.g., 1.5 MB)
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
     * Check if filename is valid
     * 
     * @param filename Filename
     * @return Whether valid
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // Check if contains invalid characters
        String invalidChars = "[\\\\/:*?\"<>|]";
        return !filename.matches(".*" + invalidChars + ".*");
    }
    
    /**
     * Get MIME type based on file extension
     * 
     * @param extension File extension (case-insensitive)
     * @return MIME type, default type (image/jpeg) if not found
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "image/jpeg";
        }
        return EXTENSION_TO_MIME.getOrDefault(extension.toLowerCase(), "image/jpeg");
    }
    
    /**
     * Check if Content-Type is image type
     * 
     * @param contentType Content-Type header
     * @return Whether it is an image
     */
    public static boolean isImageType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}

