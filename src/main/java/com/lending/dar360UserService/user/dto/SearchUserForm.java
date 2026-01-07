package com.lending.dar360UserService.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class SearchUserForm {

    @Schema(description = "Sort key", example = "name")
    private String sortKey;
    @Schema(description = "Sort direction", allowableValues = {"ASC", "DESC"}, example = "ASC")
    private String sortDirection;
    @Schema(description = "Page number", example = "1")
    private Integer pageNumber;
    @Schema(description = "Page size", example = "10")
    private Integer pageSize;
    @Schema(description = "Keyword to search", example = "username")
    private String keyword;
    private Set<UUID> ids;
    private String fromDate;
    private String toDate;
    @Schema(description = "Status of user", example = "1")
    private Integer status;

}
