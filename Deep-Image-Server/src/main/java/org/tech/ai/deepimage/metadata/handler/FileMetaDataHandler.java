package org.tech.ai.deepimage.metadata.handler;

import org.tech.ai.deepimage.metadata.FileMetaData;

/**
 * File metadata handler interface
 * Handlers are responsible for extracting metadata from specific file types
 * 
 * @author zgq
 * @since 2025-11-01
 */
public interface FileMetaDataHandler {
    
    /**
     * Extract metadata from file bytes
     * 
     * @param fileBytes File content as byte array
     * @param contentType MIME type of the file (e.g., image/jpeg, image/png)
     * @return FileMetaData instance, or null if extraction fails
     */
    FileMetaData handle(byte[] fileBytes, String contentType);
}

