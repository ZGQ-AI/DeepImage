package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Public files query request
 * Used for querying all public files (no authentication required)
 * Simple pagination query, sorted by creation time descending by default
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ListPublicFilesRequest extends PageRequest implements CacheableRequest {
    // Simple pagination only, no additional filters or sort options
    // Default sort: created_at DESC (newest first)
}

