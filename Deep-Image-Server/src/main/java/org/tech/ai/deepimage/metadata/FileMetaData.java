package org.tech.ai.deepimage.metadata;

/**
 * File metadata interface
 * Defines the common contract for all file metadata types
 * All metadata implementations must support JSON serialization
 * 
 * @author zgq
 * @since 2025-11-01
 */
public interface FileMetaData {
    
    /**
     * Convert metadata to JSON string
     * 
     * @return JSON string representation of metadata
     */
    String toJson();
    
    /**
     * Create metadata instance from JSON string
     * 
     * @param json JSON string
     * @return Metadata instance, or null if parsing fails
     */
    FileMetaData fromJson(String json);
}

