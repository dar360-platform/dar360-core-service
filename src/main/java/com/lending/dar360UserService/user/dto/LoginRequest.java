package com.lending.dar360UserService.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "Email of account", example = "admin@domain.ae")
    private String email;
    @Schema(description = "Password of account", example = "Password@!2024")
    private String password;
    private String osVersion;
    private String browserName;
    private boolean isPrivateMode;
    private boolean ssoEnabled;
}
