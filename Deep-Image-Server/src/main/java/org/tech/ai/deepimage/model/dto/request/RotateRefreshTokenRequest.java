package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;

@Data
public class RotateRefreshTokenRequest {
    private String oldRefreshToken; // Plaintext old refresh token
    private long ttlSeconds;
}


