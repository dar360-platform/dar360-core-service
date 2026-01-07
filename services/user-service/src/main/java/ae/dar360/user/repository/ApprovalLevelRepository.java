package ae.dar360.user.repository;

import ae.dar360.user.model.ApprovalLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, String> {
}
