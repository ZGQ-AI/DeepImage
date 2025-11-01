package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

/**
 * File download response
 * Contains file stream and metadata for download
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Data
@Builder
public class FileDownloadResponse {
    
    /**
     * File input stream
     */
    private InputStream inputStream;
    
    /**
     * Original file name
     */
    private String originalFilename;
    
    /**
     * Content type
     */
    private String contentType;
}

