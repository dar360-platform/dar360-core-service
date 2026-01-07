package ae.dar360.user.repository;

import ae.dar360.user.model.TokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenSessionRepository extends JpaRepository<TokenSession, UUID> {
    
    Optional<TokenSession> findByToken(String token);

    Optional<TokenSession> findByuserIdAndStatus(UUID userId, Integer status);

}
