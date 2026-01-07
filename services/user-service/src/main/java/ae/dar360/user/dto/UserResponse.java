package ae.dar360.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    @Schema(description = "Id of user", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private UUID id;
    @Schema(description = "Code of user")
    private String code;
    @Schema(description = "Full name of user", example = "John Emo")
    private String employeeId;
    @Schema(description = "EmloyeeID", example = "EmployeeID")
    private String fullName;
    @Schema(description = "Email of user", example = "admin@domain.com")
    private String email;
    @Schema(description = "Line manager email of user", example = "manager@appro.ae")
    private String lineManagerEmail;
    @Schema(description = "Phone number of user", example = "0123456789")
    private String mobile;
    @Schema(description = "Id of department", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private String departmentId;
    @Schema(description = "Name of department", example = "abc")
    private String departmentName;
    @Schema(description = "Flag to check user is lock or not", example = "true | false")
    private boolean lock;
    @Schema(description = "Status of user", example = "1")
    private Integer status;
    @Schema(description = "The date and time of created user", example = "2024-02-05T00:29:59.657Z")
    private OffsetDateTime createdDate;
    @Schema(description = "The date and time of modified user", example = "2024-02-05T00:29:59.657Z")
    private OffsetDateTime modifiedDate;
    @Schema(description = "The date and time of last user login", example = "2024-02-05T00:29:59.657Z")
    private OffsetDateTime lastLogin;
    @Schema(description = "The user who created user", example = "system")
    private String createdBy;
    @Schema(description = "The user who modified user", example = "system")
    private String modifiedBy;
    private List<String> approvalLevels = new java.util.ArrayList<>();
    private List<String> deviationApprovalLevels = new java.util.ArrayList<>();
    private List<UUID> roleIds = new java.util.ArrayList<>();
    @Schema(description = "Approved limit of user", example = "1000")
    private String approvedLimit;
    @Schema(description = "Max Approved limit of user", example = "10000")
    private String maxApprovedLimit;
    private List<String> productTypes = new java.util.ArrayList<>();
}
