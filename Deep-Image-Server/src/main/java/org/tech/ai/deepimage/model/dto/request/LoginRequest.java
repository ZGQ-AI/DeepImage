package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "password length must be at least 8")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must contain letters and numbers")
    private String password;
}


