package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.tech.ai.deepimage.enums.BusinessTypeEnum;

/**
 * 图片下载配置属性
 * 
 * @author zgq
 * @since 2025-10-22
 */
@Data
@Component
public class ImageDownloadProperties {
    
    /**
     * 下载超时时间（毫秒），默认30秒
     */
    private int timeout = 30000;
    
    /**
     * HTTP User-Agent
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    /**
     * 最大文件大小（字节），默认10MB
     */
    private long maxFileSize = 10 * 1024 * 1024;
    
    /**
     * 默认文件扩展名
     */
    private String defaultExtension = "jpg";
    
    /**
     * 业务类型标识
     */
    private String businessType = BusinessTypeEnum.IMAGE.name();
}

