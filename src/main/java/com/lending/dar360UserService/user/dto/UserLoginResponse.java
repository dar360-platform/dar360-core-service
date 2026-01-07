package com.lending.dar360UserService.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    @Schema(description = "Id of login user", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private UUID id;
    @Schema(description = "Code of login user")
    private String code;
    @Schema(description = "Password of login user")
    private String password;
    @Schema(description = "Email of login user", example = "admin@domain.com")
    private String email;
    @Schema(description = "Full name of login user", example = "admin")
    private String fullName;
    @Schema(description = "Product types of login user", example = "admin")
    private List<String> productTypes;

}
