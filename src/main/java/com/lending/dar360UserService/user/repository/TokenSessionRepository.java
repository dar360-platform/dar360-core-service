package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.TokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenSessionRepository extends JpaRepository<TokenSession, UUID> {
    
    Optional<TokenSession> findByToken(String token);

    Optional<TokenSession> findByuserIdAndStatus(UUID userId, Integer status);

}
