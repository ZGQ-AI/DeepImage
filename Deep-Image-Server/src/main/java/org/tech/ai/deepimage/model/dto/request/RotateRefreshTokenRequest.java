package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;

@Data
public class RotateRefreshTokenRequest {
    private String oldRefreshToken; // 明文旧RT
    private long ttlSeconds;
}


