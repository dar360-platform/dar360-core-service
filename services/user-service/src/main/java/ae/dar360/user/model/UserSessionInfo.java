package ae.dar360.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_session_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class UserSessionInfo extends AuditableNotModifiedBy<String> {
    private static final long serialVersionUID = -6271388606411349270L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "login_attempt_id", nullable = false, length = 36)
    private String loginAttemptId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "browser_name")
    private String browserName;

    @Column(name = "private_mode")
    private boolean isPrivateMode;

    @Column(name = "last_active")
    private OffsetDateTime lastActive;
}
