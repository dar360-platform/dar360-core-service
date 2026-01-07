package ae.dar360.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RefreshTokenDto {
    @Schema(description = "Id of refresh token", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private UUID id;
    @Schema(description = "Id of user", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private String  userId;
    @Schema(description = "Token")
    private String token;
    private UserResponse user;
    @Schema(description = "Date and time of refresh token expiry", example = "2024-02-05T00:29:59.657Z")
    private OffsetDateTime expiryDate;
}
