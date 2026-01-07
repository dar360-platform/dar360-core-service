package ae.dar360.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @Schema(description = "Email of user who forgot password", example = "admin@domain.com")
    private String email;
}
