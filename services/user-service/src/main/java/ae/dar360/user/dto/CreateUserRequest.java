package ae.dar360.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {
    @Schema(description = "Id of user need to create", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private UUID id;
    @Schema(description = "EmployeeID", example = "EmployeeID")
    private String employeeId;
    @Schema(description = "Full name of user", example = "John Emo")
    private String fullName;
    @Schema(description = "Email of user", example = "JohnEmo@domain.com")
    private String email;
    @Schema(description = "Line manager email of user", example = "manager@appro.ae")
    @NotBlank(message = "Line manager email is required")
    @Email(message = "Line manager email must be a valid email address")
    private String lineManagerEmail;
    @Schema(description = "Phone number of user", example = "0123456789")
    private String mobile;
    @Schema(description = "Id of department", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private String departmentId;
    private List<String> approvalLevels;
    private List<String> deviationApprovalLevels;
    @Schema(description = "Approved limit of user", example = "1000")
    private String approvedLimit;
    @Schema(description = "Max Approved limit of user", example = "1000")
    private String maxApprovedLimit;
    private List<String> productTypes;
}
