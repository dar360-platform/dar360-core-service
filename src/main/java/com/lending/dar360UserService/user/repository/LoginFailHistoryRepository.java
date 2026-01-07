package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.LoginFailHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LoginFailHistoryRepository extends JpaRepository<LoginFailHistory, UUID> {

    int countByUserIdAndLoginTimeAfter(UUID userId, OffsetDateTime time);

    List<LoginFailHistory> getAllByUserId(UUID userId);
}
