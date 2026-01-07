package ae.dar360.user.repository;

import ae.dar360.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findAllByNameInIgnoreCase(Collection<String> names);
}
