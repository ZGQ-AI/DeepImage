package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import org.tech.ai.deepimage.constant.FileConstant;

/**
 * File list query request (unified interface)
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class ListFilesRequest extends PageRequest {
    
    /**
     * Business type (optional)
     * Optional values: AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP
     */
    private String businessType;
    
    /**
     * Tag ID (optional)
     * Filter files by tag
     */
    private Long tagId;
    
    /**
     * File name search keyword (optional)
     * Supports fuzzy search
     */
    private String filename;
    
    /**
     * Sort field (optional)
     * Optional values: createdAt, updatedAt, fileSize, originalFilename
     * Default: updatedAt (latest updated first)
     */
    private String sortBy = FileConstant.SORT_BY_UPDATED_AT;
    
    /**
     * Sort direction (optional)
     * Optional values: asc, desc
     */
    private String sortOrder = FileConstant.SORT_ORDER_DESC;
}

