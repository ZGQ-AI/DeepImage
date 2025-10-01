package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tech.ai.deepimage.entity.Tag;

import java.time.LocalDateTime;

/**
 * 标签响应
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@Builder
public class TagResponse {
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 标签颜色（十六进制格式）
     */
    private String color;
    
    /**
     * 使用次数
     */
    private Integer usageCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 从Tag实体转换为TagResponse
     * 
     * @param tag Tag实体
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

