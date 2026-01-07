package ae.dar360.user.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LoginAttemptDto {
    private String id;
    private String userId;
    private OffsetDateTime loginTime;
    private OffsetDateTime expireTime;
    private boolean expired;
}
