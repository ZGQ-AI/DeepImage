package org.tech.ai.deepimage.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class RevokeRefreshTokenBySessionRequest {
    @NotNull
    private Long sessionId;
}
