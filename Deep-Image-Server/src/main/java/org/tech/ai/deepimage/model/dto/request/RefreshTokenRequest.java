package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}


