package ae.dar360.user.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "login_attempt")
@Getter
@Setter
public class LoginAttempt {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "login_time", nullable = false)
    private OffsetDateTime loginTime;

    @Column(name = "expire_time", nullable = false)
    private OffsetDateTime expireTime;

    @Column(name = "is_expired", nullable = false)
    private boolean expired;
}