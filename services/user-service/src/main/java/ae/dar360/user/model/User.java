/**
 * Aladdin Online Lending Application
 */
package ae.dar360.user.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import ae.dar360.user.enums.Dar360Role;
import java.util.UUID;

@Entity
@Table(name = "\"user\"") // keep table name "user" but valid for Postgres
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class User extends AuditableNotModifiedBy<String> {

    private static final long serialVersionUID = -6271388606411349270L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private Integer status;

    @Column(name = "lock")
    private boolean lock;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "last_updated_password")
    private OffsetDateTime lastUpdatedPassword;

    @Column(name = "password_expiration_date")
    private OffsetDateTime passwordExpirationDate;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "line_manager_email")
    private String lineManagerEmail;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "passport_photo")
    private byte[] passportPhoto;

    @Column(name = "passport_photo_content_type")
    private String passportPhotoContentType;

    @Column(name = "passport_photo_file_name")
    private String passportPhotoFileName;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "approval_level")
    private String approvalLevel;

    @Column(name = "approved_limit")
    private String approvedLimit;

    @Column(name = "deviation_approved_limit")
    private String deviationApprovalLevel;

    @Column(name = "max_approved_limit")
    private String maxApprovedLimit;

    @Column(name = "product_type")
    private String productType;

    @Enumerated(EnumType.STRING)
    @Column(name = "dar360_role")
    private Dar360Role dar360Role;

    @Column(name = "rera_license_number", length = 50)
    private String reraLicenseNumber;

    @Column(name = "rera_verified_at")
    private LocalDateTime reraVerifiedAt;

    @Column(name = "agency_name", length = 255)
    private String agencyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(name = "modified_by", nullable = false)
    protected String modifiedBy;
}
