package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tech.ai.deepimage.entity.Tag;

import java.time.LocalDateTime;

/**
 * Tag response
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@Builder
public class TagResponse {
    
    /**
     * Tag ID
     */
    private Long id;
    
    /**
     * Tag name
     */
    private String tagName;
    
    /**
     * Tag color (hexadecimal format)
     */
    private String color;
    
    /**
     * Usage count
     */
    private Integer usageCount;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Convert from Tag entity to TagResponse
     * 
     * @param tag Tag entity
     * @return TagResponse
     */
    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .tagName(tag.getTagName())
                .color(tag.getColor())
                .usageCount(tag.getUsageCount())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}

