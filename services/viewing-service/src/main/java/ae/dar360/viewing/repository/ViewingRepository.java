package ae.dar360.viewing.repository;

import ae.dar360.viewing.model.Viewing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewingRepository extends JpaRepository<Viewing, Long> {
}
