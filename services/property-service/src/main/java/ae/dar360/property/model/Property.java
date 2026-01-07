package ae.dar360.property.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long agentId;
    private Long ownerId;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private PropertyType type;

    @Enumerated(EnumType.STRING)
    private PropertyStatus status;

    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal areaSqft;
    private BigDecimal rentAmount;

    @Enumerated(EnumType.STRING)
    private RentFrequency rentFrequency;

    private BigDecimal depositAmount;
    private String addressLine;
    private String areaName;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> amenities;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
