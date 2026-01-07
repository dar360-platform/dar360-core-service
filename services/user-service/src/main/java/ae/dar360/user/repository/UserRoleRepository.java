package ae.dar360.user.repository;

import ae.dar360.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findAllByUserId(UUID userId);

    @Transactional
    @Modifying
    @Query("delete from UserRole ur where ur.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @Query("select ur.roleId from UserRole ur where ur.userId = :userId")
    List<UUID> findRoleIdsByUserId(@Param("userId") UUID userId);
}
