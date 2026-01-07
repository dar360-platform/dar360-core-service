package ae.dar360.user.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "token_session")
public class TokenSession extends Auditable<String> {
  private static final long serialVersionUID = -6271388606411349270L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "token")
  private String token;

  @Column(name = "expiry_date")
  private OffsetDateTime expiryDate;

  @Column(name = "type")
  private Integer type;

  @Column(name = "status")
  private Integer status;

  @Column(name = "verify_times")
  private Integer verifyTimes;

}
