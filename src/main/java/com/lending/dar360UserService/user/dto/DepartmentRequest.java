package com.lending.dar360UserService.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DepartmentRequest {
    @Schema(description = "Name of department")
    private String name;
}
