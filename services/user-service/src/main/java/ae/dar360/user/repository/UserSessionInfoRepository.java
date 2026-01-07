package ae.dar360.user.repository;

import ae.dar360.user.model.UserSessionInfo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionInfoRepository extends CrudRepository<UserSessionInfo, UUID> {
    Optional<UserSessionInfo> findByEmail(String email);
    Optional<UserSessionInfo> findByLoginAttemptId(String loginAttemptId);

}
