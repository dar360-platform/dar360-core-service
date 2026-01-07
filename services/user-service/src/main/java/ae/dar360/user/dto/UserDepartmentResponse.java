package ae.dar360.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDepartmentResponse {
    private String fullName;
    private String departmentName;
    private UUID uuid;
    private String email;
    private String approvalLevel;
    private BigDecimal approvalLimit;
}
