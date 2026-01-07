package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.UserSessionInfo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionInfoRepository extends CrudRepository<UserSessionInfo, UUID> {
    Optional<UserSessionInfo> findByEmail(String email);
    Optional<UserSessionInfo> findByLoginAttemptId(String loginAttemptId);

}
