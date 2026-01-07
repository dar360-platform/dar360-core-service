package ae.dar360.user.repository;

import ae.dar360.user.model.PasswordChangeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordChangeHistoryRepository extends JpaRepository<PasswordChangeHistory, UUID> {
    Page<PasswordChangeHistory> findByUserId(UUID userId, Pageable pageable);
}
