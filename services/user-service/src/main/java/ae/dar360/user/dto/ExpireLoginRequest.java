package ae.dar360.user.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ExpireLoginRequest {
    private String loginAttemptId;
    private OffsetDateTime expireTime;
    private boolean isExpired;
}
