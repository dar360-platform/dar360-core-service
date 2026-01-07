package ae.dar360.viewing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Table(name = "viewings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Viewing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long propertyId;
    private Long agentId;
    private String tenantName;
    private String tenantPhone;
    private String tenantEmail;
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private ViewingStatus status;

    @Enumerated(EnumType.STRING)
    private ViewingOutcome outcome;

    private String notes;
    private LocalDateTime createdAt;
}
