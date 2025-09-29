package org.tech.ai.deepimage.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRefreshTokenRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long sessionId;
}


