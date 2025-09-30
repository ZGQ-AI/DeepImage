package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class FindSessionByTokenRequest {
    @NotBlank
    private String accessToken;
    
    @NotNull
    private Long userId;
}
