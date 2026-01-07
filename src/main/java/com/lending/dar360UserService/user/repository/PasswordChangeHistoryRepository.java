package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.PasswordChangeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordChangeHistoryRepository extends JpaRepository<PasswordChangeHistory, UUID> {
    Page<PasswordChangeHistory> findByUserId(UUID userId, Pageable pageable);
}
