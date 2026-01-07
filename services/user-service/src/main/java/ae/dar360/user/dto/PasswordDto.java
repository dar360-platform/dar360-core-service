package ae.dar360.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class PasswordDto {
    @Schema(description = "Id of user", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private UUID userId;
    @Schema(description = "New password of user")
    private String password;
    @Schema(description = "Current password of user")
    private String currentPassword;
    @Schema(description = "Token")
    private String token;
}
