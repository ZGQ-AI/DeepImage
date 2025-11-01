package org.tech.ai.deepimage.model.dto.request;

/**
 * Interface for requests that have business type
 * 
 * @author zgq
 * @since 2025-11-01
 */
public interface BusinessRequest {
    
    /**
     * Get business type
     * 
     * @return Business type string (e.g., IMAGE, DOCUMENT, VIDEO, AVATAR, TEMP)
     */
    String getBusinessType();
}

