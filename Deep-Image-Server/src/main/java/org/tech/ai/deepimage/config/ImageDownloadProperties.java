package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.tech.ai.deepimage.enums.BusinessTypeEnum;

/**
 * Image download configuration properties
 * 
 * @author zgq
 * @since 2025-10-22
 */
@Data
@Component
public class ImageDownloadProperties {
    
    /**
     * Download timeout (milliseconds), default 30 seconds
     */
    private int timeout = 30000;
    
    /**
     * HTTP User-Agent
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    /**
     * Maximum file size (bytes), default 10MB
     */
    private long maxFileSize = 10 * 1024 * 1024;
    
    /**
     * Default file extension
     */
    private String defaultExtension = "jpg";
    
    /**
     * Business type identifier
     */
    private String businessType = BusinessTypeEnum.IMAGE.name();
}

