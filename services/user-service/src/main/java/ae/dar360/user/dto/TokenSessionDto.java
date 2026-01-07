package ae.dar360.user.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TokenSessionDto {
    private UUID id;
    private String  userId;
    private String token;
    private OffsetDateTime expiryDate;
}
