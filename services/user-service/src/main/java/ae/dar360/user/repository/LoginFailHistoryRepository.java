package ae.dar360.user.repository;

import ae.dar360.user.model.LoginFailHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LoginFailHistoryRepository extends JpaRepository<LoginFailHistory, UUID> {

    int countByUserIdAndLoginTimeAfter(UUID userId, OffsetDateTime time);

    List<LoginFailHistory> getAllByUserId(UUID userId);
}
