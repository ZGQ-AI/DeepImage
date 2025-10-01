package org.tech.ai.deepimage.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * JWT工具类
 * 用于解析JWT token
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Slf4j
public class JwtUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 从JWT中获取指定claim的字符串值
     */
    public static String getClaimAsString(String token, String claimName) {
        try {
            // JWT格式: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                log.error("无效的JWT格式");
                return null;
            }
            
            // 解码payload部分（Base64URL）
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // 解析JSON
            JsonNode jsonNode = OBJECT_MAPPER.readTree(payload);
            JsonNode claimNode = jsonNode.get(claimName);
            
            return claimNode != null ? claimNode.asText() : null;
        } catch (Exception e) {
            log.error("解析JWT claim失败: {}", claimName, e);
            return null;
        }
    }
}

