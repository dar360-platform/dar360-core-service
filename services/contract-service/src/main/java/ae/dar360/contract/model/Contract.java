package ae.dar360.contract.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractNumber;
    private Long propertyId;
    private Long agentId;
    private Long ownerId;
    private String tenantName;
    private String tenantPhone;
    private String tenantEmail;
    private String tenantEmiratesId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    private String pdfUrl;
    private String signedPdfUrl;
    private String otpCode;
    private LocalDateTime otpExpiresAt;
    private Integer otpAttempts;
    private LocalDateTime signedAt;
    private String signedIp;
    private LocalDateTime createdAt;
}
