package ae.dar360.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUserResponseDto implements Serializable {
  @Schema(description = "Id of user", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
  private UUID id;
  @Schema(description = "Code of user")
  private String code;
  @Schema(description = "Employee Id of user")
  private String employeeId;
  @Schema(description = "Full name of user", example = "John Emo")
  private String fullName;
  @Schema(description = "Email of user", example = "john.emo@example.com")
  private String email;
  @Schema(description = "Id of department", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
  private String departmentId;
  @Schema(description = "Name of department", example = "Marketing")
  private String departmentName;
  @Schema(description = "Status of user", example = "1")
  private int status;
  @Schema(description = "The user who modified user", example = "system")
  private String modifiedBy;
  @Schema(description = "The date and time of last user login", example = "2024-02-05T00:29:59.657Z")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private OffsetDateTime lastLogin;
  @Schema(description = "The date and time of modified user", example = "2024-02-05T00:29:59.657Z")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private OffsetDateTime modifiedDate;
}
