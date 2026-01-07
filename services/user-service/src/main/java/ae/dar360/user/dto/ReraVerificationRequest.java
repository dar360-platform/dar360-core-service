package ae.dar360.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReraVerificationRequest {
    @NotBlank
    private String licenseNumber;
}
