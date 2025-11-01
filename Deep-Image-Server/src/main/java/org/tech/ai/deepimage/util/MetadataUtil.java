package org.tech.ai.deepimage.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tech.ai.deepimage.metadata.FileMetaData;
import org.tech.ai.deepimage.metadata.handler.FileMetaDataHandlerRegistry;

/**
 * Metadata utility class
 * Provides common methods for metadata extraction and processing
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataUtil {
    
    private final FileMetaDataHandlerRegistry metadataHandlerRegistry;
    
    /**
     * Extract metadata from file and convert to JSON string
     * 
     * @param businessType Business type (e.g., IMAGE, VIDEO, DOCUMENT)
     * @param fileBytes File content as byte array
     * @param contentType MIME type of the file
     * @return JSON string of metadata, or null if extraction fails
     */
    public String extractMetadataAsJson(String businessType, byte[] fileBytes, String contentType) {
        try {
            FileMetaData metadata = metadataHandlerRegistry.extractMetadata(businessType, fileBytes, contentType);
            if (metadata != null) {
                return metadata.toJson();
            }
        } catch (Exception e) {
            log.warn("Failed to extract metadata for business type {}: {}", businessType, e.getMessage());
        }
        return null;
    }
}

