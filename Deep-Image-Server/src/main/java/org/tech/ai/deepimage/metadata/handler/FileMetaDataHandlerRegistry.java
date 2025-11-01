package org.tech.ai.deepimage.metadata.handler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tech.ai.deepimage.enums.BusinessTypeEnum;
import org.tech.ai.deepimage.metadata.FileMetaData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File metadata handler registry
 * Manages all metadata handlers and provides lookup by business type
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMetaDataHandlerRegistry {
    
    private final List<FileMetaDataHandler> handlers;
    // Use ConcurrentHashMap for thread-safe concurrent reads (though writes only happen in @PostConstruct)
    // Initial capacity set to 8 (power of 2) to accommodate all business types (AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP)
    private final Map<String, FileMetaDataHandler> handlerMap = new ConcurrentHashMap<>(8);
    
    @PostConstruct
    public void init() {
        // Register handlers by business type
        for (FileMetaDataHandler handler : handlers) {
            if (handler instanceof ImageMetaDataHandler) {
                handlerMap.put(BusinessTypeEnum.IMAGE.name(), handler);
                log.info("Registered ImageMetaDataHandler for business type: IMAGE");
            }
            // Future handlers can be registered here:
            // if (handler instanceof VideoMetaDataHandler) {
            //     handlerMap.put(BusinessTypeEnum.VIDEO.name(), handler);
            // }
            // if (handler instanceof DocumentMetaDataHandler) {
            //     handlerMap.put(BusinessTypeEnum.DOCUMENT.name(), handler);
            // }
        }
        
        log.info("Initialized FileMetaDataHandlerRegistry with {} handlers", handlerMap.size());
    }
    
    /**
     * Get handler for specific business type
     * 
     * @param businessType Business type enum name (e.g., IMAGE, VIDEO, DOCUMENT)
     * @return Handler instance, or null if not found
     */
    public FileMetaDataHandler getHandler(String businessType) {
        if (businessType == null) {
            return null;
        }
        
        return handlerMap.get(businessType.toUpperCase());
    }
    
    /**
     * Extract metadata using appropriate handler
     * 
     * @param businessType Business type enum name
     * @param fileBytes File content as byte array
     * @param contentType MIME type of the file
     * @return FileMetaData instance, or null if no handler found or extraction fails
     */
    public FileMetaData extractMetadata(String businessType, byte[] fileBytes, String contentType) {
        FileMetaDataHandler handler = getHandler(businessType);
        
        if (handler == null) {
            log.debug("No metadata handler found for business type: {}", businessType);
            return null;
        }
        
        try {
            return handler.handle(fileBytes, contentType);
        } catch (Exception e) {
            log.error("Error extracting metadata for business type {}: {}", businessType, e.getMessage(), e);
            return null;
        }
    }
}

