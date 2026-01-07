package ae.dar360.user.repository;

import ae.dar360.user.model.LoginAttempt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    @Modifying
    @Transactional
    @Query("UPDATE LoginAttempt la SET la.expired = true, la.expireTime = :expireTime WHERE la.userId = :userId AND la.expired = false")
    void expireAllByUserId(@Param("userId") String userId, @Param("expireTime") OffsetDateTime expireTime);

}
