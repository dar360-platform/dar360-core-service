package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.ApprovalLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, String> {
}
