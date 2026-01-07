package ae.dar360.property.repository;

import ae.dar360.property.model.PropertyShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyShareRepository extends JpaRepository<PropertyShare, Long> {
}
