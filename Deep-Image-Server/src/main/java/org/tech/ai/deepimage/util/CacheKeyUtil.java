package org.tech.ai.deepimage.util;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.tech.ai.deepimage.model.dto.request.BusinessRequest;
import org.tech.ai.deepimage.model.dto.request.CacheableRequest;

/**
 * Utility class for generating cache keys from requests
 * 
 * @author zgq
 * @since 2025-11-01
 */
public class CacheKeyUtil {
    
    /**
     * Generate hash from cacheable request for cache key
     * 
     * @param request Cacheable request
     * @return SHA256 hash string
     */
    public static String generateRequestHash(CacheableRequest request) {
        String jsonString = JSON.toJSONString(request);
        return HashUtil.sha256(jsonString);
    }
    
    /**
     * Get business type for cache key (normalized)
     * Returns empty string if businessType is null/blank, otherwise returns lowercase
     * 
     * @param request Business request
     * @return Normalized business type string
     */
    public static String getBusinessTypeForKey(BusinessRequest request) {
        String businessType = request.getBusinessType();
        if (StringUtils.isBlank(businessType)) {
            return "";
        }
        return businessType.toLowerCase();
    }
}

