package ae.dar360.user.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends Auditable<String> {
    private static final long serialVersionUID = -6271388606411349270L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "token")
    private String token;
    @Column(name = "expiry_date")
    private OffsetDateTime expiryDate;
}
